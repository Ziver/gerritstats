package com.holmsted.gerrit.data;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PatchSetComment {
    @Nullable
    public final String file;
    public final int line;
    @Nullable
    public final Identity reviewer;
    public final String message;

    // Note: there is no timestamp field in the json from Gerrit.
    // To have a better approximation on when a comment was written,
    // this field is set when initializing these objects.
    public final long patchSetTimestamp;


    public PatchSetComment(@Nullable String file,
                           int line,
                           @Nullable Identity reviewer,
                           @Nullable String message,
                           long patchSetTimestamp) {
        this.file = file;
        this.line = line;
        this.reviewer = reviewer;
        this.message = message;
        this.patchSetTimestamp = patchSetTimestamp;
    }


    @Nonnull
    public static PatchSetComment fromJson(JSONObject commentJson, long createdOnDate) {
        return new PatchSetComment(
                commentJson.optString("file"),
                commentJson.optInt("line"),
                Identity.fromJson(commentJson.getJSONObject("reviewer")),
                commentJson.optString("message"),
                createdOnDate
        );
    }

    @Nonnull
    public static List<PatchSetComment> fromJson(@Nullable JSONArray comments, long createdOnDate) {
        List<PatchSetComment> result = new ArrayList<>();
        if (comments != null) {
            for (int i = 0; i < comments.length(); ++i) {
                result.add(PatchSetComment.fromJson(comments.getJSONObject(i), createdOnDate));
            }
        }
        return result;
    }

    @Nullable
    public String getFile() {
        return file;
    }

    public int getLine() {
        return line;
    }

    @Nullable
    public Identity getReviewer() {
        return reviewer;
    }

    @Nullable
    public String getMessage() {
        return message;
    }
}
