package com.holmsted.gerrit.processor.file;

import com.holmsted.gerrit.data.*;
import com.holmsted.gerrit.CommitFilter;
import com.holmsted.gerrit.OutputSettings;
import com.holmsted.gerrit.QueryData;
import com.holmsted.gerrit.processor.CommitDataProcessor;
import com.holmsted.gerrit.processor.CommitVisitor;
import com.holmsted.gerrit.processor.OutputFormatter;

import javax.annotation.Nonnull;


public class FileDataProcessor extends CommitDataProcessor<FileData> {

    public FileDataProcessor(@Nonnull CommitFilter filter, @Nonnull OutputSettings outputSettings) {
        super(filter, outputSettings);
    }

    @Override
    public FileData process(@Nonnull QueryData queryData) {
        final FileData records = new FileData();

        CommitVisitor visitor = new CommitVisitor(getCommitFilter()) {
            @Override
            public void visitCommit(@Nonnull Commit commit) {}

            @Override
            public void visitPatchSet(@Nonnull Commit commit, @Nonnull PatchSet patchSet) {}

            @Override
            public void visitApproval(@Nonnull PatchSet patchSet, @Nonnull Approval approval) {}

            @Override
            public void visitPatchSetComment(@Nonnull Commit commit,
                                             @Nonnull PatchSet patchSet,
                                             @Nonnull PatchSetComment patchSetComment) {
                if (records.getCommits(patchSetComment.file) == null) {
                    records.addFile(patchSetComment.file);
                }

                FileRecord record = records.getCommits(patchSetComment.file);
                record.addCommit(commit);
            }
        };

        visitor.visit(queryData.getCommits());

        return records;
    }

    @Nonnull
    @Override
    public OutputFormatter<FileData>[] createOutputFormatter() {
        return new OutputFormatter[] {
                new FileJsonFormatter(getOutputSettings())
        };
    }
}
