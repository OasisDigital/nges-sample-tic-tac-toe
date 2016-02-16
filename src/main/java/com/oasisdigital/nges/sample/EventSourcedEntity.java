package com.oasisdigital.nges.sample;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.oasisdigital.nges.sample.tictactoe.domain.Game;
import com.oasisdigital.nges.sample.tictactoe.domain.GameEvent;

/**
 * Base class for an event-sourced entity, keeping track of new events and providing helper method to apply
 * events by calling the right <code>on(ConcreteEventType)</code> method on the subclass.
 *
 */
public class EventSourcedEntity {
    private long lastSequence;
    private List<GameEvent> newEvents = new LinkedList<>();

    protected void applyAndPersist(GameEvent event) {
        applyEvent(event);
        this.newEvents.add(event);
    }

    public void applyEvent(GameEvent e) {
        try {
            Game.class.getMethod("on", e.getClass()).invoke(this, e);
            if (e.isPersistent()) {
                this.lastSequence = e.getSequence();
            }
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException("Unable to apply event " + e, ex);
        }
    }

    public long getLastSequence() {
        return lastSequence;
    }

    public List<GameEvent> getNewEvents() {
        return Collections.unmodifiableList(newEvents);
    }

}
