package org.clustercdievents.cdievents;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.enterprise.event.Event;


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
    void testfireLocalCDIEventFromJMSMessage() {
        cdiEventEmitter.fireLocalCDIEventFromJMSMessage(TEST_JSON);

        verify(eventBus, times(1)).fire(new TestEvent("test"));
    }

}