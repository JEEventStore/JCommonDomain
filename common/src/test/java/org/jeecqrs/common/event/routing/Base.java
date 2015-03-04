package org.jeecqrs.common.event.routing;

/**
 *
 */
public class Base {

    private int calledEv1 = 0;
    private int calledEv2 = 0;

    protected void when(TestEvent1 ev) {
        System.out.println("Base.1 in " + this);
        calledEv1++;
    }

    protected void when(TestEvent2 ev) {
        System.out.println("Base.2 in " + this);
        calledEv2++;
    }

    public int getCalledEv1() {
        return calledEv1;
    }

    public int getCalledEv2() {
        return calledEv2;
    }
    
}
