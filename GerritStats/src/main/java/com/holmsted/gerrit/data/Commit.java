package com.holmsted.gerrit.data;

import com.google.common.collect.ImmutableList;
import com.holmsted.gerrit.GerritStatParser.ParserContext;

import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Commit {
    protected static final long SEC_TO_MSEC = 1000;

    public final String project;
    public final String branch;
    public final String id;
    public final int commitNumber;
    public final String subject;
    public final Identity owner;
    public final String url;
    public final String commitMessage;
    public final long createdOnDate;
    public final long lastUpdatedDate;
    public final boolean isOpen;
    public final String status;

    public final ImmutableList<Identity> reviewers;
    public final ImmutableList<ChangeComment> comments;
    public final ImmutableList<PatchSet> patchSets;

    public enum PatchSetKind {
        REWORK,
        TRIVIAL_REBASE,
        NO_CODE_CHANGE,
        NO_CHANGE
    }

    @SuppressWarnings("PMD")
    public Commit(@Nullable String project,
                  @Nullable String branch,
                  @Nullable String id,
                  int commitNumber,
                  @Nullable String subject,
                  @Nullable Identity owner,
                  @Nullable String url,
                  @Nullable String commitMessage,
                  long createdOnDate,
                  long lastUpdatedDate,
                  boolean isOpen,
                  @Nullable String status,
                  @Nonnull List<Identity> reviewers,
                  @Nonnull List<ChangeComment> comments,
                  @Nonnull List<PatchSet> patchSets) {
        this.project = project;
        this.branch = branch;
        this.id = id;
        this.commitNumber = commitNumber;
        this.subject = subject;
        this.owner = owner;
        this.url = url;
        this.commitMessage = commitMessage;
        this.createdOnDate = createdOnDate;
        this.lastUpdatedDate = lastUpdatedDate;
        this.isOpen = isOpen;
        this.status = status;
        this.reviewers = ImmutableList.copyOf(reviewers);
        this.comments = ImmutableList.copyOf(comments);
        this.patchSets = ImmutableList.copyOf(patchSets);
    }


    public static Commit fromJson(JSONObject commitJson, @Nonnull ParserContext context) {
        List<Identity> reviewers = Identity.fromJsonArray(commitJson.optJSONArray("allReviewers"));
        List<ChangeComment> changeComments = ChangeComment.fromJsonArray(commitJson.optJSONArray("comments"));
        List<PatchSet> patchSets = PatchSet.fromJsonArray(commitJson.optJSONArray("patchSets"), context);

        return new Commit(
                commitJson.optString("project"),
                commitJson.optString("branch"),
                commitJson.optString("id"),
                commitJson.optInt("number"),
                commitJson.optString("subject"),
                Identity.fromJson(commitJson.optJSONObject("owner")),
                commitJson.optString("url"),
                commitJson.optString("commitMessage"),
                commitJson.optLong("createdOn") * SEC_TO_MSEC,
                commitJson.optLong("lastUpdated") * SEC_TO_MSEC,
                commitJson.optBoolean("open"),
                commitJson.optString("status"),
                reviewers,
                changeComments,
                patchSets
        );
    }


    @Nonnull
    public PatchSet getPatchSetForComment(@Nonnull PatchSetComment patchSetComment) {
        for (PatchSet patchSet : patchSets) {
            if (patchSet.contains(patchSetComment)) {
                return patchSet;
            }
        }
        throw new IllegalArgumentException("Attempted to query for a comment not in the patch set!");
    }

    public int getPatchSetCountForKind(@Nonnull PatchSetKind kind) {
        int count = 0;
        for (PatchSet patchSet : patchSets) {
            if (patchSet.kind == kind) {
                ++count;
            }
        }
        return count;
    }

    public int getFirstPatchSetIndexWithNonAuthorReview() {
        for (int i = 0; i < patchSets.size(); ++i) {
            PatchSet patchSet = patchSets.get(i);
            for (PatchSetComment comment : patchSet.comments) {
                if (!Objects.equals(owner, comment.reviewer)) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    @Nonnull
    public ImmutableList<PatchSet> getPatchSets() {
        return patchSets;
    }

    @Nonnull
    public Date getCreatedOnDate() {
        return new Date(createdOnDate);
    }

    public static boolean isCommit(@Nonnull JSONObject lineJson) {
        return lineJson.opt("status") != null;
    }
}
