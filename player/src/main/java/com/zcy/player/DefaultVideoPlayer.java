package com.zcy.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.alivc.player.AliMediaPlayer;
import com.alivc.player.IPlayer;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class DefaultVideoPlayer extends AbstractMediaPlayer implements IPlayer.MediaPlayerPreparedListener,
        IPlayer.MediaPlayerBufferingUpdateListener,
        IPlayer.MediaPlayerCompletedListener,
        IPlayer.MediaPlayerErrorListener,
        IPlayer.MediaPlayerInfoListener,
        IPlayer.MediaPlayerSeekCompleteListener,
        IPlayer.MediaPlayerVideoSizeChangeListener {

    protected Context mAppContext;
    protected AliMediaPlayer mInternalPlayer;
    protected String mDataSource;
    protected Surface mSurface;
    protected Map<String, String> mHeaders = new HashMap<>();
    protected int mVideoWidth;
    protected int mVideoHeight;
    protected boolean isLooping = false;

    public DefaultVideoPlayer(Context context) {
        mAppContext = context.getApplicationContext();
    }

    @Override
    public void setDisplay(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            setSurface(null);
        } else {
            setSurface(surfaceHolder.getSurface());
        }
    }

    public void setSpeed(float speed) {
        if (null != mInternalPlayer) {
            mInternalPlayer.setPlaySpeed(speed);
        }
    }

    @Override
    public void setDataSource(Context context, Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mDataSource = uri.toString();
    }

    @Override
    public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        if (headers != null) {
            mHeaders.clear();
            mHeaders.putAll(headers);
        }
        setDataSource(context, uri);
    }

    @Override
    public void setDataSource(FileDescriptor fileDescriptor) throws IOException, IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException("no support");
    }

    @Override
    public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(mAppContext, Uri.parse(path));
    }

    @Override
    public String getDataSource() {
        return mDataSource;
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        if (mInternalPlayer != null) {
            throw new IllegalStateException("can't prepare a prepared player");
        }
        prepareAsyncInternal();
    }

    protected void prepareAsyncInternal() {
        new Handler(Looper.getMainLooper()).post(
                () -> {
                    mInternalPlayer = new AliMediaPlayer(mAppContext, mSurface);
                    mInternalPlayer.setPreparedListener(DefaultVideoPlayer.this);
                    mInternalPlayer.setErrorListener(DefaultVideoPlayer.this);
                    mInternalPlayer.setInfoListener(DefaultVideoPlayer.this);
                    mInternalPlayer.setSeekCompleteListener(DefaultVideoPlayer.this);
                    mInternalPlayer.setCompletedListener(DefaultVideoPlayer.this);
                    mInternalPlayer.setVideoSizeChangeListener(DefaultVideoPlayer.this);
                    mInternalPlayer.setBufferingUpdateListener(DefaultVideoPlayer.this);
                    if (mSurface != null) {
                        mInternalPlayer.setVideoSurface(mSurface);
                    }
                    mInternalPlayer.setDefaultDecoder(1);
                    mInternalPlayer.prepareAndPlay(mDataSource);
                }
        );
    }


    @Override
    public void start() throws IllegalStateException {
        if (mInternalPlayer == null) {
            return;
        }
        mInternalPlayer.play();
    }

    @Override
    public void stop() throws IllegalStateException {
        if (mInternalPlayer == null) {
            return;
        }
        mInternalPlayer.stop();
    }

    @Override
    public void pause() throws IllegalStateException {
        if (mInternalPlayer == null) {
            return;
        }
        mInternalPlayer.pause();
    }

    @Override
    public void setScreenOnWhilePlaying(boolean b) {

    }

    @Override
    public int getVideoWidth() {
        return mVideoWidth;
    }

    @Override
    public int getVideoHeight() {
        return mVideoHeight;
    }

    @Override
    public boolean isPlaying() {
        if (mInternalPlayer == null) {
            return false;
        }
        return mInternalPlayer.isPlaying();
    }

    @Override
    public void seekTo(long position) throws IllegalStateException {
        if (mInternalPlayer == null) {
            return;
        }
        mInternalPlayer.seekTo((int) position);
    }

    @Override
    public long getCurrentPosition() {
        if (mInternalPlayer == null) {
            return 0;
        }
        return mInternalPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        if (mInternalPlayer == null) {
            return 0;
        }
        return mInternalPlayer.getDuration();
    }

    @Override
    public void release() {
        if (mInternalPlayer == null) {
            return;
        }
        mInternalPlayer.releaseVideoSurface();
        mInternalPlayer = null;
        mSurface = null;
        mDataSource = null;
        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    @Override
    public void reset() {
        if (mInternalPlayer == null) {
            return;
        }
        mInternalPlayer.reset();
        mInternalPlayer = null;
        mSurface = null;
        mDataSource = null;
        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        if (mInternalPlayer != null) {
            mInternalPlayer.setVolume((int) (leftVolume + rightVolume) / 2);
        }
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }


    @Override
    public void setLogEnabled(boolean b) {
        if (null != mInternalPlayer) {
            return;
        }
        if (b) {
            mInternalPlayer.enableNativeLog();
        } else {
            mInternalPlayer.disableNativeLog();
        }
    }

    @Override
    public boolean isPlayable() {
        return true;
    }

    @Override
    public void setAudioStreamType(int i) {

    }

    @Override
    public void setKeepInBackground(boolean b) {

    }

    @Override
    public int getVideoSarNum() {
        return 1;
    }

    @Override
    public int getVideoSarDen() {
        return 1;
    }

    @Override
    public void setWakeMode(Context context, int i) {
    }

    @Override
    public void setLooping(boolean b) {
        if (null != mInternalPlayer) {
            this.isLooping = b;
            mInternalPlayer.setCirclePlay(b);
        }
    }

    @Override
    public boolean isLooping() {
        return isLooping;
    }


    @Override
    public void setSurface(Surface surface) {
        mSurface = surface;
        if (mInternalPlayer != null) {
            if (surface != null && !surface.isValid()) {
                mSurface = null;
            }
            mInternalPlayer.setVideoSurface(surface);
        }
    }

    @Override
    public void onBufferingUpdateListener(int i) {
        notifyOnBufferingUpdate(i);
    }

    @Override
    public void onCompleted() {
        notifyOnCompletion();
    }

    @Override
    public void onError(int i, String s) {
        notifyOnError(i, s);
    }

    @Override
    public void onInfo(int i, int i1) {
        notifyOnInfo(i, i1);
    }

    @Override
    public void onPrepared() {
        notifyOnPrepared();
    }

    @Override
    public void onSeekCompleted() {
        notifyOnSeekComplete();
    }

    @Override
    public void onVideoSizeChange(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        notifyOnVideoSizeChanged(width, height, 1, 1);
    }

}
