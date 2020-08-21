package com.zcy.player;


import android.app.Activity;
import android.content.Context;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.zcy.player.listener.MediaPlayerListener;
import com.zcy.player.utils.CommonUtil;
import com.zcy.player.video.base.VideoPlayer;

import static com.zcy.player.utils.CommonUtil.hideNavKey;

/**
 * 视频管理，单例
 */

public class VideoManager extends VideoBaseManager {

    public static final int SMALL_ID = R.id.small_id;

    public static final int FULLSCREEN_ID = R.id.full_id;

    private static VideoManager videoManager;


    private VideoManager() {
        init();
    }

    /**
     * 单例管理器
     */
    public static synchronized VideoManager instance() {
        if (videoManager == null) {
            videoManager = new VideoManager();
        }
        return videoManager;
    }

    /**
     * 同步创建一个临时管理器
     */
    public static synchronized VideoManager tmpInstance(MediaPlayerListener listener) {
        VideoManager videoManager = new VideoManager();
        videoManager.bufferPoint = VideoManager.videoManager.bufferPoint;
        videoManager.optionModelList = VideoManager.videoManager.optionModelList;
        videoManager.playTag = VideoManager.videoManager.playTag;
        videoManager.currentVideoWidth = VideoManager.videoManager.currentVideoWidth;
        videoManager.currentVideoHeight = VideoManager.videoManager.currentVideoHeight;
        videoManager.context = VideoManager.videoManager.context;
        videoManager.lastState = VideoManager.videoManager.lastState;
        videoManager.playPosition = VideoManager.videoManager.playPosition;
        videoManager.timeOut = VideoManager.videoManager.timeOut;
        videoManager.needMute = VideoManager.videoManager.needMute;
        videoManager.needTimeOutOther = VideoManager.videoManager.needTimeOutOther;
        videoManager.setListener(listener);
        return videoManager;
    }

    /**
     * 替换管理器
     */
    public static synchronized void changeManager(VideoManager videoManager) {
        VideoManager.videoManager = videoManager;
    }

    /**
     * 退出全屏，主要用于返回键
     *
     * @return 返回是否全屏
     */
    @SuppressWarnings("ResourceType")
    public static boolean backFromWindowFull(Context context) {
        boolean backFrom = false;
        ViewGroup vp = (ViewGroup) (CommonUtil.scanForActivity(context)).findViewById(Window.ID_ANDROID_CONTENT);
        View oldF = vp.findViewById(FULLSCREEN_ID);
        if (oldF != null) {
            backFrom = true;
            hideNavKey(context);
            if (VideoManager.instance().lastListener() != null) {
                VideoManager.instance().lastListener().onBackFullscreen();
            }
        }
        return backFrom;
    }

    /**
     * 页面销毁了记得调用是否所有的video
     */
    public static void releaseAllVideos() {
        VideoManager.instance().releaseMediaPlayer();
    }


    /**
     * 暂停播放
     */
    public static void onPause() {
        if (VideoManager.instance().listener() != null) {
            VideoManager.instance().listener().onVideoPause();
        }
    }

    /**
     * 恢复播放
     */
    public static void onResume() {
        if (VideoManager.instance().listener() != null) {
            VideoManager.instance().listener().onVideoResume();
        }
    }


    /**
     * 恢复暂停状态
     *
     * @param seek 是否产生seek动作,直播设置为false
     */
    public static void onResume(boolean seek) {
        if (VideoManager.instance().listener() != null) {
            VideoManager.instance().listener().onVideoResume(seek);
        }
    }

    /**
     * 当前是否全屏状态
     *
     * @return 当前是否全屏状态， true代表是。
     */
    public static boolean isFullState(Activity activity) {
        ViewGroup vp = (ViewGroup) (CommonUtil.scanForActivity(activity)).findViewById(Window.ID_ANDROID_CONTENT);
        final View full = vp.findViewById(FULLSCREEN_ID);
        VideoPlayer videoPlayer = null;
        if (full != null) {
            videoPlayer = (VideoPlayer) full;
        }
        return videoPlayer != null;
    }

}