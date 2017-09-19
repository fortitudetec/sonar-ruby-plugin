package com.fortitudetec.sonar.plugins.ruby.rubocop;

import org.sonar.api.batch.BatchSide;

import java.util.List;

@BatchSide
@FunctionalInterface
public interface RubocopExecutor {
    List<String> execute(RubocopExecutorConfig config, List<String> files);
}
