package org.openskyt.nostrrelay.util;

import org.openskyt.nostrrelay.model.Event;
import org.springframework.stereotype.Component;
import java.math.BigInteger;

@Component
public class Validator {

    private static final short POW_LEVEL = 20;

    public boolean validatePow(Event event) {
        return event.getCommittedPowLevel() != null && event.getCommittedPowLevel() >= POW_LEVEL && validateHash(event);
    }

    private boolean validateHash(Event event) {
        int leadingZeros = countLeadingZeroBits(event.getId());
        return leadingZeros >= POW_LEVEL;
    }

    private int countLeadingZeroBits(String hexString) {
        BigInteger bigint = new BigInteger(hexString, 16);
        return bigint.bitLength() - bigint.bitCount();
    }
}