package com.ahmednafe3.android_biometric_itegrated.ib_scan;


import static com.ahmednafe3.android_biometric_itegrated.ib_scan.IbScanConstant.PREVIEW_IMAGE_BACKGROUND;
import static com.ahmednafe3.android_biometric_itegrated.ib_scan.IbScanConstant.__INVALID_POS__;
import static com.ahmednafe3.android_biometric_itegrated.ib_scan.IbScanConstant.__NA_DEFAULT__;
import static com.ahmednafe3.android_biometric_itegrated.ib_scan.IbScanConstant.__NFIQ_DEFAULT__;

import android.graphics.Bitmap;

public class IBScanData {
    /* The usb device currently selected. */
    public int usbDevices = __INVALID_POS__;

    /* The sequence of capture currently selected. */
    public int captureSeq = __INVALID_POS__;

    /* The current contents of the nfiq TextView. */
    public String nfiq = __NFIQ_DEFAULT__;

    /* The current contents of the frame time TextView. */
    public String frameTime = __NA_DEFAULT__;

    /* The current image displayed in the image preview ImageView. */
    public Bitmap imageBitmap = null;


    /* Indicates whether the image preview ImageView can be long-clicked. */
    public boolean imagePreviewImageClickable = false;

    /* The current contents of the overlayText TextView. */
    public String overlayText = "";

    /* The current contents of the overlay color for overlayText TextView. */
    public int overlayColor = PREVIEW_IMAGE_BACKGROUND;

    /* The current contents of the status message TextView. */
    public String statusMessage = __NA_DEFAULT__;
}
