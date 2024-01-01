package org.clustercdievents.jms;

import org.clustercdievents.cdievents.CDIEventEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = JMSMessageSender.JMS_DESTINATION),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")})
public class JMSMessageReceiver implements MessageListener {

    private final static Logger log = LoggerFactory.getLogger(JMSMessageReceiver.class);

    @Inject
    private CDIEventEmitter cdiEventEmitter;

    @Override
    public void onMessage(Message receivedMessage) {
        try {
            final String json = ((TextMessage) receivedMessage).getText();
            log.debug("Received message '{}' from JMS destination {}", json, receivedMessage.getJMSDestination());
            cdiEventEmitter.fireLocalCDIEventFromJMSMessage(json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}