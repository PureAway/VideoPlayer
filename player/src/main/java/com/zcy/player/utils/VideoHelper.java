package com.zcy.player.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;


import com.zcy.player.builder.VideoOptionBuilder;
import com.zcy.player.listener.LockClickListener;
import com.zcy.player.listener.VideoAllCallBack;
import com.zcy.player.listener.VideoProgressListener;
import com.zcy.player.render.view.VideoGLView;
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
 * 视频帮助类，更加节省资源
 * Created by guoshuyu on 2018/1/15.
 */
public class VideoHelper {

    /**
     * 播放的标志
     */
    private String TAG = "NULL";
    /**
     * 播放器
     */
    private StandardVideoPlayer standardVideoPlayer;
    /**
     * 全屏承载布局
     */
    private ViewGroup mFullViewContainer;
    /**
     * 全屏承载布局
     */
    private ViewGroup mWindowViewContainer;
    /**
     * 记录列表中item的父布局
     */
    private ViewGroup mParent;
    /**
     * 布局
     */
    private ViewGroup.LayoutParams mNormalParams;
    /**
     * 选择工具类
     */
    private OrientationUtils mOrientationUtils;
    /**
     * 可配置旋转 OrientationUtils
     */
    private OrientationOption mOrientationOption;
    /**
     * 播放配置
     */
    private VideoHelperBuilder mVideoOptionBuilder;
    /**
     * 上下文
     */
    private Context mContext;
    /**
     * 播放的位置
     */
    private int mPlayPosition = -1;
    /**
     * 可视保存
     */
    private int mSystemUiVisibility;
    /**
     * 当前是否全屏
     */
    private boolean isFull;
    /**
     * 当前是否小屏
     */
    private boolean isSmall;
    /**
     * 当前item框的屏幕位置
     */
    private int[] mNormalItemRect;
    /**
     * 当前item的大小
     */
    private int[] mNormalItemSize;
    /**
     * handler
     */
    private Handler mHandler = new Handler();


    public VideoHelper(Context context) {
        this(context, new StandardVideoPlayer(context));
    }

    public VideoHelper(Context context, StandardVideoPlayer player) {
        standardVideoPlayer = player;
        this.mContext = context;
        this.mWindowViewContainer = (ViewGroup) (CommonUtil.scanForActivity(context)).findViewById(Window.ID_ANDROID_CONTENT);

    }

