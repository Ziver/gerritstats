package com.holmsted.gerrit.processor;

import com.holmsted.file.FileWriter;
import com.holmsted.gerrit.processor.user.IdentityRecord;

import javax.annotation.Nonnull;
import java.io.File;

public interface OutputFormatter<T> {
    void format(@Nonnull T format);


    /**
     * Writes a .json file.
     *
     * See alternate e.g. http://stackoverflow.com/questions/7346563/loading-local-json-file
     */
    static void writeFile(@Nonnull File dir, @Nonnull String fileName, @Nonnull String content) {
        File file = new File(dir, fileName + ".json");
        System.out.println("Creating file: " + file);

        FileWriter.writeFile(file.getAbsolutePath(), content);
    }
}