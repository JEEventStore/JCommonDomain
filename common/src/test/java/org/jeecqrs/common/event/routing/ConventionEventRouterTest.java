package org.jeecqrs.common.event.routing;

import org.jeecqrs.common.event.routing.convention.ConventionEventRouter;
import org.jeecqrs.common.event.Event;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 */
public class ConventionEventRouterTest {
    
    @Test
    public void testOnSimpleClass() throws Exception {
        Base foo = new Base();
        EventRouter<Void, Event> router = new ConventionEventRouter<>();
        router.register(foo);
        router.routeEvent(new TestEvent1());
        router.routeEvent(new TestEvent2());
        router.routeEvent(new TestEvent2());
        assertEquals(foo.getCalledEv1(), 1);
        assertEquals(foo.getCalledEv2(), 2);
    }

    @Test
    public void testOnDerived() throws Exception {
        Base bar = new Derived();
        EventRouter<Void, Event> router = new ConventionEventRouter<>();
        router.register(bar);
        router.routeEvent(new TestEvent1());
        router.routeEvent(new TestEvent2());
        router.routeEvent(new TestEvent2());
        assertEquals(bar.getCalledEv1(), 1);
        assertEquals(bar.getCalledEv2(), 0);
    }
    
    
}
