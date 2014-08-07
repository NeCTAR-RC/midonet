package org.midonet.sdn.state;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.yammer.metrics.core.Clock;

/**
 * A sharded per-flow state table.
 *
 * It aggregates the data contain in a number of children shards, its goal is
 * to allow for single-writer / multiple-reader synchronization semantics across
 * the aggregated table, by assigning ownership of different shards to different
 * threads.
 *
 * THREADING SEMANTICS:
 *
 * Puts on a shard do not touch other shards. So two writes on the same key
 * performed on different shards result in undefined behaviour. Clients should
 * distribute shards among threads in a way that results in no keyspace overlap.
 *
 * Gets, on the other hand, will fall back to the parent and the other shards
 * if a key is not found locally.
 *
 * unref() calls may require coordination, but they are meant to happen in an
 * external thread or pool, not a shard-owning thread.
 */
public class ShardedFlowStateTable<K extends IdleExpiration, V> implements FlowStateTable<K, V> {
    private ArrayList<FlowStateShard> shards = new ArrayList<>();

    private final int SHARD_NONE = -1;
    private final Clock clock;

    public ShardedFlowStateTable() {
        this.clock = Clock.defaultClock();
    }

    public ShardedFlowStateTable(Clock clock) {
        this.clock = clock;
    }

    public FlowStateTable<K, V> addShard() {
        FlowStateShard s = new FlowStateShard(shards.size());
        shards.add(s);
        return s;
    }

    /**
     * Fetches a the value associated with a key, skipping the given shard
     * index.
     */
    V get(K key, int shardToSkip) {
        assert(shardToSkip == SHARD_NONE ||
               (shardToSkip >= 0 && shardToSkip < shards.size()));

        for (int i = 0; i < shards.size(); i++) {
            if (i != shardToSkip) {
                V v = shards.get(i).shallowGet(key);
                if (v != null)
                    return v;
            }
        }
        return null;
    }

    @Override
    public V putAndRef(K key, V value) {
        // NO-OP
        throw new IllegalArgumentException();
    }

    @Override
    public V remove(K key) {
        // NO-OP
        throw new IllegalArgumentException();
    }

    @Override
    public V get(K key) {
        return get(key, SHARD_NONE);
    }

    @Override
    public V ref(K key) {
        for (int i = 0; i < shards.size(); i++) {
            V v = shards.get(i).ref(key);
            if (v != null)
                return v;
        }
        return null;
    }

    @Override
    public void touch(K key, V value) {
        for (int i = 0; i < shards.size(); i++) {
            shards.get(i).touch(key, value);
        }
    }

    @Override
    public int getRefCount(K key) {
        int count = 0;
        for (int i = 0; i < shards.size(); i++) {
            V v = shards.get(i).shallowGet(key);
            if (v != null)
                count += shards.get(i).getRefCount(key);
        }

        return count;
    }

    @Override
    public void unref(K key) {
        for (int i = 0; i < shards.size(); i++) {
            V v = shards.get(i).shallowGet(key);
            if (v != null)
                shards.get(i).unref(key);
        }
    }

    @Override
    public <U> U fold(U acc, Reducer<? super K, ? super V, U> func) {
        for (int i = 0; i < shards.size(); i++) {
            acc = shards.get(i).fold(acc, func);
        }
        return acc;
    }

    @Override
    public <U> U expireIdleEntries(U seed, Reducer<K, V, U> func) {
        for (int i = 0; i < shards.size(); i++) {
            seed = shards.get(i).expireIdleEntries(seed, func);
        }
        return seed;
    }

    @Override
    public void expireIdleEntries() {
        for (int i = 0; i < shards.size(); i++) {
            shards.get(i).expireIdleEntries();
        }
    }

    /**
     * A shard within a ShardedFlowStateTable.
     *
     * It stores entries locally but forwards queries to the parent table for
     * aggregation. Reference counting is also delegated on the parent.
     */
    class FlowStateShard implements FlowStateTable<K, V> {
        public static final int IDLE_QUEUE_SIZE = 10000;

        private final int workerId;

        private ConcurrentHashMap<K, Entry> data = new ConcurrentHashMap<>();

        /* NOTE(guillermo): At least for now we will synchronize operations on
         * idleKeys. unref() and idle entry expiration both need to modify it
         * it's not clear that both operations will happen in the same thread. */
        private final PriorityQueue<Entry> idleKeys =
                new PriorityQueue<>(IDLE_QUEUE_SIZE, new EntryComparator());

        public FlowStateShard(int workerId) {
            this.workerId = workerId;
        }

        @Override
        public V putAndRef(K key, V value) {
            V oldV = get(key);
            // TODO(guillermo) reuse Entry instances.
            data.put(key, new Entry(key, value));
            return oldV;
        }

        @Override
        public V remove(K key) {
            Entry e = data.remove(key);
            return e != null ? e.v : null;
        }

        @Override
        public V get(K key) {
            Entry e = data.get(key);
            return e != null ? e.v : ShardedFlowStateTable.this.get(key, workerId);
        }

        public V shallowGet(K key) {
            Entry e = data.get(key);
            return e != null ? e.v : null;
        }

        @Override
        public V ref(K key) {
            Entry e = data.get(key);
            if (e != null) {
                e.ref();
                return e.v;
            }
            return null;
        }

        @Override
        public int getRefCount(K key) {
            Entry entry = data.get(key);
            return entry != null ? entry.refCount.get() : 0;
        }

        @Override
        public void touch(K key, V value) {
            if (shallowGet(key) == null)
                putAndRef(key, value);
            else
                ref(key);
            unref(key);
        }

        @Override
        public void unref(K key) {
            Entry e = data.get(key);
            if (e != null && e.unref() == 0) {
                /* We don't mess with 'idleSince' while an entry is in the
                   priority queue */
                synchronized (idleKeys) {
                    if (e.idleSince != 0)
                        idleKeys.remove(e);
                    e.idleSince = clock.tick();
                    idleKeys.add(e);
                }
            }
        }

        @Override
        public <U> U fold(U acc, Reducer<? super K, ? super V, U> func) {
            for (Entry e: data.values()) {
                acc = func.apply(acc, e.k, e.v);
            }
            return acc;
        }

        @Override
        public void expireIdleEntries() {
            expireIdleEntries(null, null);
        }

        @Override
        public <U> U expireIdleEntries(U seed, Reducer<K, V, U> func) {
            long now = clock.tick();
            synchronized (idleKeys) {
                while (!idleKeys.isEmpty() && expired(idleKeys.peek(), now)) {
                    Entry e = idleKeys.remove();
                    if (e.refCount.get() == 0 && data.remove(e.k) != null) {
                        if (func != null)
                            seed = func.apply(seed, e.k, e.v);
                    }
                }
            }
            return seed;
        }

        private boolean expired(Entry entry, long now) {
            long elapsed = now - entry.idleSince;
            long expiration = entry.k.expiresAfter().toNanos();
            return elapsed > expiration;
        }

        class Entry {
            K k; V v;
            AtomicInteger refCount;
            long idleSince = 0;

            Entry(K k, V v) {
                this.k = k;
                this.v = v;
                refCount = new AtomicInteger(1);
            }

            int ref() { return refCount.incrementAndGet(); }

            int unref() {
                int n = refCount.decrementAndGet();
                return n >= 0 ? n : refCount.incrementAndGet();
            }
        }

        class EntryComparator implements Comparator<Entry> {
            @Override
            public int compare(Entry a, Entry b) {
                return Long.compare(a.idleSince, b.idleSince);
            }
        }
    }
}
