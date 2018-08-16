package com.fortitudetec.sonar.plugins.ruby.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void buildResultSetFile(File rubyFile) {
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
