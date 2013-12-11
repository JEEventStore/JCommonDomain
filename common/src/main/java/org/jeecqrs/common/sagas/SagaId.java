package org.jeecqrs.common.sagas;

import org.jeecqrs.common.AbstractId;

/**
 * Default id implementation for sagas.
 */
public class SagaId extends AbstractId<SagaId> {

    public SagaId() { }

    public SagaId(String sagaId) {
        super(sagaId);
    }

    public static SagaId fromString(String sagaId) {
        return new SagaId(sagaId);
    }
    
}
