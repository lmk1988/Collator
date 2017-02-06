package com.github.lmk1988.collator.callbacks;

import android.support.annotation.NonNull;

import java.util.List;

public interface CollatedResultsCallback<T> {
    void onCompleteCallbacks(@NonNull List<T> collatedResults);
}
