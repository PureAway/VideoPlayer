package com.zcy.player.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


import com.zcy.player.listener.VideoAllCallBack;
import com.zcy.player.video.StandardVideoPlayer;
import com.zcy.player.video.base.BaseVideoPlayer;
import com.zcy.player.video.base.VideoPlayer;

import java.io.File;
import java.util.Map;

import static com.zcy.player.utils.CommonUtil.getActionBarHeight;
import static com.zcy.player.utils.CommonUtil.getStatusBarHeight;
import static com.zcy.player.utils.CommonUtil.hideNavKey;
import static com.zcy.player.utils.CommonUtil.showNavKey;

/**
 * 列表工具类
 * 其中记得设置进来的fullViewContainer必须是在Activity布局下的最外层布局
 * <p>
 * 这个类开始不再维护了
 */
@Deprecated
public class ListVideoUtil {

    /**
     * 该类从该版本开始不再维护，望悉知
     */
    private String TAG = "NULL"; //播放的标志
    private StandardVideoPlayer standardVideoPlayer;
    private ViewGroup fullViewContainer;
    private ViewGroup listParent;//记录列表中item的父布局
    private ViewGroup.LayoutParams listParams;
    private OrientationUtils orientationUtils;
    private VideoAllCallBack videoAllCallBack;
    private String url;
    private Context context;
    private File cachePath;

    private String mTitle;

    private Map<String, String> mapHeadData;

    private int playPosition = -1; // 播放的位置
    private int speed = 1; // 播放速度，仅支持6.0
    private int systemUiVisibility;
    private boolean isFull; //当前是否全屏
    private boolean isSmall; //当前是否小屏
    private boolean hideStatusBar; //是否隐藏有状态bar
    private boolean hideActionBar; //是否隐藏有状态ActionBar
    private boolean isLoop;//循环
    private boolean hideKey = true;//隐藏按键
    private boolean needLockFull = true;//隐藏按键
    protected boolean needShowWifiTip = true; //是否需要显示流量提示


    private int[] listItemRect;//当前item框的屏幕位置
    private int[] listItemSize;//当前item的大小


    private boolean fullLandFrist = true; //是否全屏就马上横屏
    private boolean autoRotation = true;//是否自动旋转
    private boolean showFullAnimation = true;//是否需要全屏动画

    private Handler handler = new Handler();


    public ListVideoUtil(Context context) {
        standardVideoPlayer = new StandardVideoPlayer(context);
        this.context = context;
    }

    /**
     * 动态添加视频播放
     *
     * @param position  位置
     * @param imgView   封面
     * @param tag       TAG类型
     * @param container player的容器
     * @param playBtn   播放按键
     */
    public void addVideoPlayer(final int position, View imgView, String tag,
                               ViewGroup container, View playBtn) {
        container.removeAllViews();
        if (isCurrentViewPlaying(position, tag)) {
            if (!isFull) {
                ViewGroup viewGroup = (ViewGroup) standardVideoPlayer.getParent();
                if (viewGroup != null) {
                    viewGroup.removeAllViews();
                }
                container.addView(standardVideoPlayer);
                playBtn.setVisibility(View.INVISIBLE);
            }
        } else {
            playBtn.setVisibility(View.VISIBLE);
            container.removeAllViews();   //增加封面
            container.addView(imgView);
        }
    }

    /**
     * 设置列表播放中的位置和TAG,防止错位，回复播放位置
     *
     * @param playPosition 列表中的播放位置
     * @param tag          播放的是哪个列表的tag
     */
    public void setPlayPositionAndTag(int playPosition, String tag) {
        this.playPosition = playPosition;
        this.TAG = tag;
    }

