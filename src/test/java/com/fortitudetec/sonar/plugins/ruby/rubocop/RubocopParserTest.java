package com.fortitudetec.sonar.plugins.ruby.rubocop;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import com.fortitudetec.sonar.plugins.ruby.model.RubocopIssue;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class RubocopParserTest {

    private SensorContextTester context;

    @Before
    public void setUp() throws URISyntaxException {
        context = SensorContextTester.create(Paths.get("."));
    }

    @Test
    public void testParseResultsFile() throws URISyntaxException, IOException {
        RubocopParser parser = new RubocopParser();

        URL resource = RubocopParser.class.getClassLoader().getResource("result.json");
        Map<String, List<RubocopIssue>> results = parser.parse(Files.toString(new File(resource.toURI()), Charsets.UTF_8));

        assertThat(results.size()).isEqualTo(14);
        assertThat(results.get("test/controllers/logging_controller_test.rb").size()).isEqualTo(2);
        assertThat(results.get("test/controllers/logging_controller_test.rb").get(0).getRuleName()).isEqualTo("Style/StringLiterals");
    }
}
