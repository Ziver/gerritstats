package com.holmsted.gerrit.processor.user;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

import com.holmsted.gerrit.data.Identity;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Sorts reviewers in order of addition.
 * <p>
 * This comparator is not serializable and only intended for sorting things,
 * not for e.g. creating trees.
 */
@SuppressFBWarnings("SE_COMPARATOR_SHOULD_BE_SERIALIZABLE")
class ReviewerAddedCountComparator implements Comparator<Identity> {
    private final transient Map<Identity, IdentityRecord.ReviewerData> reviewsForIdentity;

    public ReviewerAddedCountComparator(Map<Identity, IdentityRecord.ReviewerData> reviewsForIdentity) {
        this.reviewsForIdentity = reviewsForIdentity;
    }

    @Override
    public int compare(Identity left, Identity right) {
        int reviewCountLeft = reviewsForIdentity.get(left).addedAsReviewerCount;
        int reviewCountRight = reviewsForIdentity.get(right).addedAsReviewerCount;
        if (reviewCountLeft < reviewCountRight) {
            return 1;
        } else if (reviewCountLeft > reviewCountRight) {
            return -1;
        } else {
            return Objects.compare(left.email, right.email, String::compareTo);
        }
    }
}
