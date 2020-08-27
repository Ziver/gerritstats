package com.holmsted.gerrit;

import javax.annotation.Nonnull;

public class OutputSettings {

    @Nonnull
    private final CommandLineParser commandLine;

    OutputSettings(@Nonnull CommandLineParser commandLine) {
        this.commandLine = commandLine;
    }

    public String getOutputDir() {
        return commandLine.getOutputDir();
    }

    public boolean isAnonymizeDataEnabled() {
        return commandLine.isAnonymizeDataEnabled();
    }
}
