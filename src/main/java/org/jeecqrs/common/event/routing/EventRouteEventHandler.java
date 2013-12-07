package org.jeecqrs.common.event.routing;

import org.jeecqrs.common.event.Event;

/**
 * Provides an endpoint for the event route strategy and handles an event.
 * 
 * @param <E>  the base event class
 */
public interface EventRouteEventHandler<E extends Event> {

    void handle(E event);
    
}
