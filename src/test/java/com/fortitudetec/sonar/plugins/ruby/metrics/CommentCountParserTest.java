package com.fortitudetec.sonar.plugins.ruby.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class CommentCountParserTest {

    @Test
    public void testCountLinesOfComments() throws URISyntaxException {
        URL resource = CommentCountParserTest.class.getClassLoader().getResource("test_controller.rb");
        int linesOfComment = CommentCountParser.countLinesOfComment(new File(resource.toURI()));
        assertThat(linesOfComment).isEqualTo(11);
    }
}
