package org.clustercdievents.cdievents;

public class CDIEventJmsMessageEnvelope {

    private String json;
    private String nodeId;
    private String objectType;

    public CDIEventJmsMessageEnvelope() {
    }

    public CDIEventJmsMessageEnvelope(String nodeId,
                                      String json,
                                      String objectType) {
        this.nodeId = nodeId;
        this.json = json;
        this.objectType = objectType;
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
}
