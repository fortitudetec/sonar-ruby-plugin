package com.fortitudetec.sonar.plugins.ruby.simplecov;

import static java.util.Objects.nonNull;
import static java.util.Objects.isNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortitudetec.sonar.plugins.ruby.Ruby;
import com.fortitudetec.sonar.plugins.ruby.RubyPlugin;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@BatchSide
public class SimpleCovParser {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleCovParser.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public Map<String, NewCoverage> parse(SensorContext ctx, File resultFile) throws IOException {

        Map<String, Map<String, Map<String, List<Integer>>>> results = parseResultsFile(resultFile);

        String testFramework = ctx.settings().getString(RubyPlugin.TEST_FRAMEWORK);

        if (isNull(testFramework)) {
            LOG.warn("Test framework is not set, unable to parse coverage metrics");
            return null;
        }

        Map<String, List<Integer>> coverageByFile = results.get(testFramework).get("coverage");

        Iterable<InputFile> inputFiles = ctx.fileSystem().inputFiles(ctx.fileSystem().predicates().hasLanguage(Ruby.LANGUAGE_KEY));

       return StreamSupport.stream(inputFiles.spliterator(), false)
            .collect(Collectors.toMap(InputFile::absolutePath,
                file -> buildCoverageForFile(ctx, file, coverageByFile.get(file.absolutePath()))));
    }

    private NewCoverage buildCoverageForFile(SensorContext ctx, InputFile file, List<Integer> lineCounts) {
        NewCoverage coverage = ctx.newCoverage()
            .onFile(file)
            .ofType(CoverageType.UNIT);

        if (lineCounts == null || lineCounts.isEmpty()) {
            updateForZeroCoverage(file, coverage, gatherNonCommentLinesOfCodeForFile(file));
        } else {
            IntStream.range(0, lineCounts.size())
                .forEach(idx -> coverageForLine(coverage, idx, lineCounts.get(idx)));
        }

        return coverage;
    }

    private void coverageForLine(NewCoverage coverage, int lineNumber, Integer lineCount) {
        if (nonNull(lineCount)) {
            coverage.lineHits(lineNumber + 1, lineCount);
        }
    }

    private Map parseResultsFile(File resultFile) throws IOException {
        String fileString = FileUtils.readFileToString(resultFile, "UTF-8");
        return MAPPER.readValue(fileString, Map.class);
    }

    private Set<Integer> gatherNonCommentLinesOfCodeForFile(InputFile inputFile) {
        HashSet<Integer> toReturn = new HashSet<>();

        int lineNumber = 0;

        try (FileReader fileReader = new FileReader(inputFile.file()); BufferedReader reader = new BufferedReader(fileReader)) {
            String line;
            while (nonNull(line = reader.readLine())) {
                lineNumber++;
                line = line.trim().replaceAll("\\n|\\t|\\s", "");
                if (!(StringUtils.isBlank(line) || line.startsWith("#"))) {
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

    private void updateForZeroCoverage(InputFile inputFile, NewCoverage newCoverage, Set<Integer> nonCommentLineNumbers) {
        if (nonCommentLineNumbers != null) {
            nonCommentLineNumbers.forEach(lineNumber -> newCoverage.lineHits(lineNumber, 0));
        }
        else {
            IntStream.rangeClosed(0, inputFile.lines())
                .forEach(idx -> newCoverage.lineHits(idx, 0));
        }
    }
}
