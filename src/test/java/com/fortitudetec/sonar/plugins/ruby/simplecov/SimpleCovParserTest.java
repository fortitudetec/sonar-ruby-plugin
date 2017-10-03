package com.fortitudetec.sonar.plugins.ruby.simplecov;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.coverage.NewCoverage;
import org.sonar.api.batch.sensor.coverage.internal.DefaultCoverage;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleCovParserTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SensorContextTester context;
    private File rubyFile;

    @Before
    public void setUp() throws URISyntaxException {
        URL filePath = SimpleCovParserTest.class.getClassLoader().getResource("./test_controller.rb");
        rubyFile = new File(filePath.toURI());

        context = SensorContextTester.create(rubyFile.getParentFile());
        context.fileSystem().add(new DefaultInputFile("myProjectKey", "test_controller.rb")
            .setLanguage("rb")
            .setLines(15));

        buildResultSetFile();
    }

    @After
    public void tearDown() throws URISyntaxException {
        URL filePath = SimpleCovParserTest.class.getClassLoader().getResource(".resultset.json");
        File resultFile = new File(filePath.toURI());
        resultFile.delete();
    }

    @Test
    public void testParse() throws URISyntaxException, IOException {
        SimpleCovParser parser = new SimpleCovParser();

        URL filePath = SimpleCovParserTest.class.getClassLoader().getResource(".resultset.json");
        File resultFile = new File(filePath.toURI());

        Map<String, NewCoverage> coverage = parser.parse(context, resultFile);

        assertThat(coverage).isNotNull();

        DefaultCoverage defaultCoverage = (DefaultCoverage) coverage.get(rubyFile.getAbsolutePath());
        assertThat(defaultCoverage.coveredLines()).isEqualTo(4);
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
            IOUtils.write(MAPPER.writeValueAsString(results), fileOut);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
