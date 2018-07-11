package com.toptech.autoplayer.network;

/**
 * Created by zoipuus on 2017/11/29.
 */
public interface DownloadFilesListener {
    void start();
    void completed(String filePath);
    void failed(int state);
    void process(int process);
    void end();
}
