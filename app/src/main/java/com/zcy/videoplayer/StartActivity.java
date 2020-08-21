package com.zcy.videoplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.zcy.player.BuildConfig;
import com.zcy.player.DefaultPlayerManager;
import com.zcy.player.player.PlayerFactory;
import com.zcy.player.utils.VideoType;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlayerFactory.setPlayManager(DefaultPlayerManager.class);
        VideoType.setShowType(VideoType.SCREEN_TYPE_DEFAULT);
        VideoType.setRenderType(VideoType.TEXTURE);
        PlayerFactory.debug(BuildConfig.DEBUG);
        setContentView(R.layout.activity_start);
        findViewById(R.id.start).setOnClickListener(view -> {
            startActivity(new Intent(StartActivity.this, MainActivity.class));
        });
    }
}
