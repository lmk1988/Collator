package com.github.lmk1988.collator;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.lmk1988.collator.callbacks.OnCompletionCallback;

import java.util.ArrayList;

/**
 * Awaits async processes completion and triggers a callback after every process is completed.
 * <br />
 * Before the start of every async process, you will need to create a {@link CompletionNode}
 * using {@link #reserveCompletion()}. This allows the collator to know the amount of
 * processes to await for.
 * <br />
 * Upon async process completion, call {@link CompletionNode#completed()}
 * to notify collator of completion.
 * <br />
 * When every CompletionNode has indicated completion, {@link OnCompletionCallback#onComplete()}
 * will be triggered.
 * <br />
 * {@link OnCompletionCallback} is added in {@link #awaitCompletion(OnCompletionCallback)}
 */
public class CompletionCollator {

    @Nullable
    private OnCompletionCallback callback;

    private final ArrayList<CompletionNode> subNodes;

    public CompletionCollator() {
        callback = null;
        subNodes = new ArrayList<>();
    }

    /**
     * Reserve a node in this collator to allow indication of completion in a separate async process.
     * <br />
     * Note that you will need to create a node before the start of the async process.
     *
     * @return {@link CompletionNode} node to call {@link CompletionNode#completed()} when async process is completed
     * @throws RuntimeException If {@link #awaitCompletion(OnCompletionCallback)} is called before this function
     */
    @NonNull
    @CheckResult
    public synchronized CompletionNode reserveCompletion() {
        if (this.callback != null) {
            throw new RuntimeException("CompletionCollator tried reserve completion after awaiting for completion");
        }
        CompletionNode tempNode = new CompletionNode(this);
        subNodes.add(tempNode);
        return tempNode;
    }

    synchronized void freeNode(@NonNull CompletionNode node) {
        if (subNodes.remove(node)) {
            if (this.callback != null && subNodes.isEmpty()) {
                callbackResultsOnAnotherThread(callback);
            }
        } else {
            throw new RuntimeException("CompletionCollator had invalid node");
        }
    }

    /**
     * Await generated {@link CompletionNode} to complete before triggering {@link OnCompletionCallback#onComplete()}
     * <br />
     * Note that it is fine to call this function even if no nodes are generated. This can happen
     * when the amount of async process varies from NONE to MANY
     *
     * @param callback {@link OnCompletionCallback} to trigger
     * @throws RuntimeException If this function is called more than once
     */
    public synchronized void awaitCompletion(@NonNull final OnCompletionCallback callback) {
        if (this.callback == null) {
            this.callback = callback;

            //If all the callbacks have replied, process this immediately
            if (subNodes.isEmpty()) {
                callbackResultsOnAnotherThread(callback);
            }

        } else {
            throw new RuntimeException("CompletionCollator tried to await completion twice");
        }
    }

    private synchronized void callbackResultsOnAnotherThread(@NonNull final OnCompletionCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                callback.onComplete();
            }
        }).start();
    }
}
