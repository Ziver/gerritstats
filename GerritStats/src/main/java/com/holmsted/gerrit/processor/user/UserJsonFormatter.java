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
import com.holmsted.file.FileWriter;
import com.holmsted.gerrit.Commit;
import com.holmsted.gerrit.Commit.Identity;
import com.holmsted.gerrit.OutputSettings;
import com.holmsted.gerrit.processor.OutputFormatter;
import com.holmsted.gerrit.processor.user.IdentityRecord.ReviewerData;
import com.holmsted.json.JsonFileBuilder;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

@SuppressWarnings("PMD.ExcessiveImports")
public class UserJsonFormatter implements OutputFormatter<UserData> {
    private static final String ROOT_OUTPUT_DIR = "data";
    private static final String USER_DATA_PATH = "users";

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
                .registerTypeAdapter(Identity.class, new IdentityMappingSerializer())
                .registerTypeAdapterFactory(new IdentityRecordTypeAdapterFactory())
                .create();

        for (IdentityRecord record : orderedList) {
            // add any potentially missing ids to the table
            record.getMyReviewerList().stream().forEach(identity ->
                    identities.put(identity.getIdentifier(), identity));
            record.getReviewRequestorList().stream().forEach(identity ->
                    identities.put(identity.getIdentifier(), identity));

            String json = gson.toJson(record);
            json = IdentityMappingSerializer.postprocess(json);

            writeUserdataJsonFile(record, json);
        }

        System.out.println("Output written to " + outputDir.getAbsolutePath());
    }

    /**
     * Writes a .js file with a json object for the given identity record.
     *
     * Ideally, pure .json files would be written,but it's not easily possible
     * to load json files locally from the pages without a web server to serve the
     * requests.
     *
     * See e.g. http://stackoverflow.com/questions/7346563/loading-local-json-file
     */
    private void writeUserdataJsonFile(@Nonnull IdentityRecord record, @Nonnull String json) {
        String filePath = USER_DATA_PATH + File.separator + record.getFilenameStem() + ".js";
        System.out.println("Creating " + filePath);

        StringWriter writer = new StringWriter();
        writer.write(String.format("userdata['%s'] = %s;",
                record.getFilenameStem(),
                json));

        FileWriter.writeFile(
                outputDir.getPath() + File.separator + filePath,
                writer.toString());
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

    /**
     * This hacky mapping reduces the .json file sizes by about 30%, by using a variable reference
     * for all identities.
     *
     * Because the writer methods in gson are final, it doesn't seem possible to
     * write e.g. variable references in the code, so any '__$$users[' strings are replaced
     * with a real variable reference in a postprocessing step.
     */
    private static class IdentityMappingSerializer implements JsonSerializer<Commit.Identity> {
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
