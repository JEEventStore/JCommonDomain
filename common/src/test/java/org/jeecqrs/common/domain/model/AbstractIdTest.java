package org.jeecqrs.common.domain.model;

import org.jeecqrs.common.AbstractId;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class AbstractIdTest {

    private AbstractIdImpl instance(String id) {
        return new AbstractIdImpl(id);
    }
    
    @Test
    public void testtoString() {
        System.out.println("toString");
        AbstractId instance = instance("TEST_ID");
        String expResult = "TEST_ID";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    @Test
    public void testSameValueAs() {
        System.out.println("sameValueAs");
        AbstractIdImpl instance = instance("TEST_ID");
        AbstractIdImpl eother = instance("TEST_ID");
        AbstractIdImpl neother = instance("OTHER_TEST_ID");
        assertTrue(instance.sameValueAs(eother));
        assertFalse(instance.sameValueAs(neother));
        assertFalse(instance.sameValueAs(null));
    }

    @Test
    public void testEquals() {
        System.out.println("sameValueAs");
        AbstractIdImpl instance = instance("TEST_ID");
        AbstractIdImpl eother = instance("TEST_ID");
        AbstractIdImpl neother = instance("OTHER_TEST_ID");
        assertTrue(instance.equals(eother));
        assertFalse(instance.equals(neother));
        assertFalse(instance.equals(null));
    }

    public class AbstractIdImpl extends AbstractId {
        public AbstractIdImpl(String id) {
            super(id);
        }
    }
}