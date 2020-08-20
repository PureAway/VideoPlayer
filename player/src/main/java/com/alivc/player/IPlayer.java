package com.alivc.player;

import android.graphics.Bitmap;
import android.view.Surface;
import java.util.List;
import java.util.Map;

public interface IPlayer {
    int PROP_DOUBLE_VIDEO_DECODE_FRAMES_PER_SECOND = 10001;
    int PROP_DOUBLE_VIDEO_OUTPUT_FRAMES_PER_SECOND = 10002;
    int FFP_PROP_DOUBLE_PLAYBACK_RATE = 10003;
    int FFP_PROP_DOUBLE_END = 10003;
    int FFP_PROP_DOUBLE_CREATE_PLAY_TIME = 18000;
    int FFP_PROP_DOUBLE_OPEN_FORMAT_TIME = 18001;
    int FFP_PROP_DOUBLE_FIND_STREAM_TIME = 18002;
    int FFP_PROP_DOUBLE_OPEN_STREAM_TIME = 18003;
    int FFP_PROP_DOUBLE_1st_VFRAME_SHOW_TIME = 18004;
    int FFP_PROP_DOUBLE_1st_AFRAME_SHOW_TIME = 18005;
    int FFP_PROP_DOUBLE_1st_VPKT_GET_TIME = 18006;
    int FFP_PROP_DOUBLE_1st_APKT_GET_TIME = 18007;
    int FFP_PROP_DOUBLE_1st_VDECODE_TIME = 18008;
    int FFP_PROP_DOUBLE_1st_ADECODE_TIME = 18009;
    int FFP_PROP_DOUBLE_DECODE_TYPE = 18010;
    int FFP_PROP_DOUBLE_LIVE_DISCARD_DURATION = 18011;
    int FFP_PROP_DOUBLE_LIVE_DISCARD_CNT = 18012;
    int FFP_PROP_DOUBLE_DISCARD_VFRAME_CNT = 18013;
    int FFP_PROP_DOUBLE_RTMP_OPEN_DURATION = 18040;
    int FFP_PROP_DOUBLE_RTMP_OPEN_RTYCNT = 18041;
    int FFP_PROP_DOUBLE_RTMP_NEGOTIATION_DURATION = 18042;
    int FFP_PROP_DOUBLE_HTTP_OPEN_DURATION = 18060;
    int FFP_PROP_DOUBLE_HTTP_OPEN_RTYCNT = 18061;
    int FFP_PROP_DOUBLE_HTTP_REDIRECT_CNT = 18062;
    int FFP_PROP_DOUBLE_TCP_CONNECT_TIME = 18080;
    int FFP_PROP_DOUBLE_TCP_DNS_TIME = 18081;
    int FFP_PROP_DOUBLE_FIRST_VIDEO_DECODE_TIME = 18082;
    int FFP_PROP_DOUBLE_FIRST_VIDEO_RENDER_TIME = 18083;
    int FFP_PROP_INT64_SELECTED_VIDEO_STREAM = 20001;
    int FFP_PROP_INT64_SELECTED_AUDIO_STREAM = 20002;
    int FFP_PROP_INT64_VIDEO_DECODER = 20003;
    int FFP_PROP_INT64_AUDIO_DECODER = 20004;
    int FFP_PROP_INT64_VIDEO_CACHED_DURATION = 20005;
    int FFP_PROP_INT64_AUDIO_CACHED_DURATION = 20006;
    int FFP_PROP_INT64_VIDEO_CACHED_BYTES = 20007;
    int FFP_PROP_INT64_AUDIO_CACHED_BYTES = 20008;
    int FFP_PROP_INT64_VIDEO_CACHED_PACKETS = 20009;
    int FFP_PROP_INT64_AUDIO_CACHED_PACKETS = 20010;
    int FFP_PROP_INT64_VIDEO_DOWNLOAD_PLAY_DIFF = 20011;
    int FFP_PROP_INT64_VIDEO_DOWNLOAD_DIFF = 20012;
    int FFP_PROP_INT64_VIDEO_LASTPTS = 20013;
    int FFP_PROP_INT64_AUDIO_LASTPTS = 20014;
    int FFP_PROP_INT64_AUDIO_RENDERBUFFER_COUNT = 20015;
    int FFP_PROP_INT64_VIDEO_RENDERBUFFER_COUNT = 20016;
    int FFP_PROP_INT64_AUDIO_DOWNLOAD_PLAY_DIFF = 20017;
    int FFP_PROP_INT64_AUDIO_FIRST_DROP_COUNT = 20018;
    int FFP_PROP_INT64_BUFFERING_COUNT = 20019;
    int FFP_PROP_INT64_DOWNLOAD_SPEED = 20020;
    int FFP_PROP_INT64_DOWNLOAD_SIZE = 20021;
    int FFP_PROP_INT64_DOWNLOAD_DURATION = 20022;
    int FFP_PROP_INT64_DOWNLOAD_TIME = 20023;
    int FFP_PROP_INT64_VIDEO_CURRENT_PTS = 20024;
    int FFP_PROP_INT64_END = 20024;
    int FFP_PROP_STRING_CDN_IP = 20100;
    int FFP_PROP_STRING_EAGLE_ID = 20101;
    int FFP_PROP_STRING_CDN_VIA = 20102;
    int FFP_PROP_STRING_CDN_ERROR = 20103;
    int FFP_PROP_STRING_OPEN_TIME_STR = 20104;
    int FFP_PROPV_DECODER_UNKNOWN = 0;
    int FFP_PROPV_DECODER_AVCODEC = 1;
    int FFP_PROPV_DECODER_MEDIACODEC = 2;
    int FFP_PROPV_DECODER_VIDEOTOOLBOX = 3;
    int PREPARED = 1;
    int PLAYING = 2;
    int STOPPED = 3;
    int PAUSED = 4;
    int ALIVC_ERR_READD = 510;
    int ALIVC_ERR_LOADING_TIMEOUT = 511;
    int ALIVC_ERR_EXTRA_OPEN_FAILED = 5;
    int ALIVC_ERR_EXTRA_PREPARE_FAILED = 2;
    int ALIVC_ERR_EXTRA_DEFAULT = 0;
    String VERSION_ID = "3.3.3";
    int MEDIA_ERROR_UNKNOW = -1001;
    int MEDIA_ERROR_UNSUPPORTED = -1002;
    int MEDIA_ERROR_TIMEOUT = -1003;
    int MEDIA_AUTHORIZE_FAILED = -1004;
    int MEDIA_INFO_VIDEO_RENDERING_START = 3;
    int MEDIA_INFO_UNKNOW = 100;
    int MEDIA_INFO_BUFFERING_START = 101;
    int MEDIA_INFO_BUFFERING_END = 102;
    int MEDIA_INFO_TRACKING_LAGGING = 103;
    int MEDIA_INFO_NETWORK_ERROR = 104;
    int MEDIA_INFO_BUFFERING_PROGRESS = 105;
    int ALIYUN_ERR_DOWNLOAD_NO_NETWORK = 8001;
    int ALIYUN_ERR_DOWNLOAD_NETWORK_TIMEOUT = 8002;
    int ALIYUN_ERR_DOWNLOAD_INVALID_INPUTFILE = 8003;
    int ALIYUN_ERR_DOWNLOAD_NO_ENCRYPT_PIC = 8004;
    int ALIYUN_ERR_DOWNLOAD_GET_KEY = 8005;
    int ALIYUN_ERR_DOWNLOAD_INVALID_URL = 8006;
    int ALIYUN_ERR_DOWNLOAD_NO_MEMORY = 8007;
    int ALIYUN_ERR_DOWNLOAD_INVALID_SAVE_PATH = 8008;
    int ALIYUN_ERR_DOWNLOAD_NO_PERMISSION = 8009;
    int ALIYUN_ERR_DOWNLOAD_ALREADY_ADDED = 8010;
    int ALIYUN_ERR_DOWNLOAD_NO_MATCH = 8011;

