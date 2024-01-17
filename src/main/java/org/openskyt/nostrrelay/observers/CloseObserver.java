package org.openskyt.nostrrelay.observers;

import org.openskyt.nostrrelay.model.NostrConsumer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CloseObserver {
    private final List<NostrConsumer> nostrConsumers = new ArrayList<>();

    public void subscribe(NostrConsumer nostrConsumer) {
        nostrConsumers.add(nostrConsumer);
    }

    public void unsubscribe(NostrConsumer nostrConsumer) {
        nostrConsumers.remove(nostrConsumer);
    }

    public void notifyConsumers(Object o) {
        for (NostrConsumer nostrConsumer : nostrConsumers) {
            nostrConsumer.handle(o);
        }
    }
}
