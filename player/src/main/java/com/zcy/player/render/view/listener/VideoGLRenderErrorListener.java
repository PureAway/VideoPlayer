package com.zcy.player.render.view.listener;

import com.zcy.player.render.glrender.VideoGLViewBaseRender;

/**
 * GL渲染错误
 */
public interface VideoGLRenderErrorListener {
    /**
     * @param render
     * @param Error                错误文本
     * @param code                 错误代码
     * @param byChangedRenderError 错误是因为切换effect导致的
     */
    void onError(VideoGLViewBaseRender render, String Error, int code, boolean byChangedRenderError);
}
