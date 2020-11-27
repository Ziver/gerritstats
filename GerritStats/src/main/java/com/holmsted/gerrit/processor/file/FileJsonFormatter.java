package com.holmsted.gerrit.processor.file;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.holmsted.file.FileWriter;
import com.holmsted.gerrit.Commit;
import com.holmsted.gerrit.Commit.Identity;
import com.holmsted.gerrit.OutputSettings;
import com.holmsted.gerrit.processor.OutputFormatter;
import com.holmsted.gerrit.processor.user.*;
import com.holmsted.gerrit.processor.user.IdentityRecord.ReviewerData;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.*;

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

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(data.getCommits(null));

        //OutputFormatter.writeFile(outputDir, data.get(), json);

        System.out.println("File data written to: " + outputDir.getAbsolutePath());
    }
}
