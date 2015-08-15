package org.jeecqrs.common.domain.model.multitenancy;

import org.jeecqrs.common.Identity;
import org.jeecqrs.common.domain.model.AbstractDomainEvent;

public abstract class AbstractMultiTenancyDomainEvent<T, TID extends Identity> 
	extends AbstractDomainEvent<T>
	implements MultiTenancyDomainEvent<T, TID> {

    private final TID tenantId;

    public AbstractMultiTenancyDomainEvent(TID tenantId) {
        if (tenantId == null)
            throw new NullPointerException("tenantId must not be null");
	this.tenantId = tenantId;
    }

    @Override
    public final TID tenantId() {
	return tenantId;
    }

}
