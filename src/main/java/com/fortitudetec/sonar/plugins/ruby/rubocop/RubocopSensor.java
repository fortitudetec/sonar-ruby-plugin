package com.fortitudetec.sonar.plugins.ruby.rubocop;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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

        Collection<ActiveRule> allRules = sensorContext.activeRules().findByRepository("rubocop");
        HashSet<String> ruleNames = new HashSet<>();
        for (ActiveRule rule : allRules) {
            ruleNames.add(rule.ruleKey().rule());
        }

        FileSystem fs = sensorContext.fileSystem();
        List<String> paths = new ArrayList<>();

        for (InputFile file : fs.inputFiles(fs.predicates().hasLanguage(Ruby.LANGUAGE_KEY))) {
            String pathAdjusted = file.absolutePath();
            paths.add(pathAdjusted);
        }

        List<String> jsonResults = this.executor.execute(config, paths);

        Map<String, List<RubocopIssue>> issues = parser.parse(jsonResults);

        if (issues.isEmpty()) {
            LOG.warn("Rubocop returned no result at all");
            return;
        }

        File baseDir = fs.baseDir();
        String baseDirPath = baseDir.getPath();
        String baseDirCanonicalPath = null;
        try {
            baseDirCanonicalPath = baseDir.getCanonicalPath();
        } catch (IOException e) {
            LOG.error("Failed to canonicalize " + baseDirPath, e);
        }

        for (Map.Entry<String, List<RubocopIssue>> kvp : issues.entrySet()) {
            String filePath = kvp.getKey();
            List<RubocopIssue> batchIssues = kvp.getValue();

            if (batchIssues.isEmpty()) {
                continue;
            }

            if (baseDirCanonicalPath != null) {
                filePath = filePath.replace(baseDirCanonicalPath, baseDirPath);
            }

            File matchingFile = fs.resolvePath(filePath);
            InputFile inputFile = null;

            if (matchingFile != null) {
                try {
                    inputFile = fs.inputFile(fs.predicates().is(matchingFile));
                }
                catch (IllegalArgumentException e) {
                    LOG.error("Failed to resolve " + filePath + " to a single path", e);
                    continue;
                }
            }

            if (inputFile == null) {
                LOG.warn("Rubocop reported issues against a file that isn't in the analysis set - will be ignored: {}", filePath);
                continue;
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("Handling Rubocop output for '{}' reporting against '{}'", filePath, inputFile.absolutePath());
            }

            for (RubocopIssue issue : batchIssues) {
                // Make sure the rule we're violating is one we recognise - if not, we'll
                // fall back to the generic 'tslint-issue' rule
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
    }
}
