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

package org.jeecqrs.common.event.sourcing;

import java.util.List;

/**
 * Provides the ability to load and mutate an object by means of event sourcing.
 * 
 * @param <E>  the base event type
 */
public interface EventSourced<E> {

    /**
     * Loads the version of the object corresponding to the given list
     * of events.
     * The object must be newly constructed, i.e., the current {@link #version}
     * must be 0 and {@link #mutatingEvents()} must be empty.
     * The events must be applied in order of the list.
     * 
     * @param version  the new version of this object after all events have been applied
     * @param events   the events, not null
     */
    void loadVersion(long version, List<E> events);

    /**
     * Gets the current version of this object.  
     * 
     * @return  the current, unmutated version, i.e., before the mutating
     *          events have been applied to this object.
     */
    long version();

    /**
     * Persists all changes and increases the {@link #version}.
     * 
     * @param eventBus  the bus that receives the events that shall be persisted
     */
    void persist(EventSourcingBus<E> eventBus);
    
}