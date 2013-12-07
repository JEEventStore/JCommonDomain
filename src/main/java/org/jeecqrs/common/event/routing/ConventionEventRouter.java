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

package org.jeecqrs.common.event.routing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.jeecqrs.common.event.Event;
import org.jeecqrs.common.util.Validate;

/**
  * This router uses reflection to find event handlers for events based
  * on a convention regarding the event handler method name and arguments.
  * The default name for the method to be called as event handler is
  * {@link when}, but the name can be overwritten in the constructor.
  * For performance reasons, the reflection method lookup is cached
  * in a static cache field, such that later calls do not need to use parse
  * the object again.
  * 
  * @param <E> the base event type
 */
public class ConventionEventRouter<E extends Event> implements EventRouter<E> {

    private final static Logger log = Logger.getLogger(ConventionEventRouter.class.getCanonicalName());

    private final static String DEFAULT_METHOD_NAME = "when";

    private final Map<Class<? extends E>, EventRouteEventHandler<E>> handlers = new HashMap<>();
    private static final Map<String, Method> methodCache = new HashMap<>();

    private Object object;
    private final boolean throwOnHandlerNotFound;
    private final String methodName;

    public ConventionEventRouter() {
        this(null);
    }

    public ConventionEventRouter(Object object) {
        this(object, true);
    }

    public ConventionEventRouter(Object object, boolean throwOnHandlerNotFound) {
        this(object, true, DEFAULT_METHOD_NAME);
    }

    public ConventionEventRouter(Object object, boolean throwOnHandlerNotFound, String methodName) {
        this.throwOnHandlerNotFound = throwOnHandlerNotFound;
        this.methodName = methodName;
        register(object);
    }

    @Override
    public void dispatch(E event) {
        Validate.notNull(event, "event must not be null");
        Class<E> type = (Class<E>) event.getClass();
        EventRouteEventHandler<E> h = findHandlerFor(type);
        if (h != null) {
            h.handle(event);
        } else if (throwOnHandlerNotFound)
            throw new HandlerNotFoundException(type);
    }

    @Override
    public final void register(Object obj) {
        this.object = obj;
    }

    @Override
    public void register(Class<? extends E> eventType, EventRouteEventHandler<E> handler) {
        Validate.notNull(eventType, "eventType must not be null");
        Validate.notNull(handler, "route endpoint must not be null");
        EventRouteEventHandler<E> h = lookup(eventType);
        if (h != null)
            throw new IllegalStateException("Handler already registered for type " + eventType.getCanonicalName());
        log.fine("Registering event handler for event " + eventType + ": " + handler);
        handlers.put(eventType, handler);
    }

    private EventRouteEventHandler<E> findHandlerFor(Class<? extends E> eventType) {
        EventRouteEventHandler<E> h = lookup(eventType);
        if (h != null)
            return h;
        if (object == null) // no object registered
            return null;
        Class<?> objType = object.getClass();
        String key = String.format("%s#%s(%s)", objType.getCanonicalName(),
                this.methodName, eventType.getCanonicalName());
        final Method handlerMethod = cacheLookup(key, eventType);
        if (handlerMethod == null)
            return null;
        h = new EventRouteEventHandler<E>() {
            @Override
            public void handle(E event) {
                try {
                    handlerMethod.invoke(object, event);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new RuntimeException("Invocation of event handler method "
                            + handlerMethod + " failed: " + e.getMessage(), e);
                }
            }
        };
        this.register(eventType, h);
        return h;
    }

    private EventRouteEventHandler<E> lookup(Class<? extends E> eventType) {
        return handlers.get(eventType);
    }

    /**
     * Use reflection to find the event handler method and add the result to
     * the global method cache.
     */
    private Method cacheLookup(String key, Class<? extends E> eventType) {
        synchronized (methodCache) {
            Method method = methodCache.get(key);
            if (method == null) {
                method = traverseClassHierarchy(object.getClass(), eventType);
                if (method != null) {
                    method.setAccessible(true);
                    methodCache.put(key, method);
                }
            }
            return method;
        }
    }

    private Method traverseClassHierarchy(Class<?> objType, Class<? extends E> eventType) {
        try {
            Method m = objType.getDeclaredMethod(methodName, eventType);
            int mods = m.getModifiers();
            /* Do not use default (package only) or private methods.  If this
             * method is neither public nor protected, no super class can have 
             * a method with the same name but with higher visibility.
             */
            if (!Modifier.isPublic(mods) && !Modifier.isProtected(mods))
                return null;
            return m;
        } catch (NoSuchMethodException | SecurityException e) {
            if (objType.getSuperclass() != null)
                return traverseClassHierarchy(objType.getSuperclass(), eventType);
        }
        return null;
    }
    
}
