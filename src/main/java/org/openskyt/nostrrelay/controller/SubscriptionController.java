package org.openskyt.nostrrelay.controller;

import org.openskyt.nostrrelay.dto.CloseData;
import org.openskyt.nostrrelay.dto.ReqFilter;
import org.openskyt.nostrrelay.dto.Subscription;
import org.openskyt.nostrrelay.model.Event;
import org.openskyt.nostrrelay.model.NostrConsumer;
import org.openskyt.nostrrelay.observers.EventObserver;
import org.openskyt.nostrrelay.service.EventService;
import org.openskyt.nostrrelay.service.SubscriptionManager;
import org.openskyt.nostrrelay.service.SubscriptionService;
import org.openskyt.nostrrelay.util.NostrUtil;
import org.openskyt.nostrrelay.observers.CloseObserver;
import org.openskyt.nostrrelay.observers.ReqObserver;
import org.openskyt.nostrrelay.observers.SessionObserver;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;

@Component
public class SubscriptionController implements NostrConsumer {

    private final SubscriptionManager subscriptionManager;
    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionManager subscriptionManager,
                                  SubscriptionService subscriptionService,
                                  ReqObserver reqObserver,
                                  CloseObserver closeObserver,
                                  SessionObserver sessionObserver,
                                  EventObserver eventObserver) {

        this.subscriptionManager = subscriptionManager;
        this.subscriptionService = subscriptionService;
        reqObserver.subscribe(this);
        closeObserver.subscribe(this);
        sessionObserver.subscribe(this);
        eventObserver.subscribe(this);
    }

    @Override
    public void handle(Object o) {
        if (o instanceof Subscription) {
            subscriptionManager.createNewSubscription((Subscription) o);
            subscriptionService.sendPersistedData((Subscription) o);

        } else if (o instanceof CloseData) {
            subscriptionManager.removeSubscription((CloseData) o);

        } else if (o instanceof WebSocketSession) {
            subscriptionManager.removeSubscription((WebSocketSession) o);

        } else if (o instanceof  Event) {
            subscriptionService.handleNewEvent((Event) o);
        }
    }
}