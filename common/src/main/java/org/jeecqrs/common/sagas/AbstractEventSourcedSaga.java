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

import org.jeecqrs.common.domain.model.DomainEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jeecqrs.common.commands.Command;
import org.jeecqrs.common.event.routing.ConventionEventRouter;
import org.jeecqrs.common.event.routing.EventRouter;
import org.jeecqrs.common.event.sourcing.EventSourcingBus;
import org.jeecqrs.common.event.sourcing.Load;
import org.jeecqrs.common.event.sourcing.Store;
import org.jeecqrs.common.util.Validate;

/**
 * Base implementation for sagas that use event sourcing as persistence technology.
 */
public abstract class AbstractEventSourcedSaga {

    private static final String EVENT_HANDLER_NAME = "when";

    private final Logger log = Logger.getLogger(this.getClass().getName());

    // the list of events that change the state of the aggregate relative to {@code version}
    private final List<DomainEvent> changes = new ArrayList<>();
    // the persisted version this saga is based on, used for optimistic concurrency
    private long version = 0l;
    private EventRouter<DomainEvent> eventRouter;

    private Set<String> handledEvents = new HashSet<>();
    private boolean eventSourceReplayActive = false;

    public AbstractEventSourcedSaga() {
        this(new ConventionEventRouter<DomainEvent>(true, EVENT_HANDLER_NAME));
    }

    @SuppressWarnings("LeakingThisInConstructor")
    public AbstractEventSourcedSaga(EventRouter<DomainEvent> eventRouter) {
        Validate.notNull(eventRouter, "eventRouter must not be null");
        this.eventRouter = eventRouter;
        eventRouter.register(this);
    }

    protected abstract void sendCommand(Command command);
    protected abstract void requestTimeout(DomainEvent event, long timeout);

    /**
     * Handle a domain event.
     * <p>
     * Changes the state of the saga by invoking a suitable event handler
     * and saves the event in the internal list of changes such that
     * they can be persisted to the database.
     * <p>
     * If the event has been handled previously, the event is discarded.
     * 
     * @param event  the event to handle
     */
    public final void handle(DomainEvent<?> event) {
	if (handledEvents.contains(event.id().idString())) {
            log.log(Level.FINE, "Event {0} of {1} handled already",
                    new Object[]{event.id().idString(), event.getClass().getSimpleName()});
	    return;
	}
	log.log(Level.FINE, "Handle event #{0}: {1}",
                new Object[]{event.id().idString(), event});
        this.changes.add(event);
        this.invokeHandler(event);
    }

    private void invokeHandler(DomainEvent<?> event) {
        eventRouter.dispatch(event);
        this.handledEvents.add(event.id().idString());
    }

    /**
     * Loads a version of the saga from a list of DomainEvents. 
     */
    @Load
    private void load(long version, List<DomainEvent> events) {
        Validate.isTrue(version == 0l, "Cannot load on dirty saga");
        this.eventSourceReplayActive = true;
        for (DomainEvent event : events)
            this.invokeHandler(event);
        this.eventSourceReplayActive = false;
        this.version = version;
    }

    /**
     * Stores the changes made to the saga to the given event bus.
     * Also increases the version number
     * 
     * @param eventBus  the recipient of the events
     */
    @Store
    private void store(EventSourcingBus<DomainEvent> eventBus) {
        if (this.changes.isEmpty())
            return;
        eventBus.store(version, new ArrayList<>(this.changes));
        this.changes.clear();
        this.version++;
    }

    protected boolean eventSourceReplayActive() {
        return eventSourceReplayActive;
    }

}
