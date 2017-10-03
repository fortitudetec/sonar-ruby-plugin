package com.fortitudetec.sonar.plugins.ruby.rubocop;

import static com.google.common.collect.Lists.newArrayList;

import com.fortitudetec.sonar.plugins.ruby.PathResolver;
import com.fortitudetec.sonar.plugins.ruby.Ruby;
import com.fortitudetec.sonar.plugins.ruby.RubyRulesDefinition;
import com.fortitudetec.sonar.plugins.ruby.model.RubocopIssue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class RubocopSensor implements Sensor {
    private static final Logger LOG = LoggerFactory.getLogger(RubocopSensor.class);

    private PathResolver resolver;
    private RubocopExecutor executor;
    private RubocopParser parser;

    public RubocopSensor(PathResolver resolver, RubocopExecutor executor, RubocopParser parser) {
        this.resolver = resolver;
        this.executor = executor;
        this.parser = parser;
    }

    @Override
    public void describe(SensorDescriptor desc) {
        desc
            .name("Linting sensor for Ruby files")
            .onlyOnLanguage(Ruby.LANGUAGE_KEY);
    }

    @Override
    public void execute(SensorContext sensorContext) {
        RubocopExecutorConfig config = RubocopExecutorConfig.fromSettings(sensorContext, resolver);

        if (!config.useExistingRubocopOutput() && config.getPathToRubocop() == null) {
            LOG.warn("Path to rubocop not defined or not found. Skipping rubocop analysis.");
            return;
        }

        String jsonResults = this.executor.execute(config, findFilesToLint(sensorContext, config));

        Map<String, List<RubocopIssue>> issues = parser.parse(jsonResults);

        if (issues.isEmpty()) {
            LOG.warn("Rubocop returned no result at all");
            return;
        }

        Collection<ActiveRule> allRules = sensorContext.activeRules().findByRepository("rubocop");
        Set<String> ruleNames = allRules.stream().map(rule -> rule.ruleKey().rule()).collect(Collectors.toSet());

        issues.entrySet().forEach(rubyFilesIssues -> generateSonarIssuesFromResults(rubyFilesIssues, sensorContext, ruleNames));
    }

    private List<String> findFilesToLint(SensorContext context, RubocopExecutorConfig config) {
        if (config.useExistingRubocopOutput()) {
            return newArrayList();
        }

        Iterable<InputFile> inputFiles = context.fileSystem().inputFiles(context.fileSystem().predicates().hasLanguage(Ruby.LANGUAGE_KEY));

        return StreamSupport.stream(inputFiles.spliterator(), false)
            .map(InputFile::absolutePath)
            .collect(Collectors.toList());
    }

    private void generateSonarIssuesFromResults(Map.Entry<String, List<RubocopIssue>> rubyFilesIssues, SensorContext sensorContext, Set<String> ruleNames) {
        List<RubocopIssue> batchIssues = rubyFilesIssues.getValue();

        if (batchIssues.isEmpty()) {
            return;
        }

        String filePath = rubyFilesIssues.getKey();
        InputFile inputFile = findMatchingFile(sensorContext.fileSystem(), filePath);
        if (inputFile == null) {
            LOG.warn("Rubocop reported issues against a file that isn't in the analysis set - will be ignored: {}", filePath);
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Handling Rubocop output for '{}' reporting against '{}'", filePath, inputFile.absolutePath());
        }

        batchIssues.forEach(issue -> saveNewIssue(issue, inputFile, ruleNames, sensorContext));
    }

    private InputFile findMatchingFile(FileSystem fs, String filePath) {
        File matchingFile = fs.resolvePath(filePath);

        if (matchingFile != null) {
            try {
                return fs.inputFile(fs.predicates().is(matchingFile));
            }
            catch (IllegalArgumentException e) {
                LOG.error("Failed to resolve " + filePath + " to a single path", e);
            }
        }
        return null;
    }

    private void saveNewIssue(RubocopIssue issue, InputFile inputFile, Set<String> ruleNames, SensorContext sensorContext) {
        // Make sure the rule we're violating is one we recognise - if not, we'll
        // fall back to the generic 'rubocop-issue' rule
        String ruleName = issue.getRuleName();
        if (!ruleNames.contains(ruleName)) {
            ruleName = RubyRulesDefinition.RUBY_LINT_UNKNOWN_RULE.key;
        }

        NewIssue newIssue =
            sensorContext
                .newIssue()
                .forRule(RuleKey.of("rubocop", ruleName));

        NewIssueLocation newIssueLocation =
            newIssue
                .newLocation()
                .on(inputFile)
                .message(issue.getFailure())
                .at(inputFile.selectLine(issue.getPosition().getLine()));

        newIssue.at(newIssueLocation);
        newIssue.save();
    }
}
