package com.fortitudetec.sonar.plugins.ruby;

import com.fortitudetec.sonar.plugins.ruby.metrics.CombinedCoverageSensor;
import com.fortitudetec.sonar.plugins.ruby.metrics.RubyMetricsSensor;
import com.fortitudetec.sonar.plugins.ruby.rubocop.RubocopExecutor;
import com.fortitudetec.sonar.plugins.ruby.rubocop.RubocopParser;
import com.fortitudetec.sonar.plugins.ruby.rubocop.RubocopSensor;
import com.fortitudetec.sonar.plugins.ruby.simplecov.SimpleCovParser;
import com.fortitudetec.sonar.plugins.ruby.simplecov.SimpleCovSensor;
import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

public class RubyPlugin implements Plugin {

    private static final String RUBY_CATEGORY = "Ruby";

    // Subcategories
    private static final String GENERAL = "General";
    private static final String TEST_AND_COVERAGE = "Tests and Coverage";
    private static final String RUBOCOP = "Rubocop";

    // Properties
    public static final String FILE_SUFFIXES = "sonar.ruby.file.suffixes";
    public static final String SIMPLECOV_REPORT_PATH = "sonar.ruby.coverage.reportPath";
    public static final String FORCE_ZERO_COVERAGE = "sonar.ruby.coverage.forceZeroCoverage";
    public static final String RUBOCOP_CONFIG = "sonar.ruby.rubocopConfig";
    public static final String RUBOCOP_BIN = "sonar.ruby.rubocop";
    public static final String RUBOCOP_REPORT_PATH = "sonar.ruby.rubocop.reportPath";
    public static final String RUBOCOP_FILES_PATH = "sonar.ruby.rubocop.filePath";

    public void define(Context context) {

        context.addExtensions(
            PropertyDefinition.builder(FILE_SUFFIXES)
                .name("File Suffixes")
                .description("Comma separated list of Ruby files to analyze.")
                .category(RUBY_CATEGORY)
                .subCategory(GENERAL)
                .onQualifiers(Qualifiers.PROJECT)
                .defaultValue("rb")
                .build(),

            // SimpleCov
            PropertyDefinition.builder(SIMPLECOV_REPORT_PATH)
                .name("Path to coverage reports")
                .description("Path to coverage reports.")
                .category(RUBY_CATEGORY)
                .subCategory(TEST_AND_COVERAGE)
                .onQualifiers(Qualifiers.PROJECT)
                .defaultValue("coverage/.resultset.json")
                .build(),

            PropertyDefinition.builder(FORCE_ZERO_COVERAGE)
                .name("Assign zero line coverage to source files without coverage report(s)")
                .description("If 'True', assign zero line coverage to source files without coverage report(s), which results in a more realistic overall Technical Debt value.")
                .category(RUBY_CATEGORY)
                .subCategory(TEST_AND_COVERAGE)
                .onQualifiers(Qualifiers.PROJECT)
                .defaultValue("false")
                .type(PropertyType.BOOLEAN)
                .build(),

            // RUBOCOP
            PropertyDefinition.builder(RUBOCOP_CONFIG)
                .name("Rubocop configuration")
                .description("Path to the Rubocop configuration file to use in rubocop analysis. Set to empty to use the default.")
                .category(RUBY_CATEGORY)
                .subCategory(RUBOCOP)
                .onQualifiers(Qualifiers.PROJECT)
                .defaultValue(".rubocop.yml")
                .build(),

            PropertyDefinition.builder(RUBOCOP_BIN)
                .name("Rubocop executable")
                .description("Path to the rubocop executable to use in rubocop analysis. Set to empty to use the default one.")
                .category(RUBY_CATEGORY)
                .subCategory(RUBOCOP)
                .onQualifiers(Qualifiers.PROJECT)
                .defaultValue("")
                .build(),

            PropertyDefinition.builder(RUBOCOP_FILES_PATH)
                .name("Rubocop files to check")
                .description("The path to the files for rubocop to check")
                .category(RUBY_CATEGORY)
                .subCategory(RUBOCOP)
                .onQualifiers(Qualifiers.PROJECT)
                .defaultValue(".")
                .build(),

            PropertyDefinition.builder(RUBOCOP_REPORT_PATH)
                .name("Rubocop's reports")
                .description("Path to Rubocop's report file, relative to projects root")
                .category(RUBY_CATEGORY)
                .subCategory(RUBOCOP)
                .onQualifiers(Qualifiers.PROJECT)
                .defaultValue("rubocop-result.json")
                .build(),

            RubyRuleProfile.class,
            Ruby.class,
            RubyRulesDefinition.class,
            RubocopSensor.class,

            PathResolver.class,
            RubocopExecutor.class,
            RubocopParser.class,
            RubyMetricsSensor.class,

            SimpleCovSensor.class,
            SimpleCovParser.class,
            CombinedCoverageSensor.class
        );
    }
}
