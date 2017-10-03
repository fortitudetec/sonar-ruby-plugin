package com.fortitudetec.sonar.plugins.ruby.rubocop;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.fortitudetec.sonar.plugins.ruby.PathResolver;
import com.fortitudetec.sonar.plugins.ruby.RubyPlugin;
import lombok.Getter;
import lombok.Setter;
import org.sonar.api.batch.sensor.SensorContext;

@Getter
@Setter
public class RubocopExecutorConfig {

    private String pathToRubocopOutput;
    private String pathToRubocop;
    private String pathToRubocopFiles;
    private String configFile;
    private Integer timeoutMs;

    public static RubocopExecutorConfig fromSettings(SensorContext ctx, PathResolver resolver) {
        RubocopExecutorConfig toReturn = new RubocopExecutorConfig();

        toReturn.setPathToRubocop(resolver.getPath(ctx, RubyPlugin.RUBOCOP_BIN, "rubocop"));
        toReturn.setPathToRubocopFiles(resolver.getPath(ctx, RubyPlugin.RUBOCOP_FILES_PATH, "."));
        toReturn.setConfigFile(resolver.getPath(ctx, RubyPlugin.RUBOCOP_CONFIG, ""));
        toReturn.setPathToRubocopOutput(resolver.getPath(ctx, RubyPlugin.RUBOCOP_REPORT_PATH, "rubocop-result.json"));

        toReturn.setTimeoutMs(6000);

        return toReturn;
    }

    public Boolean useExistingRubocopOutput() {
        return isNotBlank(pathToRubocopOutput);
    }
}
