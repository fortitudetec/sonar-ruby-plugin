package com.fortitudetec.sonar.plugins.ruby.rubocop.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class RubocopOffense {
    private String severity;
    private String message;
    private Boolean corrected;
    private Map<String, Double> location;

    @JsonProperty("cop_name")
    private String copName;

}
