package com.toptech.autoplayer.ftp;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.toptech.autoplayer.network.DownloadFilesListener;
import com.toptech.autoplayer.utils.ConstantUtil;
import com.toptech.autoplayer.utils.FileUtil;
import com.toptech.autoplayer.utils.XmlUtil;
import com.toptech.autoplayer.utils.SharedPreferencesUtil;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by zoipuus on 2017/11/28.
 */

public final class FTPManager {
    private static final String TAG = "FTPManager";
    private final FTPClient mFTPClient;
    private DownloadFilesListener mDownloadFilesListener;
    private FTPFile ftpFile;
    private String localFileName;
    private String remotePath;

    public FTPManager() {
        mFTPClient = new FTPClient();
    }

    // connect to ftp server
    public synchronized boolean connect(Context context) {
        boolean bool = mFTPClient.isConnected();
        Log.i(TAG, "mFTPClient.isConnected : " + mFTPClient.isConnected());
        if (!mFTPClient.isConnected()) {
            mFTPClient.setDataTimeout(5 * 1000);
            mFTPClient.setControlEncoding("utf-8");
            try {
                Log.i(TAG, "IP ADDRESS : " + SharedPreferencesUtil.getSharedPreferencesString(context, ConstantUtil.KEY_IP_ADDRESS));
                Log.i(TAG, "PORT : " + SharedPreferencesUtil.getSharedPreferencesInt(context, ConstantUtil.KEY_PORT));
                Log.i(TAG, "USER NAME : " + SharedPreferencesUtil.getSharedPreferencesString(context, ConstantUtil.KEY_USER_NAME));
                Log.i(TAG, "PASSWORD : " + SharedPreferencesUtil.getSharedPreferencesString(context, ConstantUtil.KEY_PASSWORD));
                Log.i(TAG, "DOWNLOAD PATH : " + SharedPreferencesUtil.getSharedPreferencesString(context, ConstantUtil.KEY_DOWNLOAD_PATH));
                mFTPClient.connect(SharedPreferencesUtil.getSharedPreferencesString(context, ConstantUtil.KEY_IP_ADDRESS),
                        SharedPreferencesUtil.getSharedPreferencesInt(context, ConstantUtil.KEY_PORT));
            } catch (IOException e) {
                Log.e(TAG, "connect to ftp server error!");
                e.printStackTrace();
            }
        }
        try {
            bool = login(context);
        } catch (IOException e) {
            Log.e(TAG, "login server error!");
            e.printStackTrace();
        }
        return bool;
    }

    private boolean login(Context context) throws IOException {
        boolean bool = false;
        if (mFTPClient.login(SharedPreferencesUtil.getSharedPreferencesString(context, ConstantUtil.KEY_USER_NAME),
                SharedPreferencesUtil.getSharedPreferencesString(context, ConstantUtil.KEY_PASSWORD))) {
            bool = true;
            FTPFile[] ftpFiles = mFTPClient.listFiles(SharedPreferencesUtil.getSharedPreferencesString(context, ConstantUtil.KEY_DOWNLOAD_PATH));
            Log.i(TAG ,"ftpFiles.length -> " + ftpFiles.length);
            for (FTPFile ftpFile:ftpFiles) {
                Log.i(TAG, "File name -->> " + ftpFile.getName());
                Log.i(TAG, "File Type -->> " + ftpFile.getType());
                if (ftpFile.getType() == FileUtil.TYPE_FILE
                        && ftpFile.getName().equals(ConstantUtil.VERSION_FILE_NAME)) {
                    Log.i(TAG, "File name -->> " + ftpFile.getName());
                    this.ftpFile = ftpFile;
                    this.localFileName = FileUtil.getCachePath(context, FileUtil.IMAGE_PATH) + ftpFile.getName();
                    this.remotePath = "/" + SharedPreferencesUtil.getSharedPreferencesString(context, ConstantUtil.KEY_DOWNLOAD_PATH);
                    downloadVersionFile(context);
                    break;
                }
            }
        } else {
            Log.i(TAG, "can not login!");
        }
        return bool;
    }

    public boolean downloadVersionFile(Context context) {
        Log.i(TAG, "downloadVersionFile()");
        boolean bool = false;
        if (ftpFile == null || TextUtils.isEmpty(this.localFileName) || TextUtils.isEmpty(this.remotePath)) {
            Log.e(TAG, "ftpFile -> " + ftpFile + "\nthis.localFileName -> " + "\nthis.remotePath -> " + this.remotePath);
            return bool;
        }
        // TODO:download version file and compare local version, if them different than download all files.
        if (downloadFileByName(FileUtil.getCachePath(context, FileUtil.IMAGE_PATH) + ftpFile.getName(), "/" + SharedPreferencesUtil.getSharedPreferencesString(context, ConstantUtil.KEY_DOWNLOAD_PATH), ftpFile, false)) {
            bool = XmlUtil.pullParserXml(context, FileUtil.getCachePath(context, FileUtil.IMAGE_PATH) + ftpFile.getName());
            if (bool && SharedPreferencesUtil.isUpdateFile(context)) {
                if (mDownloadFilesListener != null) {
                    mDownloadFilesListener.start();
                }
                bool = true;
            } else {
                bool = false;
            }
        }
        return bool;
    }

