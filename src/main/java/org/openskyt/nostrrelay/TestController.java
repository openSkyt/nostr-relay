package org.openskyt.nostrrelay;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {

    @GetMapping("/dummy")
    public String dummyEvent() {
        return "dummyFeeder";
    }
}
