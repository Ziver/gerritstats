package com.holmsted.gerrit.processor.message;

import com.holmsted.gerrit.data.Approval;
import com.holmsted.gerrit.data.Commit;
import com.holmsted.gerrit.CommitFilter;
import com.holmsted.gerrit.OutputSettings;
import com.holmsted.gerrit.QueryData;
import com.holmsted.gerrit.data.PatchSet;
import com.holmsted.gerrit.data.PatchSetComment;
import com.holmsted.gerrit.processor.CommitDataProcessor;
import com.holmsted.gerrit.processor.CommitVisitor;
import com.holmsted.gerrit.processor.OutputFormatter;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MessageDataProcessor extends CommitDataProcessor<MessageData> {
    final private String messageTag;
    final private Pattern messagePattern;


    public MessageDataProcessor(@Nonnull CommitFilter filter, @Nonnull String messageTag, @Nonnull OutputSettings outputSettings) {
        super(filter, outputSettings);

        this.messageTag = messageTag;
        this.messagePattern = Pattern.compile(".*" + messageTag + ".*");
    }

    @Override
    public MessageData process(@Nonnull QueryData queryData) {
        final MessageData records = new MessageData(messageTag);

        CommitVisitor visitor = new CommitVisitor(getCommitFilter()) {
            @Override
            public void visitCommit(@Nonnull Commit commit) {
                Matcher match = messagePattern.matcher(commit.commitMessage);

                if (match.find()) {
                    records.addCommit(commit);
                }
            }

            @Override
            public void visitPatchSet(@Nonnull Commit commit, @Nonnull PatchSet patchSet) {}

            @Override
            public void visitApproval(@Nonnull PatchSet patchSet, @Nonnull Approval approval) {}

            @Override
            public void visitPatchSetComment(@Nonnull Commit commit,
                                             @Nonnull PatchSet patchSet,
                                             @Nonnull PatchSetComment patchSetComment) {}
        };

        visitor.visit(queryData.getCommits());

        return records;
    }

    @Nonnull
    @Override
    public OutputFormatter<MessageData>[] createOutputFormatter() {
        return new OutputFormatter[] {
                new MessageJsonFormatter(getOutputSettings())
        };
    }
}
