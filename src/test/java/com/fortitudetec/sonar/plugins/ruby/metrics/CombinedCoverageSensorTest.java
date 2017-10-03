package com.fortitudetec.sonar.plugins.ruby.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.fortitudetec.sonar.plugins.ruby.Ruby;
import com.fortitudetec.sonar.plugins.ruby.simplecov.SimpleCovSensor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

import java.io.File;

public class CombinedCoverageSensorTest {

    @Mock
    private RubyMetricsSensor locSensor;

    @Mock
    private SimpleCovSensor coverageSensor;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testExecute() {
        CombinedCoverageSensor sensor = new CombinedCoverageSensor(locSensor, coverageSensor);

        SensorContextTester context = SensorContextTester.create(new File("."));
        sensor.execute(context);

        verify(locSensor).execute(context);
        verify(coverageSensor).execute(context);
    }

    @Test
    public void testDescribe() {
        CombinedCoverageSensor sensor = new CombinedCoverageSensor(locSensor, coverageSensor);

        DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
        sensor.describe(descriptor);

        assertThat(descriptor.name()).isEqualTo("Combined SimpleCov and LOC sensor");
        assertThat(descriptor.languages()).containsExactly(Ruby.LANGUAGE_KEY);

    }
}
