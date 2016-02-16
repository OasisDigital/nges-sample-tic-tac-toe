package com.oasisdigital.nges.sample.eventstream;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.oasisdigital.nges.cluster.EventUpdate;
import com.oasisdigital.nges.cluster.MessageGroup;
import com.oasisdigital.nges.event.Event;

/**
 * Queries the event store for new events using {@link EventSupplier} and rewrites the events to
 * {@link SseEmitter}. It is a {@link Runnable} that occupies its thread for the whole life time. The queries
 * are executed every 10 seconds or immediately when the event store notifies about new events. If there is no
 * activity, a tiny "ping" message is sent over SSE to keep the connection alive (or detect when it breaks).
 *
 */
public class SseEventStream implements Runnable {
    private static enum State {
        READY, STARTED, STOPPED
    };

    private static final long QUERY_INTERVAL_MS = 10_000;

    private final Logger log = LoggerFactory.getLogger(SseEventStream.class);

    private final EventSupplier eventSupplier;
    private final MessageGroup messageGroup;
    private Thread thread;

    private SseEmitter sseEmitter;
    private Semaphore newEventsSemaphore = new Semaphore(0);
    private long lastQueryTime = 0;
    private long lastEventIdInStore = 0;

    private State state;

    public SseEventStream(MessageGroup messageGroup, EventSupplier eventSupplier) {
        this.eventSupplier = eventSupplier;
        this.messageGroup = messageGroup;

        this.sseEmitter = new SseEmitter();
        this.state = State.READY;
    }

    @Override
    public void run() {
        thread = Thread.currentThread();
        start();
        sendEventsInLoop();
    }

    private void start() {
        Preconditions.checkState(state == State.READY);
        this.state = State.STARTED;

        sseEmitter.onCompletion(this::stop);
        messageGroup.registerSubscriber(this);
    }

    private void sendEventsInLoop() {
        boolean mayHaveMoreEvents = false;
        while (true) {
            if (Thread.interrupted()) {
                stop();
                break;
            }
            newEventsSemaphore.drainPermits();

            if (mayHaveMoreEvents || timeSinceLastQuery() > QUERY_INTERVAL_MS) {
                // Time to query for new events
                mayHaveMoreEvents = false;
                lastQueryTime = System.currentTimeMillis();
                List<Event> events = eventSupplier.getNewEvents();
                if (!events.isEmpty()) {
                    events.forEach(this::sendEvent);
                    mayHaveMoreEvents = true;
                    continue;
                }
            }
            // No new events, let's just ping to keep the connection alive
            sendPing();

            // Wait a while, or wake up if another event has been received in the meantime
            try {
                mayHaveMoreEvents = newEventsSemaphore.tryAcquire(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                stop();
                break;
            }
        }
    }

    private long timeSinceLastQuery() {
        return System.currentTimeMillis() - lastQueryTime;
    }

    private void sendEvent(Event event) {
        sendAndStopOnError(event.getType(), Optional.of(eventSupplier.getEventId(event)), event.getPayload());
    }

    private void sendPing() {
        sendAndStopOnError("ping", eventSupplier.getLastEventId(), "ping");
    }

    private void sendAndStopOnError(String event, Optional<String> id, Object data) {
        try {
            SseEventBuilder builder = SseEmitter.event().name(event).data(data);
            id.ifPresent(builder::id);
            sseEmitter.send(builder);
        } catch (Exception e) {
            log.warn("Error in SSE channel: " + e);
            stop();
        }
    }

    synchronized public void stop() {
        if (this.state == State.READY) {
            throw new IllegalStateException();
        }
        if (this.state == State.STARTED) {
            messageGroup.unregisterSubscriber(this);
            sseEmitter.complete();
            thread.interrupt();
            this.state = State.STOPPED;
        }
    }

    @Subscribe
    public void on(EventUpdate eventUpdate) {
        if (eventUpdate.getEventId() > lastEventIdInStore) {
            lastEventIdInStore = eventUpdate.getEventId();
            newEventsSemaphore.release();
        }
    }

    public SseEmitter getSseEmitter() {
        return sseEmitter;
    }
}
