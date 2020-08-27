package com.holmsted.gerrit.processor;

import javax.annotation.Nonnull;

public interface OutputFormatter<T> {
    void format(@Nonnull T format);
}