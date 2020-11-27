package com.holmsted.gerrit.processor.message;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.holmsted.gerrit.Commit;
import com.holmsted.gerrit.OutputSettings;
import com.holmsted.gerrit.processor.OutputFormatter;
import com.holmsted.gerrit.processor.file.FileData;
import com.holmsted.gerrit.processor.user.IdentityRecord;
import com.holmsted.gerrit.processor.user.PatchSetCommentTable;
import com.holmsted.gerrit.processor.user.ReviewerDataTable;
import com.holmsted.gerrit.processor.user.UserJsonFormatter;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOError;
import java.io.IOException;

@SuppressWarnings("PMD.ExcessiveImports")
public class MessageJsonFormatter implements OutputFormatter<MessageData> {
    private static final String ROOT_OUTPUT_DIR = "data/message/";

    private final File outputDir;

    public MessageJsonFormatter(@Nonnull OutputSettings outputSettings) {
        outputDir = new File(outputSettings.getOutputDir(), ROOT_OUTPUT_DIR);
    }

    @Override
    public void format(@Nonnull MessageData data) {
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOError(new IOException("Cannot create output directory " + outputDir.getAbsolutePath()));
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(data.getCommits());

        OutputFormatter.writeFile(outputDir, data.getMessageTagID(), json);

        System.out.println("Message data written to: " + outputDir.getAbsolutePath());
    }



}
