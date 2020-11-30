package com.holmsted.gerrit.processor.file;

import com.google.gson.*;
import com.holmsted.gerrit.data.Identity;
import com.holmsted.gerrit.OutputSettings;
import com.holmsted.gerrit.data.serializer.IdentityUsernameSerializer;
import com.holmsted.gerrit.data.serializer.ReviewDataExclusionStrategy;
import com.holmsted.gerrit.processor.OutputFormatter;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOError;
import java.io.IOException;

@SuppressWarnings("PMD.ExcessiveImports")
public class FileJsonFormatter implements OutputFormatter<FileData> {
    private static final String ROOT_OUTPUT_DIR = "data/file/";

    private final File outputDir;

    public FileJsonFormatter(@Nonnull OutputSettings outputSettings) {
        outputDir = new File(outputSettings.getOutputDir(), ROOT_OUTPUT_DIR);
    }

    @Override
    public void format(@Nonnull FileData data) {
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOError(new IOException("Cannot create output directory " + outputDir.getAbsolutePath()));
        }

        Gson gson = new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(Identity.class, new IdentityUsernameSerializer())
                .setExclusionStrategies(new ReviewDataExclusionStrategy())
                .create();

        for (FileRecord file : data) {
            String json = gson.toJson(file);
            OutputFormatter.writeFile(outputDir, file.getFileID(), json);
        }

        System.out.println("File data written to: " + outputDir.getAbsolutePath());
    }
}
