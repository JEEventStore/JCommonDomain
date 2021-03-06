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

import org.jeecqrs.common.Identifiable;
import org.jeecqrs.common.commands.Command;
import org.jeecqrs.common.event.Event;
import org.jeecqrs.common.event.routing.EventRouter;
import org.jeecqrs.common.event.routing.convention.ConventionEventRouter;
import org.jeecqrs.common.util.Validate;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base implementation for sagas.
 * Implements the event handling scheme.
 * 
 */
public abstract class AbstractSaga implements Identifiable<String> {

    private static final String EVENT_HANDLER_NAME = "when";

    private final Logger log = Logger.getLogger(this.getClass().getName());

    private final EventRouter<Void, Event> eventRouter;
    private final Set<String> handledEvents = new HashSet<>();

    private SagaCommandBus commandBus;
    private SagaTimeoutProvider timeoutProvider;

    protected AbstractSaga() {
        this(new ConventionEventRouter<Void, Event>(true, EVENT_HANDLER_NAME));
    }

    @SuppressWarnings("LeakingThisInConstructor")
    protected AbstractSaga(EventRouter<Void, Event> eventRouter) {
        Validate.notNull(eventRouter, "eventRouter must not be null");
        this.eventRouter = eventRouter;
        eventRouter.register(this);
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
                    new Object[]{event.id(), event.getClass().getSimpleName()});
	    return;
	}
	log.log(Level.FINE, "Handle event #{0}: {1}",
                new Object[]{event.id(), event});
        this.invokeHandler(event);
    }

    /**
     * Returns whether the given event has been handled already.
     * 
     * @param event  the event in question
     * @return <pre>true</pre> if the event has already been handled
     */
    protected boolean handled(Event event) {
        return handledEvents.contains(event.id().toString());
    }

    /**
     * Marks the given event as handled.
     * 
     * @param event  the event
     */
    protected void markedAsHandled(Event event) {
        this.handledEvents.add(event.id().toString());
    }

    /**
     * Invokes the handler for the given event and marks the event as handled.
     * 
     * @param event  the event to handle
     */
    protected void invokeHandler(Event<?> event) {
        eventRouter.routeEvent(event);
        markedAsHandled(event);
    }

    protected void send(Command command) {
        if (commandBus == null)
            throw new IllegalStateException("No commandBus has been injected");
        commandBus.send(command);
    }

    protected void sendAndForget(Command command) {
        if (commandBus == null)
            throw new IllegalStateException("No commandBus has been injected");
        commandBus.sendAndForget(command);
    }

    protected void raiseEvent(final Event event, final long delay) {
        if (timeoutProvider == null)
            throw new IllegalStateException("No timeout provider has been injected");
        timeoutProvider.requestTimeout(this.id(), event, delay);
    }

    /**
     * Sets the commandBus to use for sending commands.
     * 
     * @param commandBus  the command bus
     */
    public void setCommandBus(SagaCommandBus commandBus) {
        this.commandBus = commandBus;
    }

    /**
     * Sets the SagaTimeoutProvider to use.
     *
     * @param timeoutProvider  the timeout provider
     */
    public void setTimeoutProvider(SagaTimeoutProvider timeoutProvider) {
        this.timeoutProvider = timeoutProvider;
    }

}
