package com.github.lmk1988.collator;

import android.support.annotation.NonNull;

import com.github.lmk1988.collator.callbacks.OnCompletionCallback;

/**
 * CompletionNodes are created by
 * {@link CompletionCollator}
 * using function
 * {@link CompletionCollator#awaitCompletion(OnCompletionCallback) awaitCompletion()}.
 * For every async process which you would like to await for its
 * completion, a CompletionNode is required before the start of the
 * async process.
 * <br />
 * Call {@link #completed()} upon async process completion.
 */

public class CompletionNode {
    private CompletionCollator parentCollator;

    CompletionNode(@NonNull CompletionCollator parentCollator) {
        this.parentCollator = parentCollator;
    }

    /**
     * It is required to call this function to indicate completion.
     * <br />
     * Failure to do so will cause process to lock up
     * @throws RuntimeException when function is called more than once
     */
    public synchronized void completed() {
        if (parentCollator != null) {
            parentCollator.freeNode(this);
            parentCollator = null;
        } else {
            throw new RuntimeException("CompletionNode returned more than once");
        }
    }
}
