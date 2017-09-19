package com.fortitudetec.sonar.plugins.ruby.metrics;

import com.fortitudetec.sonar.plugins.ruby.Ruby;
import com.fortitudetec.sonar.plugins.ruby.squid.RubyRecognizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.squidbridge.measures.Metric;
import org.sonar.squidbridge.text.Source;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@BatchSide
public class RubyMetricsSensor {
    private static final Logger LOG = LoggerFactory.getLogger(RubyMetricsSensor.class);

    public void execute(SensorContext ctx) {
        Iterable<InputFile> affectedFiles = gatherFiles(ctx);
        affectedFiles.forEach(inputFile -> setMetricsForFile(inputFile, ctx));
    }

    private void setMetricsForFile(InputFile inputFile, SensorContext ctx) {
        try (FileReader fileReader = new FileReader(inputFile.file()); BufferedReader reader = new BufferedReader(fileReader)) {
            Source source = new Source(reader, new RubyRecognizer(), "#");

            ctx.<Integer>newMeasure().forMetric(CoreMetrics.NCLOC).on(inputFile).withValue(source.getMeasure(Metric.LINES_OF_CODE)).save();
            ctx.<Integer>newMeasure().forMetric(CoreMetrics.COMMENT_LINES).on(inputFile).withValue(CommentCountParser.countLinesOfComment(inputFile.file())).save();
            ctx.<Integer>newMeasure().forMetric(CoreMetrics.CLASSES).on(inputFile).withValue(ClassCountParser.countClasses(inputFile.file())).save();

        } catch (IOException e) {
            LOG.warn("Unable to read ruby file to gather metrics.", e);
        }
    }

    private Iterable<InputFile> gatherFiles(SensorContext ctx) {
        return ctx
            .fileSystem()
            .inputFiles(ctx.fileSystem().predicates().hasLanguage(Ruby.LANGUAGE_KEY));
    }
}
