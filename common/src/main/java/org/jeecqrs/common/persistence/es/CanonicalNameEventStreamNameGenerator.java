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

package org.jeecqrs.common.persistence.es;

import org.jeecqrs.common.Identifiable;

/**
 * Provides the class's canonical name as a name for the event stream.
 * 
 * @param <T>  the base type of objects that can be identified with this streamNameGenreator
 * @param <ID>  the type used to identify individual streams
 */
public class CanonicalNameEventStreamNameGenerator<T extends Identifiable<ID>, ID>
        implements EventStreamNameGenerator<T, ID> {

    @Override
    public String streamNameFor(T obj) {
        return streamNameFor((Class) obj.getClass(), obj.id());
    }
 
    @Override
    public String streamNameFor(Class<? extends T> clazz, ID id) {
        return new StringBuilder()
		.append(clazz.getCanonicalName())
		.append(":")
		.append(id.toString())
		.toString();
    }

}
