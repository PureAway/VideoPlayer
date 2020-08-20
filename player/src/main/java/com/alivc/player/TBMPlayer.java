package com.alivc.player;

import android.content.Context;
import android.os.PowerManager.WakeLock;
import android.view.Surface;
import java.io.File;
import java.io.FileFilter;
import java.util.Random;
import java.util.regex.Pattern;

public class TBMPlayer {
    public static final int E_MP_UNKNOW = -1;
    public static final int E_MP_OK = 0;
    public static final int E_MP_NONE = 1;
    public static final int E_MP_FAILED = 2;
    public static final int E_MP_UNSUPPORT = 3;
    public static final int E_MP_INVALID_OPERATE = 4;
    public static final int E_MP_OUTOFMEM = 5;
    public static final int E_MP_INVALID_ARGS = 6;
    private boolean mPaused = false;
    private boolean mStarted = false;
    private boolean mEnableRender = true;
    private WakeLock mWakeLock = null;
    private int mPlayerId = -1;

    private int getNumCores() {
        try {
            File dir = new File("/sys/devices/system/cpu/");

            class CpuFilter implements FileFilter {
                CpuFilter() {
                }

                @Override
                public boolean accept(File pathname) {
                    return Pattern.matches("cpu[0-9]", pathname.getName());
                }
            }

            File[] files = dir.listFiles(new CpuFilter());
            return files.length;
        } catch (Exception var3) {
            var3.printStackTrace();
            return 1;
        }
    }

    public TBMPlayer(Surface surface, IPlayingHandler handler) {
        Random random = new Random();
        this.mPlayerId = random.nextInt(10000000);
        NDKCallback.setPlayingHandler(this.getPlayerId(), handler);
        this.mpInit(TBMPlayer.class, NDKCallback.class, VideoNativeLog.class, FrameData.class, surface);
    }

    public TBMPlayer() {
    }

    private void acquireWakeLock() {
    }

    private void releaseWakeLock() {
    }

    public int prepare(String url, int start_ms, int decoderType, String videoKey, int circleCount) {
        int cpu_count = this.getNumCores();
        this.mpSetDecodeThreadNum(cpu_count);
        this.mPaused = false;
        int ms = start_ms < 0 ? 0 : start_ms;
        int result = this.mpPrepare(url, ms, decoderType, videoKey, circleCount);
        return result;
    }

    public int start() {
        this.mPaused = false;
        this.mStarted = true;
        int result = this.mpStart();
        return result;
    }

    public int stop() {
        this.mPaused = false;
        this.mStarted = false;
        int ret = this.mpStop();
        return ret;
    }

    public int pause(int buffering_ms) {
        this.mPaused = true;
        int result = this.mpPause(buffering_ms);
        return result;
    }

    public boolean paused() {
        return this.mPaused;
    }

    public int resume() {
        this.mPaused = false;
        int result = this.mpResume();
        return result;
    }

    public void setVideoScalingMode(int mode) {
        this.mpSetVideoScalingMode(mode);
    }

    public void setVideoSurface(Surface surface) {
        this.mpSetVideoSurface(surface);
    }

    public void releaseVideoSurface() {
        this.mpReleaseVideoSurface();
    }

    public int getCurrentPosition() {
        return this.mpGetCurrentPosition();
    }

    public int getBufferPosition() {
        return this.mpGetBufferPosition();
    }

    public int getTotalDuration() {
        return this.mpGetTotalDuration();
    }

    public boolean isPlaying() {
        return this.mStarted && !this.mPaused;
    }

    public int getVideoWidth() {
        return this.mpGetVideoWidth();
    }

    public void release() {
        NDKCallback.removePlayingHandler(this.mPlayerId);
        this.mpRelease();
    }

    public void setTimeout(int timeout) {
        this.mpSetTimeout(timeout);
    }

    public void setDropBufferDuration(int duration) {
        this.mpSetDropBufferDuration(duration);
    }

    public void setLivePlay(int livePlay) {
        this.mpSetLivePlay(livePlay);
    }

    public int getVideoHeight() {
        return this.mpGetVideoHeight();
    }

    public void onResume() {
        this.acquireWakeLock();
        this.mEnableRender = true;
        this.resume();
    }

    public void onPause() {
        this.releaseWakeLock();
        this.mEnableRender = false;
        this.pause(10000);
    }

    public double getPropertyDouble(int key, double defaultValue) {
        return this.mpGetPropertyDouble(key, defaultValue);
    }

    public long getPropertyLong(int key, long defaultValue) {
        return this.mpGetPropertyLong(key, defaultValue);
    }

    public String getPropertyString(int key, String defaultValue) {
        return this.mpGetPropertyString(key, defaultValue);
    }

    public int seek_to(int ms) {
        return this.mpSeekTo(ms);
    }

