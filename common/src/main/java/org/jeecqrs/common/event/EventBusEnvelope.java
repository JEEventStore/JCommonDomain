package org.jeecqrs.common.event;

/**
 * Provides the ability to wrap an event into an envelope associated with
 * metadata.
 */
public interface EventBusEnvelope {

    /**
     * Returns the enclosed event.
     * 
     * @return  the event
     */
    Event event();

    /**
     * Specifies the transmission delay for the enclosed event message.
     * 
     * @return  the delay
     */
    long delay();
    
}
