package com.pili.pldroid.streaming.camera.demo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pili.pldroid.streaming.camera.demo.Config;
import com.pili.pldroid.streaming.camera.demo.HWCodecCameraStreamingActivity;
import com.pili.pldroid.streaming.camera.demo.R;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final String url = "rtmp://192.168.1.112/live/guolijun";
    private EditText et_url;


    private static boolean isSupportHWEncode() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    void showToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startStreamingActivity(final Intent intent , final String host, final String hub, final String title) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String resByHttp = null;

                if (!Config.DEBUG_MODE) {
                    resByHttp = "{" +
                            "        \"id\":\"z1.test-hub.55d80075e3ba5723280000d2\",\n" +
                            "            \"createdAt\":\"2015-08-22T04:54:13.539Z\",\n" +
                            "            \"updatedAt\":\"2015-08-22T04:54:13.539Z\",\n" +
                            "            \"title\":"+title+",\n" +
                            "            \"hub\":"+hub+",\n" +
                            "            \"disabled\":false,\n" +
                            "            \"publishKey\":\"ca11e07f094c3a6e\",\n" +
                            "            \"publishSecurity\":\"dynamic\",\n" +
                            "            \"hosts\":{\n" +
                            "        \"publish\":{\n" +
                            "            \"rtmp\":"+host+"\n" +
                            "        },\n" +
                            "        \"live\":{\n" +
                            "            \"hdl\":"+host+",\n" +
                            "                    \"hls\":"+host+",\n" +
                            "                    \"rtmp\":"+host+"\n" +
                            "        },\n" +
                            "        \"playback\":{\n" +
                            "            \"hls\":\"ey636h.playback1.z1.pili.qiniucdn.com\"\n" +
                            "        }\n" +
                            "      }\n" +
                            "    }";
                    Log.i(TAG, "resByHttp:" + resByHttp);
                    if (resByHttp == null) {
                        showToast("Stream Json Got Fail!");
                        return;
                    }
                    intent.putExtra(Config.EXTRA_KEY_STREAM_JSON, resByHttp);
                } else {
                    showToast("Stream Json Got Fail!");
                }

                startActivity(intent);
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et_url = (EditText) findViewById(R.id.et_url);

        Button mHWCodecCameraStreamingBtn = (Button) findViewById(R.id.hw_codec_camera_streaming_btn);
        if (!isSupportHWEncode()) {
            mHWCodecCameraStreamingBtn.setVisibility(View.INVISIBLE);
        }
        mHWCodecCameraStreamingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, HWCodecCameraStreamingActivity.class);
//                startStreamingActivity(intent);
                validateUrl( HWCodecCameraStreamingActivity.class);
            }
        });

        Button mSWCodecCameraStreamingBtn = (Button) findViewById(R.id.sw_codec_camera_streaming_btn);
        mSWCodecCameraStreamingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, SWCodecCameraStreamingActivity.class);
//                startStreamingActivity(intent);
                validateUrl( SWCodecCameraStreamingActivity.class);
            }
        });

        Button mAudioStreamingBtn = (Button) findViewById(R.id.start_pure_audio_streaming_btn);
        mAudioStreamingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, AudioStreamingActivity.class);
//                startStreamingActivity(intent);
                validateUrl( AudioStreamingActivity.class);
            }
        });

        Button btn_receive = (Button) findViewById(R.id.btn_receive);
        btn_receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ReceiveActivity.class);
                startActivity(intent);
            }
        });
    }

    private void validateUrl(Class clazz) {
        String url = et_url.getText().toString().trim();
        if(TextUtils.isEmpty(url)){
            showToast("url不能为空");
            return ;
        }
        String[] split = url.split("/");
        for(String str: split){
            Log.i("split",str);
        }
        Log.i("split",split.length+"");
        String host = split[2];
        String hub = split[3];
        String title = split[4];
        if(TextUtils.isEmpty(host)||TextUtils.isEmpty(hub)||TextUtils.isEmpty(title)){
            showToast("地址不合法");
            return ;
        }
        Intent intent = new Intent(MainActivity.this,clazz);
        startStreamingActivity(intent,host,hub,title);
    }
}
