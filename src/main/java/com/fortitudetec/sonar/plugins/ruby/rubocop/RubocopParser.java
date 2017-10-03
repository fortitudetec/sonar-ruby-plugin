package com.fortitudetec.sonar.plugins.ruby.rubocop;

import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortitudetec.sonar.plugins.ruby.model.RubocopIssue;
import com.fortitudetec.sonar.plugins.ruby.model.RubocopPosition;
import com.fortitudetec.sonar.plugins.ruby.rubocop.model.RubocopFile;
import com.fortitudetec.sonar.plugins.ruby.rubocop.model.RubocopOffense;
import com.fortitudetec.sonar.plugins.ruby.rubocop.model.RubocopResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.BatchSide;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@BatchSide
public class RubocopParser {
    private static final Logger LOG = LoggerFactory.getLogger(RubocopParser.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public Map<String, List<RubocopIssue>> parse(String toParse) {
        return collectIssues(toParse).entrySet().stream()
            .filter(entry -> !entry.getValue().isEmpty())
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<RubocopIssue>> collectIssues(String resultsFile) {
        try {
            RubocopResult results = MAPPER.readValue(resultsFile, RubocopResult.class);

            return results.getFiles().stream()
                .collect(toMap(RubocopFile::getPath, this::populateIssues));
        } catch (IOException e) {
            LOG.warn("Unable to parse results file", e);
        }

        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    private List<RubocopIssue> populateIssues(RubocopFile file) {
        return file.getOffenses().stream()
            .map(offense -> RubocopIssue.builder()
                .ruleName(offense.getCopName())
                .failure(offense.getMessage())
                .position(populatePosition(offense))
                .build())
            .collect(Collectors.toList());
    }

    private RubocopPosition populatePosition(RubocopOffense offense) {
        return RubocopPosition.builder()
            .line(offense.getLocation().get("line").intValue())
            .character(offense.getLocation().get("column").intValue())
            .build();
    }
}
