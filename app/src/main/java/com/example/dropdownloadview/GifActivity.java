package com.example.dropdownloadview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.dropdownloadview.custom_view.GifView;

public class GifActivity extends AppCompatActivity implements View.OnClickListener {

    private GifView mGifView;
    private Button mStart;
    private Button mPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif);
        initView();
        initListener();
    }

    private void initView() {
        mGifView = findViewById(R.id.gifView);
        mStart = findViewById(R.id.start);
        mPause = findViewById(R.id.pause);
    }

    private void initListener() {
        mStart.setOnClickListener(this);
        mPause.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                mGifView.setPaused(false);
                break;
            case R.id.pause:
                mGifView.setPaused(true);
                mGifView.setMovieTime(0);
                break;
        }
    }
}
