package org.openskyt.nostrrelay;

public record EventMessageData(
        String type,
        EventData eventData
) {
}
