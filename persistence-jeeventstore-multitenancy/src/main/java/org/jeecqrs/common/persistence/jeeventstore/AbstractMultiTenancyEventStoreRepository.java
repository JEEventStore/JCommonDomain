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

public abstract class AbstractMultiTenancyEventStoreRepository<
            T extends AbstractTenantESAR<T, OID, TID>,
            OID extends Identity,
            TID extends Identity>
        extends AbstractJEEventStoreARRepository<T, MultiTenancyId<TID, OID>, org.jeecqrs.common.Identity> {

    protected boolean exists(TID tenantId, OID id) {
        MultiTenancyId mtid = new MultiTenancyId(tenantId, id);
        return super.exists(objectType(), mtid);
    }

    protected T ofIdentity(TID tenantId, OID id) {
        MultiTenancyId<TID, OID> mtid = new MultiTenancyId<>(tenantId, id);
        return super.ofIdentity(mtid);
    }

    @Override
    protected EventStreamNameGenerator<T, MultiTenancyId<TID, OID>> streamNameGenerator() {
        return new MultiTenancyCanonicalNameESNG<>();
    }
    
}