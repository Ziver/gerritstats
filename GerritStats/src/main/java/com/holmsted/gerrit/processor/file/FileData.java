package com.holmsted.gerrit.processor.file;

import com.holmsted.gerrit.Commit;
import com.holmsted.gerrit.QueryData;
import com.holmsted.gerrit.processor.user.IdentityRecord;
import com.holmsted.gerrit.processor.user.IdentityRecordList;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;

public class FileData {
    private Map<File,List<Commit>> files = new HashMap<>();




    public List<Commit> getCommits(File file) {
        return files.get(file);
    }
}
