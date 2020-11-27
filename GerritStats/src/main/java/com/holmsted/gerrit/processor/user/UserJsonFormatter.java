package com.holmsted.gerrit.processor.user;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.holmsted.gerrit.Commit;
import com.holmsted.gerrit.Commit.Identity;
import com.holmsted.gerrit.OutputSettings;
import com.holmsted.gerrit.processor.OutputFormatter;
import com.holmsted.gerrit.processor.user.IdentityRecord.ReviewerData;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

@SuppressWarnings("PMD.ExcessiveImports")
public class UserJsonFormatter implements OutputFormatter<UserData> {
    private static final String ROOT_OUTPUT_DIR = "data/user/";

    private final File outputDir;
    private final Map<String, Identity> identities = new HashMap<>();


    public UserJsonFormatter(@Nonnull OutputSettings outputSettings) {
        outputDir = new File(outputSettings.getOutputDir(), ROOT_OUTPUT_DIR);
    }


    @Override
    public void format(@Nonnull UserData data) {
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOError(new IOException("Cannot create output directory " + outputDir.getAbsolutePath()));
        }

        IdentityRecordList orderedList = data.toOrderedList(new AlphabeticalOrderComparator());

        for (Identity identity : data.keySet()) {
            identities.put(identity.getIdentifier(), identity);
        }

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(PatchSetCommentTable.class, new PatchSetCommentTableSerializer())
                .registerTypeAdapter(ReviewerDataTable.class, new ReviewerDataTableSerializer())
                .registerTypeAdapterFactory(new IdentityRecordTypeAdapterFactory())
                .create();

        for (IdentityRecord record : orderedList) {
            // add any potentially missing ids to the table
            record.getMyReviewerList().stream().forEach(identity ->
                    identities.put(identity.getIdentifier(), identity));
            record.getReviewRequestorList().stream().forEach(identity ->
                    identities.put(identity.getIdentifier(), identity));

            String json = gson.toJson(record);

            OutputFormatter.writeFile(outputDir, record.getFilenameStem(), json);
        }

        System.out.println("User data written to: " + outputDir.getAbsolutePath());
    }


    private static class PatchSetCommentTableSerializer implements JsonSerializer<PatchSetCommentTable> {

        @Override
        public JsonElement serialize(PatchSetCommentTable table,
                                     Type typeOfSrc,
                                     JsonSerializationContext context) {
            JsonArray tableJson = new JsonArray();
            for (Commit key : table.keySet()) {
                JsonObject pair = new JsonObject();
                pair.add("commit", context.serialize(key));
                pair.add("commentsByUser", context.serialize(table.get(key)));
                tableJson.add(pair);
            }
            return tableJson;
        }
    }

    private static class ReviewerDataTableSerializer implements JsonSerializer<ReviewerDataTable> {
        @Override
        public JsonElement serialize(ReviewerDataTable table,
                                     Type typeOfSrc,
                                     JsonSerializationContext context) {
            JsonArray tableJson = new JsonArray();
            for (Map.Entry<Identity, ReviewerData> entry : table.entrySet()) {
                JsonObject pair = new JsonObject();
                pair.add("identity", context.serialize(entry.getKey()));
                pair.add("approvalData", context.serialize(entry.getValue()));
                tableJson.add(pair);
            }
            return tableJson;
        }
    }

    private static class IdentityRecordTypeAdapterFactory implements TypeAdapterFactory {

        @Override
        public <T> TypeAdapter<T> create(final Gson gson, TypeToken<T> type) {
            if (!IdentityRecord.class.isAssignableFrom(type.getRawType())) {
                return null;
            }

            final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
            final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
            return new TypeAdapter<T>() {
                @Override
                public void write(JsonWriter writer, T value) throws IOException {
                    JsonElement tree = delegate.toJsonTree(value);

                    IdentityRecord record = (IdentityRecord) value;
                    JsonObject object = (JsonObject) tree;
                    object.add("abandonedCommitCount", new JsonPrimitive(record.getAbandonedCommitCount()));
                    object.add("projects", gson.toJsonTree(record.getGerritProjects()));
                    object.add("selfReviewedCommitCount", gson.toJsonTree(record.getSelfReviewedCommits().size()));
                    object.add("inReviewCommitCount", new JsonPrimitive(record.getInReviewCommitCount()));

                    elementAdapter.write(writer, tree);
                }

                @Override
                public T read(JsonReader reader) throws IOException {
                    JsonElement tree = elementAdapter.read(reader);
                    return delegate.fromJsonTree(tree);
                }
            };
        }
    }
}
