package com.github.lmk1988.collator;

import android.support.annotation.NonNull;

import com.github.lmk1988.collator.callbacks.CollatedResultsCallback;

/**
 * CallbackCollatorNodes are created using function
 * {@link CallbackCollator#awaitCallbacks(CollatedResultsCallback)}.
 * For every async process which you would like to get its result,
 * a CallbackCollatorNodes is required before the start of the
 * async process.
 * <br />
 * Call {@link #returnCallbackResult(Object)} with the result of the async process
 */
public class CallbackCollatorNode<T> {
    private CallbackCollator<T> parentCollator;

    CallbackCollatorNode(@NonNull CallbackCollator<T> parentCollator) {
        this.parentCollator = parentCollator;
    }

    /**
     * It is required to call this function to indicate completion.
     * <br />
     * Failure to do so will cause process to lock up
     * @param result result of the async process
     * @throws RuntimeException when function is called more than once
     */
    public synchronized void returnCallbackResult(@NonNull T result) {
        if (parentCollator != null) {
            parentCollator.appendResult(result);
            parentCollator = null;
        } else {
            throw new RuntimeException("CompletionNode returned more than once");
        }
    }
}