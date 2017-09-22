package com.fortitudetec.sonar.plugins.ruby;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

import java.io.File;
import java.net.URL;

public class PathResolverTest {
    private PathResolver resolver;
    private SensorContextTester sensorContext;

    private File existingFile;

    @Before
    public void setUp() throws Exception {
        URL filePath = PathResolverTest.class.getClassLoader().getResource("./test_controller.rb");
        existingFile = new File(filePath.toURI());
        String parentPath = existingFile.getParent();

        this.sensorContext = SensorContextTester.create(new File(parentPath));
        this.sensorContext.settings().setProperty("path key", "test_controller.rb");

        DefaultInputFile file =
            new DefaultInputFile("", "test_controller.rb")
                .setLanguage(Ruby.LANGUAGE_KEY);

        this.sensorContext.fileSystem().add(file);

        this.resolver = new PathResolver();
    }

    @Test
    public void returnsAbsolutePathToFile_ifSpecifiedAndExists() {
        String result = this.resolver.getPath(this.sensorContext, "path key", "not me");
        assertSamePath(this.existingFile, result);
    }

    @Test
    public void returnsAbsolutePathToFallbackFile_ifPrimaryNotConfiguredAndFallbackExists() {
        String result = this.resolver.getPath(this.sensorContext, "new path key", "test_controller.rb");
        assertSamePath(this.existingFile, result);
    }

    @Test
    public void returnsAbsolutePathToFallbackFile_ifPrimaryNotConfiguredButEmptyAndFallbackExists() {
        this.sensorContext.settings().setProperty("new path key",  "");
        String result = this.resolver.getPath(this.sensorContext, "new path key", "test_controller.rb");
        assertSamePath(this.existingFile, result);
    }

    @Test
    public void returnsNull_ifPrimaryNotConfiguredAndFallbackNull() {
        String result = this.resolver.getPath(this.sensorContext, "new path key", null);
        assertThat(result).isNull();
    }

    @Test
    public void returnsNull_ifRequestedPathDoesNotExist() {
        this.sensorContext.settings().setProperty("new path key",  "missing.ts");
        String result = this.resolver.getPath(this.sensorContext, "new path key", "test_controller.rb");
        assertThat(result).isNull();
    }

    @Test
    public void returnsAbsolutePathToFile_ifAlreadyAbsoluteAndExists() {
        this.sensorContext.settings().setProperty("new path key", this.existingFile.getAbsolutePath());
        String result = this.resolver.getPath(this.sensorContext, "new path key", "not me");
        assertSamePath(this.existingFile, result);
    }

    private void assertSamePath(File existingFile, String argument) {
        if (argument == null) {
            assertThat(existingFile).isNull();
        } else {
            assertThat(existingFile).isEqualTo(new File(argument));
        }
    }
}
