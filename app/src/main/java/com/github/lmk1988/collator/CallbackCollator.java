package com.github.lmk1988.collator;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.lmk1988.collator.callbacks.CollatedResultsCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Awaits zero to many async process results and
 * returns the array of async results after all reserved {@link CallbackCollatorNode} have completed.
 * <br />
 * Before the start of every async process, you will need to create a {@link CallbackCollatorNode}
 * using {@link #reserveCallback()}. This allows the collator to know the amount of
 * processes to await for.
 * <br />
 * Upon async process completion, return its result using {@link CallbackCollatorNode#returnCallbackResult(Object)}
 * <br />
 * After every CallbackCollatorNode have returned their result, {@link CollatedResultsCallback#onCompleteCallbacks(List)}
 * will be triggered.
 * <br />
 * {@link CollatedResultsCallback#onCompleteCallbacks(List)} is added in {@link #awaitCallbacks(CollatedResultsCallback)}
 */
public class CallbackCollator<T> {
    private int targetReserveCallbackCount;
    @NonNull
    private final ArrayList<T> collateResults;

    @Nullable
    private CollatedResultsCallback<T> callback;

    public CallbackCollator() {
        targetReserveCallbackCount = 0;
        collateResults = new ArrayList<>();
        callback = null;
    }

    /**
     * Reserve a node in this collator to allow collation of async process results.
     * <br />
     * Note that you will need to create a node before the start of the async process.
     *
     * @return {@link CallbackCollatorNode} node to call {@link CallbackCollatorNode#returnCallbackResult(Object)} when async process is completed
     * @throws RuntimeException If {@link #awaitCallbacks(CollatedResultsCallback)} is called before this function
     */
    @NonNull
    @CheckResult
    public synchronized CallbackCollatorNode<T> reserveCallback() {
        if (this.callback != null) {
            throw new RuntimeException("CallbackCollator tried reserve callback after awaiting for callbacks");
        }
        targetReserveCallbackCount++;
        return new CallbackCollatorNode<>(this);
    }

    synchronized void appendResult(@NonNull T result) {
        if (collateResults.size() < targetReserveCallbackCount) {
            collateResults.add(result);

            if (this.callback != null && collateResults.size() == targetReserveCallbackCount) {
                callbackResultsOnAnotherThread(callback);
            }
        } else {
            //Will enter here is targetReserveCallbackCount is >= collateResults.size() during appendResult
            throw new RuntimeException("CallbackCollator exceed target number of results");
        }
    }

    /**
     * Await generated {@link CallbackCollatorNode} to complete before triggering {@link CollatedResultsCallback#onCompleteCallbacks(List)}
     * <br />
     * Note that it is fine to call this function even if no nodes are generated. This can happen
     * when the amount of async process varies from NONE to MANY
     *
     * @param callback {@link CollatedResultsCallback} to trigger
     * @throws RuntimeException If this function is called more than once
     */
    public synchronized void awaitCallbacks(@NonNull final CollatedResultsCallback<T> callback) {
        if (this.callback == null) {
            this.callback = callback;

            //If all the callbacks have replied, process this immediately
            if (collateResults.size() == targetReserveCallbackCount) {
                callbackResultsOnAnotherThread(callback);
            }

        } else {
            throw new RuntimeException("CallbackCollator tried to await callbacks twice");
        }
    }

    private synchronized void callbackResultsOnAnotherThread(@NonNull final CollatedResultsCallback<T> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                callback.onCompleteCallbacks(collateResults);
            }
        }).start();
    }
}
