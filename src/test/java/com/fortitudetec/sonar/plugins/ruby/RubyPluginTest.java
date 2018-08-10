package com.fortitudetec.sonar.plugins.ruby;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.utils.Version;

public class RubyPluginTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testDefine() {
        RubyPlugin plugin = new RubyPlugin();

        Plugin.Context context = new Plugin.Context(Version.create(1, 0));

        plugin.define(context);

        assertThat(context.getExtensions().size()).isEqualTo(18);

        assertThat(context.getExtensions().stream().filter(ext -> ext instanceof PropertyDefinition).count()).isEqualTo(7);
        assertThat(context.getExtensions().stream().filter(ext -> ext instanceof Class).count()).isEqualTo(11);
    }
}
