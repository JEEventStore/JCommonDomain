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

package org.jeecqrs.common.event.sourcing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jeecqrs.common.event.Event;
import org.jeecqrs.common.util.ReflectionUtils;
import org.jeecqrs.common.util.Validate;

/**
 * Utilities to apply event sourcing.
 */
public class EventSourcingUtil {
    
    private final static Map<String, Method> methodCache = new HashMap<>();

    public static <T, E extends Event> T createFromEventStream(
            Class<T> clazz, long version, List<E> events) {
        Validate.notNull(clazz, "class must not be null");
        Validate.notNull(events, "event must not be null");
	try {
	    T instance = clazz.newInstance();
            Method load = ReflectionUtils.findUniqueMethod(clazz, Load.class,
                    new Object[]{long.class, List.class});
            load.invoke(instance, new Object[] { version, events });
	    return instance;
	} catch (InstantiationException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException e) {
            String msg = String.format("Cannot create object of type %s: %s",
                    clazz, e.getMessage());
	    throw new RuntimeException(msg, e);
	}
    }

}
