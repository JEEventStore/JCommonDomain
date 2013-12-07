package org.jeecqrs.common.event.sourcing;

import java.util.ArrayList;
import java.util.List;
import org.jeecqrs.common.event.Event;

/**
 *
 */
public class TestObject {

    public List<Event> stream = new ArrayList<>();
    public long version = 0l;
    private List<Event> changes = new ArrayList<>();

    public void add(Event event) {
        this.changes.add(event);
    }

    @Load
    private void load(long version, List<Event> events) {
        this.stream = new ArrayList<>(events);
        this.version = version;
    }

    @Store
    private void store(EventSourcingBus<Event> bus) {
        bus.store(version, changes);
        changes.clear();
        version++;
    }
    
}
