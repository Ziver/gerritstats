package com.holmsted.gerrit.processor.message;

import com.google.gson.*;
import com.holmsted.gerrit.OutputSettings;
import com.holmsted.gerrit.data.ChangeComment;
import com.holmsted.gerrit.data.Identity;
import com.holmsted.gerrit.data.PatchSet;
import com.holmsted.gerrit.data.serializer.IdentityUsernameSerializer;
import com.holmsted.gerrit.data.serializer.ReviewDataExclusionStrategy;
import com.holmsted.gerrit.processor.OutputFormatter;

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

        Gson gson = new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(Identity.class, new IdentityUsernameSerializer())
                .setExclusionStrategies(new ReviewDataExclusionStrategy())
                .create();
        String json = gson.toJson(data.getCommits());

        OutputFormatter.writeFile(outputDir, data.getMessageTagID(), json);

        System.out.println("Message data written to: " + outputDir.getAbsolutePath());
    }



}
