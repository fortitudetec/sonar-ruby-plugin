package com.fortitudetec.sonar.plugins.ruby;


import static org.assertj.core.api.Assertions.assertThat;

import com.fortitudetec.sonar.plugins.ruby.model.RubocopRule;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.RulesDefinition;

import java.util.List;
import java.util.stream.Collectors;

public class RubyRulesDefinitionTest {

    private RubyRulesDefinition rules;

    @Before
    public void setUp() {
        rules = new RubyRulesDefinition();
    }

    @Test
    public void testGetCoreRules() {
        List<RubocopRule> coreRules = rules.getCoreRules();

        assertThat(coreRules.size()).isEqualTo(340);

        assertSeverityCount(Severity.BLOCKER, coreRules, 0);
        assertSeverityCount(Severity.CRITICAL, coreRules, 4);
        assertSeverityCount(Severity.MAJOR, coreRules, 0);
        assertSeverityCount(Severity.MINOR, coreRules, 324);
        assertSeverityCount(Severity.INFO, coreRules, 12);

        assertTypeCount(RuleType.CODE_SMELL, coreRules, 311);
        assertTypeCount(RuleType.VULNERABILITY, coreRules, 4);
        assertTypeCount(RuleType.BUG, coreRules, 25);
    }

    @Test
    public void testDefine() {
        RulesDefinition.Context context = new RulesDefinition.Context();

        rules.define(context);

        assertThat(context.repositories().size()).isEqualTo(1);
        assertThat(context.repository("rubocop")).isNotNull();
        assertThat(context.repository("rubocop").language()).isEqualTo("rb");
        assertThat(context.repository("rubocop").rules().size()).isEqualTo(341);
    }


    private void assertSeverityCount(String severity, List<RubocopRule> rules, int expectedCount) {
        List<RubocopRule> filteredRules = rules.stream()
            .filter(rule -> severity.equals(rule.severity))
            .collect(Collectors.toList());

        assertThat(filteredRules.size()).isEqualTo(expectedCount);
    }

    private void assertTypeCount(RuleType type, List<RubocopRule> rules, int expectedCount) {
        List<RubocopRule> filteredRules = rules.stream()
            .filter(rule -> type.equals(rule.debtType))
            .collect(Collectors.toList());

        assertThat(filteredRules.size()).isEqualTo(expectedCount);
    }
}
