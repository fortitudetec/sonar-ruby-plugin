package com.fortitudetec.sonar.plugins.ruby.simplecov;

import static org.assertj.core.api.Assertions.assertThat;

import com.fortitudetec.sonar.plugins.ruby.RubyPlugin;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.coverage.CoverageType;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleCovSensorTest {

    private SensorContextTester context;
    private File rubyFile;

    @Before
    public void setUp() throws URISyntaxException {
        URL filePath = SimpleCovSensorTest.class.getClassLoader().getResource("./test_controller.rb");
        rubyFile = new File(filePath.toURI());

        context = SensorContextTester.create(rubyFile.getParentFile());
        context.fileSystem().add(new DefaultInputFile("myProjectKey", "test_controller.rb")
            .setLanguage("rb")
            .setLines(15));
        context.fileSystem().add(new DefaultInputFile("myProjectKey", "unknown_file.rb")
            .setLanguage("rb")
            .setLines(10));

        buildResultSetFile();
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

    private void buildResultSetFile() {
        Map<String, Object> results = new HashMap<>();

        Map<String, Object> coverage = Maps.newHashMap();
        List<Integer> lines = Lists.newArrayList(1, null, null, 1, null, 1, null, null, null, null, null, null, null, null, 1);
        coverage.put(rubyFile.getAbsolutePath(), lines);

        Map<String, Object> testType = Maps.newHashMap();
        testType.put("coverage", coverage);
        testType.put("timestamp", 1505253204);

        results.put("RSpec", testType);

        try (FileOutputStream fileOut = new FileOutputStream(rubyFile.getParent() + File.separatorChar + ".resultset.json")) {
            Gson gson = new Gson();

            IOUtils.write(gson.toJson(results), fileOut);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
