package org.jeecqrs.common.event;

/**
 * Provides the ability to dispatch events to interested listeners.
 */
public interface EventBus {

    /**
     * Dispatches an event on the event bus.
     * 
     * @param event the event
     */
    void dispatch(Event event);
    
}
