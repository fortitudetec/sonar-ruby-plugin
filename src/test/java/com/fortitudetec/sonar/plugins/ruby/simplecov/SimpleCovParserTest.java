package com.fortitudetec.sonar.plugins.ruby.simplecov;

import com.fortitudetec.sonar.plugins.ruby.RubyPlugin;
import com.fortitudetec.sonar.plugins.ruby.utils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.coverage.NewCoverage;
import org.sonar.api.batch.sensor.coverage.internal.DefaultCoverage;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleCovParserTest {

    private SensorContextTester context;
    private File rubyFile;

    @Before
    public void setUp() throws URISyntaxException {
        URL filePath = SimpleCovParserTest.class.getClassLoader().getResource("./test_controller.rb");
        rubyFile = new File(filePath.toURI());

        context = SensorContextTester.create(rubyFile.getParentFile());
        context.settings().setProperty(RubyPlugin.TEST_FRAMEWORK, "RSpec");
        context.fileSystem().add(new DefaultInputFile("myProjectKey", "test_controller.rb")
            .setLanguage("rb")
            .setLines(15));

        TestUtils.buildResultSetFile(rubyFile);
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

}
