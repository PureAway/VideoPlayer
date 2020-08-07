package com.zcy.player.videocache.sourcestorage;


import com.zcy.player.videocache.SourceInfo;

/**
 * Storage for {@link SourceInfo}.
 *
 */
public interface SourceInfoStorage {

    SourceInfo get(String url);

    void put(String url, SourceInfo sourceInfo);

    void release();
}
