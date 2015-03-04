package org.jeecqrs.common.event.routing;

/**
 *
 */
public class Derived extends Base {

    protected void when(TestEvent2 ev) {
        System.out.println("Foo.2 in " + this);
    }

}