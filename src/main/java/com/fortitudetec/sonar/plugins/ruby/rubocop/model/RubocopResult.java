package com.fortitudetec.sonar.plugins.ruby.rubocop.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class RubocopResult {

    private Map<String, String> metadata;
    private List<RubocopFile> files;
    private Map<String, Double> summary;

}
