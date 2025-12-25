package com.wyx.commonocr.camera;


import com.sensetime.senseid.sdk.ocr.common.type.Size;


public interface OnCameraListener {

    /**
     * Called when camera data received.
     *
     * @param data image frame data.
     * @param previewSize camera preview size.
     */
    void onCameraDataFetched(byte[] data, Size previewSize);

    /**
     * Called when camera occur.
     *
     * @param error error type.
     */
    void onError(CameraError error);
}