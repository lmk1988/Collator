package com.github.lmk1988.collator;

import android.support.annotation.NonNull;

public class CompletionNode {
    private CompletionCollator parentCollator;

    CompletionNode(@NonNull CompletionCollator parentCollator) {
        this.parentCollator = parentCollator;
    }

    public synchronized void completed() {
        if (parentCollator != null) {
            parentCollator.freeNode(this);
            parentCollator = null;
        } else {
            throw new RuntimeException("CompletionNode returned more than once");
        }
    }
}
