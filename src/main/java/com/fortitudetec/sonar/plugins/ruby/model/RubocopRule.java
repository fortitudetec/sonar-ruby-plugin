package com.fortitudetec.sonar.plugins.ruby.model;

import org.sonar.api.rules.RuleType;

public class RubocopRule {
    public final String key;
    public final String name;
    public final String severity;
    public final String htmlDescription;
    public final RuleType debtType;

    public RubocopRule(String key,String severity, String name, RuleType debtType, String htmlDescription) {
        this.key = key;
        this.severity = severity;
        this.name = name;
        this.debtType = debtType;
        this.htmlDescription = htmlDescription;
    }
}
