package com.holmsted.gerrit.data;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ChangeComment {
    public final long timestamp;
    @Nullable
    public final Identity reviewer;
    @Nullable
    public final String message;


    public ChangeComment(long timestamp, @Nullable Identity reviewer, @Nullable String message) {
        this.timestamp = timestamp;
        this.reviewer = reviewer;
        this.message = message;
    }


    static ChangeComment fromJson(JSONObject commentJson) {
        return new ChangeComment(
                commentJson.optLong("timestamp") * Commit.SEC_TO_MSEC,
                Identity.fromJson(commentJson.optJSONObject("reviewer")),
                commentJson.optString("message"));
    }

    static List<ChangeComment> fromJsonArray(@Nullable JSONArray comments) {
        List<ChangeComment> result = new ArrayList<>();
        if (comments != null) {
            for (int i = 0; i < comments.length(); ++i) {
                JSONObject commentJson = comments.getJSONObject(i);
                result.add(ChangeComment.fromJson(commentJson));
            }
        }
        return result;
    }
}
