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

package org.jeecqrs.common.domain.model;

import java.util.ArrayList;
import java.util.List;
import org.jeecqrs.common.event.routing.convention.ConventionEventRouter;
import org.jeecqrs.common.event.routing.EventRouter;
import org.jeecqrs.common.event.sourcing.EventSourcingBus;
import org.jeecqrs.common.event.sourcing.Load;
import org.jeecqrs.common.event.sourcing.Store;
import org.jeecqrs.common.event.sourcing.Version;
import org.jeecqrs.common.util.Validate;

/**
 * AbstractEventSourcedAggregateRoot is the default implementation for 
 * EventSourcedAggregateRoot instances.
 * Note that subclasses MUST NOT change their state directly -- neither by direct
 * field access nor by calling setters.  Rather, each state changing operation
 * must create a corresponding {@link DomainEvent} and call {@link #apply(event)}.
 * The latter adds the domain event to the internal list of changes and
 * calls the corresponding event handler for the domain event.  Only
 * the event handler may change the internal state of the aggregate.
 * <p>
 * By default, the {@link ConventionEventRouter} strategy is used to find
 * event handlers for the {@link DomainEvent} with a default method name
 * of {@code when}.
 * Subclasses may specify a different event handler lookup strategy.
 * <p>
 * Some implementation details: The repository loads a specific {@code version}
 * of the aggregate with the internal {@link #load} method from a given
 * (possibly empty) list of {@link DomainEvent}s.
 * Subsequent changes to the aggregate are stored internally.  To persist
 * any changes, the event store repository calls the internal {@link #store}
 * method.
 */
public abstract class AbstractEventSourcedAggregateRoot<T> extends AbstractAggregateRoot<T> {

    private static final String EVENT_HANDLER_NAME = "when";

    // the list of events that change the state of the aggregate relative to {@code version}
    private final List<DomainEvent> changes = new ArrayList<>();
    // the persisted version this object is based on, used for optimistic concurrency
    private long version = 0l;
    private EventRouter<Void, DomainEvent> eventRouter;

    public AbstractEventSourcedAggregateRoot() {
        this(new ConventionEventRouter<Void, DomainEvent>(true, EVENT_HANDLER_NAME));
    }

    @SuppressWarnings("LeakingThisInConstructor")
    public AbstractEventSourcedAggregateRoot(EventRouter<Void, DomainEvent> eventRouter) {
        Validate.notNull(eventRouter, "eventRouter must not be null");
        this.eventRouter = eventRouter;
        eventRouter.register(this);
    }

    /**
     * Applies a new domain event.
     * Changes the state of the object by invoking a suitable event handler
     * and saves the event in the internal list of changes such that
     * they can be persisted to the database.
     * 
     * @param event  the event to apply 
     */
    protected final void apply(DomainEvent<?> event) {
        this.changes.add(event);
        this.invokeHandler(event);
    }

    private void invokeHandler(DomainEvent<?> event) {
        eventRouter.routeEvent(event);
    }

    /**
     * Loads a version of the aggregate root from a list of DomainEvents. 
     */
    @Load
    private void load(long version, List<DomainEvent> events) {
        Validate.isTrue(version == 0l, "Cannot loadFromEventStream() on dirty AggregateRoot");
        for (DomainEvent event : events)
            this.invokeHandler(event);
        this.version = version;
    }

    /**
     * Stores the changes made to the aggregate root to the given event bus.
     * Also increases the version number
     * 
     * @param eventBus  the recipient of the events
     */
    @Store
    private void store(EventSourcingBus<DomainEvent> eventBus) {
        for (DomainEvent event : this.changes)
            eventBus.store(event);
        this.changes.clear();
        this.version++;
    }

    @Version
    private long version() {
        return this.version;
    }

}