    /**
     * 开始播放
     *
     * @param url 播放的URL
     */
    public void startPlay(String url) {

        if (isSmall()) {
            smallVideoToNormal();
        }

        this.url = url;

        standardVideoPlayer.release();

        standardVideoPlayer.setLooping(isLoop);

        standardVideoPlayer.setSpeed(speed);

        standardVideoPlayer.setNeedShowWifiTip(needShowWifiTip);

        standardVideoPlayer.setNeedLockFull(needLockFull);

        standardVideoPlayer.setUp(url, true, cachePath, mapHeadData, mTitle);

        if (!TextUtils.isEmpty(mTitle)) {
            standardVideoPlayer.getTitleTextView().setText(mTitle);
        }

        //增加title
        standardVideoPlayer.getTitleTextView().setVisibility(View.GONE);

        //设置返回键
        standardVideoPlayer.getBackButton().setVisibility(View.GONE);

        //设置全屏按键功能
        standardVideoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolveFullBtn();
            }
        });

        standardVideoPlayer.startPlayLogic();
    }


    public void resolveFullBtn() {
        if (fullViewContainer == null) {
            return;
        }
        if (!isFull) {
            resolveToFull();
        } else {
            resolveMaterialToNormal(standardVideoPlayer);
        }
    }

    /**
     * 处理全屏逻辑
     */
    private void resolveToFull() {
        systemUiVisibility = ((Activity) context).getWindow().getDecorView().getSystemUiVisibility();
        CommonUtil.hideSupportActionBar(context, hideActionBar, hideStatusBar);
        if (hideKey) {
            hideNavKey(context);
        }
        isFull = true;
        ViewGroup viewGroup = (ViewGroup) standardVideoPlayer.getParent();
        listParams = standardVideoPlayer.getLayoutParams();
        if (viewGroup != null) {
            listParent = viewGroup;
            viewGroup.removeView(standardVideoPlayer);
        }
        standardVideoPlayer.setIfCurrentIsFullscreen(true);
        standardVideoPlayer.getFullscreenButton().setImageResource(standardVideoPlayer.getShrinkImageRes());
        standardVideoPlayer.getBackButton().setVisibility(View.VISIBLE);
        //设置旋转
        orientationUtils = new OrientationUtils((Activity) context, standardVideoPlayer);
        orientationUtils.setEnable(isAutoRotation());
        standardVideoPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolveMaterialToNormal(standardVideoPlayer);
            }
        });
        if (showFullAnimation) {
            if (fullViewContainer instanceof FrameLayout) {
                //目前只做了frameLoayout的判断
                resolveMaterialAnimation();
            } else {
                resolveFullAdd();
            }

        } else {
            resolveFullAdd();
        }
    }

    /**
     * 添加到全屏父布局里
     */
    private void resolveFullAdd() {
        fullViewContainer.setBackgroundColor(Color.BLACK);
        fullViewContainer.addView(standardVideoPlayer);
        resolveChangeFirstLogic(50);
    }

    /**
     * 如果是5.0的动画开始位置
     */
    private void resolveMaterialAnimation() {
        listItemRect = new int[2];
        listItemSize = new int[2];
        saveLocationStatus(context, hideStatusBar, hideActionBar);
        FrameLayout.LayoutParams lpParent = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(Color.BLACK);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(listItemSize[0], listItemSize[1]);
        lp.setMargins(listItemRect[0], listItemRect[1], 0, 0);
        frameLayout.addView(standardVideoPlayer, lp);
        fullViewContainer.addView(frameLayout, lpParent);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //开始动画
                TransitionManager.beginDelayedTransition(fullViewContainer);
                resolveMaterialFullVideoShow(standardVideoPlayer);
                resolveChangeFirstLogic(600);
            }
        }, 300);
    }

    /**
     * 如果是5.0的，要从原位置过度到全屏位置
     */
    private void resolveMaterialFullVideoShow(BaseVideoPlayer baseVideoPlayer) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) baseVideoPlayer.getLayoutParams();
        lp.setMargins(0, 0, 0, 0);
        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.gravity = Gravity.CENTER;
        baseVideoPlayer.setLayoutParams(lp);
        baseVideoPlayer.setIfCurrentIsFullscreen(true);
    }


    /**
     * 处理正常逻辑
     */
    private void resolveToNormal() {
        int delay = orientationUtils.backToProtVideo();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isFull = false;
                fullViewContainer.removeAllViews();
                if (standardVideoPlayer.getParent() != null) {
                    ((ViewGroup) standardVideoPlayer.getParent()).removeView(standardVideoPlayer);
                }
                orientationUtils.setEnable(false);
                standardVideoPlayer.setIfCurrentIsFullscreen(false);
                fullViewContainer.setBackgroundColor(Color.TRANSPARENT);
                listParent.addView(standardVideoPlayer, listParams);
                standardVideoPlayer.getFullscreenButton().setImageResource(standardVideoPlayer.getEnlargeImageRes());
                standardVideoPlayer.getBackButton().setVisibility(View.GONE);
                standardVideoPlayer.setIfCurrentIsFullscreen(false);
                if (videoAllCallBack != null) {
                    Debuger.printfLog("onQuitFullscreen");
                    videoAllCallBack.onQuitFullscreen(url, mTitle, standardVideoPlayer);
                }
                if (hideKey) {
                    showNavKey(context, systemUiVisibility);
                }
                CommonUtil.showSupportActionBar(context, hideActionBar, hideStatusBar);
            }
        }, delay);
    }


    /**
     * 动画回到正常效果
     */
    private void resolveMaterialToNormal(final VideoPlayer videoPlayer) {
        if (showFullAnimation && fullViewContainer instanceof FrameLayout) {
            int delay = orientationUtils.backToProtVideo();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    TransitionManager.beginDelayedTransition(fullViewContainer);
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) videoPlayer.getLayoutParams();
                    lp.setMargins(listItemRect[0], listItemRect[1], 0, 0);
                    lp.width = listItemSize[0];
                    lp.height = listItemSize[1];
                    //注意配置回来，不然动画效果会不对
                    lp.gravity = Gravity.NO_GRAVITY;
                    videoPlayer.setLayoutParams(lp);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            resolveToNormal();
                        }
                    }, 400);
                }
            }, delay);
        } else {
            resolveToNormal();
        }
    }


    /**
     * 是否全屏一开始马上自动横屏
     */
    private void resolveChangeFirstLogic(int time) {
        if (isFullLandFrist()) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (orientationUtils.getIsLand() != 1) {
                        orientationUtils.resolveByClick();
                    }
                }
            }, time);
        }
        standardVideoPlayer.setIfCurrentIsFullscreen(true);
        if (videoAllCallBack != null) {
            Debuger.printfLog("onEnterFullscreen");
            videoAllCallBack.onEnterFullscreen(this.url, mTitle, standardVideoPlayer);
        }
    }

    /**
     * 保存大小和状态
     */
    private void saveLocationStatus(Context context, boolean statusBar, boolean actionBar) {
        listParent.getLocationOnScreen(listItemRect);
        int statusBarH = getStatusBarHeight(context);
        int actionBerH = getActionBarHeight((Activity) context);
        if (statusBar) {
            listItemRect[1] = listItemRect[1] - statusBarH;
        }
        if (actionBar) {
            listItemRect[1] = listItemRect[1] - actionBerH;
        }
        listItemSize[0] = listParent.getWidth();
        listItemSize[1] = listParent.getHeight();
    }


    /**
     * 是否当前播放
     */
    private boolean isPlayView(int position, String tag) {
        return playPosition == position && TAG.equals(tag);
    }

    private boolean isCurrentViewPlaying(int position, String tag) {
        return isPlayView(position, tag);
    }

    /**
     * 处理返回正常逻辑
     */
    public boolean backFromFull() {
        boolean isFull = false;
        if (fullViewContainer.getChildCount() > 0) {
            isFull = true;
            resolveMaterialToNormal(standardVideoPlayer);
        }
        return isFull;
    }

    /**
     * 释放持有的视频
     */
    public void releaseVideoPlayer() {
        ViewGroup viewGroup = (ViewGroup) standardVideoPlayer.getParent();
        if (viewGroup != null) {
            viewGroup.removeAllViews();
        }
        playPosition = -1;
        TAG = "NULL";
        if (orientationUtils != null) {
            orientationUtils.releaseListener();
        }

    }

    /**
     * 显示小屏幕效果
     *
     * @param size      小视频的大小
     * @param actionBar 是否有actionBar
     * @param statusBar 是否有状态栏
     */
    public void showSmallVideo(Point size, final boolean actionBar, final boolean statusBar) {
        if (standardVideoPlayer.getCurrentState() == VideoPlayer.CURRENT_STATE_PLAYING) {
            standardVideoPlayer.showSmallVideo(size, actionBar, statusBar);
            isSmall = true;
        }
    }


    /**
     * 恢复小屏幕效果
     */
    public void smallVideoToNormal() {
        isSmall = false;
        standardVideoPlayer.hideSmallVideo();
    }


    /**
     * 设置全屏显示的viewGroup
     *
     * @param fullViewContainer viewGroup
     */
    public void setFullViewContainer(ViewGroup fullViewContainer) {
        this.fullViewContainer = fullViewContainer;
    }

    /**
     * 是否全屏
     */
    public boolean isFull() {
        return isFull;
    }

    /**
     * 是否自动旋转
     *
     * @param autoRotation 是否要支持重力旋转
     */
    public void setAutoRotation(boolean autoRotation) {
        this.autoRotation = autoRotation;
    }

    public boolean isAutoRotation() {
        return autoRotation;
    }

    /**
     * 是否全屏就马上横屏
     *
     * @param fullLandFrist 如果是，那么全屏的时候就会切换到横屏
     */
    public void setFullLandFrist(boolean fullLandFrist) {
        this.fullLandFrist = fullLandFrist;
    }

    public boolean isFullLandFrist() {
        return fullLandFrist;
    }

    /**
     * 全屏动画
     *
     * @param showFullAnimation 是否使用全屏动画效果
     */
    public void setShowFullAnimation(boolean showFullAnimation) {
        this.showFullAnimation = showFullAnimation;
    }

    public boolean isShowFullAnimation() {
        return showFullAnimation;
    }


    public boolean isHideStatusBar() {
        return hideStatusBar;
    }

    /**
     * 是否隐藏statusBar
     *
     * @param hideStatusBar true的话会隐藏statusBar，在退出全屏的时候会回复显示
     */
    public void setHideStatusBar(boolean hideStatusBar) {
        this.hideStatusBar = hideStatusBar;
    }

    public boolean isHideActionBar() {
        return hideActionBar;
    }

    /**
     * 是否隐藏actionBar
     *
     * @param hideActionBar true的话会隐藏actionbar，在退出全屏的会回复时候显示
     */
    public void setHideActionBar(boolean hideActionBar) {
        this.hideActionBar = hideActionBar;
    }

    /**
     * 视频接口回调
     *
     * @param videoAllCallBack 回调
     */
    public void setVideoAllCallBack(VideoAllCallBack videoAllCallBack) {
        this.videoAllCallBack = videoAllCallBack;
        standardVideoPlayer.setVideoAllCallBack(videoAllCallBack);
    }

    public int getPlayPosition() {
        return playPosition;
    }

    public String getPlayTAG() {
        return TAG;
    }

    public boolean isSmall() {
        return isSmall;
    }


    public boolean isLoop() {
        return isLoop;
    }

    /**
     * 循环
     */
    public void setLoop(boolean loop) {
        isLoop = loop;
    }

    /**
     * 获取当前总时长
     */
    public int getDuration() {
        return standardVideoPlayer.getDuration();
    }


    public int getSpeed() {
        return speed;
    }

    /**
     * 播放速度，仅支持6.0
     */
    public void setSpeed(int speed) {
        this.speed = speed;
    }


    public File getCachePath() {
        return cachePath;
    }

    /**
     * 缓存的路径
     */
    public void setCachePath(File cachePath) {
        this.cachePath = cachePath;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public Map<String, String> getMapHeadData() {
        return mapHeadData;
    }

    public void setMapHeadData(Map<String, String> mapHeadData) {
        this.mapHeadData = mapHeadData;
    }

    /**
     * 获取当前播放进度
     */
    public int getCurrentPositionWhenPlaying() {
        return standardVideoPlayer.getCurrentPositionWhenPlaying();
    }

    /**
     * 获取播放器,直接拿播放器，根据需要自定义配置
     */
    public StandardVideoPlayer getStandardVideoPlayer() {
        return standardVideoPlayer;
    }

    public boolean isHideKey() {
        return hideKey;
    }

    /**
     * 隐藏虚拟按键
     */
    public void setHideKey(boolean hideKey) {
        this.hideKey = hideKey;
    }

    public boolean isNeedLockFull() {
        return needLockFull;
    }

    /**
     * 是否需要全屏锁定屏幕功能
     */
    public void setNeedLockFull(boolean needLoadFull) {
        this.needLockFull = needLoadFull;
    }

    public boolean isNeedShowWifiTip() {
        return needShowWifiTip;
    }

    /**
     * 是否需要显示流量提示,默认true
     */
    public void setNeedShowWifiTip(boolean needShowWifiTip) {
        this.needShowWifiTip = needShowWifiTip;
    }

}
