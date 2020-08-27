package com.holmsted.gerrit.processor.file;

import com.holmsted.gerrit.Commit;
import com.holmsted.gerrit.Commit.Identity;
import com.holmsted.gerrit.CommitFilter;
import com.holmsted.gerrit.OutputSettings;
import com.holmsted.gerrit.QueryData;
import com.holmsted.gerrit.processor.CommitDataProcessor;
import com.holmsted.gerrit.processor.CommitVisitor;
import com.holmsted.gerrit.processor.OutputFormatter;
import com.holmsted.gerrit.processor.user.IdentityRecord;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicLong;


public class FileDataProcessor extends CommitDataProcessor<FileData> {

    public FileDataProcessor(@Nonnull CommitFilter filter, @Nonnull OutputSettings outputSettings) {
        super(filter, outputSettings);
    }

    @Override
    public void process(@Nonnull OutputFormatter<FileData> formatter, @Nonnull QueryData queryData) {
        CommitVisitor visitor = new CommitVisitor(getCommitFilter()) {
            @Override
            public void visitCommit(@Nonnull Commit commit) {

            }

            @Override
            public void visitPatchSet(@Nonnull Commit commit, @Nonnull Commit.PatchSet patchSet) {

            }

            @Override
            public void visitApproval(@Nonnull Commit.PatchSet patchSet, @Nonnull Commit.Approval approval) {

            }

            @Override
            public void visitPatchSetComment(@Nonnull Commit commit,
                                             @Nonnull Commit.PatchSet patchSet,
                                             @Nonnull Commit.PatchSetComment patchSetComment) {

            }
        };
    }

    @Nonnull
    @Override
    protected OutputFormatter<FileData> createOutputFormatter() {
        return new FileJsonFormatter(getOutputSettings());
    }

}
