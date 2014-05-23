package org.jeecqrs.common.domain.model;

import java.util.ArrayList;
import java.util.List;
import org.jeecqrs.common.event.sourcing.EventSourcingUtil;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public abstract class AbstractEventSourcedAggregateRootBaseTest<T extends AbstractEventSourcedAggregateRoot> {

    protected abstract T fresh_instance();
    protected abstract Class<T> arClass();

    protected abstract void assertExpectedForNewInstance(T obj);

    protected T replayed_instance(T template) {
        List<DomainEvent> list = new ArrayList<>();
        EventSourcingUtil.transferChanges(template, list);
        long version = EventSourcingUtil.retrieveVersion(template);
        T instance = EventSourcingUtil.createByDefaultConstructor(arClass());
        EventSourcingUtil.loadEventStreamIntoObject(instance, version, list);
        return instance;
    }

    @Test
    public void test_creation() {
        System.out.println("test_creation");
        T instance = fresh_instance();
        long version = EventSourcingUtil.retrieveVersion(instance);
        List<DomainEvent> changes = new ArrayList<>();
        EventSourcingUtil.transferChanges(instance, changes);
        assertEquals(version, 0);
        assertTrue(!changes.isEmpty());
        assertExpectedForNewInstance(instance);
    }

    @Test
    public void test_creation_with_replay() {
        System.out.println("test_creation_with_replay");
        T template = fresh_instance();
        T i = replayed_instance(template);
        List<DomainEvent> changes1 = new ArrayList<>();
        EventSourcingUtil.transferChanges(i, changes1);
        List<DomainEvent> changes2 = new ArrayList<>();
        EventSourcingUtil.transferChanges(template, changes2);
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