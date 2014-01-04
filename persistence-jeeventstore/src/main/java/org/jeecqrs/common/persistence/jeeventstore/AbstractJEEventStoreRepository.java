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

package org.jeecqrs.common.persistence.jeeventstore;

import java.util.List;
import org.jeecqrs.common.Identifiable;
import org.jeecqrs.common.event.Event;
import org.jeecqrs.common.event.sourcing.EventSourcingBus;
import org.jeecqrs.common.event.sourcing.EventSourcingUtil;
import org.jeecqrs.common.persistence.es.AbstractEventSourcingRepository;
import org.jeeventstore.ConcurrencyException;
import org.jeeventstore.DuplicateCommitException;
import org.jeeventstore.EventStore;
import org.jeeventstore.ReadableEventStream;
import org.jeeventstore.WritableEventStream;
import org.jeeventstore.util.IteratorUtils;
import org.jodah.typetools.TypeResolver;

/**
 *
 * @param <T>  the entity type
 */
public abstract class AbstractJEEventStoreRepository<T extends Identifiable<ID>, ID> 
    extends AbstractEventSourcingRepository<T, ID> {

    public AbstractJEEventStoreRepository() {
    }

    public AbstractJEEventStoreRepository(Class<T> objectType) {
        super(objectType);
    }

    protected abstract String bucketId();

    @Override
    protected T loadFromStream(Class<T> clazz, String streamId) {
        ReadableEventStream stream = eventStore().openStreamForReading(bucketId(), streamId);
        List<Event> events = (List) IteratorUtils.toList(stream.events());
        return (T) EventSourcingUtil.createFromEventStream(clazz,
                stream.version(), events);
    }

    @Override
    protected EventSourcingBus<Event> busForAdd(String streamId) {
        WritableEventStream stream = eventStore().createStream(bucketId(), streamId);
        return busForStream(stream);
    }

    @Override
    protected EventSourcingBus<Event> busForSave(String streamId, long version) {
        WritableEventStream stream = eventStore().openStreamForWriting(bucketId(), streamId, version);
        return busForStream(stream);
    }

    private EventSourcingBus<Event> busForStream(final WritableEventStream stream) {
        return new EventSourcingBus<Event>() {
            @Override
            public void store(Event event) {
                appendToStream(stream, event);
            }
            @Override
            public void commit(String commitId) {
                commitChanges(stream, commitId);
            }
        };
    }

    protected void appendToStream(WritableEventStream stream, Event event) {
        stream.append(event);
    }

    protected void commitChanges(WritableEventStream stream, String commitId) {
        try {
            stream.commit(commitId);
        } catch (DuplicateCommitException | ConcurrencyException e) {
            throw new RuntimeException("committing changes failed: " + e.getMessage(), e);
        }
    }

    protected abstract EventStore eventStore();

}
