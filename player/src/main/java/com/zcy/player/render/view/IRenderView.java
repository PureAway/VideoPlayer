package com.zcy.player.render.view;


import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.view.View;


import com.zcy.player.listener.VideoShotListener;
import com.zcy.player.listener.VideoShotSaveListener;
import com.zcy.player.render.glrender.VideoGLViewBaseRender;
import com.zcy.player.render.view.listener.ISurfaceListener;
import com.zcy.player.utils.MeasureHelper;

import java.io.File;

public interface IRenderView {

    ISurfaceListener getISurfaceListener();

    /**
     * Surface变化监听，必须
     */
    void setISurfaceListener(ISurfaceListener surfaceListener);

    /**
     * 当前view高度，必须
     */
    int getSizeH();

    /**
     * 当前view宽度，必须
     */
    int getSizeW();

    /**
     * 实现该接口的view，必须
     */
    View getRenderView();

    /**
     * 渲染view通过MeasureFormVideoParamsListener获取视频的相关参数，必须
     */
    void setVideoParamsListener(MeasureHelper.MeasureFormVideoParamsListener listener);

    /**
     * 截图
     */
    void taskShotPic(VideoShotListener videoShotListener, boolean shotHigh);

    /**
     * 保存当前帧
     */
    void saveFrame(final File file, final boolean high, final VideoShotSaveListener videoShotSaveListener);

    /**
     * 获取当前画面的bitmap，没有返回空
     */
    Bitmap initCover();

    /**
     * 获取当前画面的高质量bitmap，没有返回空
     */
    Bitmap initCoverHigh();

    void onRenderResume();

    void onRenderPause();

    void releaseRenderAll();

    void setRenderMode(int mode);

    void setRenderTransform(Matrix transform);

    void setGLRenderer(VideoGLViewBaseRender renderer);

    void setGLMVPMatrix(float[] MVPMatrix);

    void setGLEffectFilter(VideoGLView.ShaderInterface effectFilter);

}
