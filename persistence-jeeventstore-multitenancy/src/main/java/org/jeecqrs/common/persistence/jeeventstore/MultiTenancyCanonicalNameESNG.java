/*
 * Copyright (c) 2014 Red Rainbow IT Solutions GmbH, Germany
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

import org.jeecqrs.common.domain.model.multitenancy.AbstractTenantESAR;
import org.jeecqrs.common.Identity;
import org.jeecqrs.common.persistence.es.EventStreamNameGenerator;

/**
 * Provides the tenantId and the class's canonical name as a name for the event stream.
 * 
 * @param <T>    the base type of objects that can be identified with this streamNameGenreator
 * @param <OID>  the type of the object identifier
 * @param <TID>  the type of the tenant identifier
 */
public class MultiTenancyCanonicalNameESNG<
            T extends AbstractTenantESAR<T, OID, TID>,
            OID extends Identity,
            TID extends Identity>
        implements EventStreamNameGenerator<T, MultiTenancyId<TID, OID>> {

    @Override
    public String streamNameFor(T obj) {
        return build((Class) obj.getClass(), obj.tenantId(), obj.id());
    }

    @Override
    public String streamNameFor(Class<? extends T> clazz, MultiTenancyId<TID, OID> id) {
        return build(clazz, id.tenantId(), id.objectId());
    }

    private String build(Class<? extends AbstractTenantESAR<?, ?, ?>> clazz, TID tenantId, Identity objId) {
        return new StringBuilder()
                .append(tenantId.toString())
                .append(":")
                .append(clazz.getCanonicalName())
    		.append(":")
		.append(objId.toString())
		.toString();
    }

}
