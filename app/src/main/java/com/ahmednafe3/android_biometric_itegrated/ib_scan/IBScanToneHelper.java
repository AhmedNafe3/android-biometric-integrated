package com.ahmednafe3.android_biometric_itegrated.ib_scan;

import android.media.AudioManager;
import android.media.ToneGenerator;

import com.integratedbiometrics.ibscanultimate.IBScanDevice;
import com.integratedbiometrics.ibscanultimate.IBScanException;

public class IBScanToneHelper {
    private static IBScanToneHelper instance;
    private IBScanDevice ibScanDevice;

    public static IBScanToneHelper getInstance(IBScanDevice device) {
        if (instance == null) {
            synchronized (IBScanToneHelper.class) {
                if (instance == null) {
                    instance = new IBScanToneHelper(device);
                }
            }
        }
        return instance;
    }

    public IBScanToneHelper(IBScanDevice ibScanDevice) {
        this.ibScanDevice = ibScanDevice;
    }

    public IBScanDevice getIBScanDevice() {
        return ibScanDevice;
    }

    public void _BeepFail() {
        try {
            IBScanDevice.BeeperType beeperType = getIBScanDevice().getOperableBeeper();
            if (beeperType != IBScanDevice.BeeperType.BEEPER_TYPE_NONE) {
                getIBScanDevice().setBeeper(IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC, 10/*Sol*/, 12/*300ms = 12*25ms*/, 0, 0);
                _Sleep(150);
                getIBScanDevice().setBeeper(IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC, 10/*Sol*/, 6/*150ms = 6*25ms*/, 0, 0);
                _Sleep(150);
                getIBScanDevice().setBeeper(IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC, 10/*Sol*/, 6/*150ms = 6*25ms*/, 0, 0);
                _Sleep(150);
                getIBScanDevice().setBeeper(IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC, 10/*Sol*/, 6/*150ms = 6*25ms*/, 0, 0);
            }
        } catch (IBScanException ibse) {
            // devices for without beep chip
            // send the tone to the "alarm" stream (classic beeps go there) with 30% volume
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 50);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 300); // 300 is duration in ms
            _Sleep(300 + 150);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150); // 150 is duration in ms
            _Sleep(150 + 150);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150); // 150 is duration in ms
            _Sleep(150 + 150);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150); // 150 is duration in ms
        }
    }

    public void _BeepSuccess() {
        try {
            IBScanDevice.BeeperType beeperType = getIBScanDevice().getOperableBeeper();
            if (beeperType != IBScanDevice.BeeperType.BEEPER_TYPE_NONE) {
                getIBScanDevice().setBeeper(IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC, 2/*Sol*/, 4/*100ms = 4*25ms*/, 0, 0);
                _Sleep(50);
                getIBScanDevice().setBeeper(IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC, 2/*Sol*/, 4/*100ms = 4*25ms*/, 0, 0);
            }
        } catch (IBScanException ibse) {
            // devices for without beep chip
            // send the tone to the "alarm" stream (classic beeps go there) with 30% volume
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 50);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100); // 100 is duration in ms
            _Sleep(100 + 50);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100); // 100 is duration in ms
        }
    }

    public void _BeepOk() {
        try {
            IBScanDevice.BeeperType beeperType = getIBScanDevice().getOperableBeeper();
            if (beeperType != IBScanDevice.BeeperType.BEEPER_TYPE_NONE) {
                getIBScanDevice().setBeeper(IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC, 2/*Sol*/, 4/*100ms = 4*25ms*/, 0, 0);
            }
        } catch (IBScanException ibse) {
            // devices for without beep chip
            // send the tone to the "alarm" stream (classic beeps go there) with 30% volume
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 50);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100); // 100 is duration in ms
        }
    }

    public void _BeepDeviceCommunicationBreak() {
        for (int i = 0; i < 8; i++) {
            // send the tone to the "alarm" stream (classic beeps go there) with 30% volume
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 50);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100); // 100 is duration in ms
            _Sleep(100 + 100);
        }
    }

    public void _Sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
        }
    }
}
