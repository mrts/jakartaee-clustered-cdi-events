/*
 * Copyright 2024 Mart Somermaa
 * SPDX-License-Identifier: Apache-2.0
 */

package org.clustercdievents.jms;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Topic;

import static java.util.UUID.randomUUID;

@Stateless
public class JMSMessageSender {

    static final String TOPIC_NAME = "CLUSTER_CDI_EVENTS";
    static final String JMS_DESTINATION = "jms/topic/" + TOPIC_NAME;

    private final static Logger log = LoggerFactory.getLogger(JMSMessageSender.class);

    @Inject
    private JMSContext jmsContext;

    @Resource(lookup = "java:/" + JMS_DESTINATION)
    private Topic topic;

    public void send(String message) {
        final Message jmsMessage = jmsContext.createTextMessage(message);
        try {
            // The _AMQ_DUPL_ID property is used by ActiveMQ Artemis for duplicate detection.
            // Messages with the same _AMQ_DUPL_ID value are considered duplicates and only one of them is processed.
            // This is needed in case a two-way JMS bridge is in use like in test-jakartaee-clustered-cdi-events.
            jmsMessage.setStringProperty("_AMQ_DUPL_ID", randomUUID().toString());
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
        log.debug("Sending message '{}' to topic {}", jmsMessage, topic);
        jmsContext.createProducer().send(topic, jmsMessage);
    }

}