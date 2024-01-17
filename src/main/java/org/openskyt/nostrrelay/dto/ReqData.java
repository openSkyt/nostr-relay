package org.openskyt.nostrrelay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
// note there might be multiple instances deserialized from a single message
public class ReqData {

    private Set<String> ids;
    private Set<String> authors;
    private Set<Integer> kinds;
    @JsonProperty("#e")
    private Set<String> e;
    @JsonProperty("#p")
    private Set<String> p;
    private long since;
    private long until;
    private long limit;

    private Subscription subscription;
}