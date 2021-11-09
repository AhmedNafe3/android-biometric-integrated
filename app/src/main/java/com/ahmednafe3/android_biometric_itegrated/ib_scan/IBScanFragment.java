package com.ahmednafe3.android_biometric_itegrated.ib_scan;

import static com.ahmednafe3.android_biometric_itegrated.ib_scan.IbScanConstant.PREVIEW_IMAGE_BACKGROUND;
import static com.ahmednafe3.android_biometric_itegrated.ib_scan.IbScanConstant.__BEEP_DEVICE_COMMUNICATION_BREAK__;
import static com.ahmednafe3.android_biometric_itegrated.ib_scan.IbScanConstant.__BEEP_FAIL__;
import static com.ahmednafe3.android_biometric_itegrated.ib_scan.IbScanConstant.__BEEP_OK__;
import static com.ahmednafe3.android_biometric_itegrated.ib_scan.IbScanConstant.__BEEP_SUCCESS__;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ahmednafe3.android_biometric_itegrated.databinding.IbScanLayoutBinding;
import com.integratedbiometrics.ibscanultimate.IBScan;
import com.integratedbiometrics.ibscanultimate.IBScanDevice;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.FingerCountState;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.FingerQualityState;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.ImageData;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.ImageType;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.PlatenState;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.SegmentPosition;
import com.integratedbiometrics.ibscanultimate.IBScanDeviceListener;
import com.integratedbiometrics.ibscanultimate.IBScanException;
import com.integratedbiometrics.ibscanultimate.IBScanListener;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;


public class IBScanFragment extends Fragment implements IBScanListener, IBScanDeviceListener {
    private IbScanLayoutBinding binding;

    private Bitmap m_BitmapImage;
    private IBScan m_ibScan;
    private IBScanDevice m_ibScanDevice;
    private IBScanData m_savedData = new IBScanData();
    private boolean m_bInitializing = false;
    private boolean m_bNeedClearPlaten = false;
    private Vector<CaptureInfo> m_vecCaptureSeq = new Vector<CaptureInfo>();
    private int m_nCurrentCaptureStep = -1;
    private byte[] m_drawBuffer;
    private IBScanViewModel viewModel;

    @Override
    public void onResume() {
        super.onResume();
        m_ibScan = IBScan.getInstance(getActivity());
        m_ibScan.setScanListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            _ReleaseDevice();
            m_ibScan.setContext(null);
        } catch (IBScanException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = IbScanLayoutBinding.inflate(getLayoutInflater(), container, false);
        viewModel = new ViewModelProvider(getActivity()).get(IBScanViewModel.class);
        handleViewInteraction();
        if (m_ibScan != null) {
            requestUsbPermission();
        }
        addClickListener();
        populateUI();
        return binding.getRoot();
    }

    private void handleViewInteraction() {
        viewModel.FingerPrintError.observe(getActivity(), result -> {
            setStatusMessage(result);
        });
        viewModel.onAgentFingerPrintSuccess.observe(getActivity(), result -> {
            if (result != null || !TextUtils.isEmpty(result)) {
                viewModel.onCustomerFingerPrintSuccess.postValue(true);
                setStatusMessage("agent_fingerprint_success_msg");
                binding.startCaptureBtn.setText("done");
            }
        });

        viewModel.FingerPrintAgentNotVerified.observe(getActivity(), result -> {
            showToast(result);
        });

        viewModel.FingerPrintErrorSimVerified.observe(getActivity(), result -> {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(getActivity());
            dlgAlert.setMessage(result);
            dlgAlert.setTitle("verify_sim");
            dlgAlert.setPositiveButton("OK",
                    (dialog, whichButton) -> {
                        dialog.dismiss();
                        getActivity().finish();
                    }
            );
            dlgAlert.setCancelable(false);
            dlgAlert.create().show();
        });

        viewModel.FingerPrintRetryFlagFalse.observe(getActivity(), result -> {
            setStatusMessage(result);
        });

        viewModel.FingerPrintRetryTrue.observe(getActivity(), result -> {
            FingerPrintStatsRetryFlagTrue(result);
        });
    }

    private void setStatusMessage(String message) {
        getActivity().runOnUiThread(() -> {
            binding.txtStatusMessage.setText(message);
        });
    }

    private void addClickListener() {
        binding.imgPreview.setBackgroundColor(PREVIEW_IMAGE_BACKGROUND);
        binding.startCaptureBtn.setOnClickListener(this.m_btnCaptureStartClickListener);
    }

