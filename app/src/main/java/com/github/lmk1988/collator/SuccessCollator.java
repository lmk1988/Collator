package com.github.lmk1988.collator;

import android.support.annotation.Nullable;

import java.util.List;

public class SuccessCollator extends CallbackCollator<Boolean> {
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
