/*
 * Copyright 2024 Mart Somermaa
 * SPDX-License-Identifier: Apache-2.0
 */

package org.clustercdievents.cdievents;

import org.clustercdievents.jms.JMSMessageSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.enterprise.inject.spi.EventMetadata;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CDIEventObserverTest {

    @InjectMocks
    private CDIEventObserver cdiEventObserver;

    @Mock
    private JMSMessageSender jmsMessageSender;
    @Mock
    private CDIEventEmitter cdiEventEmitter;
    @Mock
    private EventMetadata metaData;

    private final TestEvent testEvent = new TestEvent();

    @BeforeEach
    void setUp() {
        when(metaData.getInjectionPoint()).thenReturn(null);
        when(cdiEventEmitter.getNodeId()).thenReturn("1234");
    }

    @Test
    void testObserveAllEvents() {
        cdiEventObserver.observeAllEvents(testEvent, metaData);
        verify(jmsMessageSender, times(1)).send(anyString());
    }

    @Test
    void testObserveAllEventsAsync() {
        cdiEventObserver.observeAllEventsAsync(testEvent, metaData);
        verify(jmsMessageSender, times(1)).send(anyString());
    }

}