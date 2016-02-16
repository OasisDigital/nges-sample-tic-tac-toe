package com.oasisdigital.nges.sample.eventstream;

import java.util.List;
import java.util.Optional;

import com.oasisdigital.nges.event.Event;
import com.oasisdigital.nges.event.EventStore;

/**
 * {@link EventSupplier} including all events for all streams in the event store.
 *
 */
public class AllStreamsEventSupplier extends EventSupplier {

    public AllStreamsEventSupplier(EventStore eventStore, Optional<String> lastEventId) {
        super(eventStore, lastEventId);
    }

    public AllStreamsEventSupplier(EventStore eventStore) {
        super(eventStore);
    }

    @Override
    protected List<Event> queryEvents() {
        long lastEventId = getLastEventId().map(Long::parseLong).orElse(0L);
        return eventStore.getEventsForAllStreams(lastEventId, MAX_EVENTS_PER_BATCH);
    }

    @Override
    public String getEventId(Event event) {
        return String.valueOf(event.getEventId());
    }
}
