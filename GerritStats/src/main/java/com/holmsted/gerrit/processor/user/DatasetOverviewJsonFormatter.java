package com.holmsted.gerrit.processor.user;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.holmsted.file.FileWriter;
import com.holmsted.gerrit.Commit;
import com.holmsted.gerrit.Commit.Identity;
import com.holmsted.gerrit.OutputSettings;
import com.holmsted.gerrit.processor.OutputFormatter;
import com.holmsted.gerrit.processor.user.IdentityRecord.ReviewerData;
import com.holmsted.json.JsonFileBuilder;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.*;

@SuppressWarnings("PMD.ExcessiveImports")
public class DatasetOverviewJsonFormatter implements OutputFormatter<UserData> {
    private static final String ROOT_OUTPUT_DIR = "data";
    private static final String USER_DATA_PATH = "users";

    private final File outputDir;


    public DatasetOverviewJsonFormatter(@Nonnull OutputSettings outputSettings) {
        outputDir = new File(outputSettings.getOutputDir(), ROOT_OUTPUT_DIR);
    }


    @Override
    public void format(@Nonnull UserData data) {
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOError(new IOException("Cannot create output directory " + outputDir.getAbsolutePath()));
        }

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Identity.class, new IdentityMappingSerializer())
                .registerTypeAdapter(IdentityRecord.class, new IdentityRecordOverviewSerializer())
                .create();

        JsonObject datasetOverview = new JsonObject();
        datasetOverview.add("projectName", gson.toJsonTree(data.getQueryData().getDisplayableProjectName()));
        datasetOverview.add("filenames", gson.toJsonTree(data.getQueryData().getFilenames()));
        datasetOverview.add("branchList", gson.toJsonTree(data.getQueryData().getBranches()));
        datasetOverview.add("fromDate", gson.toJsonTree(data.getFromDate()));
        datasetOverview.add("toDate", gson.toJsonTree(data.getToDate()));
        datasetOverview.add("generatedDate", gson.toJsonTree(new Date().getTime()));
        datasetOverview.add("hashCode", gson.toJsonTree(data.getQueryData().getDatasetKey()));
        datasetOverview.add("gerritVersion", gson.toJsonTree(data.getQueryData().getMinGerritVersion()));

        new JsonFileBuilder(outputDir)
                .setOutputFilename("datasetOverview.js")
                .setMemberName("datasetOverview")
                .setSerializedJs(gson.toJson(datasetOverview))
                .build();

        System.out.println("Output written to " + outputDir.getAbsolutePath());
    }

    private static class IdentityRecordOverviewSerializer implements JsonSerializer<IdentityRecord> {

        @Override
        public JsonElement serialize(IdentityRecord identityRecord,
                                     Type typeOfSrc,
                                     JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.add("identifier", context.serialize(identityRecord.getFilenameStem()));
            json.add("identity", context.serialize(identityRecord.identity));
            json.add("reviewCountPlus2", context.serialize(identityRecord.getReviewCountPlus2()));
            json.add("reviewCountPlus1", context.serialize(identityRecord.getReviewCountPlus1()));
            json.add("reviewCountMinus1", context.serialize(identityRecord.getReviewCountMinus1()));
            json.add("reviewCountMinus2", context.serialize(identityRecord.getReviewCountMinus2()));
            json.add("allCommentsWritten", context.serialize(identityRecord.getAllCommentsWritten().size()));
            json.add("allCommentsReceived", context.serialize(identityRecord.getAllCommentsReceived().size()));
            json.add("commitCount", context.serialize(identityRecord.getCommits().size()));
            json.add("averageTimeInCodeReview", context.serialize(identityRecord.getAverageTimeInCodeReview()));
            json.add("receivedCommentRatio", context.serialize(identityRecord.getReceivedCommentRatio()));
            json.add("reviewCommentRatio", context.serialize(identityRecord.getReviewCommentRatio()));
            json.add("addedAsReviewerToCount", context.serialize(identityRecord.addedAsReviewerTo.size()));
            json.add("selfReviewedCommitCount", context.serialize(identityRecord.getSelfReviewedCommits().size()));
            json.add("abandonedCommitCount", context.serialize(identityRecord.getAbandonedCommitCount()));
            json.add("firstActiveDate", context.serialize(identityRecord.firstActiveDate));
            json.add("lastActiveDate", context.serialize(identityRecord.lastActiveDate));

            List<JsonObject> reviewerList = new ArrayList<>();
            for (Identity reviewer : identityRecord.getMyReviewerList()) {
                JsonObject reviewerRecord = new JsonObject();
                ReviewerData reviewerData = identityRecord.getReviewerDataForOwnCommitFor(reviewer);
                reviewerRecord.add("identity", context.serialize(reviewer));
                reviewerRecord.add("reviewData", context.serialize(reviewerData));
                reviewerList.add(reviewerRecord);
            }
            json.add("myReviewerList", context.serialize(reviewerList));
            return json;
        }
    }

    /**
     * This hacky mapping reduces the .json file sizes by about 30%, by using a variable reference
     * for all identities.
     *
     * Because the writer methods in gson are final, it doesn't seem possible to
     * write e.g. variable references in the code, so any '__$$users[' strings are replaced
     * with a real variable reference in a postprocessing step.
     */
    private static class IdentityMappingSerializer implements JsonSerializer<Identity> {
        @Override
        public JsonElement serialize(Identity identity, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive("__$$ids[" + identity.getIdentifier() + "]");
        }

        /**
         * Processes the passed json string so that all found __$$users[] instances are replaced
         * with actual array references.
         */
        public static String postprocess(String serializedJson) {
            return serializedJson.replaceAll("\"__\\$\\$ids\\[(.+)\\]\"", "ids[\"$1\"]");
        }
    }
}