    private void requestUsbPermission() {
        final UsbManager manager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
        final HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        final Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            final UsbDevice device = deviceIterator.next();
            final boolean isScanDevice = IBScan.isScanDevice(device);
            if (isScanDevice) {
                final boolean hasPermission = manager.hasPermission(device);
                if (!hasPermission) {
                    this.m_ibScan.requestPermission(device.getDeviceId());
                }
            }
        }
    }

    private void populateUI() {
        if (m_savedData.imageBitmap != null) {
            binding.imgPreview.setImageBitmap(m_savedData.imageBitmap);
        }
        if (m_BitmapImage != null) {
            m_BitmapImage.isRecycled();
        }
        binding.imgPreview.setLongClickable(m_savedData.imagePreviewImageClickable);
    }

    protected IBScan getIBScan() {
        return (this.m_ibScan);
    }

    protected IBScanDevice getIBScanDevice() {
        return (this.m_ibScanDevice);
    }

    protected void setIBScanDevice(IBScanDevice ibScanDevice) {
        m_ibScanDevice = ibScanDevice;
        if (ibScanDevice != null) {
            ibScanDevice.setScanDeviceListener(this);
        }
    }

    class _InitializeDeviceThreadCallback extends Thread {
        private int devIndex;

        _InitializeDeviceThreadCallback(int devIndex) {
            this.devIndex = devIndex;
        }

        @Override
        public void run() {
            try {
                m_bInitializing = true;
                IBScanDevice ibScanDeviceNew = getIBScan().openDevice(this.devIndex);
                setIBScanDevice(ibScanDeviceNew);
                m_bInitializing = false;
                if (ibScanDeviceNew != null) {
                    int outWidth = binding.imgPreview.getWidth() - 20;
                    int outHeight = binding.imgPreview.getHeight() - 20;
                    m_BitmapImage = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888);
                    m_drawBuffer = new byte[outWidth * outHeight * 4];
                    OnMsg_CaptureSeqStart();
                }
            } catch (IBScanException ibse) {
                m_bInitializing = false;
                if (ibse.getType().equals(IBScanException.Type.DEVICE_ACTIVE)) {
                    setStatusMessage("Device initialization failed because in use by another thread/process.");
                } else if (ibse.getType().equals(IBScanException.Type.USB20_REQUIRED)) {
                    setStatusMessage("Device initialization failed because SDK only works with USB 2.0.");
                } else if (ibse.getType().equals(IBScanException.Type.DEVICE_HIGHER_SDK_REQUIRED)) {
                    try {
                        String m_minSDKVersion = getIBScan().getRequiredSDKVersion(this.devIndex);
                        setStatusMessage("Devcie initialization failed because SDK Version " + m_minSDKVersion + " is required at least.");
                    } catch (IBScanException ibse1) {
                    }
                } else {
                    try {
                        setStatusMessage("Device initialization failed. " + getIBScan().getErrorString(ibse.getType().toCode()));
                    } catch (IBScanException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    protected void _AddCaptureSeqVector(String PreCaptureMessage, String PostCaptuerMessage,
                                        ImageType imageType, int NumberOfFinger, String fingerName) {
        CaptureInfo info = new CaptureInfo();
        info.preCaptureMessage = PreCaptureMessage;
        info.postCaptuerMessage = PostCaptuerMessage;
        info.imageType = imageType;
        info.numberOfFinger = NumberOfFinger;
        info.fingerName = fingerName;
        m_vecCaptureSeq.addElement(info);
    }

    protected void _ReleaseDevice() throws IBScanException {
        if (getIBScanDevice() != null) {
            if (getIBScanDevice().isOpened() == true) {
                getIBScanDevice().close();
                setIBScanDevice(null);
            }
        }
        m_nCurrentCaptureStep = -1;
        m_bInitializing = false;
    }

    private void OnMsg_Beep(final int beepType) {
        getActivity().runOnUiThread(() -> {
            if (beepType == __BEEP_FAIL__)
                IBScanToneHelper.getInstance(getIBScanDevice())._BeepFail();
            else if (beepType == __BEEP_SUCCESS__)
                IBScanToneHelper.getInstance(getIBScanDevice())._BeepSuccess();
            else if (beepType == __BEEP_OK__)
                IBScanToneHelper.getInstance(getIBScanDevice())._BeepOk();
            else if (beepType == __BEEP_DEVICE_COMMUNICATION_BREAK__)
                IBScanToneHelper.getInstance(getIBScanDevice())._BeepDeviceCommunicationBreak();
        });
    }

    private void OnMsg_CaptureSeqStart() {
        getActivity().runOnUiThread(() -> {
            m_vecCaptureSeq.clear();
            _AddCaptureSeqVector("Please put a single finger on the sensor!",
                    "Keep finger on the sensor!",
                    ImageType.FLAT_SINGLE_FINGER,
                    1,
                    "SFF_Unknown");
            OnMsg_CaptureSeqNext();
        });
    }

    private void OnMsg_CaptureSeqNext() {
        getActivity().runOnUiThread(() -> {
            m_nCurrentCaptureStep++;
            if (m_nCurrentCaptureStep >= m_vecCaptureSeq.size()) {
                // All of capture sequence completely
                CaptureInfo tmpInfo = new CaptureInfo();
                m_nCurrentCaptureStep = -1;
                return;
            }
            try {
                CaptureInfo info = m_vecCaptureSeq.elementAt(m_nCurrentCaptureStep);
                IBScanDevice.ImageResolution imgRes = IBScanDevice.ImageResolution.RESOLUTION_500;
                boolean bAvailable = getIBScanDevice().isCaptureAvailable(info.imageType, imgRes);
                if (!bAvailable) {
                    setStatusMessage("The capture mode (" + info.imageType + ") is not available");
                    m_nCurrentCaptureStep = -1;
                    return;
                }

                // Start capture
                int captureOptions = 0;
//					if (m_chkAutoContrast.isSelected())
                captureOptions |= IBScanDevice.OPTION_AUTO_CONTRAST;
//					if (m_chkAutoCapture.isSelected())
                captureOptions |= IBScanDevice.OPTION_AUTO_CAPTURE;
//					if (m_chkIgnoreFingerCount.isSelected())
                captureOptions |= IBScanDevice.OPTION_IGNORE_FINGER_COUNT;

                getIBScanDevice().beginCaptureImage(info.imageType, imgRes, captureOptions);

                String strMessage = info.preCaptureMessage;
                setStatusMessage(strMessage);
            } catch (IBScanException ibse) {
                ibse.printStackTrace();
                setStatusMessage("Failed to execute beginCaptureImage()");
                m_nCurrentCaptureStep = -1;
            }
        });
    }

    private void OnMsg_AskRecapture(final IBScanException imageStatus) {
        getActivity().runOnUiThread(() -> {
            String askMsg;
            askMsg = "[Warning = " + imageStatus.getType().toString() + "] Do you want a recapture?";
            AlertDialog.Builder dlgAskRecapture = new AlertDialog.Builder(getActivity());
            dlgAskRecapture.setMessage(askMsg);
            dlgAskRecapture.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // To recapture current finger position
                            m_nCurrentCaptureStep--;
                            OnMsg_CaptureSeqNext();
                        }
                    });
            dlgAskRecapture.setNegativeButton("No",
                    (dialog, which) -> OnMsg_CaptureSeqNext());

            dlgAskRecapture.show();
        });
    }

    private void OnMsg_DeviceCommunicationBreak() {
        getActivity().runOnUiThread(() -> {
            if (getIBScanDevice() == null)
                return;
            setStatusMessage("Device communication was broken");
            try {
                _ReleaseDevice();
                OnMsg_Beep(__BEEP_DEVICE_COMMUNICATION_BREAK__);
            } catch (IBScanException ibse) {
                if (ibse.getType().equals(IBScanException.Type.RESOURCE_LOCKED)) {
                    OnMsg_DeviceCommunicationBreak();
                }
            }
        });
    }

    private void showToastOnUiThread(final String message, final int duration) {
        getActivity().runOnUiThread(() -> {
            Toast toast = Toast.makeText(getActivity(), message, duration);
            toast.show();
        });
    }

    private void OnMsg_DrawImage(final IBScanDevice device, final ImageData image) {
        getActivity().runOnUiThread(() -> {
            int destWidth = binding.imgPreview.getWidth() - 20;
            int destHeight = binding.imgPreview.getHeight() - 20;
            try {
                if (destHeight <= 0 || destWidth <= 0)
                    return;
                if (destWidth != m_BitmapImage.getWidth() || destHeight != m_BitmapImage.getHeight()) {
                    m_BitmapImage = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888);
                    m_drawBuffer = new byte[destWidth * destHeight * 4];
                }
                if (image.isFinal) {
                    getIBScanDevice().generateDisplayImage(image.buffer, image.width, image.height,
                            m_drawBuffer, destWidth, destHeight, (byte) 255, 2 /*IBSU_IMG_FORMAT_RGB32*/, 2 /*HIGH QUALITY*/, true);

                } else {
                    getIBScanDevice().generateDisplayImage(image.buffer, image.width, image.height,
                            m_drawBuffer, destWidth, destHeight, (byte) 255, 2 /*IBSU_IMG_FORMAT_RGB32*/, 0 /*LOW QUALITY*/, true);
                }
            } catch (IBScanException e) {
                e.printStackTrace();
            }
            m_BitmapImage.copyPixelsFromBuffer(ByteBuffer.wrap(m_drawBuffer));
            Canvas canvas = new Canvas(m_BitmapImage);
            IBScanDrawHelper.getInstance(getIBScanDevice())._DrawOverlay_WarningOfClearPlaten(canvas, 0, 0, destWidth, destHeight, m_bNeedClearPlaten, m_bInitializing, m_nCurrentCaptureStep);
            IBScanDrawHelper.getInstance(getIBScanDevice())._DrawOverlay_ResultSegmentImage(canvas, image, destWidth, destHeight, 0, null);
            // _DrawOverlay_RollGuideLine(canvas, image, destWidth, destHeight);
            m_savedData.imageBitmap = m_BitmapImage;
            binding.imgPreview.setImageBitmap(m_BitmapImage);
        });
    }

    private OnClickListener m_btnCaptureStartClickListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            if (binding.startCaptureBtn.getText() == "done") {
                getActivity().finish();
            } else {
                if (m_nCurrentCaptureStep != -1) {
                    try {
                        boolean IsActive = getIBScanDevice().isCaptureActive();
                        if (IsActive) {
                            // Capture image manually for active device
                            getIBScanDevice().captureImageManually();
                            return;
                        }
                    } catch (IBScanException ibse) {
                        setStatusMessage("IBScanDevice.takeResultImageManually() returned exception "
                                + ibse.getType().toString() + ".");
                    }
                }

                if (getIBScanDevice() == null) {
                    m_bInitializing = true;

                    _InitializeDeviceThreadCallback thread = new _InitializeDeviceThreadCallback(0);
                    thread.start();
                } else {
                    OnMsg_CaptureSeqStart();
                }
            }
        }
    };

    @Override
    public void scanDeviceAttached(final int deviceId) {
        showToastOnUiThread("Device " + deviceId + " attached", Toast.LENGTH_SHORT);
        final boolean hasPermission = m_ibScan.hasPermission(deviceId);
        if (!hasPermission) {
            m_ibScan.requestPermission(deviceId);
        }
    }

    @Override
    public void scanDeviceDetached(final int deviceId) {
        showToastOnUiThread("Device " + deviceId + " detached", Toast.LENGTH_SHORT);
    }

    @Override
    public void scanDevicePermissionGranted(final int deviceId, final boolean granted) {
        if (granted) {
            showToastOnUiThread("Permission granted to device " + deviceId, Toast.LENGTH_SHORT);
        } else {
            showToastOnUiThread("Permission denied to device " + deviceId, Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void scanDeviceCountChanged(final int deviceCount) {
    }

    @Override
    public void scanDeviceInitProgress(final int deviceIndex, final int progressValue) {
        setStatusMessage("Initializing device..." + progressValue + "%");
    }

    @Override
    public void scanDeviceOpenComplete(final int deviceIndex, final IBScanDevice device,
                                       final IBScanException exception) {
    }

    @Override
    public void deviceCommunicationBroken(final IBScanDevice device) {
        OnMsg_DeviceCommunicationBreak();
    }

    @Override
    public void deviceImagePreviewAvailable(final IBScanDevice device, final ImageData image) {
        OnMsg_DrawImage(device, image);
    }

    @Override
    public void deviceFingerCountChanged(final IBScanDevice device, final FingerCountState fingerState) {

    }

    @Override
    public void deviceFingerQualityChanged(final IBScanDevice device, final FingerQualityState[] fingerQualities) {

    }

    @Override
    public void deviceAcquisitionBegun(final IBScanDevice device, final ImageType imageType) {
        if (imageType.equals(ImageType.ROLL_SINGLE_FINGER)) {
            OnMsg_Beep(__BEEP_OK__);
            setStatusMessage("When done remove finger from sensor");
        }
    }

    @Override
    public void deviceAcquisitionCompleted(final IBScanDevice device, final ImageType imageType) {
        if (imageType.equals(ImageType.ROLL_SINGLE_FINGER)) {
            OnMsg_Beep(__BEEP_OK__);
        } else {
            OnMsg_Beep(__BEEP_SUCCESS__);
            setStatusMessage("Capture completed, postprocessing..");
        }
    }

    @Override
    public void deviceImageResultAvailable(final IBScanDevice device, final ImageData image,
                                           final ImageType imageType, final ImageData[] splitImageArray) {
    }

    @Override
    public void deviceImageResultExtendedAvailable(IBScanDevice device, IBScanException imageStatus,
                                                   final ImageData image, final ImageType imageType, final int detectedFingerCount,
                                                   final ImageData[] segmentImageArray, final SegmentPosition[] segmentPositionArray) {
        m_savedData.imagePreviewImageClickable = true;
        binding.imgPreview.setLongClickable(true);
        byte[] wsqData = null;
        // imageStatus value is greater than "STATUS_OK", Image acquisition successful.
        if (imageStatus == null /*STATUS_OK*/ ||
                imageStatus.getType().compareTo(IBScanException.Type.INVALID_PARAM_VALUE) > 0) {
            if (imageType.equals(ImageType.ROLL_SINGLE_FINGER)) {
                OnMsg_Beep(__BEEP_SUCCESS__);
            }
        }
        // imageStatus value is greater than "STATUS_OK", Image acquisition successful.
        if (imageStatus == null /*STATUS_OK*/ ||
                imageStatus.getType().compareTo(IBScanException.Type.INVALID_PARAM_VALUE) > 0) {

            if (imageStatus == null /*STATUS_OK*/) {
                setStatusMessage("Capture completed successfully");

            } else {
                // > IBSU_STATUS_OK
                setStatusMessage("Capture Warning (Warning code = " + imageStatus.getType().toString() + ")");
                OnMsg_DrawImage(device, image);
                OnMsg_AskRecapture(imageStatus);
                return;
            }
        } else {
            // < IBSU_STATUS_OK
            setStatusMessage("Capture failed (Error code = " + imageStatus.getType().toString() + ")");
            m_nCurrentCaptureStep = (int) m_vecCaptureSeq.size();
        }


        OnMsg_DrawImage(device, image);

        OnMsg_CaptureSeqNext();
    }

    @Override
    public void devicePlatenStateChanged(final IBScanDevice device, final PlatenState platenState) {
        if (platenState.equals(PlatenState.HAS_FINGERS))
            m_bNeedClearPlaten = true;
        else
            m_bNeedClearPlaten = false;

        if (platenState.equals(PlatenState.HAS_FINGERS)) {
            setStatusMessage("Please remove your fingers on the platen first!");
        } else {
            if (m_nCurrentCaptureStep >= 0) {
                CaptureInfo info = m_vecCaptureSeq.elementAt(m_nCurrentCaptureStep);
                String strMessage = info.preCaptureMessage;
                setStatusMessage(strMessage);
            }
        }
    }

    @Override
    public void deviceWarningReceived(final IBScanDevice device, final IBScanException warning) {
        setStatusMessage("Warning received " + warning.getType().toString());
    }

    @Override
    public void devicePressedKeyButtons(IBScanDevice device, int pressedKeyButtons) {
    }

    public void FingerPrintStatsRetryFlagTrue(String ErrorText) {
        setStatusMessage(ErrorText);

        if (m_nCurrentCaptureStep != -1) {
            try {
                boolean IsActive = getIBScanDevice().isCaptureActive();
                if (IsActive) {
                    // Capture image manually for active device
                    getIBScanDevice().captureImageManually();
                    return;
                }
            } catch (IBScanException ibse) {
                setStatusMessage("IBScanDevice.takeResultImageManually() returned exception "
                        + ibse.getType().toString() + ".");
            }
        }

        if (getIBScanDevice() == null) {
            m_bInitializing = true;
            _InitializeDeviceThreadCallback thread = new _InitializeDeviceThreadCallback(0);
            thread.start();
        } else {
            OnMsg_CaptureSeqStart();
        }

        if (viewModel.fingerPrintIndex < 9) {
            viewModel.fingerPrintIndex++;
        } else
            getActivity().finish();
        binding.fingerprintImage.setImageResource(FingerImgIndex.values()[viewModel.arrList.get(viewModel.fingerPrintIndex - 1)].getImage());

    }

    private void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

}
