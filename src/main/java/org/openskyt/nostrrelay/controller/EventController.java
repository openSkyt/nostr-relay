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
            case 3      : handleEvent_3(event); break;
            case 5      : handleEvent_5(event); break;
            default     : handleEvent(event);
        }
    }

    /**
     * Handles EVENT by saving into the DB.
     * @param event
     * incoming EVENT-data
     */
    private void handleEvent(Event event) {
        if (!eventService.exists(event.getId())) {
            eventService.save(event);
        }
    }

    /**
     * Handles EVENT-kind 0 by checking redundancy, saves into DB.
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
     * Handles EVENT-kind 3 by checking redundancy (by pubkey), merges data or saves incoming EVENT into db.
     * @param event
     * incoming EVENT-data
     */
    private void handleEvent_3(Event event) {
        Optional<Event> optFollowList = eventService.getFollowList(event.getPubkey());
        if (optFollowList.isPresent()) {
            eventService.delete(optFollowList.get());
        } else {
            eventService.save(event);
        }
    }

    private void handleEvent_5(Event event) {
        eventService.deleteEvents(event);
    }

    // overridden method from implemented NostrConsumer interface - invokes actual impl. defined in this class
    @Override
    public void handle(Object o) {
        if (o instanceof Event) {
            handle((Event) o);
        }
    }
}