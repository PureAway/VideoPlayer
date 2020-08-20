package com.zcy.player;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.view.Surface;

import com.zcy.player.cache.ICacheManager;
import com.zcy.player.model.Model;
import com.zcy.player.model.VideoOptionModel;
import com.zcy.player.utils.RawDataSourceProvider;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.List;

public class DefaultPlayerManager extends BasePlayerManager {

    private Context context;
    private DefaultVideoPlayer mediaPlayer;
    private Surface surface;

    @Override
    public IMediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    @Override
    public void debug(boolean debug) {
        if (null != mediaPlayer) {
            mediaPlayer.setLogEnabled(debug);
        }
    }

    @Override
    public void initVideoPlayer(Context context, Message message, List<VideoOptionModel> optionModelList, ICacheManager cacheManager) {
        this.context = context.getApplicationContext();
        mediaPlayer = new DefaultVideoPlayer(context);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        Model model = (Model) message.obj;
        String url = model.getUrl();
        try {
            if (!TextUtils.isEmpty(url)) {
                Uri uri = Uri.parse(url);
                if (uri.getScheme().equals(ContentResolver.SCHEME_ANDROID_RESOURCE)) {
                    RawDataSourceProvider rawDataSourceProvider = RawDataSourceProvider.create(context, uri);
                    mediaPlayer.setDataSource(rawDataSourceProvider);
                } else if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
                    ParcelFileDescriptor descriptor;
                    try {
                        descriptor = context.getContentResolver().openFileDescriptor(uri, "r");
                        FileDescriptor fileDescriptor = descriptor.getFileDescriptor();
                        mediaPlayer.setDataSource(fileDescriptor);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    mediaPlayer.setDataSource(context, Uri.parse(url), model.getMapHeadData());
                }
            } else {
                mediaPlayer.setDataSource(context, Uri.parse(model.getUrl()));
            }
            mediaPlayer.setLooping(model.isLooping());
        } catch (IOException e) {
            e.printStackTrace();
        }
        initSuccess(model);
    }

    @Override
    public void showDisplay(Message msg) {
        if (msg.obj == null && mediaPlayer != null) {
            mediaPlayer.setSurface(null);
        } else {
            Surface holder = (Surface) msg.obj;
            surface = holder;
            if (mediaPlayer != null && holder.isValid()) {
                mediaPlayer.setSurface(holder);
            }
        }
    }

    @Override
    public void setNeedMute(boolean needMute) {
        if (mediaPlayer != null) {
            if (needMute) {
                mediaPlayer.setVolume(0, 0);
            } else {
                mediaPlayer.setVolume(1, 1);
            }
        }
    }

    @Override
    public void setVolume(float left, float right) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(left, right);
        }
    }

    @Override
    public void releaseSurface() {
        if (surface != null) {
            surface = null;
        }
    }

    @Override
    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        lastTotalRxBytes = 0;
        lastTimeStamp = 0;
    }

    @Override
    public int getBufferedPercentage() {
        return -1;
    }

    @Override
    public long getNetSpeed() {
        if (mediaPlayer != null) {
            return getNetSpeed(context);
        }
        return 0;
    }

    private long lastTimeStamp = 0;
    private long lastTotalRxBytes = 0;

    private long getNetSpeed(Context context) {
        if (context == null) {
            return 0;
        }
        long nowTotalRxBytes = TrafficStats.getUidRxBytes(context.getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);//转为KB
        long nowTimeStamp = System.currentTimeMillis();
        long calculationTime = (nowTimeStamp - lastTimeStamp);
        if (calculationTime == 0) {
            return calculationTime;
        }
        //毫秒转换
        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / calculationTime);
        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        return speed;
    }

    @Override
    public void setSpeedPlaying(float speed, boolean soundTouch) {

    }

    @Override
    public boolean isSurfaceSupportLockCanvas() {
        return false;
    }

    @Override
    public void setSpeed(float speed, boolean soundTouch) {
        if (mediaPlayer != null) {
            mediaPlayer.setSpeed(speed);
        }
    }

    @Override
    public void start() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    @Override
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    @Override
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @Override
    public int getVideoWidth() {
        if (mediaPlayer != null) {
            return mediaPlayer.getVideoWidth();
        }
        return 0;
    }

    @Override
    public int getVideoHeight() {
        if (mediaPlayer != null) {
            return mediaPlayer.getVideoHeight();
        }
        return 0;
    }

    @Override
    public boolean isPlaying() {
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        }
        return false;
    }

    @Override
    public void seekTo(long time) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(time);
        }
    }

    @Override
    public long getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public long getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public int getVideoSarNum() {
        if (mediaPlayer != null) {
            return mediaPlayer.getVideoSarNum();
        }
        return 1;
    }

    @Override
    public int getVideoSarDen() {
        if (mediaPlayer != null) {
            return mediaPlayer.getVideoSarDen();
        }
        return 1;
    }
}
