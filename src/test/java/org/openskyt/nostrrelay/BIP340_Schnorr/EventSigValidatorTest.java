package org.openskyt.nostrrelay.BIP340_Schnorr;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openskyt.nostrrelay.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EventSigValidatorTest {
    private final EventSigValidator eventSigValidator;
    @Autowired
    EventSigValidatorTest(EventSigValidator eventSigValidator) {
        this.eventSigValidator = eventSigValidator;
    }

    @Test
    void verifyEvent() {
        Assertions.assertTrue(eventSigValidator.verifyEvent(new Event("2786e0e7179f3a4eec632df6595b20c8ce7d146fcd201194c562f1ce7df7513d",
                "a23cceb5d5596f80c3249f15ebcf3e42c6aba0b3c88aaf572c018bb6c7e6180c",
                1705579214,
                1,
                new String[][]{},
                "ahoj",
                "c46e03bc0c9a852a60f91849a47199276457872fce04514aab8759998bec7e89a436eddf6e2a3febf78d768c5d103f0be9d3ca00948118191ec61b5037a8b91d")));
    }
    @Test
    void verifyEventBadInput() {
        Assertions.assertFalse(eventSigValidator.verifyEvent(new Event("1234",
                "a23cceb5d5596f80c3249f15ebcf3e42c6aba0b3c88aaf572c018bb6c7e6180c",
                1705579214,
                1,
                new String[][]{},
                "ahoj",
                "c46e03bc0c9a852a60f91849a47199276457872fce04514aab8759998bec7e89a436eddf6e2a3febf78d768c5d103f0be9d3ca00948118191ec61b5037a8b91d")));
    }
}