    void play();

    void stop();

    void pause();

    void resume();

    void reset();

    void prepareToPlay(String var1);

    void prepareAndPlay(String var1);

    int getDuration();

    int getCurrentPosition();

    int getBufferPosition();

    void seekTo(int var1);

    void seekToAccurate(int var1);

    int getVideoWidth();

    int getVideoHeight();

    boolean isPlaying();

    int getErrorCode();

    String getErrorDesc();

    void setVolume(int var1);

    int getVolume();

    void destroy();

    int getScreenBrightness();

    void setScreenBrightness(int var1);

    void setPreparedListener(IPlayer.MediaPlayerPreparedListener var1);

    void setCompletedListener(IPlayer.MediaPlayerCompletedListener var1);

    void setStoppedListener(IPlayer.MediaPlayerStoppedListener var1);

    void setInfoListener(IPlayer.MediaPlayerInfoListener var1);

    void setErrorListener(IPlayer.MediaPlayerErrorListener var1);

    void setSeekCompleteListener(IPlayer.MediaPlayerSeekCompleteListener var1);

    void setBufferingUpdateListener(IPlayer.MediaPlayerBufferingUpdateListener var1);

    void setVideoSizeChangeListener(IPlayer.MediaPlayerVideoSizeChangeListener var1);

    void setPcmDataListener(IPlayer.MediaPlayerPcmDataListener var1);

