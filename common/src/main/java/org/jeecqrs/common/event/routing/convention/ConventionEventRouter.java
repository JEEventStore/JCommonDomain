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

package org.jeecqrs.common.event.routing.convention;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.jeecqrs.common.event.Event;
import org.jeecqrs.common.event.routing.EndpointNotFoundException;
import org.jeecqrs.common.event.routing.EventRouteEndpoint;
import org.jeecqrs.common.event.routing.EventRouter;
import org.jeecqrs.common.event.routing.InvokeMethodEndpoint;
import org.jeecqrs.common.util.Validate;

/**
  * This router uses reflection to find event routing endpoints for events based
  * on a convention regarding the endpoint method name and arguments.
  * The default name for the method to be called as endpoint is
  * "{@code when}", but the name can be overwritten in the constructor.
  * For performance reasons, the reflection method lookup is cached
  * in a static cache field, such that later calls do not need to use parse
  * the object again.
  * 
  * @param <R> the return type
  * @param <E> the base event type
 */
public class ConventionEventRouter<R, E extends Event> implements EventRouter<R, E> {

    private final static Logger log = Logger.getLogger(ConventionEventRouter.class.getCanonicalName());

    private final static String DEFAULT_METHOD_NAME = "when";

    private final Map<Class<? extends E>, EventRouteEndpoint<R, E>> endpoints = new HashMap<>();
    private static final Map<String, Method> methodCache = new HashMap<>();

    private Object object;
    private final boolean throwOnEndpointNotFound;
    private final String methodName;

    public ConventionEventRouter() {
        this(true);
    }

    public ConventionEventRouter(boolean throwOnEndpointNotFound) {
        this(throwOnEndpointNotFound, DEFAULT_METHOD_NAME);
    }

    public ConventionEventRouter(boolean throwOnEndpointNotFound, String methodName) {
        this.throwOnEndpointNotFound = throwOnEndpointNotFound;
        this.methodName = methodName;
    }

    @Override
    public R routeEvent(E event) {
        Validate.notNull(event, "event must not be null");
        Class<E> type = (Class<E>) event.getClass();
        EventRouteEndpoint<R, E> h = findEndpoints(type);
        if (h != null) {
            return h.consumeEvent(event);
        } else if (throwOnEndpointNotFound)
            throw new EndpointNotFoundException(type);
        return null;
    }

    @Override
    public void register(Object obj) {
        this.object = obj;
    }

    @Override
    public void register(Class<? extends E> eventType, EventRouteEndpoint<R, E> endpoint) {
        Validate.notNull(eventType, "eventType must not be null");
        Validate.notNull(endpoint, "route endpoint must not be null");
        EventRouteEndpoint<R, E> h = lookup(eventType);
        if (h != null)
            throw new IllegalStateException("endpoint already registered for type " + eventType.getCanonicalName());
        log.fine("Registering endpoint for event " + eventType + ": " + endpoint);
        endpoints.put(eventType, endpoint);
    }

    private EventRouteEndpoint<R, E> findEndpoints(Class<? extends E> eventType) {
        EventRouteEndpoint<R, E> h = lookup(eventType);
        if (h != null)
            return h;
        if (object == null) // no object registered
            return null;
        Class<?> objType = object.getClass();
        String key = String.format("%s#%s(%s)", objType.getCanonicalName(),
                this.methodName, eventType.getCanonicalName());
        Method method = cacheLookup(key, eventType);
        if (method == null)
            return null;
        h = new InvokeMethodEndpoint<>(object, method);
        this.register(eventType, h);
        return h;
    }

    private EventRouteEndpoint<R, E> lookup(Class<? extends E> eventType) {
        return endpoints.get(eventType);
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
