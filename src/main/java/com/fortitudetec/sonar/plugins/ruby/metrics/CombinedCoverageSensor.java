package com.fortitudetec.sonar.plugins.ruby.metrics;

import com.fortitudetec.sonar.plugins.ruby.Ruby;
import com.fortitudetec.sonar.plugins.ruby.simplecov.SimpleCovSensor;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;

public class CombinedCoverageSensor implements Sensor {

    private RubyMetricsSensor locSensor;
    private SimpleCovSensor coverageSensor;

    public CombinedCoverageSensor(RubyMetricsSensor locSensor, SimpleCovSensor coverageSensor) {
        this.locSensor = locSensor;
        this.coverageSensor = coverageSensor;
    }

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.name("Combined SimpleCov and LOC sensor");
        descriptor.onlyOnLanguage(Ruby.LANGUAGE_KEY);
    }

    @Override
    public void execute(SensorContext context) {
        locSensor.execute(context);
        coverageSensor.execute(context);
    }
}