    void setCircleStartListener(IPlayer.MediaPlayerCircleStartListener var1);

    void setSEIDataListener(IPlayer.MediaPlayerSEIDataListener var1);

    double getPropertyDouble(int var1, double var2);

    long getPropertyLong(int var1, long var2);

    String getPropertyString(int var1, String var2);

    void setSurfaceChanged();

    List<VideoNativeLog> getCurrNatvieLog();

    void enableNativeLog();

    void disableNativeLog();

    void setVideoSurface(Surface var1);

    void releaseVideoSurface();

    void setVideoScalingMode(IPlayer.VideoScalingMode var1);

    void setTimeout(int var1);

    void setMaxBufferDuration(int var1);

    void setMediaType(IPlayer.MediaType var1);

    void setMuteMode(boolean var1);

    Map<String, String> getAllDebugInfo();

    void setPlaySpeed(float var1);

    void setPlayingCache(boolean var1, String var2, int var3, long var4);

    void prepare(String var1, int var2, int var3, String var4, int var5);

    Bitmap snapShot();

    void setCirclePlay(boolean var1);

    void setRenderMirrorMode(IPlayer.VideoMirrorMode var1);

    void setRenderRotate(IPlayer.VideoRotate var1);

    public interface MediaPlayerSEIDataListener {
        void onSEI_userUnregisteredData(String var1);
    }

    public interface MediaPlayerCircleStartListener {
        void onCircleStart();
    }

    public interface MediaPlayerPcmDataListener {
        void onPcmData(byte[] var1, int var2);
    }

    public interface MediaPlayerStoppedListener {
        void onStopped();
    }

    public interface MediaPlayerFrameInfoListener {
        void onFrameInfoListener();
    }

    public interface MediaPlayerBufferingUpdateListener {
        void onBufferingUpdateListener(int var1);
    }

    public interface MediaPlayerVideoSizeChangeListener {
        void onVideoSizeChange(int var1, int var2);
    }

    public interface MediaPlayerErrorListener {
        void onError(int var1, String var2);
    }

    public interface MediaPlayerSeekCompleteListener {
        void onSeekCompleted();
    }

    public interface MediaPlayerInfoListener {
        void onInfo(int var1, int var2);
    }

    public interface MediaPlayerCompletedListener {
        void onCompleted();
    }

    public interface MediaPlayerPreparedListener {
        void onPrepared();
    }

    public static class VideoRotate {
        public static IPlayer.VideoRotate ROTATE_0 = new IPlayer.VideoRotate(0);
        public static IPlayer.VideoRotate ROTATE_90 = new IPlayer.VideoRotate(90);
        public static IPlayer.VideoRotate ROTATE_180 = new IPlayer.VideoRotate(180);
        public static IPlayer.VideoRotate ROTATE_270 = new IPlayer.VideoRotate(270);
        private int rotate;

        private VideoRotate(int rotate) {
            this.rotate = rotate;
        }

        public int getRotate() {
            return this.rotate;
        }
    }

    public static enum VideoMirrorMode {
        VIDEO_MIRROR_MODE_NONE(0),
        VIDEO_MIRROR_MODE_HORIZONTAL(1),
        VIDEO_MIRROR_MODE_VERTICAL(2);

        private int mode;

        private VideoMirrorMode(int mode) {
            this.mode = mode;
        }
    }

    public static enum VideoScalingMode {
        VIDEO_SCALING_MODE_SCALE_TO_FIT(0),
        VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING(1);

        private int mode;

        private VideoScalingMode(int mode) {
            this.mode = mode;
        }
    }

    public static enum MediaType {
        Live,
        Vod;

        private MediaType() {
        }
    }
}
