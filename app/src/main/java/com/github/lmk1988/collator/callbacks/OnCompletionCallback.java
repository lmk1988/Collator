package com.github.lmk1988.collator.callbacks;

/**
 * Interface to be implemented for callback when
 * {@link com.github.lmk1988.collator.CompletionCollator CompletionCollator}
 * completes collating results in function
 * {@link com.github.lmk1988.collator.CompletionCollator#awaitCompletion(OnCompletionCallback) awaitCompletion() }
 */
public interface OnCompletionCallback {
    void onComplete();
}
