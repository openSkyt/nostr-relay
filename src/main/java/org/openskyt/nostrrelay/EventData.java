package org.openskyt.nostrrelay;

import java.util.List;

public record EventData(
    String id,
    String pubkey,
    long created_at,
    int kind,
    List<String> tags,
    String content,
    String sig) {
}
