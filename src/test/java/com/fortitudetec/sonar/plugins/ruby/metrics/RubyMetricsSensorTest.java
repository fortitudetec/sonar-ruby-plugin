package com.fortitudetec.sonar.plugins.ruby.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import com.fortitudetec.sonar.plugins.ruby.rubocop.RubocopSensorTest;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.measure.Measure;
import org.sonar.api.measures.CoreMetrics;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;

public class RubyMetricsSensorTest {

    private SensorContextTester context;
    private File rubyFile;

    @Before
    public void setUp() throws URISyntaxException {
        URL filePath = RubocopSensorTest.class.getClassLoader().getResource("./test_controller.rb");
        rubyFile = new File(filePath.toURI());

        context = SensorContextTester.create(rubyFile.getParentFile());
        context.fileSystem().add(new DefaultInputFile("myProjectKey", "test_controller.rb")
            .setLanguage("rb")
            .setOriginalLineOffsets(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15})
            .setLines(15));
    }

    @Test
    public void testExecute() {
        RubyMetricsSensor sensor = new RubyMetricsSensor();

        sensor.execute(context);

        Collection<Measure> measures = context.measures("myProjectKey:test_controller.rb");
        assertThat(measures.size()).isEqualTo(3);

        measures.forEach(measure -> {
            if (CoreMetrics.NCLOC.equals(measure.metric())) {
                assertThat(measure.value()).isEqualTo(4);
            } else if (CoreMetrics.COMMENT_LINES.equals(measure.metric())) {
                assertThat(measure.value()).isEqualTo(11);
            } else if (CoreMetrics.CLASSES.equals(measure.metric())) {
                assertThat(measure.value()).isEqualTo(1);
            }
        });
    }
}
