package org.openskyt.nostrrelay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
// note there might be multiple instances deserialized from a single message
public class ReqFilter {

    private Set<String> ids;
    private Set<String> authors;
    private Set<Integer> kinds;
    @JsonProperty("#e")
    private Set<String> e;
    @JsonProperty("#p")
    private Set<String> p;
    private Long since;
    private Long until;
    private Integer limit;
}