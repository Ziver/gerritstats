package com.holmsted.gerrit.data.serializer;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.holmsted.gerrit.data.ChangeComment;
import com.holmsted.gerrit.data.PatchSet;

public class ReviewDataExclusionStrategy implements ExclusionStrategy {

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return f.getName().equals("reviewers") ||
                f.getName().equals("patchSets") ||
                f.getName().equals("comments");
    }

    @Override
    public boolean shouldSkipClass(Class<?> incomingClass) {
        return incomingClass == PatchSet.class ||
                incomingClass == ChangeComment.class;
    }
}