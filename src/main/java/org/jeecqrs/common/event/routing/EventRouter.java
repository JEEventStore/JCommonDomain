/*
 * Copyright (c) 2013 Red Rainbow IT Solutions GmbH, Germany
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
