package com.oasisdigital.nges.sample.tictactoe;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.oasisdigital.nges.cluster.MessageGroup;
import com.oasisdigital.nges.event.Event;
import com.oasisdigital.nges.event.EventStore;
import com.oasisdigital.nges.sample.eventstream.AllStreamsEventSupplier;
import com.oasisdigital.nges.sample.eventstream.EventSupplier;
import com.oasisdigital.nges.sample.eventstream.SingleStreamEventSupplier;
import com.oasisdigital.nges.sample.eventstream.SseEventStream;
import com.oasisdigital.nges.sample.tictactoe.domain.GameCreated;
import com.oasisdigital.nges.sample.tictactoe.domain.GameFinished;

/**
 * Creates SSE streams backed by the event store, rewriting events from the store to {@link SseEmitter}.
 * Maintains thread pools.
 *
 */
@Component
public class EventStreams {
    private final EventStore eventStore;
    private final MessageGroup messageGroup;

    private ExecutorService gameListPool;
    private ExecutorService singleGamePool;

    @Autowired
    public EventStreams(EventStore eventStore, MessageGroup messageGroup) {
        super();
        this.eventStore = eventStore;
        this.messageGroup = messageGroup;
    }

    @PostConstruct
    public void init() {
        gameListPool = Executors.newCachedThreadPool(
                new ThreadFactoryBuilder().setNameFormat("GameList-%d").setDaemon(true).build());
        singleGamePool = Executors.newCachedThreadPool(
                new ThreadFactoryBuilder().setNameFormat("SingleGame-%d").setDaemon(true).build());
    }

    public SseEventStream makeSingleGameStream(UUID gameId, Optional<String> lastEventId) {
        EventSupplier supplier = new SingleStreamEventSupplier(eventStore, gameId, lastEventId);
        SseEventStream stream = new SseEventStream(messageGroup, supplier);
        singleGamePool.submit(stream);
        return stream;
    }

    public SseEventStream makeGameListStream(Optional<String> lastEventId) {
        EventSupplier supplier = new AllStreamsEventSupplier(eventStore, lastEventId) {
            final Set<String> EVENT_TYPES = ImmutableSet.of(GameCreated.class.getSimpleName(),
                    GameFinished.class.getSimpleName());

            @Override
            protected boolean matchesEvent(Event event) {
                return EVENT_TYPES.contains(event.getType());
            }
        };
        SseEventStream stream = new SseEventStream(messageGroup, supplier);
        gameListPool.submit(stream);
        return stream;
    }

    @PreDestroy
    public void destroy() {
        gameListPool.shutdown();
        singleGamePool.shutdown();
    }

}
