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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jeecqrs.common.Identity;

/**
 * Provides the event stream name using an annotation.
 */
public class AnnotationEventStreamNameGenerator implements EventStreamNameGenerator {
 
    private static final Map<Class, String> streamNameCache = new ConcurrentHashMap<>();
    
    @Override
    public String streamNameFor(Class<?> clazz, Identity id) {
        // no need to synchronize cache access, since result is deterministic
	String name = streamNameCache.get(clazz);
	if (name == null) {
	    EventStreamName annotation = clazz.getAnnotation(EventStreamName.class);
	    if (annotation == null)
		throw new IllegalArgumentException(
			"Class " + clazz.getCanonicalName() + " misses required " +
			EventStreamName.class.getCanonicalName() + " annotation");
            name = annotation.value();
	    if (name == null || name.trim().equals(""))
		throw new IllegalArgumentException(
			"Class " + clazz.getCanonicalName() + " has invalid " +
			EventStreamName.class.getCanonicalName() + " annotation: " + annotation.value());
	    name = name.trim();
	    streamNameCache.put(clazz, name);
	}
	return name;
    }

}
