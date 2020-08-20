package com.alivc.player;

import android.media.AudioTrack;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build.VERSION;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class NDKCallback {
    private static Map<String, Integer> sKnownCodecList;
    private static Map<String, Integer> sKnownLeastSDKList;
    private static int sDecodeType = 0;
    private static AudioTrack sAudioTrack = null;
    private static Map<Integer, IPlayingHandler> mHandlerMap = new HashMap();
    private static Map<Integer, AudioTrack> sAudioTrackMap = new HashMap();
    private static Map<Integer, Boolean> sAudioStatusMap = new HashMap();
    private static Map<Integer, Float> sAudioVolumn = new HashMap();
    private static float sDefaultVolumn = 0.5F;

    public NDKCallback() {
    }

    public static synchronized Map<String, Integer> getKnownCodecList() {
        if (sKnownCodecList != null) {
            return sKnownCodecList;
        } else {
            sKnownCodecList = new TreeMap(String.CASE_INSENSITIVE_ORDER);
            sKnownCodecList.put("OMX.Nvidia.h264.decode", 800);
            sKnownCodecList.put("OMX.Nvidia.h264.decode.secure", 300);
            sKnownCodecList.put("OMX.Intel.hw_vd.h264", 801);
            sKnownCodecList.put("OMX.Intel.VideoDecoder.AVC", 800);
            sKnownCodecList.put("OMX.qcom.video..avc", 800);
            sKnownCodecList.put("OMX.ittiam.video.decoder.avc", 0);
            sKnownCodecList.put("OMX.SEC.avc.dec", 800);
            sKnownCodecList.put("OMX.SEC.AVC.Decoder", 799);
            sKnownCodecList.put("OMX.SEC.avcdec", 798);
            sKnownCodecList.put("OMX.SEC.avc.sw.dec", 200);
            sKnownCodecList.put("OMX.Exynos.avc.dec", 800);
            sKnownCodecList.put("OMX.Exynos.AVC.Decoder", 799);
            sKnownCodecList.put("OMX.k3.video.decoder.avc", 800);
            sKnownCodecList.put("OMX.IMG.MSVDX.Decoder.AVC", 800);
            sKnownCodecList.put("OMX.TI.DUCATI1.VIDEO.DECODER", 800);
            sKnownCodecList.put("OMX.rk.video_decoder.avc", 800);
            sKnownCodecList.put("OMX.amlogic.avc.decoder.awesome", 800);
            sKnownCodecList.put("OMX.MARVELL.VIDEO.HW.CODA7542DECODER", 800);
            sKnownCodecList.put("OMX.MARVELL.VIDEO.H264DECODER", 200);
            sKnownCodecList.remove("OMX.BRCM.vc4.decoder.avc");
            sKnownCodecList.remove("OMX.brcm.video.h264.hw.decoder");
            sKnownCodecList.remove("OMX.brcm.video.h264.decoder");
            sKnownCodecList.remove("OMX.ST.VFM.H264Dec");
            sKnownCodecList.remove("OMX.allwinner.video.decoder.avc");
            sKnownCodecList.remove("OMX.MS.AVC.Decoder");
            sKnownCodecList.remove("OMX.hantro.81x0.video.decoder");
            sKnownCodecList.remove("OMX.hisi.video.decoder");
            sKnownCodecList.remove("OMX.cosmo.video.decoder.avc");
            sKnownCodecList.remove("OMX.duos.h264.decoder");
            sKnownCodecList.remove("OMX.bluestacks.hw.decoder");
            sKnownCodecList.put("OMX.google.h264.decoder", 200);
            sKnownCodecList.put("OMX.google.h264.lc.decoder", 200);
            sKnownCodecList.put("OMX.k3.ffmpeg.decoder", 200);
            sKnownCodecList.put("OMX.ffmpeg.video.decoder", 200);
            return sKnownCodecList;
        }
    }

    public static void setPlayingHandler(int playerId, IPlayingHandler h) {
        mHandlerMap.put(playerId, h);
    }

    public static void removePlayingHandler(int playerId) {
        mHandlerMap.remove(playerId);
    }

    public static int onNotification(int playerId, int what, int arg0, int arg1, String obj_id) {
        if (what == 4 && arg1 == 6) {
        }

        IPlayingHandler mHandler = (IPlayingHandler) mHandlerMap.get(playerId);
        if (mHandler != null) {
            return mHandler.onStatus(what, arg0, arg1, obj_id);
        } else {
            VcPlayerLog.d("MPlayer", "not find handle. " + playerId);
            return -1;
        }
    }

    public static int onDataNotification(int playerId, int what, int arg0, int arg1, byte[] data) {
        IPlayingHandler mHandler = (IPlayingHandler) mHandlerMap.get(playerId);
        if (mHandler != null) {
            return mHandler.onData(what, arg0, arg1, data);
        } else {
            VcPlayerLog.d("MPlayer", "not find handle. " + playerId);
            return -1;
        }
    }

    public static int getAndroidVersion() {
        return VERSION.SDK_INT;
    }

    private static AudioTrack getAudioTrack(int audioPlayerId) {
        AudioTrack audioTrack = null;
        if (sAudioTrackMap.containsKey(audioPlayerId)) {
            audioTrack = (AudioTrack) sAudioTrackMap.get(audioPlayerId);
        }

        return audioTrack;
    }

    private static Boolean getAudioPlayingStatus(int audioPlayerId) {
        boolean status = false;
        if (sAudioStatusMap.containsKey(audioPlayerId)) {
            status = (Boolean) sAudioStatusMap.get(audioPlayerId);
        }

        return status;
    }

    private static void setAudioPlayingStatus(int audioPlayerId, boolean status) {
        sAudioStatusMap.put(audioPlayerId, status);
    }

    public static int audioInit(int audioPlayerId, int sampleRate, boolean is16Bit, boolean isStereo, int desired_buf_size) {
        int channelConfig = isStereo ? 12 : 4;
        int audioFormat = is16Bit ? 2 : 3;
        int frameSize = (isStereo ? 2 : 1) * (is16Bit ? 2 : 1);
        desired_buf_size = Math.max(desired_buf_size, AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat));
        VcPlayerLog.e("lfj0926", "to new desired_buf_size :" + desired_buf_size);
        AudioTrack audioTrack = getAudioTrack(audioPlayerId);
        if (audioTrack == null) {
            VcPlayerLog.e("Audio", "to new audioTrack :" + audioPlayerId + ", size :" + sAudioTrackMap.size());
            setAudioPlayingStatus(audioPlayerId, false);

            try {
                VcPlayerLog.e("Audio", "to new audioTrack sampleRate:" + sampleRate + ", channelConfig :" + channelConfig + " , audioFormat = " + audioFormat + " , desired_buf_size = " + desired_buf_size);
                audioTrack = new AudioTrack(3, sampleRate, channelConfig, audioFormat, desired_buf_size, 1);
            } catch (Exception var10) {
                VcPlayerLog.e("Audio", "to new audioTrack Exception :" + var10.getMessage());
                return -1;
            }

            audioTrack.setStereoVolume(sDefaultVolumn, sDefaultVolumn);
            sAudioTrack = audioTrack;
            sAudioTrackMap.put(audioPlayerId, audioTrack);
            if (audioTrack.getState() != 1) {
                VcPlayerLog.e("Audio", "NDKCallback Failed during initialization of Audio Track");
                audioTrack = null;
                return -1;
            }
        }

        return desired_buf_size;
    }

    public static int audioPause(int audioPlayerId) {
        AudioTrack audioTrack = getAudioTrack(audioPlayerId);
        if (audioTrack != null && getAudioPlayingStatus(audioPlayerId)) {
            setAudioPlayingStatus(audioPlayerId, false);

            try {
                audioTrack.pause();
            } catch (IllegalStateException var3) {
                VcPlayerLog.w("Audio", "IllegalStateException .. " + var3.getMessage());
            }
        }

        return 0;
    }

    public static int audioStart(int audioPlayerId) {
        AudioTrack audioTrack = getAudioTrack(audioPlayerId);
        if (audioTrack != null && !getAudioPlayingStatus(audioPlayerId)) {
            setAudioPlayingStatus(audioPlayerId, true);
            if (audioTrack.getState() != 1) {
                VcPlayerLog.e("Audio", "NDKCallback Failed during initialization of Audio Track");
                return -1;
            }

            try {
                audioTrack.play();
            } catch (IllegalStateException var3) {
                VcPlayerLog.w("Audio", "IllegalStateException .. " + var3.getMessage());
            }
        }

        return 0;
    }

    public static int audioStop(int audioPlayerId) {
        VcPlayerLog.w("Audio", "audioStop :" + audioPlayerId);
        AudioTrack audioTrack = getAudioTrack(audioPlayerId);
        if (audioTrack != null) {
            try {
                if (audioTrack != null && getAudioPlayingStatus(audioPlayerId)) {
                    audioTrack.flush();
                    audioTrack.stop();
                }

                audioTrack.release();
                sAudioTrackMap.remove(audioPlayerId);
            } catch (IllegalStateException var3) {
                VcPlayerLog.w("Audio", "IllegalStateException .. " + var3.getMessage());
            }

            setAudioPlayingStatus(audioPlayerId, false);
            audioTrack = null;
        }

        return 0;
    }

    public static int audioFlush(int audioPlayerId) {
        return 0;
    }

    public static void audioWriteData(int audioPlayerId, byte[] buffer, int size) {
        AudioTrack audioTrack = getAudioTrack(audioPlayerId);
        if (audioTrack != null) {
            if (buffer == null) {
                VcPlayerLog.w("Audio", "NDKCallback audio: buffer = NULL");
            } else {
                int i = 0;
                while (i < size) {
                    try {
                        int result = audioTrack.write(buffer, i, size - i);
                        if (result > 0) {
                            i += result;
                        } else {
                            if (result != 0) {
                                VcPlayerLog.w("Audio", "NDKCallback audio: error return from write(byte)");
                                return;
                            }

                            try {
                                Thread.sleep(10L);
                            } catch (InterruptedException var7) {
                            }
                        }
                    } catch (Exception var8) {
                        VcPlayerLog.w("Audio", "NDKCallback audio: error :" + var8.getMessage());
                    }
                }

            }
        }
    }

    public static void setVolume(int audioPlayerId, int vol) {
        AudioTrack audioTrack = getAudioTrack(audioPlayerId);
        if (audioTrack != null) {
            audioTrack.setStereoVolume((float) vol / 100.0F, (float) vol / 100.0F);
        }

    }

    public static void saveDecoderType(int decoderType) {
        sDecodeType = decoderType;
    }

    public static int getDecoderType() {
        return sDecodeType;
    }

    public static String getCodecNameByType(String mime) {
        int num_codecs = MediaCodecList.getCodecCount();
        ArrayList<TBCodecInfo> candidateList = new ArrayList();

        for (int i = 0; i < num_codecs; ++i) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                String[] types = codecInfo.getSupportedTypes();
                if (types != null) {
                    String[] var6 = types;
                    int var7 = types.length;

                    for (int var8 = 0; var8 < var7; ++var8) {
                        String type = var6[var8];
                        if (!TextUtils.isEmpty(type) && type.equalsIgnoreCase(mime)) {
                            TBCodecInfo info = new TBCodecInfo(codecInfo, mime);
                            candidateList.add(info);
                        }
                    }
                }
            }
        }

        if (candidateList.isEmpty()) {
            return null;
        } else {
            TBCodecInfo bestCodec = (TBCodecInfo) candidateList.get(0);
            Iterator var12 = candidateList.iterator();

            while (var12.hasNext()) {
                TBCodecInfo codec = (TBCodecInfo) var12.next();
                if (codec.mRank > bestCodec.mRank) {
                    bestCodec = codec;
                }
            }

            if (bestCodec.mRank < 600) {
                return null;
            } else {
                return bestCodec.mCodecInfo.getName();
            }
        }
    }

    public static void setMuteModeOn(boolean muteModeOn) {
        if (muteModeOn) {
            sDefaultVolumn = 0.0F;
        } else {
            sDefaultVolumn = 0.5F;
        }

        if (sAudioTrack != null) {
            sAudioTrack.setStereoVolume(sDefaultVolumn, sDefaultVolumn);
        }

    }
}
