package org.openskyt.nostrrelay.controller;

import org.openskyt.nostrrelay.model.Event;
import org.openskyt.nostrrelay.model.NostrConsumer;
import org.openskyt.nostrrelay.service.EventService;
import org.openskyt.nostrrelay.observers.EventObserver;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Handles EVENT-data processing logic
 */
@Component
public class EventController implements NostrConsumer {
    private final EventService eventService;

    public EventController(EventObserver observer, EventService eventService) {
        this.eventService = eventService;

        observer.subscribe(this);
    }

    /**
     * Handles EVENT by delegating EVENT-data to a proper logic-handling method defined by EVENT-kind
     * @param event
     * parsed EVENT-message data
     */
    private void handle(Event event) {
        switch (event.getKind()) {
            case 0      : handleEvent_0(event); break;
            default     : handleEvent(event); break;
        }
    }

    /**
     * Handles EVENT-kind 0 by checking redundancy, saves to DB. Responds with OK-message
     * @param event
     * incoming EVENT-data
     */
    private void handleEvent_0(Event event) {
        Optional<Event> optEventData = eventService.getMetaData(event.getPubkey());
        // remove redundant data
        optEventData.ifPresent(eventService::delete);
        handleEvent(event);
    }

    /**
     * Handles EVENT-kind 1 by saving into the DB. Responds with OK-message.
     * @param event
     * incoming EVENT-data
     */
    private void handleEvent(Event event) {
        if (!eventService.exists(event.getId())) {
            eventService.save(event);
        }
    }

    // overridden method from implemented NostrConsumer interface - invokes actual impl. defined in this class
    @Override
    public void handle(Object o) {
        if (o instanceof Event) {
            handle((Event) o);
        }
    }
}