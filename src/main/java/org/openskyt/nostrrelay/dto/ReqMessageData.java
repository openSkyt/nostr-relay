package org.openskyt.nostrrelay.dto;

import java.util.List;

public record ReqMessageData(
        String type,
        String subscription_id,
        List<ReqData> reqData
) {
}
