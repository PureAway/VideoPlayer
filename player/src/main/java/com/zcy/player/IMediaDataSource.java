package com.zcy.player;

import java.io.IOException;

public interface IMediaDataSource {

    int readAt(long var1, byte[] var3, int var4, int var5) throws IOException;

    long getSize() throws IOException;

    void close() throws IOException;

}
