package com.github.lmk1988.collator;

import android.support.annotation.NonNull;

public class CallbackCollatorNode<T> {
    private CallbackCollator<T> parentCollator;

    CallbackCollatorNode(@NonNull CallbackCollator<T> parentCollator) {
        this.parentCollator = parentCollator;
    }

    public synchronized void returnCallbackResult(@NonNull T result) {
        if (parentCollator != null) {
            parentCollator.appendResult(result);
            parentCollator = null;
        } else {
            throw new RuntimeException("CompletionNode returned more than once");
        }
    }
}