package org.jeecqrs.common.event.routing;

import org.jeecqrs.common.event.Event;

/**
 * Provides the strategy to route an event to the desired event handler.
 */
public interface EventRouter<E extends Event> {
    
    /**
     * Registers all event handlers within the given object with the
     * event router. 
     * The handler lookup strategy depends on the implementation.
     * 
     * @param obj  the object to be registered
     */
    void register(Object obj);

    /**
     * Registers the given event handler for the event given type.
     * There must not be already an handler registered for the given type.
     * 
     * @param eventType  the event type to be handled, not null
     * @param handler   the route handler for the event type, not null
     */
    void register(Class<? extends E> eventType, EventRouteEventHandler<E> handler);

    /**
     * Dispatches the given event to a suitable event handler.
     * May throw an exception when no handler is found (implementation dependent).
     * 
     * @param event  the event to bis dispatched, not null
     */
    void dispatch(E event);
    
}
