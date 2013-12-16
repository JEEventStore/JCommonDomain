package org.jeecqrs.common.sagas;

import org.jeecqrs.common.event.Event;

/**
 * Provides the ability to request a timeout for a saga.
 */
public interface SagaTimeoutProvider {
    
    void requestTimeout(SagaId sagaId, Event event, long timeout);
    
}
