package org.jeecqrs.common.util;

import java.util.List;

/**
 *
 */
public class Derived extends Base {

    @Annotation2
    protected void hello(String foobar) {
        System.out.println("method hello in Derived");
    }

    @Annotation3
    private void narf(String foo, Long bar, List baz) {
        System.out.println("method narf in Derived");
    }
    
}
