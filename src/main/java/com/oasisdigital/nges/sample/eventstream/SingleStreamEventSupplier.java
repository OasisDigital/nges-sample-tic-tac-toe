package com.oasisdigital.nges.sample.eventstream;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.oasisdigital.nges.event.Event;
import com.oasisdigital.nges.event.EventStore;

/**
 * {@link EventSupplier} including all events for a particular stream (i.e. using {@link Event} stream ID).
 *
 */
public class SingleStreamEventSupplier extends EventSupplier {

    private final UUID streamId;

    public SingleStreamEventSupplier(EventStore eventStore, UUID streamId, Optional<String> lastEventId) {
        super(eventStore, lastEventId);
        this.streamId = streamId;
    }

    public SingleStreamEventSupplier(EventStore eventStore, UUID streamId) {
        super(eventStore);
        this.streamId = streamId;
    }

    @Override
    protected List<Event> queryEvents() {
        long lastEventSeq = getLastEventId().map(Long::parseLong).orElse(0L);
        return eventStore.getEventsForStream(streamId, lastEventSeq, MAX_EVENTS_PER_BATCH);
    }

    @Override
    public String getEventId(Event event) {
        return String.valueOf(event.getSequence());
    }
}
