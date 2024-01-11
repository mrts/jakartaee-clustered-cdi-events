/*
 * Copyright 2024 Mart Somermaa
 * SPDX-License-Identifier: Apache-2.0
 */

package org.clustercdievents.jms;

import org.clustercdievents.cdievents.CDIEventEmitter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jms.TextMessage;

import static org.clustercdievents.cdievents.CDIEventEmitterTest.TEST_JSON;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JMSMessageReceiverTest {

    @InjectMocks
    private JMSMessageReceiver jmsMessageReceiver;

    @Mock
    private CDIEventEmitter cdiEventEmitter;

    @Mock
    private TextMessage textMessage;

    @Test
    void testOnMessage() throws Exception {
        when(textMessage.getText()).thenReturn(TEST_JSON);

        jmsMessageReceiver.onMessage(textMessage);

        verify(cdiEventEmitter, times(1)).fireLocalAsyncCDIEventFromJMSMessage(TEST_JSON);
    }

}
