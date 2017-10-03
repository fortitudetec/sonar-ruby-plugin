package com.fortitudetec.sonar.plugins.ruby.rubocop;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.utils.System2;
import org.sonar.api.utils.TempFolder;
import org.sonar.api.utils.command.Command;
import org.sonar.api.utils.command.CommandExecutor;
import org.sonar.api.utils.command.StreamConsumer;
import org.sonar.api.utils.command.StringStreamConsumer;

import java.io.File;
import java.io.IOException;
import java.util.List;

@BatchSide
public class RubocopExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(RubocopExecutor.class);

    private boolean mustQuoteSpaceContainingPaths;
    private TempFolder tempFolder;

    public RubocopExecutor(System2 system, TempFolder tempFolder) {
        this.mustQuoteSpaceContainingPaths = system.isOsWindows();
        this.tempFolder = tempFolder;
    }

    public String execute(RubocopExecutorConfig config, List<String> files) {
        checkNotNull(config, "RubcopExecutorConfig must not be null");
        checkNotNull(files, "List of files must not be null");

        if (config.useExistingRubocopOutput()) {
            LOG.debug("Running with existing JSON file '{}' instead of calling rubocop", config.getPathToRubocopOutput());
            return getFileContent(new File(config.getPathToRubocopOutput()));
        }

        File rubocopOutputFile = tempFolder.newFile();
        String rubocopOutputFilePath = rubocopOutputFile.getAbsolutePath();
        Command baseCommand = getBaseCommand(config, rubocopOutputFilePath);

        StringStreamConsumer stdOutConsumer = new StringStreamConsumer();
        StringStreamConsumer stdErrConsumer = new StringStreamConsumer();
        return getCommandOutput(baseCommand, stdOutConsumer, stdErrConsumer, rubocopOutputFile, config.getTimeoutMs());
    }

    private String getFileContent(File rubocopOutputFile) {
        try {
            return Files.toString(rubocopOutputFile, Charsets.UTF_8);
        } catch (IOException e) {
            LOG.error("Failed to read Rubocop output", e);
        }

        return "";
    }

    private Command getBaseCommand(RubocopExecutorConfig config, String tempPath) {
        Command command =
            Command
                .create(config.getPathToRubocop())
                .addArgument(preparePath(config.getPathToRubocop()))
                .addArgument("--format")
                .addArgument("json");

        if (StringUtils.isNotBlank(tempPath)) {
            command
                .addArgument("--out")
                .addArgument(preparePath(tempPath));
        }

        command
            .addArgument("--config")
            .addArgument(preparePath(config.getConfigFile()));

        command.setNewShell(false);

        return command;
    }

    private String preparePath(String path) {
        if (path == null) {
            return "";
        } else if (path.contains(" ") && mustQuoteSpaceContainingPaths) {
            return '"' + path + '"';
        } else {
            return path;
        }
    }

    private String getCommandOutput(Command thisCommand, StreamConsumer stdOutConsumer, StreamConsumer stdErrConsumer, File rubocopOutputFile, Integer timeoutMs) {
        createExecutor().execute(thisCommand, stdOutConsumer, stdErrConsumer, timeoutMs);
        return getFileContent(rubocopOutputFile);
    }

    protected CommandExecutor createExecutor() {
        return CommandExecutor.create();
    }
}
