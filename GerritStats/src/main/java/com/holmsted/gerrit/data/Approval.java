package com.holmsted.gerrit.data;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Approval {
    public static final String TYPE_CODE_REVIEW = "Code-Review";
    public static final String TYPE_SUBMITTED = "SUBM";

    public final String type;
    public final String description;
    public final int value;
    public final long grantedOnDate;
    public final Identity grantedBy;


    public Approval(@Nullable String type,
                    @Nullable String description,
                    int value,
                    long grantedOnDate,
                    @Nullable Identity grantedBy) {
        this.type = type;
        this.description = description;
        this.value = value;
        this.grantedOnDate = grantedOnDate;
        this.grantedBy = grantedBy;
    }


    @Nonnull
    public static List<Approval> fromJson(@Nullable JSONArray approvals) {
        List<Approval> result = new ArrayList<>();
        if (approvals != null) {
            for (int i = 0; i < approvals.length(); ++i) {
                result.add(Approval.fromJson(approvals.getJSONObject(i)));
            }
        }
        return result;
    }

    public static Approval fromJson(JSONObject approvalJson) {
        return new Approval(
                approvalJson.optString("type"),
                approvalJson.optString("description"),
                approvalJson.optInt("value"),
                approvalJson.optLong("grantedOn") * Commit.SEC_TO_MSEC,
                Identity.fromJson(approvalJson.getJSONObject("by"))
        );
    }
}
