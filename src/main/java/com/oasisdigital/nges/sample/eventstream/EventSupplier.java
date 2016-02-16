package com.oasisdigital.nges.sample.eventstream;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;

import com.oasisdigital.nges.event.Event;
import com.oasisdigital.nges.event.EventStore;

/**
 * Encapsulates a query on the event store, possibly with some filtering. Keeps track of the last event it has
 * seen. Generates event IDs for use over SSE, and is able to resume querying from such an ID provided by the
 * browser when it reconnects.
 *
 */
abstract public class EventSupplier {
    protected final int MAX_EVENTS_PER_BATCH = 100;

    protected final EventStore eventStore;
    private Optional<String> lastEventId;

    public EventSupplier(EventStore eventStore, Optional<String> lastEventId) {
        this.eventStore = eventStore;
        this.lastEventId = lastEventId;
    }

    public EventSupplier(EventStore eventStore) {
        this(eventStore, Optional.empty());
    }

    final public List<Event> getNewEvents() {
        List<Event> events = queryEvents();
        List<Event> filtered = events.stream().filter(this::matchesEvent).collect(toList());
        rememberLastEvent(events);
        return filtered;
    }

    abstract protected List<Event> queryEvents();

    protected boolean matchesEvent(Event event) {
        return true;
    }

    abstract public String getEventId(Event event);

    final public Optional<String> getLastEventId() {
        return lastEventId;
    }

    protected void rememberLastEvent(List<Event> events) {
        if (!events.isEmpty()) {
            this.lastEventId = Optional.of(getEventId(events.get(events.size() - 1)));
        }
    }
}
