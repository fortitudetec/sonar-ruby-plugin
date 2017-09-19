package com.fortitudetec.sonar.plugins.ruby.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class ClassCountParserTest {

    @Test
    public void testCountClasses() throws URISyntaxException {
        URL resource = ClassCountParser.class.getClassLoader().getResource("test_controller.rb");
        int linesOfComment = ClassCountParser.countClasses(new File(resource.toURI()));
        assertThat(linesOfComment).isEqualTo(1);
    }
}
