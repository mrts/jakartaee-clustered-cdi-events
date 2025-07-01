/*
 * Copyright 2024 Mart Somermaa
 * SPDX-License-Identifier: Apache-2.0
 */

package org.clustercdievents.cdievents;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.enterprise.event.Event;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CDIEventEmitterTest {

    public static final String TEST_JSON = "{\"nodeId\": \"12345\", \"objectType\": \"org.clustercdievents.cdievents.TestEvent\", \"json\": \"{\\\"data\\\":\\\"test\\\"}\"}";

    @InjectMocks
    private CDIEventEmitter cdiEventEmitter;

    @Mock
    private Event<Object> eventBus;

    @Test
    void testFireLocalCDIEventFromJMSMessage() {
        cdiEventEmitter.fireLocalCDIEventFromJMSMessage(TEST_JSON);
        verify(eventBus, times(1)).fire(new TestEvent("test"));
    }

    @Test
    void testFireLocalCDIEventFromJMSMessageAsync() {
        cdiEventEmitter.fireLocalCDIEventFromJMSMessage(TEST_JSON.replace("cdievents.TestEvent\",", "cdievents.TestEvent\", \"async\": true,"));
        verify(eventBus, times(1)).fireAsync(new TestEvent("test"));
    }

}