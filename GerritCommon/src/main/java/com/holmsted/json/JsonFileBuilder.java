package com.holmsted.json;

import com.holmsted.file.FileWriter;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.StringWriter;

public class JsonFileBuilder {

        @Nonnull
        final File outputDir;

        String outputFilename;
        String serializedJs;
        String memberVariableName;

        public JsonFileBuilder(@Nonnull File outputDir) {
            this.outputDir = outputDir;
        }

        public JsonFileBuilder setOutputFilename(@Nonnull String outputFilename) {
            this.outputFilename = outputFilename;
            System.out.println("Creating " + this.outputFilename);
            return this;
        }

        public JsonFileBuilder setMemberName(String memberVariableName) {
            this.memberVariableName = memberVariableName;
            return this;
        }

        public JsonFileBuilder setSerializedJs(String serializedJs) {
            this.serializedJs = serializedJs;
            return this;
        }

        public void build() {
            StringWriter writer = new StringWriter();

            writer.write(String.format("var %s = %s;",
                    memberVariableName,
                    serializedJs));

            FileWriter.writeFile(
                    outputDir.getPath() + File.separator + outputFilename,
                    writer.toString());
        }
    }