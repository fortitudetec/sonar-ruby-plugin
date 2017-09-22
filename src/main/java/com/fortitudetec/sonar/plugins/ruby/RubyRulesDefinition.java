package com.fortitudetec.sonar.plugins.ruby;

import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.sonar.api.rule.Severity.CRITICAL;
import static org.sonar.api.rule.Severity.INFO;
import static org.sonar.api.rule.Severity.MINOR;
import static org.sonar.api.rules.RuleType.BUG;
import static org.sonar.api.rules.RuleType.CODE_SMELL;
import static org.sonar.api.rules.RuleType.VULNERABILITY;

import com.fortitudetec.sonar.plugins.ruby.model.RubocopRule;
import org.sonar.api.rule.RuleStatus;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.RulesDefinition;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class RubyRulesDefinition implements RulesDefinition {

    /** The SonarQube rule that will contain all unknown TsLint issues. */
    public static final RubocopRule RUBY_LINT_UNKNOWN_RULE = new RubocopRule(
        "rubocop-issue",
        Severity.MAJOR,
        "rubocop issues that are not yet known to the plugin",
        CODE_SMELL,
        "No description for Rubocop rule");

    private List<RubocopRule> rubylintCoreRules = new ArrayList<>();

    public RubyRulesDefinition() {
        loadCoreRules();
    }

    @Override
    public void define(Context context) {
        NewRepository repository =
            context
                .createRepository("rubocop", Ruby.LANGUAGE_KEY)
                .setName("Rubocop Analyzer");

        createRule(repository, RUBY_LINT_UNKNOWN_RULE);

        // add the Rubocop builtin core rules
        for (RubocopRule coreRule : rubylintCoreRules) {
            createRule(repository, coreRule);
        }

        repository.done();
    }

    public List<RubocopRule> getCoreRules() {
        return rubylintCoreRules;
    }

    private void createRule(NewRepository repository, RubocopRule rubocopRule) {
            repository
                .createRule(rubocopRule.key)
                .setName(rubocopRule.name)
                .setSeverity(rubocopRule.severity)
                .setHtmlDescription(rubocopRule.htmlDescription)
                .setStatus(RuleStatus.READY)
                .setType(rubocopRule.debtType);
    }

    @SuppressWarnings("unchecked")
    private void loadCoreRules() {
        InputStream coreRulesStream = RubyRulesDefinition.class.getResourceAsStream("/rubocop/rubocop.yml");
        Yaml yaml = new Yaml();

        Map<String, Map<String, Object>> rules = (Map<String, Map<String, Object>>) yaml.load(coreRulesStream);

        rules.forEach((rule, metadata) -> {
            if ((Boolean) metadata.get("Enabled")) {
                rubylintCoreRules.add(new RubocopRule(rule, findSeverity(rule), rule, findType(rule), (String) metadata.get("Description")));
            }
        });

        rubylintCoreRules.sort(Comparator.comparing(r -> r.key));
    }

    private static String findSeverity(String rule) {
        String severity = MINOR;

        if (startsWith(rule, "Metrics") || startsWith(rule, "Bundler")) {
            severity = INFO;
        } else if (startsWith(rule, "Security")) {
            severity = CRITICAL;
        }

        return severity;
    }

    private static RuleType findType(String rule) {
        RuleType type = CODE_SMELL;

        if (startsWith(rule, "Performance")) {
            type = BUG;
        } else if (startsWith(rule, "Security")) {
            type = VULNERABILITY;
        }

        return type;
    }
}
