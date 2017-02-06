package com.github.lmk1988.collator;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.lmk1988.collator.callbacks.CollatedResultsCallback;

import java.util.ArrayList;

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
