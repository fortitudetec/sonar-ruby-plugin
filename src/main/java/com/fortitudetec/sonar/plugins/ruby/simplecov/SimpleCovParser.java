package com.fortitudetec.sonar.plugins.ruby.simplecov;

import static java.util.Objects.nonNull;

import com.fortitudetec.sonar.plugins.ruby.Ruby;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.coverage.CoverageType;
import org.sonar.api.batch.sensor.coverage.NewCoverage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@BatchSide
public class SimpleCovParser {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleCovParser.class);

    @SuppressWarnings("unchecked")
    public Map<String, NewCoverage> parse(SensorContext ctx, File resultFile) throws IOException {
        LOG.info("Simplecov result file: {}", resultFile.getAbsolutePath());
        Map<String, NewCoverage> coveredFiles = Maps.newHashMap();

        String fileString = FileUtils.readFileToString(resultFile, "UTF-8");

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        Map<String, Map<String, Map<String, List<Integer>>>> results = gson.fromJson(fileString, Map.class);

        Map<String, List<Integer>> coverageByFile = results.get("RSpec").get("coverage");

        Iterable<InputFile> inputFiles = ctx.fileSystem().inputFiles(ctx.fileSystem().predicates().hasLanguage(Ruby.LANGUAGE_KEY));

        inputFiles.forEach(file -> {
            NewCoverage coverage = ctx.newCoverage()
                .onFile(file)
                .ofType(CoverageType.UNIT);

            List<Integer> lineCounts = coverageByFile.get(file.absolutePath());

            if (lineCounts == null || lineCounts.isEmpty()) {
                updateForZeroCoverage(file, coverage, gatherNonCommentLinesOfCodeForFile(file));
            } else {
                for (int i = 0; i < lineCounts.size(); i++) {
                    Double line = (Double) lineCounts.toArray()[i];
                    int lineNumber = i + 1;
                    if (line != null) {
                        Integer intLine = line.intValue();
                        coverage.lineHits(lineNumber, intLine);
                    }
                }
            }

            coveredFiles.put(file.absolutePath(), coverage);
        });

        return coveredFiles;
    }

    private Set<Integer> gatherNonCommentLinesOfCodeForFile(InputFile inputFile) {
        HashSet<Integer> toReturn = new HashSet<>();

        int lineNumber = 0;

        try (FileReader fileReader = new FileReader(inputFile.file()); BufferedReader reader = new BufferedReader(fileReader)) {
            String line;
            while (nonNull(line = reader.readLine())) {
                lineNumber++;
                line = line.trim().replaceAll("\\n|\\t|\\s", "");
                if (!("".equals(line) || line.startsWith("#"))) {
                    toReturn.add(lineNumber);
                }
            }

            reader.close();

        } catch (FileNotFoundException e) {
            LOG.error("File not found", e);
        } catch (IOException e) {
            LOG.error("Error while reading BufferedReader", e);
        }

        return toReturn;
    }

    private NewCoverage updateForZeroCoverage(InputFile inputFile, NewCoverage newCoverage, Set<Integer> nonCommentLineNumbers) {
        LOG.info("Saving zero for {}, commentLineNumbers: {}", inputFile.absolutePath(), nonCommentLineNumbers);

        if (nonCommentLineNumbers != null) {
            for (Integer nonCommentLineNumber : nonCommentLineNumbers) {
                newCoverage.lineHits(nonCommentLineNumber, 0);
            }
        }
        else {
            for (int i = 1; i <= inputFile.lines(); i++) {
                newCoverage.lineHits(i, 0);
            }
        }

        return newCoverage;
    }
}
