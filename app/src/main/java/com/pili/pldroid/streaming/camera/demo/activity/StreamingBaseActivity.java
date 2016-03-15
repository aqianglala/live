package com.pili.pldroid.streaming.camera.demo.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.pili.pldroid.streaming.CameraStreamingManager;
import com.pili.pldroid.streaming.camera.demo.Config;
import com.pili.pldroid.streaming.camera.demo.R;
import com.pili.pldroid.streaming.camera.demo.ui.RotateLayout;
import com.pili.pldroid.streaming.camera.demo.utils.StreamJsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by jerikc on 15/7/6.
 */
public class StreamingBaseActivity extends Activity implements
        CameraStreamingManager.StreamingSessionListener
         {

    private static final String TAG = "StreamingBaseActivity";

    protected Button mShutterButton;
    protected Button mMuteButton;
    protected boolean mShutterButtonPressed = false;
    private boolean mIsNeedMute = false;

    private static final int ZOOM_MINIMUM_WAIT_MILLIS = 33; //ms

    protected static final int MSG_START_STREAMING  = 0;
    protected static final int MSG_STOP_STREAMING   = 1;
    private static final int MSG_SET_ZOOM           = 2;
    private static final int MSG_MUTE               = 3;

    protected String mStatusMsgContent;
    protected TextView mSatusTextView;
    protected String mLogContent = "\n";

    protected CameraStreamingManager mCameraStreamingManager;
    protected boolean mIsReady = false;

    protected RotateLayout mRotateLayout;

    protected JSONObject mJSONObject;
    protected boolean isEncOrientationPort = true;

    private int mCurrentZoom = 0;
    private int mMaxZoom = 0;

    protected Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_STREAMING:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // disable the shutter button before startStreaming
                            boolean res = mCameraStreamingManager.startStreaming();
                            mShutterButtonPressed = true;
                            Log.i(TAG, "res:" + res);
                            if (!res) {
                                mShutterButtonPressed = false;
                            }
                        }
                    }).start();
                    break;
                case MSG_STOP_STREAMING:
                    // disable the shutter button before stopStreaming
                    boolean res = mCameraStreamingManager.stopStreaming();
                    if (!res) {
                        mShutterButtonPressed = true;
                    }
                    break;
                case MSG_SET_ZOOM:
                    mCameraStreamingManager.setZoomValue(mCurrentZoom);
                    break;
                case MSG_MUTE:
                    mIsNeedMute = !mIsNeedMute;
                    mCameraStreamingManager.mute(mIsNeedMute);
                    break;
                default:
                    Log.e(TAG, "Invalid message");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Config.SCREEN_ORIENTATION == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            isEncOrientationPort = true;
        } else if (Config.SCREEN_ORIENTATION == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            isEncOrientationPort = false;
        }
        setRequestedOrientation(Config.SCREEN_ORIENTATION);

        setContentView(R.layout.activity_push_and_play);
//
//        SharedLibraryNameHelper.getInstance().renameSharedLibrary(
//                SharedLibraryNameHelper.PLSharedLibraryType.PL_SO_TYPE_AAC,
//                getApplicationInfo().nativeLibraryDir + "/libpldroid_streaming_aac_encoder_v7a.so");
//
//        SharedLibraryNameHelper.getInstance().renameSharedLibrary(
//                SharedLibraryNameHelper.PLSharedLibraryType.PL_SO_TYPE_CORE, "pldroid_streaming_core");
//
//        SharedLibraryNameHelper.getInstance().renameSharedLibrary(
//                SharedLibraryNameHelper.PLSharedLibraryType.PL_SO_TYPE_H264, "pldroid_streaming_h264_encoder_v7a");
        String streamJson = StreamJsonUtils.createStreamJson("rtmp://192.168.1.112/live/123");
        try {
            mJSONObject = new JSONObject(streamJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraStreamingManager.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mIsReady = false;
        mCameraStreamingManager.pause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraStreamingManager.destroy();
    }


    protected void startStreaming() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_START_STREAMING), 50);
    }

    protected void stopStreaming() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_STOP_STREAMING), 50);
    }

    @Override
    public boolean onRecordAudioFailedHandled(int err) {
        mCameraStreamingManager.updateEncodingType(CameraStreamingManager.EncodingType.SW_VIDEO_CODEC);
        mCameraStreamingManager.startStreaming();
        return true;
    }

    @Override
    public boolean onRestartStreamingHandled(int err) {
        Log.i(TAG, "onRestartStreamingHandled");
        return mCameraStreamingManager.startStreaming();
    }

    @Override
    public Camera.Size onPreviewSizeSelected(List<Camera.Size> list) {
        Camera.Size size = null;
//        if (list != null) {
//            for (Camera.Size s : list) {
//                Log.i(TAG, "w:" + s.width + ", h:" + s.height);
//            }
//        }
//        Log.e(TAG, "selected size :" + size.width + "x" + size.height);
        return size;
    }

}
