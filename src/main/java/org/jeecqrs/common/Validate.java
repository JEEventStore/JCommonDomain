package org.jeecqrs.common;

/**
 *
 */
public class Validate {

    public static void notNull(Object o, String msg) {
        if (o == null)
            throw new NullPointerException(msg);
    }
    
}
