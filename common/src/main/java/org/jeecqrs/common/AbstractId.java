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

package org.jeecqrs.common;

import org.jeecqrs.common.util.Validate;
import org.jeecqrs.common.domain.model.ValueObject;
import java.util.UUID;

/**
 * A layer supertype for ID value objects.
 * This class is mainly here to follow the DRY-principle:
 * All ID value objects require an id-string that uniquely define
 * the aggregate, and any two ids of the same type are considered
 * equals if their idString is equal.
 */
public abstract class AbstractId<T> implements ValueObject<T>, Identity {

    private String id;

    /**
     * The default constructor creates a random id.
     */
    public AbstractId() {
        this(UUID.randomUUID().toString().toUpperCase());
    }

    /**
     * Create a new {@code AbstractId} from a {@code String} representation.
     * 
     * @param idString  the string representation
     */
    public AbstractId(String idString) {
	Validate.notNull(idString, "idString must not be null");
	this.id = idString;
    }

    @Override
    public final boolean sameValueAs(T other) {
        if (other == null || !this.getClass().equals(other.getClass()))
            return false;
        AbstractId<T> o = (AbstractId<T>) other;
        return this.toString().equals(o.toString());
    }

    @Override
    public final boolean equals(Object o) {
	if (this == o)
	    return true;
	if (o == null || getClass() != o.getClass())
	    return false;
        T other = (T) o;
	return sameValueAs(other);
    }

    @Override
    public final int hashCode() {
	return toString().hashCode();
    }

    @Override
    public String toString() {
        return this.id;
    }

}
