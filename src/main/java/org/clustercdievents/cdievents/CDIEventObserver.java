/*
 * Copyright 2024 Mart Somermaa
 * SPDX-License-Identifier: Apache-2.0
 */

package org.clustercdievents.cdievents;

import org.clustercdievents.jms.JMSMessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.ObservesAsync;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.EventMetadata;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

@ApplicationScoped
public class CDIEventObserver {

    private static final Logger log = LoggerFactory.getLogger(CDIEventObserver.class);

    @Inject
    private JMSMessageSender jmsMessageSender;

    @Inject
    private CDIEventEmitter cdiEventEmitter;

    void observeAllEventsAsync(@ObservesAsync Object event, EventMetadata metaData) {
        observeAllEventsImpl(event, metaData, true);
    }

    void observeAllEvents(@Observes(during = TransactionPhase.AFTER_SUCCESS) Object event, EventMetadata metaData) {
        observeAllEventsImpl(event, metaData, false);
    }

    private void observeAllEventsImpl(Object event, EventMetadata metaData, boolean isAsync) {
        log.debug("Observing CDI event: {}, async: {}", event.getClass().getName(), isAsync);
        if (shouldObserveThisEvent(event, metaData)) {
            log.debug("Sending observed CDI event to JMS: {}, async: {}", event.getClass().getName(), isAsync);
            jmsMessageSender.send(toJSON(event, isAsync));
        } else {
            log.debug("CDI event not applicable for forwarding to JMS: {}, async: {}", event.getClass().getName(), isAsync);
        }
    }

    private String toJSON(Object event, boolean isAsync) {
        try (Jsonb jsonb = JsonbBuilder.create()) {
            final String eventJson = jsonb.toJson(event);
            final String objectType = event.getClass().getName();
            final CDIEventJmsMessageEnvelope messageEnvelope = new CDIEventJmsMessageEnvelope(cdiEventEmitter.getNodeId(), eventJson, objectType, isAsync);
            return jsonb.toJson(messageEnvelope);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean shouldObserveThisEvent(Object event, EventMetadata metaData) {
        return event.getClass().isAnnotationPresent(Clustered.class) && !createdByCdiEventEmitter(metaData);
    }

    private boolean createdByCdiEventEmitter(EventMetadata metaData) {
        if (metaData == null || metaData.getInjectionPoint() == null) {
            return false;
        } else {
            return metaData.getInjectionPoint().getBean().getBeanClass().equals(CDIEventEmitter.class);
        }
    }

}
