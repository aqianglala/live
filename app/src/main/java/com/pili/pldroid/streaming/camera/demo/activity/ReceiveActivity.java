package com.pili.pldroid.streaming.camera.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pili.pldroid.streaming.camera.demo.R;


public class ReceiveActivity extends ActionBarActivity {

    private static final String MSG_NOT_ALLOW_EMPTY_URL = "Error! URL is empty!";

    private EditText mInputUrlEditText;
    private Button mVideoBtn;
    private Button mAudioBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        mInputUrlEditText = (EditText) findViewById(R.id.input_url);

        mVideoBtn = (Button) findViewById(R.id.btn_video);
        mVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = mInputUrlEditText.getText().toString().trim();
                if (url == null || url.isEmpty()) {
                    Toast.makeText(getApplicationContext(), MSG_NOT_ALLOW_EMPTY_URL, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(ReceiveActivity.this, VideoPlayerActivity.class);
                intent.putExtra("videoPath", url);
                startActivity(intent);
            }
        });

        mAudioBtn = (Button) findViewById(R.id.btn_audio);
        mAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = mInputUrlEditText.getText().toString().trim();
                if (url == null || url.isEmpty()) {
                    Toast.makeText(getApplicationContext(), MSG_NOT_ALLOW_EMPTY_URL, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(ReceiveActivity.this, AudioPlayerActivity.class);
                intent.putExtra("audioPath", url);
                startActivity(intent);
            }
        });
    }

}
