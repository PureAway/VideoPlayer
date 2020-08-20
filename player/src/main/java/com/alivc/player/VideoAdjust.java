package com.alivc.player;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import java.lang.ref.WeakReference;

public class VideoAdjust {
    private static final String TAG = VideoAdjust.class.getSimpleName();
    private AudioManager mAudioManage;
    private int maxVolume = 0;
    private int currentVolume = 0;
    private WeakReference<Context> mContextWeak = null;

    public VideoAdjust(Context context) {
        this.mContextWeak = new WeakReference(context);
        this.mAudioManage = (AudioManager) context.getApplicationContext().getSystemService("audio");
        this.maxVolume = this.mAudioManage.getStreamMaxVolume(3);
        this.currentVolume = this.mAudioManage.getStreamVolume(3);
    }

    public void SetVolumn(float fVol) {
        this.mAudioManage.setStreamVolume(3, (int) (fVol * (float) this.maxVolume), 0);
    }

    public int getVolume() {
        this.currentVolume = this.mAudioManage.getStreamVolume(3);
        return (int) ((float) this.currentVolume * 100.0F / (float) this.maxVolume);
    }

    public void setBrightness(int brightness) {
        if (this.mContextWeak != null) {
            Context context = (Context) this.mContextWeak.get();
            if (context != null) {
                if (context instanceof Activity) {
                    VcPlayerLog.d(TAG, "setScreenBrightness mContextWeak instanceof Activity brightness = " + brightness);
                    if (brightness > 0) {
                        Window localWindow = ((Activity) context).getWindow();
                        LayoutParams localLayoutParams = localWindow.getAttributes();
                        localLayoutParams.screenBrightness = (float) brightness / 100.0F;
                        localWindow.setAttributes(localLayoutParams);
                    }

                } else {
                    try {
                        boolean suc = System.putInt(context.getContentResolver(), "screen_brightness_mode", 0);
                        System.putInt(context.getContentResolver(), "screen_brightness", (int) ((float) brightness * 2.55F));
                        VcPlayerLog.d(TAG, "setScreenBrightness suc " + suc);
                    } catch (Exception var5) {
                        VcPlayerLog.e(TAG, "cannot set brightness cause of no write_setting permission e = " + var5.getMessage());
                    }

                }
            }
        }
    }

    public int getScreenBrightness() {
        if (this.mContextWeak == null) {
            return -1;
        } else {
            Context context = (Context) this.mContextWeak.get();
            if (context == null) {
                return -1;
            } else if (context instanceof Activity) {
                Window localWindow = ((Activity) context).getWindow();
                LayoutParams localLayoutParams = localWindow.getAttributes();
                float screenBrightness = localLayoutParams.screenBrightness;
                if (screenBrightness > 1.0F) {
                    screenBrightness = 1.0F;
                } else if ((double) screenBrightness < 0.1D) {
                    screenBrightness = 0.1F;
                }

                VcPlayerLog.d(TAG, "getActivityBrightness layoutParams.screenBrightness = " + screenBrightness);
                return (int) (screenBrightness * 100.0F);
            } else {
                try {
                    int brightNess = System.getInt(context.getContentResolver(), "screen_brightness");
                    return (int) ((float) (brightNess * 100) / 255.0F);
                } catch (SettingNotFoundException var5) {
                    VcPlayerLog.e(TAG, "getScreenBrightness failed: " + var5.getMessage());
                    return -1;
                }
            }
        }
    }

    public void destroy() {
        if (this.mContextWeak != null) {
            this.mContextWeak = null;
        }

    }
}
