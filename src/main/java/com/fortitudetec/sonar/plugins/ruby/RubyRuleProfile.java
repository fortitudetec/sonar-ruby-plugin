package com.fortitudetec.sonar.plugins.ruby;

import com.fortitudetec.sonar.plugins.ruby.model.RubocopRule;
import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.Rule;
import org.sonar.api.utils.ValidationMessages;

public class RubyRuleProfile extends ProfileDefinition {
    @Override
    public RulesProfile createProfile(ValidationMessages validationMessages) {
        RulesProfile profile = RulesProfile.create("Rubocop", Ruby.LANGUAGE_KEY);

        RubyRulesDefinition rules = new RubyRulesDefinition();

        activateRule(profile, RubyRulesDefinition.RUBY_LINT_UNKNOWN_RULE.key);

        for (RubocopRule coreRule : rules.getCoreRules()) {
            activateRule(profile, coreRule.key);
        }

        return profile;
    }

    private static void activateRule(RulesProfile profile, String ruleKey) {
        profile.activateRule(Rule.create("rubocop", ruleKey), null);
    }
}
