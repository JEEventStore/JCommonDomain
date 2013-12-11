package org.jeecqrs.common.sagas;

import org.jeecqrs.common.AbstractId;

/**
 * Default id implementation for sagas.
 */
public class DefaultSagaId extends AbstractId<DefaultSagaId> {

    public DefaultSagaId() { }

    public DefaultSagaId(String sagaId) {
        super(sagaId);
    }

    public static DefaultSagaId fromString(String sagaId) {
        return new DefaultSagaId(sagaId);
    }
    
}
