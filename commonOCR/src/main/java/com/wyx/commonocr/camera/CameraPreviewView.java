package com.wyx.commonocr.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class CameraPreviewView extends FrameLayout {

    private int mPreviewWidth = -1;
    private int mPreviewHeight = -1;

    private SurfaceView mSurfaceView = null;
    private View mDebugView = null;

    public CameraPreviewView(Context context) {
        this(context, null);
    }

    public CameraPreviewView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public CameraPreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    /**
     * Get screen orientation from Android resources configuration.
     *
     * @return Configuration.ORIENTATION_LANDSCAPE or Configuration.ORIENTATION_PORTRAIT.
     */
    public int getScreenOrientation() {
        return getResources().getConfiguration().orientation;
    }

    /**
     * Update preview view with new size.
     *
     * @param previewWidth preview view width.
     * @param previewHeight preview view height.
     */
    public void updatePreviewSize(int previewWidth, int previewHeight) {
        mPreviewWidth = previewWidth;
        mPreviewHeight = previewHeight;
        requestLayout();
    }

    /**
     * Draw a rect over preview view.
     *
     * @param color rect color.
     * @param rect rect with left top right bottom.
     */
    public void drawRect(int color, Rect rect) {
        if (rect == null || rect.left >= rect.right || rect.top >= rect.bottom) {
            return;
        }

        if (mDebugView == null) {
            mDebugView = new View(getContext());
            mDebugView.setBackgroundColor(color);
            LayoutParams params =
                    new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.width = rect.right - rect.left;
            params.height = rect.bottom - rect.top;
            params.leftMargin = rect.left;
            params.topMargin = rect.top;
            addView(mDebugView, params);
        } else {
            mDebugView.setBackgroundColor(color);
            LayoutParams params = (LayoutParams) mDebugView.getLayoutParams();
            params.width = rect.right - rect.left;
            params.height = rect.bottom - rect.top;
            params.leftMargin = rect.left;
            params.topMargin = rect.top;
            updateViewLayout(mDebugView, params);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mPreviewWidth < 0 || mPreviewHeight < 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int originalWidth = MeasureSpec.getSize(widthMeasureSpec);
        int originalHeight = MeasureSpec.getSize(heightMeasureSpec);

        int finalWidth;
        int finalHeight;
        if (getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
            if ((float) originalWidth / mPreviewWidth < (float) originalHeight / mPreviewHeight) {
                finalWidth = originalWidth;
                finalHeight = originalWidth * mPreviewHeight / mPreviewWidth;
            } else {
                finalHeight = originalHeight;
                finalWidth = originalHeight * mPreviewWidth / mPreviewHeight;
            }
        } else {
            if ((float) originalWidth / mPreviewHeight < (float) originalHeight / mPreviewWidth) {
                finalHeight = originalHeight;
                finalWidth = originalHeight * mPreviewHeight / mPreviewWidth;
            } else {
                finalWidth = originalWidth;
                finalHeight = originalWidth * mPreviewWidth / mPreviewHeight;
            }
        }

        super.onMeasure(MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY));
    }

    void addSurfaceCallback(SurfaceHolder.Callback callback) {
        mSurfaceView.getHolder().addCallback(callback);
    }

    private void initViews() {
        mSurfaceView = new SurfaceView(getContext());
        addView(mSurfaceView);
    }
}