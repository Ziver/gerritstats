package com.holmsted.gerrit.data;

import com.google.common.collect.ImmutableList;
import com.holmsted.gerrit.GerritStatParser;
import com.holmsted.json.JsonUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PatchSet {
    public final int number;
    public final String revision;
    public final List<String> parents;
    public final String ref;
    public final Identity uploader;
    public final long createdOnDate;
    public final Identity author;
    public final boolean isDraft;
    public final Commit.PatchSetKind kind;
    public final List<Approval> approvals;
    public final List<PatchSetComment> comments;
    public final int sizeInsertions;
    public final int sizeDeletions;


    @SuppressWarnings("PMD")
    public PatchSet(int number,
                    @Nullable String revision,
                    @Nonnull List<String> parents,
                    @Nullable String ref,
                    @Nullable Identity uploader,
                    long createdOnDate,
                    @Nullable Identity author,
                    boolean isDraft,
                    @Nonnull Commit.PatchSetKind kind,
                    @Nonnull List<Approval> approvals,
                    @Nonnull List<PatchSetComment> comments,
                    int sizeInsertions,
                    int sizeDeletions) {
        this.number = number;
        this.revision = revision;
        this.parents = parents;
        this.ref = ref;
        this.uploader = uploader;
        this.createdOnDate = createdOnDate;
        this.author = author;
        this.isDraft = isDraft;
        this.kind = kind;
        this.approvals = ImmutableList.copyOf(approvals);
        this.comments = ImmutableList.copyOf(comments);
        this.sizeInsertions = sizeInsertions;
        this.sizeDeletions = sizeDeletions;
    }


    static List<PatchSet> fromJsonArray(@Nullable JSONArray patchSetsJson, @Nonnull GerritStatParser.ParserContext context) {
        List<PatchSet> result = new ArrayList<>();
        if (patchSetsJson != null) {
            for (int i = 0; i < patchSetsJson.length(); ++i) {
                JSONObject patchSetJson = patchSetsJson.getJSONObject(i);
                result.add(PatchSet.fromJson(patchSetJson, context));
            }
        }
        return result;
    }

    static PatchSet fromJson(@Nonnull JSONObject patchSetJson, @Nonnull GerritStatParser.ParserContext context) {
        Identity uploader = Identity.fromJson(patchSetJson.optJSONObject("uploader"));

        JSONObject authorJson = patchSetJson.optJSONObject("author");
        Identity author;
        if (authorJson != null) {
            author = Identity.fromJson(authorJson);
        } else {
            author = uploader;
        }

        String kindString = patchSetJson.optString("kind");
        Commit.PatchSetKind patchSetKind = Commit.PatchSetKind.REWORK;
        try {
            patchSetKind = Commit.PatchSetKind.valueOf(kindString);
        } catch (IllegalArgumentException e) {
            if (context.version.isAtLeast(2, 9)) {
                System.err.println("Unknown patch set kind '" + kindString + "'");
            } else {
                // the 'kind' field does not exist before Gerrit 2.9 or so.
                patchSetKind = Commit.PatchSetKind.REWORK;
            }
        }

        long createdOnDate = patchSetJson.optLong("createdOn") * Commit.SEC_TO_MSEC;

        return new PatchSet(
                patchSetJson.optInt("number"),
                patchSetJson.optString("revision"),
                JsonUtils.readStringArray(patchSetJson.optJSONArray("parents")),
                patchSetJson.optString("ref"),
                uploader,
                createdOnDate,
                author,
                patchSetJson.optBoolean("isDraft"),
                patchSetKind,
                Approval.fromJson(patchSetJson.optJSONArray("approvals")),
                PatchSetComment.fromJson(patchSetJson.optJSONArray("comments"), createdOnDate),
                patchSetJson.optInt("sizeInsertions"),
                patchSetJson.optInt("sizeDeletions")
        );
    }


    public int getNumber() {
        return number;
    }

    public Identity getAuthor() {
        return author;
    }

    public Date getCreatedOnDate() {
        return new Date(createdOnDate);
    }

    public List<PatchSetComment> getComments() {
        return comments;
    }

    public boolean contains(PatchSetComment patchSetComment) {
        return comments.contains(patchSetComment);
    }
}
