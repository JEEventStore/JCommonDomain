/*
 * Copyright (c) 2013-2014 Red Rainbow IT Solutions GmbH, Germany
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

package org.jeecqrs.common.persistence.es;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jeecqrs.common.event.Event;
import org.jeecqrs.common.event.sourcing.EventSourcingBus;
import org.jeecqrs.common.event.sourcing.EventSourcingUtil;
import org.jeecqrs.common.util.Validate;

/**
 *
 * @param <T>  the entity type
 * @param <ID>  the type used to identify entities
 * @param <CID>  the type used to identify commits
 */
public abstract class AbstractEventSourcingRepository<T, ID, CID> {

    private Class<T> objectType;

    public AbstractEventSourcingRepository() {
    }

    public AbstractEventSourcingRepository(Class<T> objectType) {
        this.objectType = objectType;
    }

    private Logger log = Logger.getLogger(this.getClass().getCanonicalName());

    protected abstract EventStreamNameGenerator<T, ID> streamNameGenerator();
    protected abstract T createFreshInstance(Class<T> clazz, ID id);
    protected abstract void loadFromStream(T obj, String streamId);
    protected abstract EventSourcingBus<Event> busForAdd(String streamId);
    protected abstract EventSourcingBus<Event> busForSave(String streamId, long version);

    protected T ofIdentity(Class<T> clazz, ID id) {
        Validate.notNull(id, "id must not be null");
	long start = System.currentTimeMillis();
        String streamId = streamNameGenerator().streamNameFor(clazz, id);
        T obj = createFreshInstance(clazz, id);
        loadFromStream(obj, streamId);
	long end = System.currentTimeMillis();
	log.log(Level.FINE, "Loaded in {0} ms entity {1}#{2}",
                new Object[]{end-start, clazz.getSimpleName(), id});
	return obj;
    }

    public T ofIdentity(ID id) {
        if (objectType == null)
            throw new IllegalStateException("Cannot call #ofIdentity() without objectType");
        return this.ofIdentity(objectType, id);
    }

    public void add(T obj, CID commitId) {
        Validate.notNull(obj, "object must not be null");
        String streamId = streamNameGenerator().streamNameFor(obj);
        EventSourcingBus<Event> bus = busForAdd(streamId);
        invokeStore(obj, bus);
        bus.commit(commitId.toString());
    }

    public void save(T obj, CID commitId) {
        Validate.notNull(obj, "object must not be null");
        String streamId = streamNameGenerator().streamNameFor(obj);
        long version = retrieveVersion(obj);
        EventSourcingBus<Event> bus = busForSave(streamId, version);
        invokeStore(obj, bus);
        bus.commit(commitId.toString());
    }

    private void invokeStore(T obj, EventSourcingBus<Event> bus) {
        EventSourcingUtil.invokeStoreMethod(obj, bus);
    }

    protected long retrieveVersion(T obj) {
        return EventSourcingUtil.retrieveVersion(obj);
    }

    public Class<T> objectType() {
        return objectType;
    }

    protected final void setObjectType(Class<T> objectType) {
        this.objectType = objectType;
    }

}
