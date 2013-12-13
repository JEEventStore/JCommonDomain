package org.jeecqrs.common.event;

/**
 * Provides the ability to dispatch events to interested listeners.
 */
public interface EventBus {

    /**
     * Dispatches an event wrapped in an envelope on the event bus.
     * 
     * @param envelope     the envelope
     */
    void dispatch(EventBusEnvelope envelope);
    
}
