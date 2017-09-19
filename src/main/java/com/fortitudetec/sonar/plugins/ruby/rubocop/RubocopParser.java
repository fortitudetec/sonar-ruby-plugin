package com.fortitudetec.sonar.plugins.ruby.rubocop;

import com.fortitudetec.sonar.plugins.ruby.model.RubocopIssue;

import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface RubocopParser {
    Map<String, List<RubocopIssue>> parse(List<String> rawOutputBatches);
}
