package com.fortitudetec.sonar.plugins.ruby.rubocop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

import com.fortitudetec.sonar.plugins.ruby.PathResolver;
import com.fortitudetec.sonar.plugins.ruby.Ruby;
import com.fortitudetec.sonar.plugins.ruby.RubyPlugin;
import com.fortitudetec.sonar.plugins.ruby.model.RubocopIssue;
import com.fortitudetec.sonar.plugins.ruby.model.RubocopPosition;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.utils.System2;
import org.sonar.api.utils.internal.JUnitTempFolder;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RubocopSensorTest {

    private SensorContextTester context;
    private File rubyFile;

    @Mock
    private RubocopExecutor executor;

    @Mock
    private RubocopParser parser;

    @Before
    public void setUp() throws URISyntaxException {
        MockitoAnnotations.initMocks(this);

        URL filePath = RubocopSensorTest.class.getClassLoader().getResource("./test_controller.rb");
        rubyFile = new File(filePath.toURI());

        context = SensorContextTester.create(rubyFile.getParentFile());
        context.fileSystem().add(new DefaultInputFile("myProjectKey", "test_controller.rb")
            .setLanguage("rb")
            .setOriginalLineOffsets(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15})
            .setLines(15));
        context.fileSystem().add(new DefaultInputFile("myProjectKey", "unknown_file.rb")
            .setLanguage("rb")
            .setOriginalLineOffsets(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
            .setLines(10));
    }

    @Test
    public void testDescribe() {
        RubocopExecutor executor = new RubocopExecutor(System2.INSTANCE, new JUnitTempFolder());

        RubocopSensor sensor = new RubocopSensor(new PathResolver(), executor, new RubocopParser());

        DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
        sensor.describe(descriptor);

        assertThat(descriptor.name()).isEqualTo("Linting sensor for Ruby files");
        assertThat(descriptor.languages()).containsExactly(Ruby.LANGUAGE_KEY);
    }

    @Test
    public void testExecute_SkippingRubocop() {
        RubocopSensor sensor = new RubocopSensor(new PathResolver(), executor, parser);
        sensor.execute(context);

        assertThat(context.allIssues()).isEmpty();
    }

    @Test
    public void testExecute() throws URISyntaxException {
        String reportPaths = "rubocop-result.json";
        when(executor.execute(isA(RubocopExecutorConfig.class), any())).thenReturn(reportPaths);
        when(parser.parse(reportPaths)).thenReturn(buildIssues());

        URL filePath = RubocopSensorTest.class.getClassLoader().getResource("./result.json");
        File resultFile = new File(filePath.toURI());
        context.settings().setProperty(RubyPlugin.RUBOCOP_REPORT_PATH, resultFile.getAbsolutePath());

        RubocopSensor sensor = new RubocopSensor(new PathResolver(), executor, parser);
        sensor.execute(context);

        assertThat(context.allIssues().size()).isEqualTo(1);
    }

    private Map<String, List<RubocopIssue>> buildIssues() {
        Map<String, List<RubocopIssue>> issues = new HashMap<>();

        issues.put(rubyFile.getAbsolutePath(), Lists.newArrayList(
            RubocopIssue.builder()
                .failure("Foo")
                .position(RubocopPosition.builder()
                    .line(1)
                    .character(1)
                    .build())
                .ruleName("Layout/AlignArray")
                .build()
        ));

        return issues;
    }
}
