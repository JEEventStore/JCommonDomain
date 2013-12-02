package org.jeecqrs.common.domain.model;

/**
 * Layer supertype for domain events.
 */
public abstract class AbstractValueObject<T> implements ValueObject<T> {

    @Override
    public final boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null || getClass() != obj.getClass())
	    return false;
	final T other = (T) obj;
	return sameValueAs(other);
    }
    
}
