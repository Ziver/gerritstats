package com.holmsted.gerrit.processor.file;

import com.holmsted.gerrit.data.Commit;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileRecord {
    private String file;
    private List<Commit> commits = new ArrayList<>();


    public FileRecord(String file) {
        this.file = file;
    }


    public String getFileID() {
        return file.replaceAll("\\W+", "_");
    }

    public void addCommit(Commit commit) {
        if (!commits.contains(commit))
            commits.add(commit);
    }
}
