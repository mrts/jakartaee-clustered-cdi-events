/*
 * Copyright 2024 Mart Somermaa
 * SPDX-License-Identifier: Apache-2.0
 */

package org.clustercdievents.cdievents;

public class CDIEventJmsMessageEnvelope {

    private String json;
    private String nodeId;
    private String objectType;
    private boolean isAsync;

    public CDIEventJmsMessageEnvelope() {
    }

    public CDIEventJmsMessageEnvelope(String nodeId, String json, String objectType, boolean isAsync) {
        this.nodeId = nodeId;
        this.json = json;
        this.objectType = objectType;
        this.isAsync = isAsync;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getJson() {
        return json;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public void setAsync(boolean async) {
        isAsync = async;
    }
}
