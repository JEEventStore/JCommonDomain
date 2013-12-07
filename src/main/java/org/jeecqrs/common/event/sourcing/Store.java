package org.jeecqrs.common.event.sourcing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides the ability to store changes of an object as a list of events.
 * Needs to be a method with arguments {@code EventSourcingBus}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Store {
    
}
