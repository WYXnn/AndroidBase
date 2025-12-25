package com.wyx.commonocr.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sensetime.senseid.sdk.ocr.bank.BankCardApi;
import com.sensetime.senseid.sdk.ocr.common.util.FileUtil;
import com.wyx.commonocr.camera.ActivityUtils;
import com.wyx.commonocr.camera.SenseCamera;
import com.wyx.commonocr.camera.SenseCameraPreview;

import java.io.File;
import java.io.IOException;

abstract class AbstractBankCardActivity extends AppCompatActivity
        implements Camera.PreviewCallback, SenseCameraPreview.StartListener {

    public static final String EXTRA_CARD_ORIENTATION = "extra_card_orientation";
    public static final String EXTRA_CARD_NUMBER = "extra_card_number";
    public static final String EXTRA_BANK_NAME = "extra_bank_name";
    public static final String EXTRA_BANK_ID = "extra_bank_id";
    public static final String EXTRA_CARD_NAME = "extra_card_name";
    public static final String EXTRA_CARD_TYPE = "extra_card_type";
    public static final String EXTRA_CARD_RESULT_IMAGE = "extra_card_result_image";

    public static final int CARD_ORIENTATION_VERTICAL = 1;
    public static final int CARD_ORIENTATION_HORIZONTAL = 2;

//    protected static final String FILES_PATH = Utils.getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath();
    protected static final String MODEL_FILE_NAME = "SenseID_Ocr_Bankcard.model";
    protected static final String LICENSE_FILE_NAME = "SenseID_OCR.lic";

    protected static final int DEFAULT_PREVIEW_WIDTH = 1280;
    protected static final int DEFAULT_PREVIEW_HEIGHT = 960;

    protected SenseCameraPreview mCameraPreview;

    protected SenseCamera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkPermission(Manifest.permission.CAMERA)) {
            setResult(ActivityUtils.RESULT_CODE_NO_PERMISSIONS);
            finish();
            return;
        }

        this.setContentView(getLayoutId());

        this.mCameraPreview = this.findViewById(getPreviewId());
        this.mCameraPreview.setStartListener(this);
        this.mCamera =
                new SenseCamera.Builder(this).setRequestedPreviewSize(DEFAULT_PREVIEW_WIDTH, DEFAULT_PREVIEW_HEIGHT)
                        .build();

        File dir = new File(getFilePath());
        if (!dir.exists()) {
            dir.mkdirs();
        }

        FileUtil.copyAssetsToFile(this, MODEL_FILE_NAME, getFilePath() + MODEL_FILE_NAME);
        FileUtil.copyAssetsToFile(this, LICENSE_FILE_NAME, getFilePath() + LICENSE_FILE_NAME);
    }

    protected abstract int getLayoutId();

    protected abstract int getPreviewId();

    protected abstract String getFilePath();

    @Override
    protected void onResume() {
        super.onResume();

        try {
            this.mCameraPreview.start(this.mCamera);
            this.mCamera.setOnPreviewFrameCallback(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BankCardApi.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        BankCardApi.stop();
        BankCardApi.release();

        this.mCameraPreview.stop();
        this.mCameraPreview.release();

        setResult(RESULT_CANCELED);

        if (!isFinishing()) {
            finish();
        }
    }

    protected boolean checkPermission(String... permissions) {
        if (permissions == null || permissions.length < 1) {
            return true;
        }
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onFail() {
        setResult(ActivityUtils.RESULT_CODE_CAMERA_ERROR);
        if (!isFinishing()) {
            finish();
        }
    }
}