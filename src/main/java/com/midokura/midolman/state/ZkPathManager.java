/*
 * @(#)ZkPathManager        1.6 11/09/08
 *
 * Copyright 2011 Midokura KK
 */
package com.midokura.midolman.state;

import java.util.UUID;

import com.midokura.midolman.layer3.Route;

/**
 * This class was created to have all state classes
 * share the Zk path information.
 * 
 * @version        1.6 08 Sept 2011
 * @author         Ryu Ishimoto
 */
public class ZkPathManager {
    
    private String basePath = null;
    
    /**
     * Default constructor.
     * 
     * @param basePath Base path of Zk.
     */
    public ZkPathManager(String basePath) {
        this.basePath = basePath;
    }
    
    /**
     * Get ZK tenant path.
     * 
     * @param id  Tenant UUID
     * @return  Tenant path in ZK.
     */
    public String getTenantPath(UUID id) {
        return new StringBuilder(basePath)
            .append("/tenants/")
            .append(id.toString()).toString();
    }

    /**
     * Get ZK router path.
     * 
     * @param id  Router UUID
     * @return  Router path in ZK.
     */
    public String getRouterPath(UUID id) {
        StringBuilder sb = new StringBuilder(basePath)
            .append("/routers");
        if (id != null) {
            sb.append("/").append(id.toString());
        }
        return sb.toString();   
    }

    /**
     * Get ZK port path.
     * 
     * @param id  Port UUID
     * @return  Port path in ZK.
     */
    public String getPortPath(UUID id) {
        StringBuilder sb = new StringBuilder(basePath)
            .append("/ports");
        if (id != null) {
            sb.append("/").append(id.toString());
        }
        return sb.toString();   
    }

    /**
     * Get ZK tenant router path.
     * 
     * @param tenantId  Tenant UUID
     * @return  Tenant router path in ZK.
     */
    public String getTenantRouterPath(UUID tenantId) {
        return getTenantRouterPath(tenantId, null);
    }
    
    /**
     * Get ZK tenant router path.
     * 
     * @param tenantId  Tenant UUID
     * @param routerId  Router UUID
     * @return  Tenant router path in ZK.
     */
    public String getTenantRouterPath(UUID tenantId, UUID routerId) {
        StringBuilder sb = new StringBuilder(getTenantPath(tenantId))
            .append("/routers");
        if (routerId != null) {
            sb.append("/").append(routerId.toString());
        }
        return sb.toString();
    }

    /**
     * Get ZK router port path.
     * 
     * @param routerId  Router UUID
     * @return  Router port path in ZK.
     */
    public String getRouterPortPath(UUID routerId) {
        return getRouterPortPath(routerId, null);
    }

    /**
     * Get ZK router port path.
     * 
     * @param routerId  Router UUID
     * @param portId  Port UUID.
     * @return  Router port path in ZK.
     */
    public String getRouterPortPath(UUID routerId, UUID portId) {
        StringBuilder sb = new StringBuilder(getRouterPath(routerId))
            .append("/ports");
        if (portId != null) {
            sb.append("/").append(portId.toString());
        }
        return sb.toString();
    }

    /**
     * Get ZK router routing table path.
     * 
     * @param routerId  Router UUID
     * @return  Router routing table path in ZK.
     */
    public String getRouterRoutingTablePath(UUID routerId) {
        return new StringBuilder(getRouterPath(routerId))
            .append("/routing_table").toString();
    }

    /**
     * Get ZK router routes path.
     * 
     * @param routerId  Router UUID
     * @return  Router routes path in ZK.
     */
    public String getRouterRoutesPath(UUID routerId) {
        return new StringBuilder(getRouterPath(routerId))
            .append("/routes").toString();        
    }

    /**
     * Get ZK router rule chains path.
     * 
     * @param routerId  Router UUID
     * @return  Router rule chains path in ZK.
     */
    public String getRouterRuleChainsPath(UUID routerId) {
        return new StringBuilder(getRouterPath(routerId))
            .append("/rule_chains").toString();        
    }
    
    /**
     * Get ZK router SNAT blocks path.
     * 
     * @param routerId  Router UUID
     * @return  Router SNAT blocks path in ZK.
     */
    public String getRouterSnatBlocksPath(UUID routerId) {
        return new StringBuilder(getRouterPath(routerId))
            .append("/snat_blocks").toString();        
    }     

    /**
     * Get ZK port routes path.
     * 
     * @param portId  Port UUID
     * @return  Port routes path in ZK.
     */
    public String getPortRoutesPath(UUID portId) {
        return getPortRoutesPath(portId, null);
    }

    /**
     * Get ZK port routes path.
     * 
     * @param portId  Port UUID
     * @param Route  Route object to store.
     * @return  Port routes path in ZK.
     */
    public String getPortRoutesPath(UUID portId, Route route) {
        StringBuilder sb = new StringBuilder(getPortPath(portId))
            .append("/routes");
        if (route != null) {
            sb.append(route.toString());
        }
        return sb.toString();
    }

}
