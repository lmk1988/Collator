package com.github.lmk1988.collator;

import android.support.annotation.Nullable;

import java.util.List;

/**
 * A template for {@link CallbackCollator} to collate async success/failure results
 */
public class SuccessCollator extends CallbackCollator<Boolean> {

    /**
     * Checks if the given list of boolean are all true
     *
     * @param collatedSuccess
     * @return true when collatedSuccess is not null and are all true
     */
    public static boolean isAllSuccess(@Nullable List<Boolean> collatedSuccess) {
        if (collatedSuccess == null) {
            return false;
        }

        for (Boolean b : collatedSuccess) {
            if (!b) {
                return false;
            }
        }
        return true;
    }
}
