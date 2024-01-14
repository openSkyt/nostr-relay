package org.openskyt.nostrrelay;

import java.util.List;

public record ReqMessageData(
        String type,
        String subscription_id,
        List<ReqData> reqData
) {
}
