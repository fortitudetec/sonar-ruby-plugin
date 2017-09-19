package com.fortitudetec.sonar.plugins.ruby.simplecov;

import static java.util.Objects.isNull;

import com.fortitudetec.sonar.plugins.ruby.Ruby;
import com.fortitudetec.sonar.plugins.ruby.RubyPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.coverage.CoverageType;
import org.sonar.api.batch.sensor.coverage.NewCoverage;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

@BatchSide
public class SimpleCovSensor {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleCovSensor.class);

    public void execute(SensorContext context) {
        String reportPath = context.settings().getString(RubyPlugin.SIMPLECOV_REPORT_PATH);

        if (isNull(reportPath)) {
            LOG.warn("Report path is not set, unable to generate coverage metrics");
            return;
        }

        SimpleCovParser parser = new SimpleCovParser();

        try {
            Map<String, NewCoverage> fileCoverages = parser.parse(context, new File(reportPath));
            fileCoverages.values().forEach(NewCoverage::save);
        } catch (IOException e) {
            LOG.warn("Unable to generate coverage metrics", e);
        }
    }


}
