/*
 * Copyright (c) 2013 Red Rainbow IT Solutions GmbH, Germany
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.jeecqrs.common.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Provides utility methods for reflection.
 */
public class ReflectionUtils {
    
    /**
     * Finds the unique method with the given annotation on the given class or
     * any of its super classes.
     * Also sets the method accessible.
     * Throws a runtime exception if no method with this annotation can be
     * found or if the annotated method has a different signature.
     * 
     * @param clazz  the class to search
     * @param annotation  the annotation to find
     * @param params  the required method parameters
     * @return   the method
     */
    public static Method findUniqueMethod(
            Class<?> clazz,
            Class<? extends Annotation> annotation,
            Object[] params) {
        Method m = findAnnotatedMethodOrFail(clazz, annotation);
        if (!Arrays.deepEquals(params, m.getParameterTypes())) {
            String msg = String.format(
                    "Method annotated with @%s has wrong signature, required %s, but is %s",
                    annotation.getCanonicalName(), Arrays.asList(params), m);
            throw new IllegalArgumentException(msg);
        }
        m.setAccessible(true);
        return m;
    }

    /**
     * Finds the unique method with the given annotation or throws
     * a {@link RuntimeException} if no such method can be found on the
     * class or any of its super classes.
     * 
     * @param clazz  the class to search
     * @param annotation  the annotation to find
     * @return   the method
     */
    public static Method findAnnotatedMethodOrFail(Class<?> clazz, Class<? extends Annotation> annotation) {
        Method m = findAnnotatedMethod(clazz, annotation);
        if (m == null) {
            String msg = String.format(
                    "Neither class %s nor any of its super classes provide a method annotated with @%s",
                    clazz.getCanonicalName(), annotation.getCanonicalName());
            throw new IllegalStateException(msg);
        }
        return m;
    }

    /**
     * Finds the first method with the given annotation on the given
     * class or any of its super classes.
     * 
     * @param clazz  the class to search
     * @param annotation  the annotation to find
     * @return   the method or null if no such method exists
     */
    public static Method findAnnotatedMethod(Class<?> clazz, Class<? extends Annotation> annotation) {
        Class<?> current = clazz;
        while (current != Object.class) {
            Method[] methods = current.getDeclaredMethods();
            for (Method m : methods)
                if (m.isAnnotationPresent(annotation))
                    return m;
            current = current.getSuperclass();
        }
        return null;
    }

}
