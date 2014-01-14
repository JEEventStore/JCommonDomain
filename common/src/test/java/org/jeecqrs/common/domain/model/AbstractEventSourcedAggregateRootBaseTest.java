package org.jeecqrs.common.domain.model;

import java.util.List;
import org.jeecqrs.common.event.sourcing.EventSourcingUtil;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public abstract class AbstractEventSourcedAggregateRootBaseTest<T extends AbstractEventSourcedAggregateRoot> {

    protected abstract T fresh_instance();
    protected abstract Class<T> arClass();

    protected abstract void assertExpectedForNewInstance(T obj);

    protected T replayed_instance(T template) {
        List<DomainEvent> list = (List) EventSourcingUtil.retrieveChanges(template);
        long version = EventSourcingUtil.retrieveVersion(template);
        return EventSourcingUtil.createFromEventStream(arClass(), version, list);
    }

    @Test
    public void test_creation() {
        System.out.println("test_creation");
        T instance = fresh_instance();
        long version = EventSourcingUtil.retrieveVersion(instance);
        List<DomainEvent> changes = EventSourcingUtil.retrieveChanges(instance);
        assertEquals(version, 0);
        assertTrue(!changes.isEmpty());
        assertExpectedForNewInstance(instance);
    }

    @Test
    public void test_creation_with_replay() {
        System.out.println("test_creation_with_replay");
        T template = fresh_instance();
        T i = replayed_instance(template);
        List<DomainEvent> changes1 = EventSourcingUtil.retrieveChanges(i);
        List<DomainEvent> changes2 = EventSourcingUtil.retrieveChanges(template);
        assertEquals(changes1, changes2);
        assertExpectedForNewInstance(i);
    }

    @Test
    public void test_unknown_event_throws_exception() {
        T instance = fresh_instance();
        try {
            instance.apply(new NotHandledEvent());
            fail("Should have thrown by now");
        } catch (RuntimeException e) {
            // ok
        }
    }

    public class NotHandledEvent extends AbstractDomainEvent<NotHandledEvent> {
    }

}