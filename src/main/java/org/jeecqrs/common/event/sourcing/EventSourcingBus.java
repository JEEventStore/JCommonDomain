package org.jeecqrs.common.event.sourcing;

/**
 *
 * @param <E>  the base event type
 */
public interface EventSourcingBus<E> {

    void persist(E event);
    
}
