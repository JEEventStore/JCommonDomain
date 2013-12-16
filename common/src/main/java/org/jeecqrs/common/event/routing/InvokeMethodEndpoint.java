package org.jeecqrs.common.event.routing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jeecqrs.common.event.Event;

/**
 *
 */
public class InvokeMethodEndpoint<R, E extends Event> implements EventRouteEndpoint<R, E>{

    private final Object object;
    private final Method method;

    public InvokeMethodEndpoint(Object object, Method method) {
        this.object = object;
        this.method = method;
    }

    @Override
    public R consumeEvent(E event) {
        try {
            return (R) method.invoke(object, event);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException("Invocation of endpoint method "
                    + method + " failed: " + e.getMessage(), e);
        }
    }
    
}
