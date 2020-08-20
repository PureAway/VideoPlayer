package com.zcy.player;


import com.zcy.player.model.Model;


/**
 播放器初始化成果回调
 */
public interface IPlayerInitSuccessListener {
    void onPlayerInitSuccess(IMediaPlayer player, Model model);
}
