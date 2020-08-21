package com.zcy.player;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Map;

public interface IMediaPlayer {
    int MEDIA_INFO_UNKNOWN = 1;
    int MEDIA_INFO_STARTED_AS_NEXT = 2;
    int MEDIA_INFO_VIDEO_RENDERING_START = 3;
    int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;
    int MEDIA_INFO_BUFFERING_START = 701;
    int MEDIA_INFO_BUFFERING_END = 702;
    int MEDIA_INFO_NETWORK_BANDWIDTH = 703;
    int MEDIA_INFO_BAD_INTERLEAVING = 800;
    int MEDIA_INFO_NOT_SEEKABLE = 801;
    int MEDIA_INFO_METADATA_UPDATE = 802;
    int MEDIA_INFO_TIMED_TEXT_ERROR = 900;
    int MEDIA_INFO_UNSUPPORTED_SUBTITLE = 901;
    int MEDIA_INFO_SUBTITLE_TIMED_OUT = 902;
    int MEDIA_INFO_VIDEO_ROTATION_CHANGED = 10001;
    int MEDIA_INFO_AUDIO_RENDERING_START = 10002;
    int MEDIA_INFO_AUDIO_DECODED_START = 10003;
    int MEDIA_INFO_VIDEO_DECODED_START = 10004;
    int MEDIA_INFO_OPEN_INPUT = 10005;
    int MEDIA_INFO_FIND_STREAM_INFO = 10006;
    int MEDIA_INFO_COMPONENT_OPEN = 10007;
    int MEDIA_INFO_VIDEO_SEEK_RENDERING_START = 10008;
    int MEDIA_INFO_AUDIO_SEEK_RENDERING_START = 10009;
    int MEDIA_INFO_MEDIA_ACCURATE_SEEK_COMPLETE = 10100;
    int MEDIA_ERROR_UNKNOWN = 1;
    int MEDIA_ERROR_SERVER_DIED = 100;
    int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;
    int MEDIA_ERROR_IO = -1004;
    int MEDIA_ERROR_MALFORMED = -1007;
    int MEDIA_ERROR_UNSUPPORTED = -1010;
    int MEDIA_ERROR_TIMED_OUT = -110;

    void setDisplay(SurfaceHolder var1);

    void setDataSource(Context var1, Uri var2) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    @TargetApi(14)
    void setDataSource(Context var1, Uri var2, Map<String, String> var3) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    void setDataSource(FileDescriptor var1) throws IOException, IllegalArgumentException, IllegalStateException;

    void setDataSource(String var1) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    String getDataSource();

    void prepareAsync() throws IllegalStateException;

    void start() throws IllegalStateException;

    void stop() throws IllegalStateException;

    void pause() throws IllegalStateException;

    void surfaceChanged() throws IllegalStateException;

   void setScreenOnWhilePlaying(boolean var1);

    int getVideoWidth();

    int getVideoHeight();

    boolean isPlaying();

    void seekTo(long var1) throws IllegalStateException;

    long getCurrentPosition();

    long getDuration();

    void release();

    void reset();

    void setVolume(float var1, float var2);

    int getAudioSessionId();

    /**
     * @deprecated
     */
    @Deprecated
    void setLogEnabled(boolean var1);

    /**
     * @deprecated
     */
    @Deprecated
    boolean isPlayable();

    void setOnPreparedListener(IMediaPlayer.OnPreparedListener var1);

    void setOnCompletionListener(IMediaPlayer.OnCompletionListener var1);

    void setOnBufferingUpdateListener(IMediaPlayer.OnBufferingUpdateListener var1);

    void setOnSeekCompleteListener(IMediaPlayer.OnSeekCompleteListener var1);

    void setOnVideoSizeChangedListener(IMediaPlayer.OnVideoSizeChangedListener var1);

    void setOnErrorListener(IMediaPlayer.OnErrorListener var1);

    void setOnInfoListener(IMediaPlayer.OnInfoListener var1);

    void setAudioStreamType(int var1);

    /**
     * @deprecated
     */
    @Deprecated
    void setKeepInBackground(boolean var1);

    int getVideoSarNum();

    int getVideoSarDen();

    /**
     * @deprecated
     */
    @Deprecated
    void setWakeMode(Context var1, int var2);

    void setLooping(boolean var1);

    boolean isLooping();

    void setSurface(Surface var1);

    void setDataSource(IMediaDataSource var1);


    public interface OnInfoListener {
        boolean onInfo(IMediaPlayer var1, int var2, int var3);
    }

    public interface OnErrorListener {
        boolean onError(IMediaPlayer var1, int var2, String var3);
    }

    public interface OnVideoSizeChangedListener {
        void onVideoSizeChanged(IMediaPlayer var1, int var2, int var3, int var4, int var5);
    }

    public interface OnSeekCompleteListener {
        void onSeekComplete(IMediaPlayer var1);
    }

    public interface OnBufferingUpdateListener {
        void onBufferingUpdate(IMediaPlayer var1, int var2);
    }

    public interface OnCompletionListener {
        void onCompletion(IMediaPlayer var1);
    }

    public interface OnPreparedListener {
        void onPrepared(IMediaPlayer var1);
    }

}
