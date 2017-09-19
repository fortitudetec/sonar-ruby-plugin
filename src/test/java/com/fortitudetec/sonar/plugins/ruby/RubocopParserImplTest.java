package com.fortitudetec.sonar.plugins.ruby;

import static com.google.common.collect.Lists.newArrayList;

import com.fortitudetec.sonar.plugins.ruby.rubocop.RubocopParserImpl;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class RubocopParserImplTest {

    @Test
    public void testParseResultsFile() throws URISyntaxException, IOException {
        RubocopParserImpl parser = new RubocopParserImpl();
        URL resource = RubocopParserImpl.class.getClassLoader().getResource("result.json");
        parser.parse(newArrayList(Files.toString(new File(resource.toURI()), Charsets.UTF_8)));
    }
}
