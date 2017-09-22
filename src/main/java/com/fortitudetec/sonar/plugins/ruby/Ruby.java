package com.fortitudetec.sonar.plugins.ruby;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.AbstractLanguage;

import java.util.List;

public class Ruby extends AbstractLanguage {

    public static final String LANGUAGE_KEY = "rb";

    private static final String[] DEFAULT_FILE_SUFFIXES = { "rb", "Gemfile", "gemspec", "rake", "spec", "Capfile", "ru", "Rakefile" };

    public static final String[] RUBY_KEYWORDS_ARRAY =
        {
            "alias", "and", "BEGIN", "begin", "break", "case", "class", "def", "defined?",
            "do", "else", "elsif", "END", "end", "ensure", "false", "for", "if", "in", "module",
            "next", "nil", "not", "or", "redo", "rescue", "retry", "return", "self", "super",
            "then", "true", "undef", "unless", "until", "when", "while", "yield"
        };

    private Settings settings;

    public Ruby(Settings settings) {
        super(LANGUAGE_KEY, "Ruby");
        this.settings = settings;
    }

    @Override
    public String[] getFileSuffixes() {
        String[] suffixes = filterEmptyStrings(settings.getStringArray("sonar.ruby.file.suffixes"));
        return suffixes.length == 0 ? Ruby.DEFAULT_FILE_SUFFIXES : suffixes;
    }

    private static String[] filterEmptyStrings(String[] stringArray) {
        List<String> nonEmptyStrings = Lists.newArrayList();
        for (String string : stringArray) {
            if (StringUtils.isNotBlank(string.trim())) {
                nonEmptyStrings.add(string.trim());
            }
        }
        return nonEmptyStrings.toArray(new String[nonEmptyStrings.size()]);
    }
}
