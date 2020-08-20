package com.alivc.player;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.BitmapFactory.Options;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AliMediaPlayer implements IPlayer {
    private static final String TAG = "AliMediaPlayer";
    private static Context sContext = null;
    private static final int CMD_PREPARE = 1;
    private static final int CMD_PLAY = 2;
    private static final int CMD_STOP = 3;
    private static final int CMD_PAUSE = 4;
    private static final int CMD_DESTROY = 5;
    private static final int CMD_PREPARE_AND_START = 8;
    private String mUrl;
    private String mKey;
    private int mCircleCount;
    private Surface mSurface;
    private TBMPlayer mPlayer;
    private AliyunErrorCode mErrorCode;
    private int mStatus;
    private MediaPlayerPreparedListener mPreparedListener;
    private MediaPlayerCompletedListener mCompleteListener;
    private MediaPlayerInfoListener mInfoListener;
    private MediaPlayerErrorListener mErrorListener;
    private MediaPlayerVideoSizeChangeListener mVideoSizeChangeListener;
    private MediaPlayerSeekCompleteListener mSeekCompleteListener;
    private MediaPlayerStoppedListener mStopListener;
    private MediaPlayerCircleStartListener mCircleListener;
    private MediaPlayerBufferingUpdateListener mBufferingUpdateListener;
    private MediaPlayerFrameInfoListener mFrameInfoListener;
    private MediaPlayerPcmDataListener mPcmDataListener;
    private MediaPlayerSEIDataListener mSEIDataListener;
    private VideoAdjust mVA;
    private HandlerThread mMediaThread;
    private Handler mHandler;
    private Handler mUIStatusHandler;
    private int mDefaultDecoder;
    private int mSeekPosition;
    private int mPreparePositon;
    private static AtomicBoolean isCanStart = new AtomicBoolean(true);
    private static AtomicInteger WaiteForStartCount = new AtomicInteger(0);
    private static boolean sEnableLog = true;
    private boolean isEOS;
    private long mDownloadBytes;
    private long mReportIndex;
    private boolean cachetEnable;
    private String cachetDir;
    private int cachetMaxDuration;
    private long cachetMaxSize;
    private boolean mIsPublicParamIncome;
    private ScheduledExecutorService executor;
    private boolean reportHeartStarted;
    private boolean isFirstPrepare;
    private VideoMirrorMode mirrorMode;

    protected static void d(String tag, String message) {
        if (sEnableLog) {
            Log.d(tag, message);
        }
    }


    private void stopReportHeart() {
        if (this.executor != null && !this.executor.isShutdown()) {
            this.executor.shutdown();
            this.executor = null;
        }

        this.reportHeartStarted = false;
    }

    private void doHandlePrepare() {
        VcPlayerLog.d(TAG, "prepare");
        isCanStart = new AtomicBoolean(false);
        this._prepare();
    }

    public AliMediaPlayer(Context context, SurfaceView view) {
        this(context, view.getHolder().getSurface());
    }

    public AliMediaPlayer(Context context, Surface surface) {
        this.mUrl = null;
        this.mKey = null;
        this.mCircleCount = 10;
        this.mSurface = null;
        this.mPlayer = null;
        this.mErrorCode = AliyunErrorCode.ALIVC_SUCCESS;
        this.mStatus = CMD_STOP;
        this.mPreparedListener = null;
        this.mCompleteListener = null;
        this.mInfoListener = null;
        this.mErrorListener = null;
        this.mVideoSizeChangeListener = null;
        this.mSeekCompleteListener = null;
        this.mStopListener = null;
        this.mCircleListener = null;
        this.mBufferingUpdateListener = null;
        this.mFrameInfoListener = null;
        this.mPcmDataListener = null;
        this.mSEIDataListener = null;
        this.mVA = null;
        this.mMediaThread = null;
        this.mHandler = null;
        this.mDefaultDecoder = 1;
        this.mSeekPosition = 0;
        this.mPreparePositon = 0;
        this.isEOS = false;
        this.mDownloadBytes = -1L;
        this.mReportIndex = 0L;
        this.cachetEnable = false;
        this.cachetDir = "";
        this.cachetMaxDuration = 0;
        this.cachetMaxSize = 0L;
        this.mIsPublicParamIncome = false;
        this.reportHeartStarted = false;
        this.isFirstPrepare = true;
        this.mirrorMode = VideoMirrorMode.VIDEO_MIRROR_MODE_NONE;
        this.setSurface(surface);
        this.mVA = new VideoAdjust(context);
        VcPlayerLog.d(TAG, "mVA Call create " + this.mVA);
        this.mUIStatusHandler = new AliMediaPlayer.UIStatusHandler(this);
        this.mMediaThread = new HandlerThread("media_thread");
        this.mMediaThread.setName("media_control_1");
        this.mMediaThread.start();
        this.mHandler = new AliMediaPlayer.MediaThreadHandler(this.mMediaThread.getLooper(), this);
        VcPlayerLog.d(TAG, "ThreadManage: media thread id =  " + this.mMediaThread.getId());
    }

    private void handlMediaMesssage(Message msg) {
        VcPlayerLog.d(TAG, "mHandler: handleMessage =  " + msg.what);
        switch (msg.what) {
            case CMD_PREPARE:
                if (this.mStatus != CMD_STOP) {
                    return;
                }
                this.isEOS = false;
                this.doHandlePrepare();
                break;
            case CMD_PLAY:
                if (this.mStatus != CMD_PREPARE && this.mStatus != CMD_PAUSE) {
                    VcPlayerLog.d(TAG, "play , illegalStatus result = ");
                    return;
                }

                VcPlayerLog.d(TAG, "play");
                this._play();
                break;
            case CMD_STOP:
                if (this.mStatus == CMD_STOP) {
                    VcPlayerLog.e(TAG, "stop , mStatus == STOPPED return result = ");
                    return;
                }

                VcPlayerLog.d(TAG, "stop.");
                this._stop();
                this.stopReportHeart();
                break;
            case CMD_PAUSE:
                VcPlayerLog.d(TAG, "pause");
                this._pause();
                this.stopReportHeart();
                break;
            case CMD_DESTROY:
                VcPlayerLog.i(TAG, "mVA destroy " + this.mVA);
                if (this.mPlayer != null) {
                    this._stop();
                    this.mPlayer.release();
                }

                this.stopReportHeart();
                if (this.mVA != null) {
                    VcPlayerLog.d(TAG, "mVA Call destroy " + this.mVA);
                    this.mVA.destroy();
                } else {
                    VcPlayerLog.d(TAG, " mVA destroy !!!NULL!!!");
                }

                this.mVA = null;
                this.mPlayer = null;
                this.mHandler.getLooper().quit();
                this.mMediaThread.quit();
                this.mHandler = null;
                this.mUIStatusHandler = null;
                this.mMediaThread = null;
            case 6:
            case 7:
            default:
                break;
            case CMD_PREPARE_AND_START:
                if (this.mStatus == CMD_PLAY) {
                    return;
                }

                if (this.mStatus == CMD_PREPARE || this.mStatus == CMD_PAUSE) {
                    VcPlayerLog.e(TAG, "prepareAndPlay , mStatus == PREPARED return result = ");
                    this._play();
                    return;
                }

                this.isEOS = false;

                try {
                    while (!isCanStart.get() && WaiteForStartCount.get() < CMD_DESTROY) {
                        Thread.sleep(200L);
                        WaiteForStartCount.incrementAndGet();
                    }
                } catch (InterruptedException var3) {
                    var3.printStackTrace();
                }

                VcPlayerLog.d(TAG, "CMD_PREPARE_AND_START prepare");
                isCanStart = new AtomicBoolean(false);
                int result = this._prepare();
                VcPlayerLog.d(TAG, "CMD_PREPARE_AND_START _prepare result = " + result);
                if (result == 0) {
                    VcPlayerLog.d(TAG, "CMD_PREPARE_AND_START play");
                    this._play();
                } else {
                    VcPlayerLog.d(TAG, "CMD_PREPARE_AND_START prepare fail");
                }
        }
    }


    private TBMPlayer getMPlayer() {
        if (this.mPlayer == null) {
            this.mPlayer = new TBMPlayer(this.mSurface, new IPlayingHandler() {
                @Override
                public int onStatus(int what, int arg0, int arg1, String obj_id) {
                    Message msg = AliMediaPlayer.this.mUIStatusHandler.obtainMessage(what, arg0, arg1, obj_id);
                    AliMediaPlayer.this.mUIStatusHandler.sendMessage(msg);
                    return 0;
                }

                @Override
                public int onData(int what, int arg0, int arg1, byte[] data) {
                    Message msg = AliMediaPlayer.this.mUIStatusHandler.obtainMessage(what, arg0, arg1, data);
                    AliMediaPlayer.this.mUIStatusHandler.sendMessage(msg);
                    return 0;
                }
            });
            this.mPlayer.setPlaySpeed(1.0F);
        }

        return this.mPlayer;
    }

    private void handlUiStatusMesssage(Message msg) {
        int what = msg.what;
        int arg0 = msg.arg1;
        int arg1 = msg.arg2;
        if (what == 9) {
            if (this.mPcmDataListener != null) {
                byte[] msgData = (byte[]) ((byte[]) msg.obj);
                byte[] data = Arrays.copyOf(msgData, msgData.length);
                this.mPcmDataListener.onPcmData(data, arg0);
            }

        } else {
            String customData = "" + msg.obj;
            VcPlayerLog.v(TAG, "receive message : what = " + what + " , arg0 = " + arg0 + " , arg1 = " + arg1);
            switch (what) {
                case 0:
                    if (arg0 == CMD_DESTROY) {
                        if (arg1 == CMD_PREPARE) {
                            this.mErrorCode = AliyunErrorCode.ALIVC_ERROR_LOADING_TIMEOUT;
                            if (this.mErrorListener != null) {
                                this.mErrorListener.onError(this.mErrorCode.getCode(), this.mErrorCode.getDescription(sContext));
                            }
                        } else if (arg1 == CMD_PLAY) {
                            this.mErrorCode = AliyunErrorCode.ALIVC_ERR_INVALID_INPUTFILE;
                            if (this.mErrorListener != null) {
                                this.mErrorListener.onError(this.mErrorCode.getCode(), this.mErrorCode.getDescription(sContext));
                            }
                        } else {
                            this.mErrorCode = AliyunErrorCode.ALIVC_ERR_NO_SUPPORT_CODEC;
                            if (this.mErrorListener != null) {
                                this.mErrorListener.onError(this.mErrorCode.getCode(), this.mErrorCode.getDescription(sContext));
                            }
                        }
                    }
                    break;
                case CMD_PREPARE:
                    if (arg0 == 20) {
                        if (this.mInfoListener != null) {
                            this.mInfoListener.onInfo(101, 0);
                        }
                    }

                    if (arg0 == 21 && this.mInfoListener != null) {
                        this.mInfoListener.onInfo(102, 0);
                    }

                    if (arg0 == 22) {
                        if (this.mInfoListener != null) {
                            this.mInfoListener.onInfo(105, arg1);
                        }

                        if (this.mBufferingUpdateListener != null) {
                            this.mBufferingUpdateListener.onBufferingUpdateListener(arg1);
                        }
                    }

                    if (arg0 == 23) {
                        this.mErrorCode = AliyunErrorCode.ALIVC_ERROR_LOADING_TIMEOUT;
                        if (this.mErrorListener != null) {
                            this.mErrorListener.onError(this.mErrorCode.getCode(), this.mErrorCode.getDescription(sContext));
                        }
                    }

                    if (arg0 == CMD_PREPARE_AND_START) {
                        this.mErrorCode = AliyunErrorCode.ALIVC_ERROR_LOADING_TIMEOUT;
                        if (arg1 == CMD_PREPARE) {
                            if (this.mErrorListener != null) {
                                this.mErrorListener.onError(this.mErrorCode.getCode(), this.mErrorCode.getDescription(sContext));
                            }
                        } else if (arg1 == CMD_PLAY) {
                            this.mErrorCode = AliyunErrorCode.ALIVC_ERR_INVALID_INPUTFILE;
                            if (this.mErrorListener != null) {
                                this.mErrorListener.onError(this.mErrorCode.getCode(), this.mErrorCode.getDescription(sContext));
                            }
                        }
                    }
                    break;
                case CMD_PLAY:
                    if (arg0 == 18) {
                        if (arg1 == 1) {
                            this.mStatus = CMD_STOP;
                            this.isEOS = true;
                            this.mSeekPosition = 0;
                            if (this.mCompleteListener != null) {
                                this.mCompleteListener.onCompleted();
                            }
                        }
                    } else if (arg0 == 17) {
                        if (this.mSeekCompleteListener != null) {
                            this.mSeekCompleteListener.onSeekCompleted();
                        }
                    } else if (arg0 == 16) {
                        if (this.mStopListener != null) {
                            this.mStopListener.onStopped();
                        }
                    } else if (arg0 == 25 && this.mCircleListener != null) {
                        this.mCircleListener.onCircleStart();
                    }
                    break;
                case CMD_STOP:
                    if (arg0 == CMD_STOP) {
                        if (this.mPreparedListener != null) {
                            this.mPreparedListener.onPrepared();
                        }
                    }

                    if (arg0 == CMD_DESTROY) {
                        this.mErrorCode = AliyunErrorCode.ALIVC_ERR_NO_SUPPORT_CODEC;
                        if (arg1 == CMD_PREPARE) {
                            this.mErrorCode = AliyunErrorCode.ALIVC_ERROR_LOADING_TIMEOUT;
                        } else if (arg1 == 12) {
                            this.mErrorCode = AliyunErrorCode.ALIVC_ERR_DONWNLOAD_GET_KEY;
                        } else if (arg1 == CMD_STOP) {
                            this.mErrorCode = AliyunErrorCode.ALIVC_ERR_NO_SUPPORT_CODEC;
                        }

                        if (this.mErrorListener != null) {
                            this.mErrorListener.onError(this.mErrorCode.getCode(), this.mErrorCode.getDescription(sContext));
                        }
                    }

                    if (arg0 == CMD_PLAY) {
                        AliyunErrorCode errorCode = AliyunErrorCode.ALIVC_ERR_NO_SUPPORT_CODEC;
                        if (arg1 == 7) {
                            errorCode = AliyunErrorCode.ALIVC_ERROR_NO_INPUTFILE;
                        } else if (arg1 == 9) {
                            errorCode = AliyunErrorCode.ALIVC_ERR_NO_SUPPORT_CODEC;
                        } else if (arg1 == CMD_PREPARE_AND_START) {
                            errorCode = AliyunErrorCode.ALIVC_ERR_NO_MEMORY;
                        } else if (arg1 == CMD_PAUSE || arg1 == CMD_PLAY || arg1 == 10) {
                            errorCode = AliyunErrorCode.ALIVC_ERR_INVALID_INPUTFILE;
                        }

                        this.mErrorCode = errorCode;
                        if (this.mErrorListener != null) {
                            this.mErrorListener.onError(this.mErrorCode.getCode(), this.mErrorCode.getDescription(sContext));
                        }
                    }
                case CMD_PAUSE:
                case 9:
                case 10:
                default:
                    break;
                case CMD_DESTROY:
                    if (arg1 == 13) {
                        if (this.mErrorListener != null) {
                            this.mErrorListener.onError(AliyunErrorCode.ALIVC_ERROR_DECODE_FAILED.getCode(), AliyunErrorCode.ALIVC_ERROR_DECODE_FAILED.getDescription(sContext));
                        }
                    }
                    break;
                case 6:
                    if (this.mVideoSizeChangeListener != null) {
                        this.mVideoSizeChangeListener.onVideoSizeChange(arg0, arg1);
                    }
                    break;
                case 7:
                    final Map<String, String> userInfo = new HashMap();
                    userInfo.put("videoTime", "" + arg1);
                    userInfo.put("infoType", "" + arg0);
                    if (arg0 != CMD_DESTROY && arg0 != CMD_STOP && arg0 != CMD_PREPARE_AND_START) {
                        if (arg0 == CMD_PLAY) {
                            userInfo.put("seekTime", arg1 + "");
                        }
                    } else {
                        userInfo.put("costTime", customData);
                    }

                    break;
                case CMD_PREPARE_AND_START:
                    if (this.mInfoListener != null) {
                        this.mInfoListener.onInfo(CMD_STOP, 0);
                    }

                    if (this.mFrameInfoListener != null) {
                        this.mFrameInfoListener.onFrameInfoListener();
                    }
                    break;
                case 11:
                    if (arg0 == CMD_DESTROY) {
                        long pts = this.getPropertyLong(20024, 0L);
                    }
                    if (this.mSEIDataListener != null && arg0 == CMD_DESTROY) {
                        this.mSEIDataListener.onSEI_userUnregisteredData(msg.obj.toString());
                    }
            }
        }
    }


    private void setSurface(Surface surface) {
        this.mSurface = surface;
        this.getMPlayer();
    }

    private void _play() {
        if (this.mPlayer != null) {
            int result = this.mPlayer.start();
            if (result == 0) {
                this.mStatus = CMD_PLAY;
            }
        }
    }

    @Override
    public void resume() {
        this.play();
    }

    @Override
    public void play() {
        VcPlayerLog.d(TAG, "play , sendMessage CMD_PLAY result = ");
        Message msg = this.mHandler.obtainMessage();
        msg.what = CMD_PLAY;
        this.mHandler.sendMessage(msg);
    }

    private void _stop() {
        this.mSeekPosition = 0;
        if (this.mPlayer != null) {
            this.mPlayer.stop();
            this.mStatus = CMD_STOP;
            isCanStart = new AtomicBoolean(true);
            WaiteForStartCount = new AtomicInteger(0);
        }
    }

    private long getVideoTime() {
        long videoTime = this.getPropertyLong(20014, 0L);
        if (videoTime < 0L) {
            videoTime = 0L;
        }

        if (videoTime == 0L) {
            videoTime = this.getPropertyLong(20013, 0L);
        }

        if (videoTime < 0L) {
            videoTime = 0L;
        }

        videoTime /= 1000L;
        if (videoTime == 0L) {
            videoTime = (long) this.getCurrentPosition();
        }

        return videoTime;
    }

    @Override
    public void stop() {
        if (this.mHandler == null) {
            VcPlayerLog.e(TAG, "MPlayer: !!!!!!!!!!!!!!!May not stop!!!!!!!!!!.");
        } else {
            this.mHandler.removeMessages(CMD_PAUSE);
            this.mHandler.removeMessages(CMD_PLAY);
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(CMD_STOP);
            VcPlayerLog.d(TAG, "MPlayer: send stop message.");
            Message msg = this.mHandler.obtainMessage();
            msg.what = CMD_STOP;
            boolean result = this.mHandler.sendMessage(msg);
            VcPlayerLog.d(TAG, "stop , sendMessage = CMD_STOP result = " + result);
        }
    }

    @Override
    public void pause() {
        Message msg = this.mHandler.obtainMessage();
        msg.what = CMD_PAUSE;
        this.mHandler.sendMessage(msg);
    }

    private void _pause() {
        if (this.mPlayer != null) {
            this.mPlayer.pause(30000);
            this.mStatus = CMD_PAUSE;
        }

    }

    public int getPlayerState() {
        return this.mStatus;
    }

    private int _prepare() {
        if (!this.mIsPublicParamIncome) {
            if (this.isFirstPrepare) {
                this.isFirstPrepare = false;
            }
        }

        if (this.mIsPublicParamIncome) {
            VcPlayerLog.d(TAG, "saas ... playingcache " + this.cachetEnable);
            this.mPlayer.setPlayingCache(this.cachetEnable, this.cachetDir, this.cachetMaxDuration, this.cachetMaxSize);
        } else {
            CacheRuleChecker cacheRuleChecker = new CacheRuleChecker(this.cachetDir, (long) this.cachetMaxDuration, this.cachetMaxSize);
            boolean enable = cacheRuleChecker.canUrlCache(this.mUrl);
            this.mPlayer.setPlayingCache(enable, this.cachetDir, this.cachetMaxDuration, this.cachetMaxSize);
        }

        int result = -1;
        if (this.mPlayer != null) {
            VcPlayerLog.d("lifujun download", "prepare url = " + this.mUrl + ", key = " + this.mKey + " ， count = " + this.mCircleCount);
            if (this.mPreparePositon > 0) {
                VcPlayerLog.d("lfj1204 ", "vcPlayer prepare mSeekPosition = " + this.mPreparePositon);
                result = this.mPlayer.prepare(this.mUrl, this.mPreparePositon, this.mDefaultDecoder, this.mKey, this.mCircleCount);
            } else {
                VcPlayerLog.d("lfj1204 ", "vcPlayer prepare mSeekPosition = " + this.mSeekPosition);
                result = this.mPlayer.prepare(this.mUrl, this.mSeekPosition, this.mDefaultDecoder, this.mKey, this.mCircleCount);
            }

            this.mStatus = 1;
            VcPlayerLog.d("lfj1204 ", "vcPlayer after prepare  mSeekPosition = 0 ");
            this.mSeekPosition = 0;
            this.mPreparePositon = 0;
        }

        return result;
    }

    public void setDefaultDecoder(int defaultDecoder) {
        this.mDefaultDecoder = defaultDecoder;
    }

    @Override
    public void prepareToPlay(String url) {
        this.setUrl(url);
        this.mKey = null;
        this.mCircleCount = 10;
        Message msg = this.mHandler.obtainMessage();
        msg.what = 1;
        this.mHandler.sendMessage(msg);
    }

    @Override
    public void prepareAndPlay(String url) {
        if (!this.checkAuth()) {
            this.mUIStatusHandler.sendEmptyMessage(20);
            VcPlayerLog.e(TAG, "prepareAndPlay , mStatus == checkAuth return result = ");
        } else {
            this.setUrl(url);
            this.mKey = null;
            this.mCircleCount = 10;
            VcPlayerLog.d(TAG, "prepareAndPlay , status = " + this.mStatus);
            Message msg = this.mHandler.obtainMessage();
            msg.what = CMD_PREPARE_AND_START;
            this.mHandler.sendMessage(msg);
        }
    }

    @Override
    public void prepare(String url, int start_ms, int decoderType, String videoKey, int circleCount) {
        this.setUrl(url);
        this.mKey = videoKey;
        this.mCircleCount = circleCount;
        this.mPreparePositon = start_ms;
        this.mSeekPosition = start_ms;
        this.mDefaultDecoder = decoderType;
        Message msg = this.mHandler.obtainMessage();
        msg.what = 1;
        this.mHandler.sendMessage(msg);
    }

    @Override
    public int getDuration() {
        if (this.mPlayer != null) {
            int totalDuration = this.mPlayer.getTotalDuration();
            return totalDuration;
        } else {
            return 0;
        }
    }

    @Override
    public int getCurrentPosition() {
        if (this.mPlayer != null) {
            return this.isEOS ? this.getDuration() : this.mPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    @Override
    public int getBufferPosition() {
        return this.mPlayer != null ? this.mPlayer.getBufferPosition() : 0;
    }

    @Override
    public void seekTo(int msc) {
        if (this.mPlayer != null) {
            if (this.mStatus == CMD_STOP) {
                this.mSeekPosition = msc;
            } else {
                this.mPlayer.seek_to(msc);
            }

            this.mSeekPosition = msc;
        }

    }

    @Override
    public void seekToAccurate(int msc) {
        if (this.mPlayer != null) {
            if (this.mStatus == CMD_STOP) {
                this.mSeekPosition = msc;
            } else {
                this.mPlayer.seek_to_accurate(msc);
            }

            this.mSeekPosition = msc;
        }

    }

    private void _reset() {
        this.setUrl((String) null);
        this.mErrorCode = AliyunErrorCode.ALIVC_SUCCESS;
        this._stop();
    }

    @Override
    public void reset() {
        this.mHandler.removeMessages(CMD_PAUSE);
        this.mHandler.removeMessages(CMD_PLAY);
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(CMD_STOP);
        this._reset();
    }

    @Override
    public int getErrorCode() {
        return this.mErrorCode.getCode();
    }

    @Override
    public String getErrorDesc() {
        return this.mErrorCode.getDescription(sContext);
    }

    @Override
    public int getVideoWidth() {
        return this.mPlayer != null ? this.mPlayer.getVideoWidth() : 0;
    }

    @Override
    public int getVideoHeight() {
        return this.mPlayer != null ? this.mPlayer.getVideoHeight() : 0;
    }

    @Override
    public boolean isPlaying() {
        return this.mPlayer != null ? this.mPlayer.isPlaying() : false;
    }

    @Override
    public void setVolume(int vol) {
        if (this.mVA != null) {
            this.mVA.SetVolumn((float) vol * 1.0F / 100.0F);
        }

    }

    @Override
    public int getVolume() {
        return this.mVA != null ? this.mVA.getVolume() : 0;
    }

    @Override
    public void setScreenBrightness(int brightness) {
        if (this.mVA != null) {
            this.mVA.setBrightness(brightness);
        }

    }

    @Override
    public int getScreenBrightness() {
        return this.mVA != null ? this.mVA.getScreenBrightness() : -1;
    }

    @Override
    public void setPreparedListener(MediaPlayerPreparedListener listener) {
        this.mPreparedListener = listener;
    }

    @Override
    public void setCompletedListener(MediaPlayerCompletedListener listener) {
        this.mCompleteListener = listener;
    }

    @Override
    public void setInfoListener(MediaPlayerInfoListener listener) {
        this.mInfoListener = listener;
    }

    @Override
    public void setErrorListener(MediaPlayerErrorListener listener) {
        this.mErrorListener = listener;
    }

    @Override
    public void setStoppedListener(MediaPlayerStoppedListener stoppedListener) {
        this.mStopListener = stoppedListener;
    }

    @Override
    public void setSeekCompleteListener(MediaPlayerSeekCompleteListener listener) {
        this.mSeekCompleteListener = listener;
    }

    @Override
    public void setBufferingUpdateListener(MediaPlayerBufferingUpdateListener listener) {
        this.mBufferingUpdateListener = listener;
    }

    @Override
    public void setVideoSizeChangeListener(MediaPlayerVideoSizeChangeListener listener) {
        this.mVideoSizeChangeListener = listener;
    }

    @Override
    public void setPcmDataListener(MediaPlayerPcmDataListener listener) {
        this.mPcmDataListener = listener;
    }

    @Override
    public void setCircleStartListener(MediaPlayerCircleStartListener listener) {
        this.mCircleListener = listener;
    }

    @Override
    public void setSEIDataListener(MediaPlayerSEIDataListener listener) {
        this.mSEIDataListener = listener;
    }

    public void setFrameInfoListener(MediaPlayerFrameInfoListener listener) {
        this.mFrameInfoListener = listener;
    }

    private boolean checkAuth() {
        return true;
    }

    public static void init(Context context) {
        sContext = context.getApplicationContext();
    }

    @Override
    public void destroy() {
        this.setStoppedListener((MediaPlayerStoppedListener) null);
        this.setCompletedListener(null);
        this.setErrorListener(null);
        this.setInfoListener(null);
        this.setVideoSizeChangeListener(null);
        this.setPreparedListener(null);
        this.setBufferingUpdateListener(null);
        this.setSeekCompleteListener(null);
        if (this.mHandler != null) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = CMD_DESTROY;
            this.mHandler.sendMessage(msg);
        }

    }

    @Override
    public double getPropertyDouble(int key, double defaultValue) {
        return this.mPlayer != null ? this.mPlayer.getPropertyDouble(key, defaultValue) : 0.0D;
    }

    @Override
    public long getPropertyLong(int key, long defaultValue) {
        return this.mPlayer != null ? this.mPlayer.getPropertyLong(key, defaultValue) : 0L;
    }

    @Override
    public String getPropertyString(int key, String defaultValue) {
        return this.mPlayer != null ? this.mPlayer.getPropertyString(key, defaultValue) : "";
    }


    @Override
    public void setSurfaceChanged() {
        if (this.mPlayer != null) {
            this.mPlayer.setSurfaceChanged();
        }
    }

    @Override
    public List<VideoNativeLog> getCurrNatvieLog() {
        List<VideoNativeLog> result = new ArrayList();
        if (this.mPlayer != null) {
            VideoNativeLog[] logs = this.mPlayer.getCurrNatvieLog();
            if (logs != null && logs.length > 0) {
                Collections.addAll(result, logs);
            }
        }

        return result;
    }

    @Override
    public void enableNativeLog() {
        if (this.mPlayer != null) {
            this.mPlayer.enableNativeLog();
            VcPlayerLog.enableLog();
        }

    }

    @Override
    public void disableNativeLog() {
        if (this.mPlayer != null) {
            this.mPlayer.disableNativeLog();
            VcPlayerLog.disableLog();
        }

    }

    @Override
    public void setVideoSurface(Surface surface) {
        if (this.mPlayer != null) {
            this.mPlayer.setVideoSurface(surface);
        }

    }

    @Override
    public void releaseVideoSurface() {
        if (this.mPlayer != null) {
            this.mPlayer.releaseVideoSurface();
        }

    }

    @Override
    public void setTimeout(int timeout) {
        if (this.mPlayer != null) {
            this.mPlayer.setTimeout(timeout);
        }

    }

    @Override
    public void setMaxBufferDuration(int duration) {
        if (this.mPlayer != null) {
            this.mPlayer.setDropBufferDuration(duration);
        }

    }

    @Override
    public void setMediaType(MediaType type) {
        if (this.mPlayer != null) {
            this.mPlayer.setLivePlay(type == MediaType.Live ? 1 : 0);
        }

    }

    @Override
    public void setVideoScalingMode(VideoScalingMode scalingMode) {
        if (this.mPlayer != null) {
            this.mPlayer.setVideoScalingMode(scalingMode.ordinal());
        }

    }

    @Override
    public void setRenderMirrorMode(VideoMirrorMode mirrorMode) {
        if (mirrorMode != null) {
            this.mirrorMode = mirrorMode;
            if (this.mPlayer != null) {
                this.mPlayer.setRenderMirrorMode(mirrorMode.ordinal());
            }
        }
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }

    @Override
    public Bitmap snapShot() {
        if (this.mPlayer == null) {
            return null;
        } else if (this.mStatus != CMD_PAUSE && this.mStatus != CMD_PLAY) {
            VcPlayerLog.e(TAG, "stop , mStatus == STOPPED return null ");
            return null;
        } else {
            FrameData data = this.mPlayer.snapShot();
            if (data != null && data.getYuvData() != null && data.getYuvData().length != 0 && this.getVideoWidth() != 0 && this.getVideoHeight() != 0) {
                int width = this.getVideoWidth();
                int height = this.getVideoHeight();
                int frameSize = width * height;
                int size = frameSize * CMD_STOP / CMD_PLAY;
                byte[] yuvData = Arrays.copyOf(data.getYuvData(), size);
                byte[] nv21Data = new byte[yuvData.length];
                System.arraycopy(yuvData, 0, nv21Data, 0, frameSize);

                for (int i = 0; i < frameSize / CMD_PAUSE; ++i) {
                    nv21Data[frameSize + CMD_PLAY * i] = yuvData[frameSize * CMD_DESTROY / CMD_PAUSE + i];
                    nv21Data[frameSize + CMD_PLAY * i + 1] = yuvData[frameSize + i];
                }

                YuvImage yuvImage = new YuvImage(nv21Data, 17, width, height, (int[]) null);
                ByteArrayOutputStream outputSteam = new ByteArrayOutputStream();
                yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, outputSteam);
                byte[] jpgData = outputSteam.toByteArray();
                Bitmap bitmap = BitmapFactory.decodeByteArray(jpgData, 0, jpgData.length, (Options) null);
                if (bitmap != null) {
                    bitmap = this.adjustPhotoRotation(bitmap, data.getRotate());
                }

                return bitmap;
            } else {
                return null;
            }
        }
    }

    @Override
    public void setCirclePlay(boolean circlePlay) {
        if (this.mPlayer != null) {
            this.mPlayer.setCirclePlay(circlePlay);
        }
    }

    @Override
    public void setRenderRotate(VideoRotate rotate) {
        if (rotate != null) {
            if (this.mPlayer != null) {
                this.mPlayer.setRenderRotate(rotate.getRotate());
            }
        }
    }

    Bitmap adjustPhotoRotation(Bitmap source, int angle) {
        Matrix matrix = new Matrix();
        matrix.preRotate((float) angle);
        if (this.mirrorMode != VideoMirrorMode.VIDEO_MIRROR_MODE_NONE) {
            if (this.mirrorMode == VideoMirrorMode.VIDEO_MIRROR_MODE_HORIZONTAL) {
                matrix.preScale(-1.0F, 1.0F);
            } else if (this.mirrorMode == VideoMirrorMode.VIDEO_MIRROR_MODE_VERTICAL) {
                matrix.preScale(1.0F, -1.0F);
            }
        }

        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    @Override
    public Map<String, String> getAllDebugInfo() {
        int propertyId = 10001;

        Object infoMap;
        for (infoMap = new HashMap(); propertyId <= 10003; ++propertyId) {
            infoMap = this.getPropertyInfo(propertyId, (Map) infoMap);
        }

        for (propertyId = 18000; propertyId <= 18004; ++propertyId) {
            infoMap = this.getPropertyInfo(propertyId, (Map) infoMap);
        }

        for (propertyId = 20001; propertyId <= 20024; ++propertyId) {
            infoMap = this.getPropertyInfo(propertyId, (Map) infoMap);
        }

        return (Map) infoMap;
    }

    @Override
    public void setPlaySpeed(float playSpeed) {
        if (this.mPlayer != null) {
            this.mPlayer.setPlaySpeed(playSpeed);
        }
    }

    @Override
    public void setPlayingCache(boolean enable, String saveDir, int maxDuration, long maxSize) {
        this.cachetEnable = enable;
        this.cachetDir = saveDir;
        this.cachetMaxDuration = maxDuration;
        this.cachetMaxSize = maxSize;
    }

    private Map<String, String> getPropertyInfo(int propertyId, Map<String, String> infoMap) {
        long intgerVaule = 0L;
        double doubleValue = 0.0D;
        String strVaule = null;
        if (propertyId <= 10003 && 10001 <= propertyId) {
            doubleValue = this.getPropertyDouble(propertyId, 0.0D);
            strVaule = AliMediaPlayer.PropertyName.getName(propertyId);
            infoMap.put(strVaule, Double.toString(doubleValue).concat(AliMediaPlayer.PropertyName.getSuffixName(propertyId)));
        }

        if (propertyId >= 18000 && 18003 >= propertyId) {
            doubleValue = this.getPropertyDouble(propertyId, 0.0D);
            strVaule = AliMediaPlayer.PropertyName.getName(propertyId);
            infoMap.put(strVaule, Double.toString(doubleValue));
        }

        if (propertyId <= 20024 && 20001 <= propertyId) {
            intgerVaule = this.getPropertyLong(propertyId, 0L);
            strVaule = AliMediaPlayer.PropertyName.getName(propertyId);
            String strCnv;
            if (propertyId != 20007 && propertyId != 20008) {
                if (propertyId != 20005 && propertyId != 20006) {
                    if (propertyId == 20003) {
                        if (intgerVaule == 1L) {
                            strCnv = "AVCodec";
                        } else if (intgerVaule == 2L) {
                            strCnv = "MediaCodec";
                        } else {
                            strCnv = Long.toString(intgerVaule).concat(AliMediaPlayer.PropertyName.getSuffixName(propertyId));
                        }
                    } else {
                        strCnv = Long.toString(intgerVaule).concat(AliMediaPlayer.PropertyName.getSuffixName(propertyId));
                    }
                } else {
                    strCnv = formatedDurationMilli(intgerVaule);
                }
            } else {
                strCnv = formatedSize(intgerVaule);
            }

            infoMap.put(strVaule, strCnv);
        }

        return infoMap;
    }

    private static String formatedDurationMilli(long duration) {
        return duration >= 1000L ? String.format(Locale.US, "%.2f sec", (float) duration / 1000.0F) : String.format(Locale.US, "%d msec", duration);
    }

    private static String formatedSize(long bytes) {
        if (bytes >= 100000L) {
            return String.format(Locale.US, "%.2f MB", (float) bytes / 1000.0F / 1000.0F);
        } else {
            return bytes >= 100L ? String.format(Locale.US, "%.1f KB", (float) bytes / 1000.0F) : String.format(Locale.US, "%d B", bytes);
        }
    }

    @Override
    public void setMuteMode(boolean on) {
        if (this.mPlayer != null) {
            if (on) {
                this.mPlayer.setSteroVolume(0);
            } else {
                this.mPlayer.setSteroVolume(50);
            }
        }

    }

    static enum PropertyName {
        FLT_VIDEO_DECODE_FPS("dec-fps", 10001),
        FLT_VIDEO_OUTPUT_FSP("out-fps", 10002),
        FLT_FFP_PLAYBACK_RATE("plybk-rate", 10003),
        INT64_SELECT_VIDEO_STREAM("select-v", 20001),
        INT64_SELECT_AUDIO_STREAM("select_a", 20002),
        INT64_VIDEO_DECODER("v-dec", 20003),
        INT64_AUDIO_DECODER("a-dec", 20004),
        INT64_VIDEO_CACHE_DURATION("vcache-dur", "sec", 20005),
        INT64_AUDIO_CACHE_DURATION("acache-dur", "sec", 20006),
        INT64_VIDEO_CACHE_BYTES("vcache-bytes", 20007),
        INT64_AUDIO_CACHE_BYTES("acache-bytes", 20008),
        INT64_VIDEO_CACHE_PACKETS("vcache-pkts", 20009),
        INT64_AUDIO_CACHE_PACKETS("acache-pkts", 20010),
        DOUBLE_CREATE_PLAY_TIME("create_player", 18000),
        DOUBLE_OPEN_FORMAT_TIME("open-url", 18001),
        DOUBLE_FIND_STREAM_TIME("find-stream", 18002),
        DOUBLE_OPEN_STREAM_TIME("open-stream", 18003);

        private int mIndex;
        private String mName;
        private String mSuffix;

        private PropertyName(String name, int index) {
            this.mName = name;
            this.mIndex = index;
            this.mSuffix = new String("");
        }

        private PropertyName(String name, String suffix, int index) {
            this.mName = name;
            this.mIndex = index;
            this.mSuffix = suffix;
        }

        public static String getName(int index) {
            AliMediaPlayer.PropertyName[] var1 = values();
            int var2 = var1.length;

            for (int var3 = 0; var3 < var2; ++var3) {
                AliMediaPlayer.PropertyName p = var1[var3];
                if (p.getIndex() == index) {
                    return p.mName;
                }
            }

            return null;
        }

        public static String getSuffixName(int index) {
            AliMediaPlayer.PropertyName[] var1 = values();
            int var2 = var1.length;

            for (int var3 = 0; var3 < var2; ++var3) {
                AliMediaPlayer.PropertyName p = var1[var3];
                if (p.getIndex() == index) {
                    return p.mSuffix;
                }
            }

            return new String("");
        }

        public String getName() {
            return this.mName;
        }

        public int getIndex() {
            return this.mIndex;
        }
    }

    private static class UIStatusHandler extends Handler {
        private WeakReference<AliMediaPlayer> weakPlayer;

        public UIStatusHandler(AliMediaPlayer aliVcMediaPlayer) {
            super(Looper.getMainLooper());
            this.weakPlayer = new WeakReference(aliVcMediaPlayer);
        }

        @Override
        public void handleMessage(Message msg) {
            AliMediaPlayer aliVcMediaPlayer = (AliMediaPlayer) this.weakPlayer.get();
            if (aliVcMediaPlayer != null) {
                aliVcMediaPlayer.handlUiStatusMesssage(msg);
            }

            super.handleMessage(msg);
        }
    }

    private static class MediaThreadHandler extends Handler {
        private WeakReference<AliMediaPlayer> weakPlayer;

        public MediaThreadHandler(Looper looper, AliMediaPlayer aliVcMediaPlayer) {
            super(looper);
            this.weakPlayer = new WeakReference(aliVcMediaPlayer);
        }

        @Override
        public void handleMessage(Message msg) {
            AliMediaPlayer aliVcMediaPlayer = (AliMediaPlayer) this.weakPlayer.get();
            if (aliVcMediaPlayer != null) {
                aliVcMediaPlayer.handlMediaMesssage(msg);
            }

            super.handleMessage(msg);
        }
    }
}
