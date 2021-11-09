package com.ahmednafe3.android_biometric_itegrated.ib_scan;

import com.integratedbiometrics.ibscanultimate.IBScanDevice;

public class CaptureInfo {
    public String preCaptureMessage;        // to display on fingerprint window
    public String postCaptuerMessage;        // to display on fingerprint window
    public IBScanDevice.ImageType imageType;                // capture mode
    public int numberOfFinger;            // number of finger count
    public String fingerName;                // finger name (e.g left thumbs, left index ... )


}
