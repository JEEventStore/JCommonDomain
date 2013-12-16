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

import java.util.ArrayList;
import java.util.List;
import org.jeecqrs.common.Identity;
import org.jeecqrs.common.event.Event;
import org.jeecqrs.common.event.routing.EventRouter;
import org.jeecqrs.common.event.sourcing.EventSourcingBus;
import org.jeecqrs.common.event.sourcing.Load;
import org.jeecqrs.common.event.sourcing.Store;
import org.jeecqrs.common.event.sourcing.Version;
import org.jeecqrs.common.util.Validate;

/**
 * Base implementation for sagas that use event sourcing as persistence technology.
 */
public abstract class AbstractEventSourcedSaga extends AbstractSaga {

    // the list of events that change the state of the aggregate relative to {@code version}
    private final List<Event> changes = new ArrayList<>();
    // the persisted version this saga is based on, used for optimistic concurrency
    private long version = 0l;
    private boolean eventSourceReplayActive = false;

    protected AbstractEventSourcedSaga(SagaId sagaId) {
        super(sagaId);
    }

    protected AbstractEventSourcedSaga(SagaId sagaId, EventRouter<Void, Event> eventRouter) {
        super(sagaId, eventRouter);
    }

    @Override
    protected void invokeHandler(Event<?> event) {
        super.invokeHandler(event);
        if (!eventSourceReplayActive)
            this.changes.add(event);
    }

    /**
     * Loads a version of the saga from a list of Event. 
     */
    @Load
    private void load(long version, List<Event> events) {
        Validate.isTrue(version == 0l, "Cannot load on dirty saga");
        this.eventSourceReplayActive = true;
        for (Event event : events)
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
    private void store(EventSourcingBus<Event> eventBus) {
        for (Event event : this.changes)
            eventBus.store(event);
        this.changes.clear();
        this.version++;
    }

    @Version
    private long version() {
        return this.version;
    }

    protected boolean eventSourceReplayActive() {
        return eventSourceReplayActive;
    }

}
