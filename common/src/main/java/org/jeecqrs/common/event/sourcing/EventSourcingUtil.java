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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jeecqrs.common.domain.model.DomainEvent;
import org.jeecqrs.common.event.Event;
import org.jeecqrs.common.util.ReflectionUtils;
import org.jeecqrs.common.util.Validate;

/**
 * Utilities to apply event sourcing.
 */
public class EventSourcingUtil {

    private final static Map<String, Method> methodCache = new HashMap<>();

    public static <T> T createByDefaultConstructor(Class<T> clazz) {
        Validate.notNull(clazz, "class must not be null");
        try {
            Constructor<T> constr = clazz.getDeclaredConstructor(new Class<?>[]{});
            constr.setAccessible(true);
            return (T) constr.newInstance(new Object[]{});
        } catch (InstantiationException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException e) {
            String msg = String.format("Cannot create object of type %s: %s",
                    clazz, e.getMessage());
            throw new RuntimeException(msg, e);
        } catch (NoSuchMethodException e) {
            String msg = String.format("Class does not provide default constructor: %s: %s",
                    clazz, e.getMessage());
            throw new RuntimeException(msg, e);
        }
    }

    public static <T, E extends Event> void loadEventStreamIntoObject(T obj, long version, List<E> events) {
        Validate.notNull(obj, "obj must not be null");
        Validate.notNull(events, "event must not be null");
        long curVersion = retrieveVersion(obj);
        if (curVersion > 0)
            throw new IllegalArgumentException("object is not fresh, has version " + curVersion);
        try {
            Method load = ReflectionUtils.findUniqueMethod(obj.getClass(), Load.class,
                    new Object[]{long.class, List.class});
            load.invoke(obj, new Object[]{version, events});
        } catch (IllegalAccessException | InvocationTargetException ex) {
            String msg = String.format("Cannot load event stream for object of type %s: %s",
                    obj.getClass(), ex.getMessage());
            throw new RuntimeException(msg, ex);
        }
    }

    public static <T> long retrieveVersion(T obj) {
        Class<T> clazz = (Class<T>) obj.getClass();
        Validate.notNull(obj, "obj must not be null");
        try {
            Method version = ReflectionUtils.findUniqueMethod(clazz, Version.class, new Object[]{});
            Object res = version.invoke(obj, new Object[]{});
            if (!Long.class.equals(res.getClass()) && !long.class.equals(res.getClass())) {
                throw new IllegalStateException("@Version method does not return long: " + clazz);
            }
            return (long) res;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            String msg = String.format("Cannot obtain version of class of type %s: %s", clazz, e);
            throw new RuntimeException(msg, e);
        }
    }

    public static <T, E extends Event> void transferChanges(T obj, final List<E> changes) {
        EventSourcingBus<E> bus = new EventSourcingBus<E>() {
            @Override
            public void store(E event) {
                changes.add(event);
            }
            @Override
            public void commit(String commitId) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        invokeStoreMethod(obj, bus);;
    }

    public static <T> void invokeStoreMethod(T obj, EventSourcingBus<? extends Event> bus) {
        try {
            Method store = ReflectionUtils.findUniqueMethod(obj.getClass(),
                    Store.class, new Object[]{EventSourcingBus.class});
            store.invoke(obj, new Object[]{bus});
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            String msg = String.format("Cannot find store method for type %s: %s",
                    obj.getClass(), e.getMessage());
            throw new RuntimeException(msg, e);
        }
    }

}
