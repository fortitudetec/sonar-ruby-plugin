package com.fortitudetec.sonar.plugins.ruby;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.sonar.api.profiles.RulesProfile;

public class RubyRuleProfileTest {

    @Test
    public void testCreateProfile() {
        RubyRuleProfile rubyProfile = new RubyRuleProfile();

        RulesProfile profile = rubyProfile.createProfile(null);

        assertThat(profile.getName()).isEqualTo("Rubocop");
        assertThat(profile.getLanguage()).isEqualTo("rb");
        assertThat(profile.getActiveRules().size()).isEqualTo(341);
    }
}
