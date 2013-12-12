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

package org.jeecqrs.common.sagas;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jeecqrs.common.Identifiable;
import org.jeecqrs.common.Identity;
import org.jeecqrs.common.event.Event;
import org.jeecqrs.common.event.routing.ConventionEventRouter;
import org.jeecqrs.common.event.routing.EventRouter;
import org.jeecqrs.common.util.Validate;

/**
 * Base implementation for sagas.
 * Implements the event handling scheme.
 */
public abstract class AbstractSaga implements Identifiable {

    private static final String EVENT_HANDLER_NAME = "when";

    private final Logger log = Logger.getLogger(this.getClass().getName());

    private Identity id;
    private EventRouter<Event> eventRouter;
    private final Set<String> handledEvents = new HashSet<>();

    protected AbstractSaga(String sagaId) {
        this(sagaId, new ConventionEventRouter<>(true, EVENT_HANDLER_NAME));
    }

    protected AbstractSaga(String sagaId, EventRouter<Event> eventRouter) {
        this(new SagaId(sagaId), eventRouter);
    }

    @SuppressWarnings("LeakingThisInConstructor")
    protected AbstractSaga(Identity id, EventRouter<Event> eventRouter) {
        Validate.notNull(id, "id must not be null");
        Validate.notNull(eventRouter, "eventRouter must not be null");
        this.id = id;
        this.eventRouter = eventRouter;
        eventRouter.register(this);
    }

    @Override
    public Identity id() {
        return id;
    }

    /**
     * Handle a domain event.
     * If the event has been handled previously, the event is discarded.
     * 
     * @param event  the event to handle
     */
    public void handle(Event event) {
	if (handled(event)) {
            log.log(Level.FINE, "Event {0} of {1} handled already",
                    new Object[]{event.id().idString(), event.getClass().getSimpleName()});
	    return;
	}
	log.log(Level.FINE, "Handle event #{0}: {1}",
                new Object[]{event.id().idString(), event});
        this.invokeHandler(event);
    }

    /**
     * Returns whether the given event has been handled already.
     * 
     * @param event  the event in question
     * @return <pre>true</pre> if the event has already been handled
     */
    protected boolean handled(Event event) {
        return handledEvents.contains(event.id().idString());
    }

    /**
     * Marks the given event as handled.
     * 
     * @param event  the event
     */
    protected void markedAsHandled(Event event) {
        this.handledEvents.add(event.id().idString());
    }

    /**
     * Invokes the handler for the given event and marks the event as handled.
     * 
     * @param event  the event to handle
     */
    protected void invokeHandler(Event<?> event) {
        eventRouter.dispatch(event);
        markedAsHandled(event);
    }

}
