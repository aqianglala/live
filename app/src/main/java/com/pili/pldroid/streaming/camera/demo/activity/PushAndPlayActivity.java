package com.pili.pldroid.streaming.camera.demo.activity;

import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PlayerCode;
import com.pili.pldroid.player.widget.VideoView;
import com.pili.pldroid.streaming.CameraStreamingManager;
import com.pili.pldroid.streaming.CameraStreamingSetting;
import com.pili.pldroid.streaming.StreamStatusCallback;
import com.pili.pldroid.streaming.StreamingPreviewCallback;
import com.pili.pldroid.streaming.StreamingProfile;
import com.pili.pldroid.streaming.SurfaceTextureCallback;
import com.pili.pldroid.streaming.camera.demo.CameraPreviewFrameView;
import com.pili.pldroid.streaming.camera.demo.Config;
import com.pili.pldroid.streaming.camera.demo.R;
import com.pili.pldroid.streaming.camera.demo.global.BaseActivity;
import com.pili.pldroid.streaming.camera.demo.utils.StreamJsonUtils;
import com.pili.pldroid.streaming.camera.demo.widget.MediaController;
import com.pili.pldroid.streaming.widget.AspectFrameLayout;
import com.qiniu.android.dns.DnsManager;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.NetworkInfo;
import com.qiniu.android.dns.http.DnspodFree;
import com.qiniu.android.dns.local.AndroidDnsServer;
import com.qiniu.android.dns.local.Resolver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class PushAndPlayActivity extends BaseActivity implements
        IjkMediaPlayer.OnInfoListener,
        IjkMediaPlayer.OnErrorListener,
        IjkMediaPlayer.OnVideoSizeChangedListener,
        IjkMediaPlayer.OnPreparedListener,

        View.OnLayoutChangeListener,
        StreamStatusCallback,
        StreamingPreviewCallback,
        SurfaceTextureCallback,
        CameraPreviewFrameView.Listener,
        CameraStreamingManager.StreamingSessionListener,
        CameraStreamingManager.StreamingStateListener {


    private VideoView mVideoView;
    private EditText et_play;
    private EditText et_push;
    private Button btn_play;
    private Button btn_push;

    private String url;

    private MediaController mMediaController;
    private static final int REQ_DELAY_MILLS = 3000;
    private int mReqDelayMills = REQ_DELAY_MILLS;
    private boolean mIsCompleted = false;
    private Runnable mVideoReconnect;
    private CameraStreamingManager mCameraStreamingManager;
    private AspectFrameLayout afl;

    private StreamingProfile mProfile;
    private CameraPreviewFrameView cameraPreviewFrameView;

    protected static final int MSG_START_STREAMING  = 0;
    protected static final int MSG_STOP_STREAMING   = 1;
    private static final int MSG_SET_ZOOM           = 2;
    private static final int MSG_MUTE               = 3;

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
                            Log.i(TAG, "res:" + res);
                        }
                    }).start();
                    break;
                case MSG_STOP_STREAMING:
                    // disable the shutter button before stopStreaming
                    boolean res = mCameraStreamingManager.stopStreaming();
                    break;
                case MSG_SET_ZOOM:
//                    mCameraStreamingManager.setZoomValue(mCurrentZoom);
                    break;
                case MSG_MUTE:
                    break;
                default:
                    Log.e(TAG, "Invalid message");
            }
        }
    };

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

    public void startStreaming() {
        boolean res = mCameraStreamingManager.startStreaming();
        showToast(res?"0":"1");
    }

    public void stopStreaming() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_STOP_STREAMING), 50);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_push_and_play);
        mVideoView = getViewById(R.id.video_view);

        et_play = getViewById(R.id.et_play);
        et_push = getViewById(R.id.et_push);

        btn_play = getViewById(R.id.btn_play);
        btn_push = getViewById(R.id.btn_push);

        afl = (AspectFrameLayout) findViewById(R.id.cameraPreview_afl);
        afl.setShowMode(AspectFrameLayout.SHOW_MODE.REAL);
        cameraPreviewFrameView = (CameraPreviewFrameView) findViewById(R.id.cameraPreview_surfaceView);
    }

    @Override
    protected void setListener() {
        btn_play.setOnClickListener(this);
        btn_push.setOnClickListener(this);

        mVideoView.setOnErrorListener(this);
//        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnInfoListener(this);
        mVideoView.setOnPreparedListener(this);
//        mVideoView.setOnVideoSizeChangedListener(this);

    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        // 设置显示控制面板
//        mMediaController = new MediaController(this,false,true);
//        mMediaController.setMediaPlayer(mVideoView);
//        mVideoView.setMediaController(mMediaController);

        setAVOptions();

        mVideoView.requestFocus();

        String streamJson = StreamJsonUtils.createStreamJson("rtmp://192.168.1.112/live/123");
        JSONObject jsonObject=null;
        try {
            jsonObject = new JSONObject(streamJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        init(jsonObject);
        mCameraStreamingManager.setStreamingStateListener(this);
        mCameraStreamingManager.setStreamingPreviewCallback(this);
        mCameraStreamingManager.setSurfaceTextureCallback(this);
        mCameraStreamingManager.setStreamingSessionListener(this);
        mCameraStreamingManager.setStreamStatusCallback(this);

        startStreaming();
    }

    /**
     * 播放设置
     */
    private void setAVOptions() {
        AVOptions options = new AVOptions();
        options.setInteger(AVOptions.KEY_MEDIACODEC, 0); // 1 -> enable, 0 -> disable
        options.setInteger(AVOptions.KEY_BUFFER_TIME, 1000); // the unit of buffer time is ms
        options.setInteger(AVOptions.KEY_GET_AV_FRAME_TIMEOUT, 10 * 1000); // the unit of timeout
        // is ms
        options.setString(AVOptions.KEY_FFLAGS, AVOptions.VALUE_FFLAGS_NOBUFFER); // "nobuffer"
        options.setInteger(AVOptions.KEY_LIVE_STREAMING, 1);
        mVideoView.setAVOptions(options);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_play:
                url = et_play.getText().toString().trim();
                if(TextUtils.isEmpty(url)){
                    showToast("地址不能为空！");
                    return;
                }
                mVideoView.setVideoPath(url);
                break;
            case R.id.btn_push:
                url = et_push.getText().toString().trim();
                if(TextUtils.isEmpty(url)){
                    showToast("地址不能为空！");
                    return;
                }
                String streamJson = null;
                JSONObject mJSONObject = null;
                try {
                    streamJson = StreamJsonUtils.createStreamJson(url);
                    mJSONObject = new JSONObject(streamJson);
                }catch (Exception e){
                    showToast(e.getMessage());
                }

                mProfile.setStream(new StreamingProfile.Stream(mJSONObject));
                mCameraStreamingManager.setStreamingProfile(mProfile);
                // update the StreamingProfile
//        mProfile.setStream(new Stream(mJSONObject1));
//        mCameraStreamingManager.setStreamingProfile(mProfile);

                // 开始推流
                startStreaming();
                break;
        }
    }

    private void init(JSONObject mJSONObject) {

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
                .setStreamStatusConfig(new StreamingProfile.StreamStatusConfig(3))
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

        mCameraStreamingManager = new CameraStreamingManager(this, afl, cameraPreviewFrameView, CameraStreamingManager.EncodingType.SW_VIDEO_WITH_SW_AUDIO_CODEC);  // soft codec
        mCameraStreamingManager.prepare(setting, mProfile);
    }

//    @Override
//    public void onCompletion(IMediaPlayer iMediaPlayer) {
//        Log.d(TAG, "onCompletion");
//        showToast("加载完成");
//        mIsCompleted = true;
//        mVideoView.start();
//    }

    @Override
    public boolean onError(IMediaPlayer mp, int what, int extra) {
        Log.d(TAG, "onError what=" + what + ", extra=" + extra);
        if (what == -10000) {
            if (extra == PlayerCode.EXTRA_CODE_INVALID_URI || extra == PlayerCode.EXTRA_CODE_EOF) {
                return true;
            }
            if (mIsCompleted && extra == PlayerCode.EXTRA_CODE_EMPTY_PLAYLIST) {
                Log.d(TAG, "mVideoView reconnect!!!");
                mVideoView.removeCallbacks(mVideoReconnect);
                mVideoReconnect = new Runnable() {
                    @Override
                    public void run() {
                        mVideoView.setVideoPath(url);
                    }
                };
                mVideoView.postDelayed(mVideoReconnect, mReqDelayMills);
                mReqDelayMills += 200;
            } else if (extra == PlayerCode.EXTRA_CODE_404_NOT_FOUND) {
                // NO ts exist
            } else if (extra == PlayerCode.EXTRA_CODE_IO_ERROR) {
                // NO rtmp stream exist
                showToast("网络连接失败，可以弹出提示框，或直接退出页面");
            }
        }
        // return true means you handle the onError, hence System wouldn't handle it again(popup a dialog).
        return true;
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        Log.d(TAG, "onPrepared");
        mReqDelayMills = REQ_DELAY_MILLS;
        showToast("加载成功，可以关闭加载框了");
    }

    /**
     * 横竖屏切换时的回调
     * @param iMediaPlayer
     * @param i
     * @param i1
     * @param i2
     * @param i3
     */
    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {

    }


    @Override
    public void onResume() {
        super.onResume();
        mReqDelayMills = REQ_DELAY_MILLS;
        mVideoView.start();
//        if(mCameraStreamingManager.){
//            mCameraStreamingManager.resume();
//        }
    }

    @Override
    public void onPause() {
        if (mVideoView != null) {
            mVideoView.pause();
        }
        super.onPause();
//        if(mCameraStreamingManager!=null){
//            mCameraStreamingManager.pause();
//        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStop() {
        mVideoView.stopPlayback();
        super.onStop();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if(mCameraStreamingManager!=null){
//            mCameraStreamingManager.destroy();
//        }
    }


    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int
            oldTop, int oldRight, int oldBottom) {

    }

    @Override
    public void notifyStreamStatusChanged(StreamingProfile.StreamStatus streamStatus) {

    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {

    }

    @Override
    public boolean onPreviewFrame(byte[] bytes, int i, int i1) {
        return true;
    }

    @Override
    public void onSurfaceCreated() {

    }

    @Override
    public void onSurfaceChanged(int i, int i1) {

    }

    @Override
    public void onSurfaceDestroyed() {

    }

    @Override
    public int onDrawFrame(int i, int i1, int i2) {
        return 0;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onZoomValueChanged(float factor) {
        return false;
    }

    @Override
    public boolean onRecordAudioFailedHandled(int i) {
        return false;
    }

    @Override
    public boolean onRestartStreamingHandled(int i) {
        return false;
    }

    @Override
    public Camera.Size onPreviewSizeSelected(List<Camera.Size> list) {
        return null;
    }

    @Override
    public void onStateChanged(int i, Object o) {

    }

    @Override
    public boolean onStateHandled(int i, Object o) {
        return false;
    }
}
