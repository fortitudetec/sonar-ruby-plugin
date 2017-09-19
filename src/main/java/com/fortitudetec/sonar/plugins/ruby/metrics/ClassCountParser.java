package com.fortitudetec.sonar.plugins.ruby.metrics;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class ClassCountParser {
    private static final Logger LOG = LoggerFactory.getLogger(ClassCountParser.class);

    private ClassCountParser() {}

    public static int countClasses(File file) {
        int numClasses = 0;
        LineIterator iterator = null;
        try {
            iterator = FileUtils.lineIterator(file);

            while (iterator.hasNext()) {
                String line = iterator.nextLine();
                if (StringUtils.contains(line.trim(), "class ")) {
                    numClasses++;
                }
            }
        } catch (IOException e) {
            LOG.error("Error determining class count for file " + file, e);
        } finally {
            LineIterator.closeQuietly(iterator);
        }

        return numClasses;
    }
}
