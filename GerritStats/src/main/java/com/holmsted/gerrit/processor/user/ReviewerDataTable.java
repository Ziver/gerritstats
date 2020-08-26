package com.holmsted.gerrit.processor.user;

import com.holmsted.gerrit.Commit;
import com.holmsted.gerrit.processor.user.IdentityRecord.ReviewerData;

import java.util.Hashtable;

public class ReviewerDataTable extends Hashtable<Commit.Identity, ReviewerData> {
}
