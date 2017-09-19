package com.fortitudetec.sonar.plugins.ruby.rubocop;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.utils.System2;
import org.sonar.api.utils.TempFolder;
import org.sonar.api.utils.command.Command;
import org.sonar.api.utils.command.CommandExecutor;
import org.sonar.api.utils.command.StreamConsumer;
import org.sonar.api.utils.command.StringStreamConsumer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RubocopExecutorImpl implements RubocopExecutor {

    public static final int MAX_COMMAND_LENGTH = 4096;

    private static final Logger LOG = LoggerFactory.getLogger(RubocopExecutorImpl.class);

    private boolean mustQuoteSpaceContainingPaths;
    private TempFolder tempFolder;

    public RubocopExecutorImpl(System2 system, TempFolder tempFolder) {
        this.mustQuoteSpaceContainingPaths = system.isOsWindows();
        this.tempFolder = tempFolder;
    }

    @Override
    public List<String> execute(RubocopExecutorConfig config, List<String> files) {
        checkNotNull(config, "RubcopExecutorConfig must not be null");
        checkNotNull(files, "List of files must not be null");

        if (config.useExistingRubocopOutput()) {
            LOG.debug("Running with existing JSON file '{}' instead of calling rubocop", config.getPathToRubocopOutput());
            return newArrayList(getFileContent(new File(config.getPathToRubocopOutput())));
        }

        File rubocopOutputFile = tempFolder.newFile();
        String rubocopOutputFilePath = rubocopOutputFile.getAbsolutePath();
        Command baseCommand = getBaseCommand(config, rubocopOutputFilePath);

        LOG.debug("Using a temporary path for Rubocop output: {}", rubocopOutputFilePath);

        StringStreamConsumer stdOutConsumer = new StringStreamConsumer();
        StringStreamConsumer stdErrConsumer = new StringStreamConsumer();

        List<String> toReturn = new ArrayList<>();

        if (config.useRubocopConfigInsteadOfFileList()) {
            LOG.debug("Rubocop config file will specify the files to check");
            toReturn.add(getCommandOutput(baseCommand, stdOutConsumer, stdErrConsumer, rubocopOutputFile, config.getTimeoutMs()));
        } else {
            int baseCommandLength = baseCommand.toCommandLine().length();
            int availableForBatching = MAX_COMMAND_LENGTH - baseCommandLength;

            List<List<String>> batches = new ArrayList<>();
            List<String> currentBatch = new ArrayList<>();
            batches.add(currentBatch);

            int currentBatchLength = 0;
            for (int i = 0; i < files.size(); i++) {
                String nextPath = this.preparePath(files.get(i).trim());

                // +1 for the space we'll be adding between filenames
                if (currentBatchLength + nextPath.length() + 1 > availableForBatching) {
                    // Too long to add to this batch, create new
                    currentBatch = new ArrayList<>();
                    currentBatchLength = 0;
                    batches.add(currentBatch);
                }

                currentBatch.add(nextPath);
                currentBatchLength += nextPath.length() + 1;
            }

            LOG.debug("Split {} files into {} batches for processing", files.size(), batches.size());

            for (int i = 0; i < batches.size(); i++) {
                List<String> thisBatch = batches.get(i);

                Command thisCommand = getBaseCommand(config, rubocopOutputFilePath);

                for (int fileIndex = 0; fileIndex < thisBatch.size(); fileIndex++) {
                    thisCommand.addArgument(thisBatch.get(fileIndex));
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Executing Rubocop with command: {}", thisCommand.toCommandLine());
                }

                // Timeout is specified per file, not per batch (which can vary a lot)
                // so multiply it up
                toReturn.add(this.getCommandOutput(thisCommand, stdOutConsumer, stdErrConsumer, rubocopOutputFile, config.getTimeoutMs() * thisBatch.size()));
            }
        }

        return null;
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
