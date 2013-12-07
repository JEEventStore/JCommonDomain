package org.jeecqrs.common.util;

/**
 *
 */
public class Base {

    @Annotation1
    private void method1(String foo, Integer bar) {
        System.out.println("method1 in Base");
    }

    @Annotation2
    public void method2(Long baz) {
        System.out.println("method2 in Base");
    }
    
}
