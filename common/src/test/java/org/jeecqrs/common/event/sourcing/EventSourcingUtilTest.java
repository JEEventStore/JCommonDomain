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

package org.jeecqrs.common.event.sourcing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.jeecqrs.common.event.Event;
import org.jeecqrs.common.util.ReflectionUtils;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 */
public class EventSourcingUtilTest {
    
    @Test
    public void testCreateFromEventStream() throws Exception {
        List<Event> orig = new ArrayList<>();
        orig.add(new TestEvent());
        orig.add(new TestEvent());
        orig.add(new TestEvent());

        TestObject obj = EventSourcingUtil.createFromEventStream(TestObject.class, 2, orig);
        assertNotNull(obj);
        assertEquals(obj.version, 2l);
        assertEquals(obj.stream, orig);

        obj.add(new TestEvent());
        obj.add(new TestEvent());

        Method m = ReflectionUtils.findUniqueMethod(TestObject.class,
                Store.class, new Object[]{EventSourcingBus.class});

        final List<Event> stored = new ArrayList<>();
        assertEquals(stored.size(), 0);
        EventSourcingBus mybus = new EventSourcingBus() {
            @Override
            public void store(long originalVersion, List changes) {
                stored.addAll(changes);
                assertEquals(originalVersion, 2);
            }
        };
        m.invoke(obj, new Object[]{ mybus });
        assertEquals(stored.size(), 2);
    }

}
