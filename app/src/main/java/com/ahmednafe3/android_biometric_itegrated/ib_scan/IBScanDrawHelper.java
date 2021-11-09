package com.ahmednafe3.android_biometric_itegrated.ib_scan;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.integratedbiometrics.ibscanultimate.IBScanDevice;

public class IBScanDrawHelper {
    private static IBScanDrawHelper instance;
    private IBScanDevice ibScanDevice;
    private int m_leftMargin;
    private int m_topMargin;
    private double m_scaleFactor;

    public static IBScanDrawHelper getInstance(IBScanDevice device) {
        if (instance == null) {
            synchronized (IBScanDrawHelper.class) {
                if (instance == null) {
                    instance = new IBScanDrawHelper(device);
                }
            }
        }
        return instance;
    }

    public IBScanDrawHelper(IBScanDevice ibScanDevice) {
        this.ibScanDevice = ibScanDevice;
    }

    public IBScanDevice getIBScanDevice() {
        return ibScanDevice;
    }

    public void _DrawOverlay_WarningOfClearPlaten(Canvas canvas, int left, int top, int width, int height, boolean m_bNeedClearPlaten, boolean m_bInitializing, int m_nCurrentCaptureStep) {
        if (getIBScanDevice() == null)
            return;

        boolean idle = !m_bInitializing && (m_nCurrentCaptureStep == -1);

        if (!idle && m_bNeedClearPlaten) {
            Paint g = new Paint();
            g.setStyle(Paint.Style.STROKE);
            g.setColor(Color.RED);
            g.setStrokeWidth(20);
            g.setAntiAlias(true);
            canvas.drawRect(left, top, width - 1, height - 1, g);
        }
    }


    protected void _DrawOverlay_ResultSegmentImage(Canvas canvas, IBScanDevice.ImageData image, int outWidth, int outHeight, int m_nSegmentImageArrayCount, IBScanDevice.SegmentPosition[] m_SegmentPositionArray) {
        if (image.isFinal) {
//			if (m_chkDrawSegmentImage.isSelected())
            {
                // Draw quadrangle for the segment image

                _CalculateScaleFactors(image, outWidth, outHeight);
                Paint g = new Paint();
                g.setColor(Color.rgb(0, 128, 0));
//				g.setStrokeWidth(1);
                g.setStrokeWidth(4);
                g.setAntiAlias(true);
                for (int i = 0; i < m_nSegmentImageArrayCount; i++) {
                    int x1, x2, x3, x4, y1, y2, y3, y4;
                    x1 = m_leftMargin + (int) (m_SegmentPositionArray[i].x1 * m_scaleFactor);
                    x2 = m_leftMargin + (int) (m_SegmentPositionArray[i].x2 * m_scaleFactor);
                    x3 = m_leftMargin + (int) (m_SegmentPositionArray[i].x3 * m_scaleFactor);
                    x4 = m_leftMargin + (int) (m_SegmentPositionArray[i].x4 * m_scaleFactor);
                    y1 = m_topMargin + (int) (m_SegmentPositionArray[i].y1 * m_scaleFactor);
                    y2 = m_topMargin + (int) (m_SegmentPositionArray[i].y2 * m_scaleFactor);
                    y3 = m_topMargin + (int) (m_SegmentPositionArray[i].y3 * m_scaleFactor);
                    y4 = m_topMargin + (int) (m_SegmentPositionArray[i].y4 * m_scaleFactor);

                    canvas.drawLine(x1, y1, x2, y2, g);
                    canvas.drawLine(x2, y2, x3, y3, g);
                    canvas.drawLine(x3, y3, x4, y4, g);
                    canvas.drawLine(x4, y4, x1, y1, g);
                }
            }
        }
    }

    protected void _CalculateScaleFactors(IBScanDevice.ImageData image, int outWidth, int outHeight) {
        int left = 0, top = 0;
        int tmp_width = outWidth;
        int tmp_height = outHeight;
        int imgWidth = image.width;
        int imgHeight = image.height;
        int dispWidth, dispHeight, dispImgX, dispImgY;

        if (outWidth > imgWidth) {
            tmp_width = imgWidth;
            left = (outWidth - imgWidth) / 2;
        }
        if (outHeight > imgHeight) {
            tmp_height = imgHeight;
            top = (outHeight - imgHeight) / 2;
        }

        float ratio_width = (float) tmp_width / (float) imgWidth;
        float ratio_height = (float) tmp_height / (float) imgHeight;

        dispWidth = outWidth;
        dispHeight = outHeight;

        if (ratio_width >= ratio_height) {
            dispWidth = tmp_height * imgWidth / imgHeight;
            dispWidth -= (dispWidth % 4);
            dispHeight = tmp_height;
            dispImgX = (tmp_width - dispWidth) / 2 + left;
            dispImgY = top;
        } else {
            dispWidth = tmp_width;
            dispWidth -= (dispWidth % 4);
            dispHeight = tmp_width * imgHeight / imgWidth;
            dispImgX = left;
            dispImgY = (tmp_height - dispHeight) / 2 + top;
        }

        if (dispImgX < 0) {
            dispImgX = 0;
        }
        if (dispImgY < 0) {
            dispImgY = 0;
        }

        ///////////////////////////////////////////////////////////////////////////////////
        m_scaleFactor = (double) dispWidth / image.width;
        m_leftMargin = dispImgX;
        m_topMargin = dispImgY;
        ///////////////////////////////////////////////////////////////////////////////////
    }

}
