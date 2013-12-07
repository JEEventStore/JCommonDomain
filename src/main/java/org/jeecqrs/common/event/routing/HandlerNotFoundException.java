package org.jeecqrs.common.event.routing;

import org.jeecqrs.common.event.Event;

/**
 * Indicates that a suitable handler for an event could not be found.
 */
public class HandlerNotFoundException extends RuntimeException {

    private final Class<? extends Event> type;

    public HandlerNotFoundException(Class<? extends Event> type) {
        super("Handler not found for event of type " + type.getCanonicalName());
        this.type = type;
    }

    public Class<? extends Event> getType() {
        return type;
    }

}
