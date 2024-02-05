package org.openskyt.nostrrelay.model;

public interface NostrConsumer {

    void handle(Object o);
}