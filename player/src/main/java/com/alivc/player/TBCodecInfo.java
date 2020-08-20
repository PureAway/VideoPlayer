package com.alivc.player;

import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.os.Build.VERSION;
import java.util.Locale;

class TBCodecInfo {
    public MediaCodecInfo mCodecInfo;
    public int mRank = 0;
    public String mMimeType;

    public TBCodecInfo(MediaCodecInfo codecInfo, String mimeType) {
        String name = codecInfo.getName().toLowerCase(Locale.US);
        int rank;
        if (!name.startsWith("omx.")) {
            rank = 100;
        } else if (name.startsWith("omx.pv")) {
            rank = 200;
        } else if (name.startsWith("omx.google.")) {
            rank = 200;
        } else if (name.startsWith("omx.ffmpeg.")) {
            rank = 200;
        } else if (name.startsWith("omx.k3.ffmpeg.")) {
            rank = 200;
        } else if (name.startsWith("omx.avcodec.")) {
            rank = 200;
        } else if (name.startsWith("omx.ittiam.")) {
            rank = 0;
        } else if (name.startsWith("omx.mtk.")) {
            if (VERSION.SDK_INT < 18) {
                rank = 0;
            } else {
                rank = 800;
            }
        } else {
            Integer knownRank = (Integer)NDKCallback.getKnownCodecList().get(name);
            if (knownRank != null) {
                rank = knownRank;
            } else {
                try {
                    CodecCapabilities cap = codecInfo.getCapabilitiesForType(mimeType);
                    if (cap != null) {
                        rank = 700;
                    } else {
                        rank = 600;
                    }
                } catch (Throwable var7) {
                    rank = 600;
                }
            }
        }

        this.mCodecInfo = codecInfo;
        this.mRank = rank;
        this.mMimeType = mimeType;
    }
}
