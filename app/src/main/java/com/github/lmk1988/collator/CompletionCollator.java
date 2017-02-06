package com.github.lmk1988.collator;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.lmk1988.collator.callbacks.OnCompletionCallback;

import java.util.ArrayList;

public class CompletionCollator {

    @Nullable
    private OnCompletionCallback callback;

    private final ArrayList<CompletionNode> subNodes;

    public CompletionCollator() {
        callback = null;
        subNodes = new ArrayList<>();
    }

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
