package org.openskyt.nostrrelay.dto;

public record EventMessageData(
        String type,
        EventData eventData
) {
}
