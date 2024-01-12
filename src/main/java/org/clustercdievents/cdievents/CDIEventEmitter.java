/*
 * Copyright 2024 Mart Somermaa
 * SPDX-License-Identifier: Apache-2.0
 */

package org.clustercdievents.cdievents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.util.Objects;
import java.util.UUID;

@ApplicationScoped
public class CDIEventEmitter {

    private static final Logger log = LoggerFactory.getLogger(CDIEventEmitter.class);

    @Inject
    private Event<Object> eventBus;

    private final String nodeId = UUID.randomUUID().toString();

    public void fireLocalCDIEventFromJMSMessage(String jsonFromJMSMessage) {
        log.debug("Processing JMS message for emitting local CDI event: {}", jsonFromJMSMessage);
        try (Jsonb jsonb = JsonbBuilder.create()) {
            final CDIEventJmsMessageEnvelope envelope = jsonb.fromJson(jsonFromJMSMessage, CDIEventJmsMessageEnvelope.class);
            Objects.requireNonNull(envelope.getNodeId(), "nodeId must not be null");
            if (!nodeId.equals(envelope.getNodeId())) {
                Objects.requireNonNull(envelope.getObjectType(), "objectType must not be null");
                final Class<?> objectClass = Class.forName(envelope.getObjectType());
                final Object event = jsonb.fromJson(envelope.getJson(), objectClass);
                if (envelope.isAsync()) {
                    eventBus.fireAsync(event);
                    log.debug("Local async CDI event fired from JMS message for class {}", objectClass.getName());
                } else {
                    eventBus.fire(event);
                    log.debug("Local CDI event fired from JMS message for class {}", objectClass.getName());
                }
            } else {
                log.debug("JMS message node ID matches own node ID, ignoring message to self");
            }
        } catch (Exception e) {
            log.error("Error processing JMS message for CDI event: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public String getNodeId() {
        return nodeId;
    }

}
