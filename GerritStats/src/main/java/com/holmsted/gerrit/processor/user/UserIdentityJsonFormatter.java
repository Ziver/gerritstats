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
public class UserIdentityJsonFormatter implements OutputFormatter<UserData> {
    private static final String ROOT_OUTPUT_DIR = "data";

    private final File outputDir;
    private final Map<String, Identity> identities = new HashMap<>();


    public UserIdentityJsonFormatter(@Nonnull OutputSettings outputSettings) {
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
                .registerTypeAdapter(Identity.class, new IdentitySerializer())
                .create();

        new JsonFileBuilder(outputDir)
                .setOutputFilename("ids.js")
                .setMemberName("ids")
                .setSerializedJs(gson.toJson(identities))
                .build();

        System.out.println("Output written to " + outputDir.getAbsolutePath());
    }


    private class IdentitySerializer implements JsonSerializer<Identity> {
        @Override
        public JsonElement serialize(Identity identity, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            String identifier = identity.getIdentifier();
            json.add("identifier", context.serialize(identifier));
            json.add("name", context.serialize(identity.name));
            json.add("email", context.serialize(identity.email));
            json.add("username", context.serialize(identity.username));

            // There can be some identities in the reviewer data that are not in the per-person data.
            // To make sure all the references to users map work, add them here.
            if (!identities.containsKey(identifier)) {
                identities.put(identifier, identity);
            }

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
