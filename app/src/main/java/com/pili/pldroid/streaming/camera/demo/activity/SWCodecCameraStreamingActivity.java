package com.pili.pldroid.streaming.camera.demo.activity;

import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pili.pldroid.streaming.CameraStreamingManager;
import com.pili.pldroid.streaming.CameraStreamingManager.EncodingType;
import com.pili.pldroid.streaming.CameraStreamingSetting;
import com.pili.pldroid.streaming.StreamingProfile;
import com.pili.pldroid.streaming.StreamingProfile.StreamStatusConfig;
import com.pili.pldroid.streaming.camera.demo.CameraPreviewFrameView;
import com.pili.pldroid.streaming.camera.demo.Config;
import com.pili.pldroid.streaming.camera.demo.R;
import com.pili.pldroid.streaming.widget.AspectFrameLayout;
import com.qiniu.android.dns.DnsManager;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.NetworkInfo;
import com.qiniu.android.dns.http.DnspodFree;
import com.qiniu.android.dns.local.AndroidDnsServer;
import com.qiniu.android.dns.local.Resolver;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by jerikc on 15/10/29.
 */
public class SWCodecCameraStreamingActivity extends StreamingBaseActivity {
    private static final String TAG = "SWCodecCameraStreaming";
    private StreamingProfile mProfile;
    private AspectFrameLayout afl;


    public static DnsManager getMyDnsManager() {
        IResolver r0 = new DnspodFree();
        IResolver r1 = AndroidDnsServer.defaultResolver();
        IResolver r2 = null;
        try {
            r2 = new Resolver(InetAddress.getByName("119.29.29.29"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new DnsManager(NetworkInfo.normal, new IResolver[]{r0, r1, r2});
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        } else {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        super.onCreate(savedInstanceState);

        afl = (AspectFrameLayout) findViewById(R.id.cameraPreview_afl);
        afl.setShowMode(AspectFrameLayout.SHOW_MODE.FULL);
        CameraPreviewFrameView cameraPreviewFrameView = (CameraPreviewFrameView) findViewById(R.id.cameraPreview_surfaceView);

        mShutterButton = (Button) findViewById(R.id.toggleRecording_button);

        mSatusTextView = (TextView) findViewById(R.id.streamingStatus);

        StreamingProfile.AudioProfile aProfile = new StreamingProfile.AudioProfile(44100, 96 * 1024);
        StreamingProfile.VideoProfile vProfile = new StreamingProfile.VideoProfile(30, 1000 * 1024, 48);
        StreamingProfile.AVProfile avProfile = new StreamingProfile.AVProfile(vProfile, aProfile);

        StreamingProfile.Stream stream = new StreamingProfile.Stream(mJSONObject);
        mProfile = new StreamingProfile();
        mProfile.setVideoQuality(StreamingProfile.VIDEO_QUALITY_HIGH3)
                .setAudioQuality(StreamingProfile.AUDIO_QUALITY_MEDIUM2)
//                .setPreferredVideoEncodingSize(960, 544)
                .setEncodingSizeLevel(Config.ENCODING_LEVEL)
                .setEncoderRCMode(StreamingProfile.EncoderRCModes.QUALITY_PRIORITY)
                .setStream(stream)
                .setAVProfile(avProfile)
                .setDnsManager(getMyDnsManager())
                .setStreamStatusConfig(new StreamStatusConfig(3))
//                .setEncodingOrientation(StreamingProfile.ENCODING_ORIENTATION.PORT)
                .setSendingBufferProfile(new StreamingProfile.SendingBufferProfile(0.2f, 0.8f, 3.0f, 20 * 1000));

        CameraStreamingSetting setting = new CameraStreamingSetting();
        setting.setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK)
                .setContinuousFocusModeEnabled(true)
                .setRecordingHint(false)
                .setResetTouchFocusDelayInMs(3000)
                .setFocusMode(CameraStreamingSetting.FOCUS_MODE_CONTINUOUS_PICTURE)
                .setCameraPrvSizeLevel(CameraStreamingSetting.PREVIEW_SIZE_LEVEL.MEDIUM)
                .setCameraPrvSizeRatio(CameraStreamingSetting.PREVIEW_SIZE_RATIO.RATIO_16_9);

        mCameraStreamingManager = new CameraStreamingManager(this, afl, cameraPreviewFrameView, EncodingType.SW_VIDEO_WITH_SW_AUDIO_CODEC);  // soft codec
        mCameraStreamingManager.prepare(setting, mProfile);

        // update the StreamingProfile
//        mProfile.setStream(new Stream(mJSONObject1));
//        mCameraStreamingManager.setStreamingProfile(mProfile);
        mCameraStreamingManager.setStreamingSessionListener(this);
//        mCameraStreamingManager.setNativeLoggingEnabled(false);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) afl
                .getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        afl.setLayoutParams(layoutParams);
    }
}
