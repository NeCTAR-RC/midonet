/*
 * Copyright 2011 Midokura Europe SARL
 */

package com.midokura.midolman.mgmt.data.dto;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.UUID;

/**
 * Author: Toader Mihai Claudiu <mtoader@gmail.com>
 * <p/>
 * Date: 11/28/11
 * Time: 3:33 PM
 */
@XmlRootElement
public class DtoBgp {
    private UUID id = null;
    private int localAS;
    private int peerAS;
    private String peerAddr = null;
    private URI adRoutes;
    private URI uri;

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getLocalAS() {
        return localAS;
    }

    public void setLocalAS(int localAS) {
        this.localAS = localAS;
    }

    public int getPeerAS() {
        return peerAS;
    }

    public void setPeerAS(int peerAS) {
        this.peerAS = peerAS;
    }

    public String getPeerAddr() {
        return peerAddr;
    }

    public void setPeerAddr(String peerAddr) {
        this.peerAddr = peerAddr;
    }

    public URI getAdRoutes() {
        return adRoutes;
    }

    public void setAdRoutes(URI adRoutes) {
        this.adRoutes = adRoutes;
    }
}