    /**
     * 处理全屏逻辑
     */
    private void resolveToFull() {
        mSystemUiVisibility = ((Activity) mContext).getWindow().getDecorView().getSystemUiVisibility();
        CommonUtil.hideSupportActionBar(mContext, mVideoOptionBuilder.isHideActionBar(), mVideoOptionBuilder.isHideStatusBar());
        if (mVideoOptionBuilder.isHideKey()) {
            hideNavKey(mContext);
        }
        isFull = true;
        ViewGroup viewGroup = (ViewGroup) standardVideoPlayer.getParent();
        mNormalParams = standardVideoPlayer.getLayoutParams();
        if (viewGroup != null) {
            mParent = viewGroup;
            viewGroup.removeView(standardVideoPlayer);
        }
        standardVideoPlayer.setIfCurrentIsFullscreen(true);
        standardVideoPlayer.getFullscreenButton().setImageResource(standardVideoPlayer.getShrinkImageRes());
        standardVideoPlayer.getBackButton().setVisibility(View.VISIBLE);
        //设置旋转
        mOrientationUtils = new OrientationUtils((Activity) mContext, standardVideoPlayer, mOrientationOption);
        mOrientationUtils.setEnable(mVideoOptionBuilder.isRotateViewAuto());
        standardVideoPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolveMaterialToNormal(standardVideoPlayer);
            }
        });
        if (mVideoOptionBuilder.isShowFullAnimation()) {
            if (mFullViewContainer instanceof FrameLayout) {
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
        if (mVideoOptionBuilder.isShowFullAnimation()) {
            if (mFullViewContainer != null) {
                mFullViewContainer.setBackgroundColor(Color.BLACK);
            }
        }
        resolveChangeFirstLogic(0);
        if (mFullViewContainer != null) {
            mFullViewContainer.addView(standardVideoPlayer);
        } else {
            mWindowViewContainer.addView(standardVideoPlayer);
        }
    }

    /**
     * 如果是5.0的动画开始位置
     */
    private void resolveMaterialAnimation() {
        mNormalItemRect = new int[2];
        mNormalItemSize = new int[2];
        saveLocationStatus(mContext, mVideoOptionBuilder.isHideActionBar(), mVideoOptionBuilder.isHideStatusBar());
        FrameLayout.LayoutParams lpParent = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        FrameLayout frameLayout = new FrameLayout(mContext);
        frameLayout.setBackgroundColor(Color.BLACK);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(mNormalItemSize[0], mNormalItemSize[1]);
        lp.setMargins(mNormalItemRect[0], mNormalItemRect[1], 0, 0);
        frameLayout.addView(standardVideoPlayer, lp);
        if (mFullViewContainer != null) {
            mFullViewContainer.addView(frameLayout, lpParent);
        } else {
            mWindowViewContainer.addView(frameLayout, lpParent);
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //开始动画
                if (mFullViewContainer != null) {
                    TransitionManager.beginDelayedTransition(mFullViewContainer);
                } else {
                    TransitionManager.beginDelayedTransition(mWindowViewContainer);
                }
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
        int delay = mOrientationUtils.backToProtVideo();
        if (!mVideoOptionBuilder.isShowFullAnimation()) {
            delay = 0;
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isFull = false;
                removeWindowContainer();
                if (mFullViewContainer != null) {
                    mFullViewContainer.removeAllViews();
                }
                if (standardVideoPlayer.getParent() != null) {
                    ((ViewGroup) standardVideoPlayer.getParent()).removeView(standardVideoPlayer);
                }
                mOrientationUtils.setEnable(false);
                standardVideoPlayer.setIfCurrentIsFullscreen(false);
                if (mFullViewContainer != null) {
                    mFullViewContainer.setBackgroundColor(Color.TRANSPARENT);
                }
                mParent.addView(standardVideoPlayer, mNormalParams);
                standardVideoPlayer.getFullscreenButton().setImageResource(standardVideoPlayer.getEnlargeImageRes());
                standardVideoPlayer.getBackButton().setVisibility(View.GONE);
                standardVideoPlayer.setIfCurrentIsFullscreen(false);
                standardVideoPlayer.restartTimerTask();
                if (mVideoOptionBuilder.getVideoAllCallBack() != null) {
                    Debuger.printfLog("onQuitFullscreen");
                    mVideoOptionBuilder.getVideoAllCallBack().onQuitFullscreen(mVideoOptionBuilder.getUrl(), mVideoOptionBuilder.getVideoTitle(), standardVideoPlayer);
                }
                if (mVideoOptionBuilder.isHideKey()) {
                    showNavKey(mContext, mSystemUiVisibility);
                }
                CommonUtil.showSupportActionBar(mContext, mVideoOptionBuilder.isHideActionBar(), mVideoOptionBuilder.isHideStatusBar());
            }
        }, delay);
    }


    /**
     * 动画回到正常效果
     */
    private void resolveMaterialToNormal(final VideoPlayer videoPlayer) {
        if (mVideoOptionBuilder.isShowFullAnimation() && mFullViewContainer instanceof FrameLayout) {
            int delay = mOrientationUtils.backToProtVideo();
            mHandler.postDelayed(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void run() {
                    TransitionManager.beginDelayedTransition(mFullViewContainer);
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) videoPlayer.getLayoutParams();
                    lp.setMargins(mNormalItemRect[0], mNormalItemRect[1], 0, 0);
                    lp.width = mNormalItemSize[0];
                    lp.height = mNormalItemSize[1];
                    //注意配置回来，不然动画效果会不对
                    lp.gravity = Gravity.NO_GRAVITY;
                    videoPlayer.setLayoutParams(lp);
                    mHandler.postDelayed(new Runnable() {
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
        if (mVideoOptionBuilder.isLockLand()) {
            if (time > 0) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mOrientationUtils.getIsLand() != 1) {
                            if (mFullViewContainer != null) {
                                mFullViewContainer.setBackgroundColor(Color.BLACK);
                            }
                            mOrientationUtils.resolveByClick();
                        }
                    }
                }, time);
            } else {
                if (mOrientationUtils.getIsLand() != 1) {
                    if (mFullViewContainer != null) {
                        mFullViewContainer.setBackgroundColor(Color.BLACK);
                    }
                    mOrientationUtils.resolveByClick();
                }
            }
        }
        standardVideoPlayer.setIfCurrentIsFullscreen(true);
        standardVideoPlayer.restartTimerTask();
        if (mVideoOptionBuilder.getVideoAllCallBack() != null) {
            Debuger.printfLog("onEnterFullscreen");
            mVideoOptionBuilder.getVideoAllCallBack().onEnterFullscreen(mVideoOptionBuilder.getUrl(), mVideoOptionBuilder.getVideoTitle(), standardVideoPlayer);
        }
    }

    /**
     * 保存大小和状态
     */
    private void saveLocationStatus(Context context, boolean statusBar, boolean actionBar) {
        mParent.getLocationOnScreen(mNormalItemRect);
        int statusBarH = getStatusBarHeight(context);
        int actionBerH = getActionBarHeight((Activity) context);
        if (statusBar) {
            mNormalItemRect[1] = mNormalItemRect[1] - statusBarH;
        }
        if (actionBar) {
            mNormalItemRect[1] = mNormalItemRect[1] - actionBerH;
        }
        mNormalItemSize[0] = mParent.getWidth();
        mNormalItemSize[1] = mParent.getHeight();
    }


    /**
     * 是否当前播放
     */
    private boolean isPlayView(int position, String tag) {
        return mPlayPosition == position && TAG.equals(tag);
    }

    private boolean isCurrentViewPlaying(int position, String tag) {
        return isPlayView(position, tag);
    }

    private boolean removeWindowContainer() {
        if (mWindowViewContainer != null && mWindowViewContainer.indexOfChild(standardVideoPlayer) != -1) {
            mWindowViewContainer.removeView(standardVideoPlayer);
            return true;
        }
        return false;
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
        this.mPlayPosition = playPosition;
        this.TAG = tag;
    }

    /**
     * 开始播放
     */
    public void startPlay() {

        if (isSmall()) {
            smallVideoToNormal();
        }

        standardVideoPlayer.release();


        if (mVideoOptionBuilder == null) {
            throw new NullPointerException("mVideoOptionBuilder can't be null");
        }

        mVideoOptionBuilder.build(standardVideoPlayer);

        //增加title
        if (standardVideoPlayer.getTitleTextView() != null) {
            standardVideoPlayer.getTitleTextView().setVisibility(View.GONE);
        }

        //设置返回键
        if (standardVideoPlayer.getBackButton() != null) {
            standardVideoPlayer.getBackButton().setVisibility(View.GONE);
        }

        //设置全屏按键功能
        if (standardVideoPlayer.getFullscreenButton() != null) {
            standardVideoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doFullBtnLogic();
                }
            });
        }
        standardVideoPlayer.startPlayLogic();
    }

    /**
     * 全屏按键逻辑
     */
    public void doFullBtnLogic() {
        if (!isFull) {
            resolveToFull();
        } else {
            resolveMaterialToNormal(standardVideoPlayer);
        }
    }


    /**
     * 处理返回正常逻辑
     */
    public boolean backFromFull() {
        boolean isFull = false;
        if (mFullViewContainer != null && mFullViewContainer.getChildCount() > 0) {
            isFull = true;
            resolveMaterialToNormal(standardVideoPlayer);
        } else if (mWindowViewContainer != null && mWindowViewContainer.indexOfChild(standardVideoPlayer) != -1) {
            isFull = true;
            resolveMaterialToNormal(standardVideoPlayer);
        }
        return isFull;
    }

    /**
     * 释放持有的视频
     */
    public void releaseVideoPlayer() {
        removeWindowContainer();
        ViewGroup viewGroup = (ViewGroup) standardVideoPlayer.getParent();
        if (viewGroup != null) {
            viewGroup.removeAllViews();
        }
        mPlayPosition = -1;
        TAG = "NULL";
        if (mOrientationUtils != null) {
            mOrientationUtils.releaseListener();
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
     * 如果不设置即使用默认的 mWindowViewContainer
     *
     * @param fullViewContainer viewGroup
     */
    public void setFullViewContainer(ViewGroup fullViewContainer) {
        this.mFullViewContainer = fullViewContainer;
    }

    /**
     * 可配置旋转 OrientationUtils
     */
    public void setOrientationOption(OrientationOption orientationOption) {
        this.mOrientationOption = orientationOption;
    }

    /**
     * 是否全屏
     */
    public boolean isFull() {
        return isFull;
    }

    /**
     * 设置配置
     */
    public void setVideoOptionBuilder(VideoHelperBuilder mVideoOptionBuilder) {
        this.mVideoOptionBuilder = mVideoOptionBuilder;
    }

    public VideoOptionBuilder getVideoOptionBuilder() {
        return mVideoOptionBuilder;
    }

    public int getPlayPosition() {
        return mPlayPosition;
    }

    public String getPlayTAG() {
        return TAG;
    }

    public boolean isSmall() {
        return isSmall;
    }

    /**
     * 获取播放器,直接拿播放器，根据需要自定义配置
     */
    public StandardVideoPlayer getVideoPlayer() {
        return standardVideoPlayer;
    }

    /**
     * 配置
     */
    public static class VideoHelperBuilder extends VideoOptionBuilder {

        protected boolean mHideActionBar;

        protected boolean mHideStatusBar;

        public boolean isHideActionBar() {
            return mHideActionBar;
        }

        public VideoHelperBuilder setHideActionBar(boolean hideActionBar) {
            this.mHideActionBar = hideActionBar;
            return this;
        }

        public boolean isHideStatusBar() {
            return mHideStatusBar;
        }

        public VideoHelperBuilder setHideStatusBar(boolean hideStatusBar) {
            this.mHideStatusBar = hideStatusBar;
            return this;
        }

        public int getShrinkImageRes() {
            return mShrinkImageRes;
        }

        public int getEnlargeImageRes() {
            return mEnlargeImageRes;
        }

        public int getPlayPosition() {
            return mPlayPosition;
        }

        public int getDialogProgressHighLightColor() {
            return mDialogProgressHighLightColor;
        }

        public int getDialogProgressNormalColor() {
            return mDialogProgressNormalColor;
        }

        public int getDismissControlTime() {
            return mDismissControlTime;
        }

        public long getSeekOnStart() {
            return mSeekOnStart;
        }

        public float getSeekRatio() {
            return mSeekRatio;
        }

        public float getSpeed() {
            return mSpeed;
        }

        public boolean isHideKey() {
            return mHideKey;
        }

        public boolean isShowFullAnimation() {
            return mShowFullAnimation;
        }

        public boolean isNeedShowWifiTip() {
            return mNeedShowWifiTip;
        }

        public boolean isRotateViewAuto() {
            return mRotateViewAuto;
        }

        public boolean isLockLand() {
            return mLockLand;
        }

        public boolean isLooping() {
            return mLooping;
        }

        public boolean isIsTouchWiget() {
            return mIsTouchWiget;
        }

        public boolean isIsTouchWigetFull() {
            return mIsTouchWigetFull;
        }

        public boolean isShowPauseCover() {
            return mShowPauseCover;
        }

        public boolean isRotateWithSystem() {
            return mRotateWithSystem;
        }

        public boolean isCacheWithPlay() {
            return mCacheWithPlay;
        }

        public boolean isNeedLockFull() {
            return mNeedLockFull;
        }

        public boolean isThumbPlay() {
            return mThumbPlay;
        }

        public boolean isSounchTouch() {
            return mSounchTouch;
        }

        public boolean isSetUpLazy() {
            return mSetUpLazy;
        }

        public String getPlayTag() {
            return mPlayTag;
        }

        public String getUrl() {
            return mUrl;
        }

        public String getVideoTitle() {
            return mVideoTitle;
        }

        public File getCachePath() {
            return mCachePath;
        }

        public Map<String, String> getMapHeadData() {
            return mMapHeadData;
        }

        public VideoAllCallBack getVideoAllCallBack() {
            return mVideoAllCallBack;
        }

        public LockClickListener getLockClickListener() {
            return mLockClickListener;
        }

        public View getThumbImageView() {
            return mThumbImageView;
        }

        public Drawable getBottomProgressDrawable() {
            return mBottomProgressDrawable;
        }

        public Drawable getBottomShowProgressDrawable() {
            return mBottomShowProgressDrawable;
        }

        public Drawable getBottomShowProgressThumbDrawable() {
            return mBottomShowProgressThumbDrawable;
        }

        public Drawable getVolumeProgressDrawable() {
            return mVolumeProgressDrawable;
        }

        public Drawable getDialogProgressBarDrawable() {
            return mDialogProgressBarDrawable;
        }

        public VideoGLView.ShaderInterface getEffectFilter() {
            return mEffectFilter;
        }

        public VideoProgressListener getVideoProgressListener() {
            return mVideoProgressListener;
        }
    }


}
