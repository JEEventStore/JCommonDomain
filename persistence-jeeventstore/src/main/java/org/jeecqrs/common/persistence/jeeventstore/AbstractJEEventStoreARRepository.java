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

import org.jeecqrs.common.Identity;
import org.jeecqrs.common.domain.model.AbstractEventSourcedAggregateRoot;
import org.jeecqrs.common.util.ReflectionUtils;

/**
 *
 * @param <T>  the aggregate type
 * @param <ID>  the type used to identify aggregate roots
 */
public abstract class
AbstractJEEventStoreARRepository<
        T extends AbstractEventSourcedAggregateRoot<T, ? extends Identity>,
        ID,
        CID> 
    extends AbstractJEEventStoreRepository<T, ID, CID> {
    
    public AbstractJEEventStoreARRepository() {
        Class<T> type = (Class) ReflectionUtils.findSuperClassParameterType(this, AbstractJEEventStoreARRepository.class, 0);
        this.setObjectType(type);
    }
}
