package com.holmsted.gerrit.processor.file;

import com.holmsted.gerrit.data.Commit;

import java.io.File;
import java.util.*;

public class FileData implements Iterable<FileRecord> {
    private Map<String,FileRecord> files = new HashMap<>();



    public FileRecord getCommits(String file) {
        return files.get(file);
    }

    @Override
    public Iterator<FileRecord> iterator() {
        return files.values().iterator();
    }

    public void addFile(String file) {
        if (!files.containsKey(file))
            files.put(file, new FileRecord(file));
    }
}
