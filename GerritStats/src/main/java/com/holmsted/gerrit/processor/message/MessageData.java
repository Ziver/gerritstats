package com.holmsted.gerrit.processor.message;

import com.holmsted.gerrit.Commit;
import com.holmsted.gerrit.QueryData;
import com.holmsted.gerrit.processor.user.IdentityRecord;

import java.util.LinkedList;
import java.util.List;

public class MessageData {
    private final String messageTag;
    private final List<Commit> commits = new LinkedList<>();


    public MessageData(String messageTag) {
        this.messageTag = messageTag;
    }


    public String getMessageTag() {
        return messageTag;
    }

    public String getMessageTagID() {
        return messageTag.toLowerCase().replace("\\W+", "_");
    }


    public List<Commit> getCommits() {
        return commits;
    }

    public void addCommit(Commit commit) {
        commits.add(commit);
    }
}
