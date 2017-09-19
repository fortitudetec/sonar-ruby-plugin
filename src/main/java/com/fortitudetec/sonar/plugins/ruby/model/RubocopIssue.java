package com.fortitudetec.sonar.plugins.ruby.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class RubocopIssue {
    private RubocopPosition position;
    private String failure;
    private String ruleName;
}
