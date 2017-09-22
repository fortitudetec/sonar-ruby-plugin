package com.fortitudetec.sonar.plugins.ruby;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.sonar.api.config.Settings;

public class RubyTest {

    @Test
    public void testGetFileSuffixes_Default() {
        Ruby ruby = new Ruby(new Settings());

        String[] fileSuffixes = ruby.getFileSuffixes();

        assertThat(fileSuffixes).hasSize(8);
        assertThat(fileSuffixes).hasSameElementsAs(Lists.newArrayList("rb", "Gemfile", "gemspec", "rake", "spec", "Capfile", "ru", "Rakefile"));
    }

    @Test
    public void testGetFileSuffixes_Override() {
        Settings settings = new Settings();
        settings.setProperty("sonar.ruby.file.suffixes", "rb");

        Ruby ruby = new Ruby(settings);

        String[] fileSuffixes = ruby.getFileSuffixes();

        assertThat(fileSuffixes).hasSize(1);
        assertThat(fileSuffixes).hasSameElementsAs(Lists.newArrayList("rb"));
    }

    @Test
    public void testGetFileSuffixes_WithSpaces() {
        Settings settings = new Settings();
        settings.setProperty("sonar.ruby.file.suffixes", "rb, , gemspec");

        Ruby ruby = new Ruby(settings);

        String[] fileSuffixes = ruby.getFileSuffixes();

        assertThat(fileSuffixes).hasSize(2);
        assertThat(fileSuffixes).hasSameElementsAs(Lists.newArrayList("rb", "gemspec"));
    }
}
