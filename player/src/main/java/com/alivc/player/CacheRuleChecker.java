package com.alivc.player;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class CacheRuleChecker {
    private static final String TAG = "lfj0913" + CacheRuleChecker.class.getSimpleName();
    private String saveDirStr;
    private long maxDuration;
    private long maxSize;

    public CacheRuleChecker(String saveDirStr, long maxDuration, long maxSize) {
        VcPlayerLog.d(TAG, " saveDirStr = " + saveDirStr + " , maxSize = " + maxSize + " MB" + " ， maxDuration = " + maxDuration + "s");
        this.saveDirStr = saveDirStr;
        this.maxDuration = maxDuration;
        this.maxSize = maxSize * 1024L * 1024L;
    }

    public boolean canCache(long duration, long size) {
        VcPlayerLog.d(TAG, " size = " + size + " B" + " , duration = " + duration + "s");
        if (size > this.maxSize) {
            VcPlayerLog.e(TAG, " 文件大小直接超过最大大小。不可缓存 ");
            return false;
        } else if (duration > this.maxDuration) {
            VcPlayerLog.e(TAG, " 文件时长直接超过最大时长。不可缓存 ");
            return false;
        } else {
            File saveDir = new File(this.saveDirStr);
            boolean mkRet = true;
            if (!saveDir.exists() || !saveDir.isDirectory()) {
                mkRet = saveDir.mkdirs();
            }

            if (!mkRet) {
                VcPlayerLog.e(TAG, " 创建文件夹失败，则不能缓存 ");
                return false;
            } else {
                File[] files = saveDir.listFiles();
                if (files != null && files.length != 0) {
                    long cacheFileTotalSize = 0L;
                    List<File> cacheFileList = new ArrayList();
                    int cacheFileNameLen = 36;
                    File[] var12 = files;
                    int var13 = files.length;

                    for (int var14 = 0; var14 < var13; ++var14) {
                        File child = var12[var14];
                        String childName = child.getName();
                        if (child.isFile() && childName.endsWith(".mp4") && childName.length() == cacheFileNameLen) {
                            VcPlayerLog.d(TAG, " 满足这几个条件，认为是缓存的视频 .. " + childName);
                            cacheFileList.add(child);
                            cacheFileTotalSize += child.length();
                        }
                    }

                    if (cacheFileTotalSize + size <= this.maxSize) {
                        VcPlayerLog.d(TAG, " 已经缓存的文件大小 + 新的文件大小 小于 设置的最大缓存的大小 那么可以继续缓存");
                        return true;
                    } else {
                        Comparator<File> fileComparator = new Comparator<File>() {
                            @Override
                            public int compare(File oldLastModifiedFile, File newLastModifiedFile) {
                                return (int) (oldLastModifiedFile.lastModified() - newLastModifiedFile.lastModified());
                            }
                        };
                        Collections.sort(cacheFileList, fileComparator);
                        Iterator var18 = cacheFileList.iterator();

                        while (var18.hasNext()) {
                            File cacheFile = (File) var18.next();
                            VcPlayerLog.d(TAG, " cacheFile.lastModified = " + cacheFile.lastModified());
                            if (cacheFileTotalSize + size <= this.maxSize) {
                                break;
                            }

                            cacheFileTotalSize -= cacheFile.length();
                            cacheFile.deleteOnExit();
                        }

                        VcPlayerLog.d(TAG, " 可以继续缓存");
                        return true;
                    }
                } else {
                    VcPlayerLog.d(TAG, " 没有缓存文件，则可以缓存。 ");
                    return true;
                }
            }
        }
    }

    public boolean canUrlCache(String url) {
        File maybeSdFile = new File(url);
        if (maybeSdFile.exists()) {
            VcPlayerLog.d(TAG, "paas ...SD文件 playingcache false");
            return false;
        } else {
            long netFileSize = getFileByteSize(url);
            VcPlayerLog.d(TAG, "paas ...网络文件 netFileSize " + netFileSize);
            if (netFileSize <= 0L) {
                VcPlayerLog.d(TAG, "paas ...网络文件 playingcache false");
                return false;
            } else {
                boolean enable = this.canCache(-1L, netFileSize);
                VcPlayerLog.d(TAG, "paas ...网络文件 playingcache " + enable);
                return enable;
            }
        }
    }

    private static long getFileByteSize(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            int responseCode = connection.getResponseCode();
            long contentLength = (long) connection.getContentLength();
            connection.disconnect();
            if (responseCode == 200) {
                return contentLength;
            }
        } catch (Exception var6) {
            VcPlayerLog.d(TAG, " getFileByteSize e = " + var6.getLocalizedMessage());
        }

        return -1L;
    }
}
