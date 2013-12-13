package org.jeecqrs.common.commands;

/**
 * Provides the ability to send commands that are dispatched to corresponding
 * handlers.
 */
public interface CommandBus {

    void send(Command command);
    
}
