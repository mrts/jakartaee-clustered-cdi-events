/*
 * Copyright 2024 Mart Somermaa
 * SPDX-License-Identifier: Apache-2.0
 */

package org.clustercdievents.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Topic;

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
        log.debug("Sending message '{}' to topic {}", message, topic);
        jmsContext.createProducer().send(topic, message);
    }

}