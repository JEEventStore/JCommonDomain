package org.jeecqrs.common.domain.model;

import java.util.List;
import org.jeecqrs.common.AbstractId;
import org.jeecqrs.common.event.sourcing.EventSourcingUtil;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class AbstractEventSourcedAggregateRootTest
        extends AbstractEventSourcedAggregateRootBaseTest<AbstractEventSourcedAggregateRootTest.AESARImpl> {

    @Override
    protected AESARImpl fresh_instance() {
        AESARImpl instance = new AESARImpl(new AbstractIdImpl("TEST_ID"), 8);
        instance.apply(new IncreasedCounter(5));
        instance.apply(new IncreasedCounter(27));
        return instance;
    }

    @Override
    protected Class<AESARImpl> arClass() {
        return AESARImpl.class;
    }

    @Override
    protected void assertExpectedForNewInstance(AESARImpl obj) {
        assertEquals(obj.id.toString(), "TEST_ID");
        assertEquals(obj.counter, 40);
    }

    @Test
    public void test_apply_increasecounter() {
        AESARImpl fresh = fresh_instance();
        assertEquals(changesIn(fresh).size(), 3, "number of events in fresh instance");
        AESARImpl instance = replayed_instance(fresh);
        assertTrue(changesIn(instance).isEmpty(), "loaded instance must have no changes");

        int counter = instance.counter;
        for (int i = 4; i < 10; i++) {
            IncreasedCounter e = new IncreasedCounter(i);
            instance.apply(e);
            counter += i;
            assertEquals(instance.counter, counter);
        }
        assertEquals(changesIn(instance).size(), 6);
    }

    protected List<DomainEvent> changesIn(AESARImpl instance) {
        return (List) EventSourcingUtil.retrieveChanges(instance);
    }

    public static class AESARImpl extends AbstractEventSourcedAggregateRoot {
        AbstractIdImpl id;
        int counter;

        protected AESARImpl(AbstractIdImpl id, int counter) {
            DomainEvent ev = new InstanceCreated(id, counter);
            apply(ev);
        }

	// required by ORM framework
	private AESARImpl() { }

        @Override
        public AbstractIdImpl id() {
            return this.id;
        }

        protected void when(InstanceCreated e) {
            this.id = e.id;
            this.counter = e.initialCounter;
        }

        protected void when(IncreasedCounter e) {
            this.counter += e.step;
        }
    }

    public static class AbstractIdImpl extends AbstractId<AbstractIdImpl> {

        public AbstractIdImpl(String idString) {
            super(idString);
        }
    }

    public static class InstanceCreated extends AbstractDomainEvent<InstanceCreated> {
        AbstractIdImpl id;
        int initialCounter;
        public InstanceCreated(AbstractIdImpl id, int initialCounter) {
            this.id = id;
            this.initialCounter = initialCounter;
        }
    }

    public class IncreasedCounter extends AbstractDomainEvent<IncreasedCounter> {
        int step;
        public IncreasedCounter(int step) {
            this.step = step;
        }
    }

}