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
    private static final String RES_OUTPUT_DIR = ".";
    private static final String DATA_PATH = ".";

    private final File outputDir;
    private final File resOutputDir;

    private final Map<String, Identity> identities = new HashMap<>();

    public FileJsonFormatter(@Nonnull OutputSettings outputSettings) {
        outputDir = new File(outputSettings.getOutputDir());
        resOutputDir = new File(outputDir.getAbsolutePath() + File.separator + RES_OUTPUT_DIR);
    }

    @Override
    public void format(@Nonnull FileData data) {

    }
}
