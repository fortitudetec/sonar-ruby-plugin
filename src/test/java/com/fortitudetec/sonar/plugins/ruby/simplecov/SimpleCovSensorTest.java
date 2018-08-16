package com.fortitudetec.sonar.plugins.ruby.simplecov;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortitudetec.sonar.plugins.ruby.RubyPlugin;
import com.fortitudetec.sonar.plugins.ruby.utils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.coverage.CoverageType;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleCovSensorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SensorContextTester context;
    private File rubyFile;

    @Before
    public void setUp() throws URISyntaxException {
        URL filePath = SimpleCovSensorTest.class.getClassLoader().getResource("./test_controller.rb");
        rubyFile = new File(filePath.toURI());

        context = SensorContextTester.create(rubyFile.getParentFile());
        context.settings().setProperty(RubyPlugin.TEST_FRAMEWORK, "RSpec");
        context.fileSystem().add(new DefaultInputFile("myProjectKey", "test_controller.rb")
                .setLanguage("rb")
                .setLines(15));
        context.fileSystem().add(new DefaultInputFile("myProjectKey", "unknown_file.rb")
                .setLanguage("rb")
                .setLines(10));

        TestUtils.buildResultSetFile(rubyFile);
    }

    @After
    public void tearDown() throws URISyntaxException {
        URL filePath = SimpleCovSensorTest.class.getClassLoader().getResource(".resultset.json");
        File resultFile = new File(filePath.toURI());
        resultFile.delete();
    }

    @Test
    public void testExecute_EmptyReportPath() {
        SimpleCovSensor sensor = new SimpleCovSensor();
        sensor.execute(context);

        assertThat(context.lineHits(rubyFile.getAbsolutePath(), CoverageType.UNIT, 1)).isNull();
    }

    @Test
    public void testExecute() throws URISyntaxException {
        URL filePath = SimpleCovSensorTest.class.getClassLoader().getResource(".resultset.json");
        File resultFile = new File(filePath.toURI());
        context.settings().setProperty(RubyPlugin.SIMPLECOV_REPORT_PATH, resultFile.getAbsolutePath());

        SimpleCovSensor sensor = new SimpleCovSensor();
        sensor.execute(context);

        assertThat(context.lineHits("myProjectKey:test_controller.rb", CoverageType.UNIT, 1)).isEqualTo(1);
    }

    @Test
    public void testExecute_ZeroOutUnknownFiles() throws URISyntaxException {
        URL filePath = SimpleCovSensorTest.class.getClassLoader().getResource(".resultset.json");
        File resultFile = new File(filePath.toURI());
        context.settings().setProperty(RubyPlugin.SIMPLECOV_REPORT_PATH, resultFile.getAbsolutePath());

        SimpleCovSensor sensor = new SimpleCovSensor();
        sensor.execute(context);

        assertThat(context.lineHits("myProjectKey:unknown_file.rb", CoverageType.UNIT, 1)).isEqualTo(0);
    }

}
