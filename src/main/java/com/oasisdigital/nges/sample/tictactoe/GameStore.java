package com.oasisdigital.nges.sample.tictactoe;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasisdigital.nges.event.Event;
import com.oasisdigital.nges.event.EventStore;
import com.oasisdigital.nges.sample.tictactoe.domain.Game;
import com.oasisdigital.nges.sample.tictactoe.domain.GameEvent;

/**
 * Writes new events for a {@link Game} to the event store, and loads it by applying events from the store.
 */
@Component
public class GameStore {
    private static final String EVENT_PACKAGE = GameEvent.class.getPackage().getName();

    private final EventStore eventStore;
    private final ObjectMapper objectMapper;

    @Autowired
    public GameStore(EventStore eventStore, ObjectMapper objectMapper) {
        this.eventStore = eventStore;
        this.objectMapper = objectMapper;
    }

    public Game load(UUID gameId) {
        // Each game is fairly short, no harm just loading all events in memory
        Game game = new Game(gameId);
        // @formatter:off
        eventStore.getEventsForStream(gameId, 0, Integer.MAX_VALUE)
                  .stream()
                  .map(this::toGameEvent)
                  .forEach(game::applyEvent);
        // @formatter:on
        return game;
    }

    public void save(Game game) {
        List<Event> newEvents = game.getNewEvents().stream().map(this::toRawEvent).collect(toList());
        eventStore.save(newEvents, "Game", game.getLastSequence());
    }

    @SuppressWarnings("unchecked")
    public GameEvent toGameEvent(Event raw) {
        try {
            Class<GameEvent> type = (Class<GameEvent>) Class.forName(EVENT_PACKAGE + "." + raw.getType());
            GameEvent event = objectMapper.readValue(raw.getPayload(), type);
            event.setSequence(raw.getSequence());
            return event;
        } catch (Exception e) {
            throw new RuntimeException("Unable to read event: " + raw, e);
        }
    }

    public Event toRawEvent(GameEvent gameEvent) {
        try {
            return new Event(gameEvent.getGameId(), gameEvent.getClass().getSimpleName(), UUID.randomUUID(),
                    objectMapper.writeValueAsString(gameEvent));
        } catch (Exception e) {
            throw new RuntimeException("Unable to write event: " + gameEvent, e);
        }
    }
}
