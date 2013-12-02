package org.jeecqrs.common;

/**
 * A layer supertype for Identifiable objects.
 */
public abstract class AbstractIdentifiable<T> implements Identifiable, CompareById<T> {

    @Override
    public final boolean sameIdentityAs(T other) {
        if (other == null || !this.getClass().equals(other.getClass()))
            return false;
        AbstractIdentifiable<T> o = (AbstractIdentifiable<T>) other;
        return this.id().equals(o.id());
    }
 
    @Override
    public final boolean equals(Object obj) {
        if (this == obj)
            return true;
	if (obj == null || getClass() != obj.getClass())
	    return false;
	final T other = (T) obj;
	return sameIdentityAs(other);
    }
    
    @Override
    public final int hashCode() {
        return this.id().hashCode();
    }

}
