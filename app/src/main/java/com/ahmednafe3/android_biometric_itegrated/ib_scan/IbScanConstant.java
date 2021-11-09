package com.ahmednafe3.android_biometric_itegrated.ib_scan;

import android.graphics.Color;

public class IbScanConstant {
    /* The tag used for Android log messages from this app. */
    public static final String TAG = "Simple Scan";

    public static final int __INVALID_POS__ = -1;

    /* The default value of the status TextView. */
    public static final String __NFIQ_DEFAULT__ = "0-0-0-0";

    /* The default value of the frame time TextView. */
    public static final String __NA_DEFAULT__ = "n/a";

    /* The background color of the preview image ImageView. */
    public static final int PREVIEW_IMAGE_BACKGROUND = Color.LTGRAY;

    /* The background color of a finger quality TextView when the finger is not present. */
    public static final int FINGER_QUALITY_NOT_PRESENT_COLOR = Color.LTGRAY;

    // Beep definitions
    public static final int __BEEP_FAIL__ = 0;
    public static final int __BEEP_SUCCESS__ = 1;
    public static final int __BEEP_OK__ = 2;
    public static final int __BEEP_DEVICE_COMMUNICATION_BREAK__ = 3;

    /* The number of finger segments set in the result image. */
    public static final int FINGER_SEGMENT_COUNT = 4;

}