    public int seek_to_accurate(int ms) {
        return this.mpSeekToAccurate(ms);
    }

    public void setSurfaceChanged() {
        this.mpSetSurfaceChanged();
    }

    public VideoNativeLog[] getCurrNatvieLog() {
        return this.mpGetCurrNatvieLog();
    }

    public void enableNativeLog() {
        this.mpEnableNativeLog();
    }

    public void disableNativeLog() {
        this.mpDisableNativeLog();
    }

    public int getPlayerId() {
        return this.mPlayerId;
    }

    public void setSteroVolume(int volume) {
        this.mpSetStereoVolume(volume);
    }

    public static String getClientRand() {
        return mpGetRand();
    }

    public static String getEncryptRand(String clientRand) {
        return mpGetEncryptRand(clientRand);
    }

    public static String getKey(String clientRand, String serverRand, String serverPlainText) {
        return mpGetKey(clientRand, serverRand, serverPlainText);
    }

    public static int getCircleCount(String clientRand, String serverRand, String complexity) {
        return mpGetCircleCount(clientRand, serverRand, complexity);
    }

    public static void setEncryptFile(String filepath, Context context) {
        mpSetEncryptFile(filepath, context);
    }

    public static void setDownloadMode(String downloadMode) {
        mpSetDownloadMode(downloadMode);
    }

    public void setPlayingCache(boolean enable, String saveDir, int maxDuration, long maxSize) {
        this.mpSetPlayingDownload(enable, saveDir, maxDuration, maxSize);
    }

    public void setPlaySpeed(float playSpeed) {
        this.mpSetPlaySpeed(playSpeed);
    }

    public FrameData snapShot() {
        return this.mpCaptureFrame();
    }

    public void setCirclePlay(boolean isCirclePlay) {
        this.mpSetCirclePlay(isCirclePlay);
    }

    public void setRenderRotate(int rotate) {
        this.mpSetRenderRotate(rotate);
    }

    public void setRenderMirrorMode(int mode) {
        this.mpSetRenderMirrorMode(mode);
    }

    public boolean isLivePlayer() {
        return this.mpIsLivePlayer();
    }

    private native int mpPrepare(String var1, int var2, int var3, String var4, int var5);

    private native int mpStart();

    private native int mpStop();

    private native int mpPause(int var1);

    private native int mpResume();

    private native int mpSeekTo(int var1);

    private native int mpSeekToAccurate(int var1);

    private native int mpGetCurrentPosition();

    private native int mpGetBufferPosition();

    private native int mpGetTotalDuration();

    private native boolean mpIsPlaying();

    private native int mpGetVideoHeight();

    private native int mpGetVideoWidth();

    private native int mpSetDecodeThreadNum(int var1);

    private native int mpInit(Class<TBMPlayer> var1, Class<NDKCallback> var2, Class<VideoNativeLog> var3, Class<FrameData> var4, Surface var5);

    private native void mpRelease();

    private native double mpGetPropertyDouble(int var1, double var2);

    private native long mpGetPropertyLong(int var1, long var2);

    private native String mpGetPropertyString(int var1, String var2);

    private native void mpSetSurfaceChanged();

    private native void mpEnableNativeLog();

    private native void mpDisableNativeLog();

    private native VideoNativeLog[] mpGetCurrNatvieLog();

    private native void mpSetVideoSurface(Surface var1);

    private native void mpReleaseVideoSurface();

    private native void mpSetTimeout(int var1);

    private native void mpSetDropBufferDuration(int var1);

    private native void mpSetLivePlay(int var1);

    private native void mpSetVideoScalingMode(int var1);

    private native void mpSetStereoVolume(int var1);

    private native int mpFoo();

    private static native String mpGetRand();

    private static native String mpGetEncryptRand(String var0);

    private static native String mpGetKey(String var0, String var1, String var2);

    private static native int mpGetCircleCount(String var0, String var1, String var2);

    private static native void mpSetEncryptFile(String var0, Context var1);

    private static native void mpSetDownloadMode(String var0);

    private native void mpSetPlayingDownload(boolean var1, String var2, int var3, long var4);

    private native void mpSetPlaySpeed(float var1);

    private native FrameData mpCaptureFrame();

    private native void mpSetCirclePlay(boolean var1);

    private native void mpSetRenderRotate(int var1);

    private native void mpSetRenderMirrorMode(int var1);

    private native boolean mpIsLivePlayer();

    static {
        try {
            System.loadLibrary("ffmpeg");
        } catch (Throwable var1) {
            VcPlayerLog.e("AlivcPlayer", "ffmepg.so not found.");
        }

        System.loadLibrary("curl");
        System.loadLibrary("tbSoundTempo");
        System.loadLibrary("tbMPlayer");
    }
}
