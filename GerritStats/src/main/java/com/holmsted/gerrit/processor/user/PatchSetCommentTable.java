package com.holmsted.gerrit.processor.user;

import com.holmsted.gerrit.data.Commit;
import com.holmsted.gerrit.data.PatchSetComment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

public class PatchSetCommentTable implements Map<Commit, List<PatchSetComment>> {

    private final Map<Commit, List<PatchSetComment>> commitToComment = new Hashtable<>();
    private final Map<PatchSetComment, Commit> commentToCommit = new Hashtable<>();
    private final List<PatchSetComment> allComments = new ArrayList<>();

    public void addCommentForCommit(@Nonnull Commit commit, @Nonnull PatchSetComment patchSetComment) {
        List<PatchSetComment> patchSetComments = commitToComment.computeIfAbsent(commit,
                keyCommit -> new ArrayList<>());
        patchSetComments.add(patchSetComment);

        commentToCommit.put(patchSetComment, commit);
        allComments.add(patchSetComment);

        commitToComment.put(commit, patchSetComments);
    }

    @Nonnull
    public List<PatchSetComment> getAllComments() {
        return allComments;
    }

    @Override
    public int size() {
        return commitToComment.size();
    }

    @Override
    public boolean isEmpty() {
        return commitToComment.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return commitToComment.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return commitToComment.containsValue(value);
    }

    @Override
    public List<PatchSetComment> get(Object key) {
        return commitToComment.get(key);
    }

    @Override
    public List<PatchSetComment> put(Commit key, List<PatchSetComment> value) {
        throw new UnsupportedOperationException("Call addCommentForCommit()");
    }

    @Override
    public List<PatchSetComment> remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends Commit, ? extends List<PatchSetComment>> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Commit> keySet() {
        return commitToComment.keySet();
    }

    @Override
    public Collection<List<PatchSetComment>> values() {
        return commitToComment.values();
    }

    @Override
    public Set<Entry<Commit, List<PatchSetComment>>> entrySet() {
        return commitToComment.entrySet();
    }
}
