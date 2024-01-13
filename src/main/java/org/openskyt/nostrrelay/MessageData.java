package org.openskyt.nostrrelay;

public record MessageData(
        String type,
        EventData eventData
) {
}