    public void onDestroyFTPConnect() {
        onDisconnectFTP();
    }

    //TODO:DISCONNECT FTP SERVER
    public void onDisconnectFTP() {
        if (mFTPClient.isConnected()) {
            try {
                mFTPClient.disconnect();
                Log.i(TAG, "disconnect ftp server!");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "disconnect ftp error!");
            }
        }
    }

    public boolean isConnected() {
        return mFTPClient.isConnected();
    }

    public void downloadFilesTask(String localPath, String serverPath) {
        LoadAsyncTask loadAsyncTask = new LoadAsyncTask();
        loadAsyncTask.execute(localPath, serverPath);
    }

    // download all files by dir
    private synchronized boolean downloadFilesByDir(String localPath, String serverPath) {
        boolean bool = false;
        FTPFile[] ftpFiles;
        try {
            ftpFiles = mFTPClient.listFiles(serverPath);
            int count = ftpFiles.length;
            if (count == 0) {
                Log.e(TAG , "has no files!");
                if (mDownloadFilesListener != null) {
                    mDownloadFilesListener.failed(ConstantUtil.FTP_SERVER_ERROR_HAS_NO_FILES);
                }
                return false;
            }
            for (FTPFile ftpFile:ftpFiles) {
                /**
                 * FTP download file step
                 * mFTPClient.enterLocalActiveMode();
                 * mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
                 * mFTPClient.setRestartOffset(localSize);
                 * inputStream = mFTPClient.retrieveFileStream(serverPath + "/" + ftpFile.getName());
                 * inputStream.close();
                 * mFTPClient.completePendingCommand();
                 */
                Log.i(TAG, "remote file type -> " + ftpFile.getType());
                if (ftpFile.getType() == FileUtil.TYPE_DIR) {
                    continue;
                }
                Log.i(TAG, "remote file name -> " + serverPath + "/" + ftpFile.getName());
                Log.i(TAG, "local file name -> " + localPath + ftpFile.getName());

                // TODO:download one file function
                downloadFileByName(localPath + ftpFile.getName(), serverPath, ftpFile, FileUtil.isImageType(ftpFile.getName()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (mDownloadFilesListener != null) {
                mDownloadFilesListener.end();
            }
            try {
                if (mFTPClient.isConnected()) {
                    if (mFTPClient.completePendingCommand()) {
                        Log.i(TAG, "download file success!");
                        bool = true;
                    } else {
                        Log.i(TAG, "download file fail!");
                    }
                } else {
                    Log.i(TAG, "mFTPClient.disconnected!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return bool;
    }

    // download one file by name
    private boolean downloadFileByName(String localFileName, String serverPath, FTPFile ftpFile, boolean isCallBack) {
        boolean bool = false;
        if (!mFTPClient.isConnected())
            return false;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        long serverSize = ftpFile.getSize();
        File localFile =  new File(localFileName);
        // TODO:delete old file
        if (localFile.exists()) {
            localFile.delete();
        }
        long localSize = 0, step = serverSize / 100, currentSize = 0;
        mFTPClient.enterLocalActiveMode();
        try {
            mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
            outputStream = new FileOutputStream(localFile, true);
            mFTPClient.setRestartOffset(localSize);
            inputStream = mFTPClient.retrieveFileStream(serverPath + "/" + ftpFile.getName());
            byte[] bytes = new byte[1024];
            int process = 0, length = 0;
            Log.i(TAG, "inputStream -->> " + inputStream);
            while (inputStream != null && (length = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, length);
                currentSize += length;
                if (currentSize / step != process) {
                    process = (int) (currentSize / step);
                    if (isCallBack && mDownloadFilesListener != null) {
                        mDownloadFilesListener.process(process);
                    }
                }
            }
            Log.i(TAG, "length -->> " + length);
            outputStream.flush();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (mFTPClient.isConnected()) {
                if (mFTPClient.completePendingCommand()) {
                    Log.i(TAG, "download file success!");
                    bool = true;
                    if (isCallBack && mDownloadFilesListener != null) {
                        mDownloadFilesListener.completed(localFileName);
                    }
                } else {
                    bool = false;
                    Log.i(TAG, "download file fail!");
                }
            } else {
                Log.i(TAG, "mFTPClient.disconnected!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (!bool) {
                // TODO:delete file when download failed
                if (localFile.exists()) {
                    localFile.delete();
                }
            }
        }
        return bool;
    }

    public void setDownloadFilesListener(DownloadFilesListener mDownloadFilesListener) {
        this.mDownloadFilesListener = mDownloadFilesListener;
    }

    private class LoadAsyncTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            Log.i(TAG, "localPath -> " + params[0]);
            Log.i(TAG, "serverPath -> " + params[1]);
            boolean bool = downloadFilesByDir(params[0], params[1]);
            return bool;
        }
    }

}
