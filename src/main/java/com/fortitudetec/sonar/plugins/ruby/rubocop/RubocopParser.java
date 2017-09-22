package com.fortitudetec.sonar.plugins.ruby.rubocop;

import com.fortitudetec.sonar.plugins.ruby.model.RubocopIssue;
import com.fortitudetec.sonar.plugins.ruby.model.RubocopPosition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.sonar.api.batch.BatchSide;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@BatchSide
public class RubocopParser {
    @SuppressWarnings("unchecked")
    public Map<String, List<RubocopIssue>> parse(List<String> toParse) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        Map<String, List<RubocopIssue>> toReturn = new HashMap<>();

        for (String batch : toParse) {
            Map<String, List<Map<String, Object>>> json = gson.fromJson(batch, Map.class);


            json.get("files").forEach(file -> {
                List<RubocopIssue> issues = populateIssues((List<Map<String, Object>>) file.get("offenses"));

                if (!issues.isEmpty()) {
                    toReturn.put((String) file.get("path"), issues);
                }
            });
        }

        return toReturn;
    }

    @SuppressWarnings("unchecked")
    private List<RubocopIssue> populateIssues(List<Map<String, Object>> offenses) {
        return offenses.stream()
            .map(offense -> RubocopIssue.builder()
                .ruleName((String) offense.get("cop_name"))
                .failure((String) offense.get("message"))
                .position(populatePosition((Map<String, Double>) offense.get("location")))
                .build())
            .collect(Collectors.toList());
    }

    private RubocopPosition populatePosition(Map<String, Double> location) {
        return RubocopPosition.builder()
            .line(location.get("line").intValue())
            .character(location.get("column").intValue())
            .build();
    }
}
