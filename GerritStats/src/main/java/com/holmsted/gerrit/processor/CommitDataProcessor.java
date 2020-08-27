package com.holmsted.gerrit.processor;

import com.holmsted.gerrit.CommitFilter;
import com.holmsted.gerrit.OutputSettings;
import com.holmsted.gerrit.QueryData;

import javax.annotation.Nonnull;

public abstract class CommitDataProcessor<T> {

    @Nonnull
    private final CommitFilter filter;
    @Nonnull
    private final OutputSettings outputSettings;

    public CommitDataProcessor(@Nonnull CommitFilter filter, @Nonnull OutputSettings outputSettings) {
        this.filter = filter;
        this.outputSettings = outputSettings;
    }

    /**
     * Processes the list of commits and builds output for it,
     * returning it as a string.
     */
    public void invoke(@Nonnull QueryData queryData) {
        OutputFormatter<T> formatter = createOutputFormatter();
        process(formatter, queryData);
    }

    protected abstract void process(@Nonnull OutputFormatter<T> formatter, @Nonnull QueryData queryData);

    @Nonnull
    protected abstract OutputFormatter<T> createOutputFormatter();

    @Nonnull
    protected CommitFilter getCommitFilter() {
        return filter;
    }

    @Nonnull
    protected OutputSettings getOutputSettings() {
        return outputSettings;
    }
}
