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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import org.jeecqrs.common.Identifiable;
import org.jeecqrs.common.Identity;
import org.jeecqrs.common.domain.model.AbstractEventSourcedAggregateRoot;
import org.jeecqrs.common.domain.model.DomainEvent;
import org.jeecqrs.common.event.sourcing.EventSourcingBus;
import org.jeecqrs.common.event.sourcing.EventSourcingUtil;
import org.jeecqrs.common.event.sourcing.Store;
import org.jeecqrs.common.util.ReflectionUtils;
import org.jeecqrs.common.util.Validate;
import org.jeeventstore.ConcurrencyException;
import org.jeeventstore.DuplicateCommitException;
import org.jeeventstore.EventStore;
import org.jeeventstore.ReadableEventStream;
import org.jeeventstore.WritableEventStream;

/**
 *
 * @param <T>  the entity type
 */
public abstract class AbstractEventSourcingRepository<T extends Identifiable> {

    private Logger log = Logger.getLogger(this.getClass().getCanonicalName());

    protected T ofIdentity(Identity id) {
        Validate.notNull(id, "id must not be null");
	long start = System.currentTimeMillis();
        String streamId = streamIdFor(id);
        ReadableEventStream stream = eventStore().openStreamForReading(bucketId(), streamId);
        T obj = (T) EventSourcingUtil.createFromEventStream(entityClass(),
                stream.version(), (List) stream.events());
	long end = System.currentTimeMillis();
	log.log(Level.FINE, "Loaded in {0} ms entity {1}#{2}",
                new Object[]{end-start, entityClass().getSimpleName(), id});
	return obj;
    }

    protected String streamIdFor(Identity id) {
        return streamNameGenerator().streamNameFor(entityClass(), id);
    }

    protected void add(T obj, String commitId) {
        Validate.notNull(obj, "object must not be null");
        String streamId = streamIdFor(obj.id());
        WritableEventStream stream = eventStore().createStream(bucketId(), streamId);
        copyChanges(obj, stream);
        commit(stream, commitId);
    }

    public void save(T obj, String commitId) {
        Validate.notNull(obj, "object must not be null");
        long version = retrieveVersion(obj);
        String streamId = streamIdFor(obj.id());
        WritableEventStream stream = eventStore().openStreamForWriting(bucketId(), streamId, version);
        commit(stream, commitId);
    }

    private void copyChanges(T obj, final WritableEventStream stream) {
        EventSourcingBus<DomainEvent> bus = new EventSourcingBus<DomainEvent>() {
            @Override
            public void store(DomainEvent event) {
                stream.append(event);
            }
        };
        invokeStore(obj, bus);
    }

    private void commit(WritableEventStream stream, String commitId) {
        try {
            stream.commit(commitId);
        } catch (DuplicateCommitException | ConcurrencyException e) {
            throw new RuntimeException("committing changes failed: " + e.getMessage(), e);
        }
    }

    private void invokeStore(T obj, EventSourcingBus<DomainEvent> bus) {
	try {
            Method store = ReflectionUtils.findUniqueMethod(entityClass(), Store.class,
                    new Object[]{EventSourcingBus.class});
            store.invoke(obj, new Object[] { bus });
	} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            String msg = String.format("Cannot find store method for type %s: %s",
                    entityClass(), e.getMessage());
	    throw new RuntimeException(msg, e);
	}
    }

    protected long retrieveVersion(T obj) {
        return EventSourcingUtil.retrieveVersion(obj);
    }

    protected String bucketId() {
        return "DEFAULT";
    }

    protected EventStreamNameGenerator streamNameGenerator() {
        return new CanonicalNameEventStreamNameGenerator();
    }

    protected abstract Class<T> entityClass();
    protected abstract EventStore eventStore();

}
