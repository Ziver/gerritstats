package com.holmsted.gerrit.processor.user;

import com.holmsted.gerrit.Commit;
import com.holmsted.gerrit.Commit.Identity;
import com.holmsted.gerrit.CommitFilter;
import com.holmsted.gerrit.OutputSettings;
import com.holmsted.gerrit.QueryData;
import com.holmsted.gerrit.processor.CommitDataProcessor;
import com.holmsted.gerrit.processor.CommitVisitor;
import com.holmsted.gerrit.processor.OutputFormatter;

import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;

public class UserDataProcessor extends CommitDataProcessor<UserData> {

    private final UserData records = new UserData();

    public UserDataProcessor(@Nonnull CommitFilter filter, @Nonnull OutputSettings outputSettings) {
        super(filter, outputSettings);
    }

    @Override
    public void process(@Nonnull OutputFormatter<UserData> formatter, @Nonnull QueryData queryData) {
        records.clear();
        final AtomicLong fromDate = new AtomicLong(Long.MAX_VALUE);
        final AtomicLong toDate = new AtomicLong(Long.MIN_VALUE);

        CommitVisitor visitor = new CommitVisitor(getCommitFilter()) {
            @Override
            public void visitCommit(@Nonnull Commit commit) {
                IdentityRecord ownerRecord = getOrCreateRecord(commit.owner);
                ownerRecord.addCommit(commit);

                if (commit.lastUpdatedDate > toDate.get()) {
                    toDate.set(commit.lastUpdatedDate);
                }
                if (commit.lastUpdatedDate < fromDate.get()) {
                    fromDate.set(commit.lastUpdatedDate);
                }

                for (Commit.Identity identity : commit.reviewers) {
                    if (!getCommitFilter().isIncluded(identity)) {
                        continue;
                    }
                    if (!ownerRecord.identity.equals(identity)) {
                        ownerRecord.addReviewerForOwnCommit(identity);
                    }

                    IdentityRecord reviewerRecord = getOrCreateRecord(identity);
                    if (!commit.owner.equals(reviewerRecord.identity)) {
                        reviewerRecord.addReviewedCommit(commit);
                    }
                }
            }

            @Override
            public void visitPatchSet(@Nonnull Commit commit, @Nonnull Commit.PatchSet patchSet) {
                IdentityRecord ownerRecord = getOrCreateRecord(commit.owner);
                for (Commit.Approval approval : patchSet.approvals) {
                    if (approval.type == null) {
                        continue;
                    }
                    switch (approval.type) {
                        case Commit.Approval.TYPE_CODE_REVIEW: {
                            if (getCommitFilter().isIncluded(approval.grantedBy)
                                    && !ownerRecord.identity.equals(approval.grantedBy)) {
                                ownerRecord.addApprovalForOwnCommit(approval.grantedBy, approval);
                            }
                            break;
                        }
                        case Commit.Approval.TYPE_SUBMITTED: {
                            ownerRecord.updateAverageTimeInCodeReview(approval.grantedOnDate - commit.createdOnDate);
                            break;
                        }
                        default:
                            break;
                    }
                }
            }

            @Override
            public void visitApproval(@Nonnull Commit.PatchSet patchSet, @Nonnull Commit.Approval approval) {
                Identity grantedBy = approval.grantedBy;
                Identity patchSetAuthor = patchSet.author;
                if (grantedBy == null || patchSetAuthor == null) {
                    return;
                }

                if (!grantedBy.equals(patchSetAuthor)) {
                    IdentityRecord record = getOrCreateRecord(grantedBy);
                    record.addApprovalByThisIdentity(patchSetAuthor, approval);
                }
            }

            @Override
            public void visitPatchSetComment(@Nonnull Commit commit,
                                             @Nonnull Commit.PatchSet patchSet,
                                             @Nonnull Commit.PatchSetComment patchSetComment) {
                IdentityRecord reviewerRecord = getOrCreateRecord(patchSetComment.reviewer);
                if (!patchSet.author.equals(patchSetComment.reviewer)) {
                    reviewerRecord.addWrittenComment(commit, patchSet, patchSetComment);
                }

                IdentityRecord authorRecord = getOrCreateRecord(patchSet.author);
                if (!patchSet.author.equals(patchSetComment.reviewer)) {
                    authorRecord.addReceivedComment(commit, patchSet, patchSetComment);
                }
            }
        };
        visitor.visit(queryData.getCommits());
        records.setQueryData(queryData);
        records.setFromDate(fromDate.get());
        records.setToDate(toDate.get());

        formatter.format(records);
    }

    @Nonnull
    @Override
    protected OutputFormatter<UserData> createOutputFormatter() {
        return new UserJsonFormatter(getOutputSettings());
    }

    @Nonnull
    private IdentityRecord getOrCreateRecord(@Nonnull Commit.Identity identity) {
        IdentityRecord identityRecord = records.get(identity);
        if (identityRecord == null) {
            identityRecord = new IdentityRecord(identity);
            records.put(identity, identityRecord);
        }
        return identityRecord;
    }
}
