package org.jeecqrs.common.domain.model;

import org.jeecqrs.common.AbstractId;
import org.jeecqrs.common.Identity;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class AbstractEntityTest {
    
    public AbstractEntityImpl instance(String id) {
        return new AbstractEntityImpl(id);
    }

    @Test
    public void test_that_idString_is_expected() {
        System.out.println("id idString");
        AbstractEntity instance = instance("TEST_ID");
        String expResult = "TEST_ID";
        assertEquals(instance.id().toString(), expResult);
    }

    @Test
    public void test_that_ids_are_equal() {
        System.out.println("id equals");
        AbstractEntity instance = instance("TEST_ID");
        AbstractEntity other = instance("TEST_ID");
        assertEquals(instance.id(), other.id());
    }

    @Test
    public void testSameIdentityAs() {
        System.out.println("sameIdentityAs");
        AbstractEntityImpl instance = instance("TEST_ID");
        AbstractEntityImpl eother = instance("TEST_ID");
        AbstractEntityImpl neother = instance("ANOTHER_TEST_ID");

        assertTrue(instance.sameIdentityAs(eother));
        assertFalse(instance.sameIdentityAs(neother));
        assertFalse(instance.sameIdentityAs(null));
    }

    @Test
    public void testEquals() {
        System.out.println("equals");
        AbstractEntityImpl instance = instance("TEST_ID");
        AbstractEntityImpl eother = instance("TEST_ID");
        AbstractEntityImpl neother = instance("ANOTHER_TEST_ID");

        assertTrue(instance.equals(eother));
        assertFalse(instance.equals(neother));
        assertFalse(instance.equals(null));
    }

    public class AbstractEntityImpl extends AbstractEntity {
        public class AbstractIdImpl extends AbstractId<AbstractIdImpl> {
            public AbstractIdImpl(String idString) {
                super(idString);
            }
        }
        private AbstractIdImpl id;

        public AbstractEntityImpl(String id) {
            this.id = new AbstractIdImpl(id);
        }

        @Override
        public Identity id() {
            return this.id;
        }
    }
}