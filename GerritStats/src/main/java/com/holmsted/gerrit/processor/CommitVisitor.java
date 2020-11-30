package com.holmsted.gerrit.processor;

import com.holmsted.gerrit.data.*;
import com.holmsted.gerrit.CommitFilter;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class CommitVisitor {

    @Nonnull
    private final CommitFilter filter;

    public CommitVisitor(@Nonnull CommitFilter filter) {
        this.filter = filter;
    }

    public void visit(@Nonnull List<Commit> commits) {
        for (Commit commit : commits) {
            if (!isIncluded(commit) || !isIncluded(commit.owner)) {
                continue;
            }
            visitCommit(commit);

            for (PatchSet patchSet: commit.patchSets) {
                if (!isIncluded(patchSet.author)) {
                    continue;
                }
                visitPatchSet(commit, patchSet);

                for (Approval approval : patchSet.approvals) {
                    if (!isIncluded(approval.grantedBy)) {
                        continue;
                    }
                    visitApproval(patchSet, approval);
                }

                for (PatchSetComment patchSetComment : patchSet.comments) {
                    if (!isIncluded(patchSetComment.reviewer)) {
                        continue;
                    }
                    visitPatchSetComment(commit, patchSet, patchSetComment);
                }
            }
        }
    }

    private boolean isIncluded(@Nonnull Commit commit) {
        return filter.isIncluded(commit);
    }

    private boolean isIncluded(@Nullable Identity identity) {
        return filter.isIncluded(identity);
    }

    public abstract void visitCommit(@Nonnull Commit commit);

    public abstract void visitPatchSet(@Nonnull Commit commit, @Nonnull PatchSet patchSet);

    public abstract void visitApproval(@Nonnull PatchSet patchSet, @Nonnull Approval approval);

    public abstract void visitPatchSetComment(@Nonnull Commit commit,
                                              @Nonnull PatchSet patchSet,
                                              @Nonnull PatchSetComment patchSetComment);
}
