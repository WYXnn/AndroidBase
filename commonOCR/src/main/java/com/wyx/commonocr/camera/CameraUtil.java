package com.wyx.commonocr.camera;

import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.SystemClock;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.sensetime.senseid.sdk.ocr.common.type.Size;

import java.util.List;


public final class CameraUtil {

    private static final int AUTO_FOCUS_INTERVAL = 1000;

    private boolean mIsAutoFocusSuccessed = false;
    private int mCurrentCameraOrientation = -1;
    private long mAutoFocusStartTime = 0L;

    private Camera mCamera = null;
    private Camera.CameraInfo mCameraInfo = null;

    private OnCameraListener mListener = null;

    private Size mPreviewSize = null;
    private Size mScreenSize = null;

    /**
     * Bind PreviewView with camera.
     *
     * @param previewView preview view to show preview from camera.
     */
    public void setPreviewView(final CameraPreviewView previewView, final boolean preferFront, Size previewSize,
                               Size screenSize, final int activityRotation) {
        if (previewView == null) {
            return;
        }

        mIsAutoFocusSuccessed = false;
        mAutoFocusStartTime = -1L;

        mPreviewSize = previewSize;
        mScreenSize = screenSize;
        previewView.addSurfaceCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                openCamera(holder, preferFront);
                updateCameraParameters(previewView, mScreenSize, activityRotation);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                releaseCamera();
            }
        });
    }

    /**
     * Set callback when camera error or received image data from camera.
     *
     * @param listener listener to receive camera messages.
     */
    public void setOnCameraListener(OnCameraListener listener) {
        mListener = listener;
    }

    /**
     * Get camera orientation from camera info.
     *
     * @return camera orientation, it should be 0, 90, 180, or 270.
     */
    public int getCameraOrientation() {
        if (mCurrentCameraOrientation > -1) {
            return mCurrentCameraOrientation;
        }
        if (mCameraInfo == null) {
            return -1;
        }
        return mCameraInfo.orientation;
    }

    /**
     * Get current camera preview size.
     *
     * @return preview size.
     */
    public Size getPreviewSize() {
        return mPreviewSize;
    }

    public boolean isUsingFrontCamera() {
        return mCameraInfo != null && mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
    }

    /**
     * Release camera.
     */
    public void releaseCamera() {
        if (mCamera == null) {
            return;
        }
        try {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCamera = null;
    }

    private void openCamera(SurfaceHolder holder, boolean preferFront) {
        releaseCamera();

        Camera.CameraInfo info = new Camera.CameraInfo();
        int preferFacing = preferFront ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == preferFacing) {
                try {
                    mCamera = Camera.open(i);
                    mCameraInfo = info;
                    break;
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    if (mCamera != null) {
                        mCamera.release();
                        mCamera = null;
                    }
                }
            }
        }
        if (mCamera == null) {
            try {
                mCamera = Camera.open(0);
                mCameraInfo = info;
            } catch (RuntimeException e) {
                e.printStackTrace();
                if (mCamera != null) {
                    mCamera.release();
                    mCamera = null;
                }
            }
        }
        if (mCamera == null) {
            if (mListener != null) {
                mListener.onError(CameraError.OPEN_CAMERA);
            }
            return;
        }
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (Exception ex) {
            releaseCamera();
            if (mListener != null) {
                mListener.onError(CameraError.OPEN_CAMERA);
            }
        }
    }

    private void updateCameraParameters(CameraPreviewView previewView, Size screenSize, int activityRotation) {
        if (mCamera == null) {
            return;
        }

        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewFormat(ImageFormat.NV21);

            // preview size.
            mPreviewSize = getBestPreviewSize(screenSize, mPreviewSize);
            parameters.setPreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            if (parameters.getMinExposureCompensation() < 0 && parameters.getMaxExposureCompensation() > 0 && (Math.abs(
                    parameters.getMinExposureCompensation()) == parameters.getMaxExposureCompensation())) {
                parameters.setExposureCompensation(0);
            }

            // focus mode.
            if (mCameraInfo.facing
                    == Camera.CameraInfo.CAMERA_FACING_BACK) {
                // Do not set focus mode when is front camera.Some devices like SumSung Note4 have problems.
                if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    mIsAutoFocusSuccessed = true;
                } else if (parameters.getSupportedFocusModes()
                        .contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    mIsAutoFocusSuccessed = true;
                } else if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                } else {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
                }
            }

            // scene mode
            parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);

            // display orientation.
            if (previewView.getScreenOrientation() != Configuration.ORIENTATION_LANDSCAPE) {
                parameters.set("orientation", "portrait");
                parameters.set("rotation", 90);
            } else {
                parameters.set("orientation", "landscape");
            }
            setCameraDisplayOrientation(activityRotation);

            mCamera.setParameters(parameters);

            // Camera.setParameters(...) 会设置不成功。重新获取真实的 Camera Preview Size
            final Camera.Size truePreviewSize = mCamera.getParameters().getPreviewSize();

            if (truePreviewSize != null) {
                mPreviewSize = new Size(truePreviewSize.width, truePreviewSize.height);
            }

            previewView.updatePreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    if (mListener != null) {
                        mListener.onCameraDataFetched(data, mPreviewSize);
                        triggerAutoFocus();
                    }
                }
            });
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCameraDisplayOrientation(int rotation) {
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
                break;
        }
        int result;
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (mCameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (mCameraInfo.orientation - degrees + 360) % 360;
        }

        mCamera.setDisplayOrientation(result);
        mCurrentCameraOrientation = result;
    }

    private void triggerAutoFocus() {
        if (mCameraInfo.facing != Camera.CameraInfo.CAMERA_FACING_BACK || mIsAutoFocusSuccessed) {
            return;
        }
        long duration = SystemClock.elapsedRealtime() - mAutoFocusStartTime;
        if (duration < AUTO_FOCUS_INTERVAL) {
            return;
        }
        mAutoFocusStartTime = SystemClock.elapsedRealtime();
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                mIsAutoFocusSuccessed = success;
            }
        });
    }

    private Size getBestPreviewSize(Size screenSize, Size previewSize) {
        List<Camera.Size> supportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();

        if (supportedPreviewSizes == null || supportedPreviewSizes.size() < 1) {
            return null;
        }

        if (previewSize != null) {
            for (Camera.Size size : supportedPreviewSizes) {
                if (size.width == previewSize.getWidth() && size.height == previewSize.getHeight()) {
                    return previewSize;
                } else if (size.width == previewSize.getHeight() && size.height == previewSize.getWidth()) {
                    return new Size(size.width, size.height);
                }
            }
        }

        if (previewSize == null && screenSize == null) {
            return new Size(supportedPreviewSizes.get(0).width, supportedPreviewSizes.get(0).height);
        }

        float sizeRate =
                previewSize == null ? (screenSize.getWidth() > screenSize.getHeight() ? ((float) screenSize.getWidth()
                        / screenSize.getHeight()) : ((float) screenSize.getHeight() / screenSize.getWidth()))
                        : (previewSize.getWidth() > previewSize.getHeight() ? ((float) previewSize.getWidth()
                        / previewSize.getHeight())
                        : ((float) previewSize.getHeight() / previewSize.getWidth()));
        float minDistance = Float.MAX_VALUE;
        for (Camera.Size size : supportedPreviewSizes) {
            float rate = (float) size.width / size.height;
            float distance = Math.abs(rate - sizeRate);
            if (distance < minDistance) {
                minDistance = distance;
                previewSize = new Size(size.width, size.height);
            }
        }
        return previewSize;
    }

    public void openLightOn() {
        if (null == mCamera) {
            mCamera = Camera.open();
        }

        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(parameters);
            mCamera.startPreview();
        } catch (Exception e) {
        }
    }

    public void closeLightOff() {
        if (null == mCamera) {
            mCamera = Camera.open();
        }
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
            mCamera.startPreview();
        } catch (Exception e) {
        }
    }
}