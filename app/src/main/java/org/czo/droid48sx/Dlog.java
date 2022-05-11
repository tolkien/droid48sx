package org.czo.droid48sx;

import android.util.Log;

public class Dlog {

    static final String TAG = "48sx";

    public static final void d(String message) {
        if (BuildConfig.DEBUG && BuildConfig.BUILD_TYPE.equals("debug")) Log.d(TAG, message);
    }

}

