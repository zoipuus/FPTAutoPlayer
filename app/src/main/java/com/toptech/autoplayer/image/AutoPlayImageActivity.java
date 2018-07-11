package com.toptech.autoplayer.image;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.toptech.autoplayer.R;
import com.toptech.autoplayer.dialog.ConfirmDialog;
import com.toptech.autoplayer.dialog.FTPServerInfoDialog;
import com.toptech.autoplayer.network.DownloadFilesListener;
import com.toptech.autoplayer.ftp.FTPConnectListener;
import com.toptech.autoplayer.ftp.FTPManager;
import com.toptech.autoplayer.network.NetworkManager;
import com.toptech.autoplayer.utils.BitmapUtil;
import com.toptech.autoplayer.utils.ConstantUtil;
import com.toptech.autoplayer.utils.FileUtil;
import com.toptech.autoplayer.utils.SharedPreferencesUtil;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

//import com.mstar.android.tv.TvCommonManager;

public class AutoPlayImageActivity extends Activity implements ViewSwitcher.ViewFactory, DownloadFilesListener, FTPConnectListener, View.OnClickListener {

    private static final String TAG = "AutoPlayPicture";
    private Animation mInAnimation, mOutAnimation;
    private ImageSwitcher img_switcher;
    private int position = 0;
    private AutoPlayHandler mAutoPlayHandler;
    private FTPManager mFTPManager;
    private ProgressBar pro_loading;
    private TextView text_loading;
    private List<String> mFileList = new ArrayList<String>();
    private int file_position = 0;
    private boolean isEnablePlayNextPicture = true, isFirstPlay = true, isDeleteDirEnable = false;
    private FTPServerInfoDialog mFTPServerInfoDialog;
    private ConfirmDialog mConfirmDialog;
    private Thread mFTPThread;
    private boolean isConnectEnable = false, isShowLoadingEnable = true, isFTPThreadRunning = false;

    @Override
    public void start() {
        startDownloadFiles();
    }

    @Override
    public void completed(String filePath) {
        Log.i(TAG, "file path -> " + filePath);
        // TODO:set local version
        SharedPreferencesUtil.setSharedPreferencesString(AutoPlayImageActivity.this.getApplicationContext(), ConstantUtil.KEY_FTP_LOCAL_VERSION,
                SharedPreferencesUtil.getSharedPreferencesString(AutoPlayImageActivity.this.getApplicationContext(), ConstantUtil.KEY_FTP_REMOTE_VERSION));
        if (!mFileList.contains(filePath)) {
            mFileList.add(filePath);
            // TODO:delete all files
            if (isDeleteDirEnable) {
                FileUtil.onDeleteFilesEx(new File(FileUtil.getCachePath(AutoPlayImageActivity.this.getApplicationContext(), FileUtil.IMAGE_PATH)), filePath);
                isFirstPlay = true;
                isDeleteDirEnable = false;
            }
        }
        //Collections.sort(mFileList);
        if (isFirstPlay && isEnablePlayNextPicture) {
            sendMessage(ConstantUtil.PLAY_NEXT_PICTURE);
            isEnablePlayNextPicture = false;
        }
    }

    @Override
    public void failed(int state) {
        sendMessage(state);
    }

    @Override
    public void process(int process) {

    }

    @Override
    public void end() {
        // files = new File(FileUtil.getCachePath(this.getApplicationContext(), FileUtil.IMAGE_PATH)).listFiles();
        //Log.i(TAG, "files.length -> " + files.length);
        sendMessageDelayed(ConstantUtil.FTP_SERVER_DISCONNECTED, ConstantUtil.DELAY_TIME);
        sendMessageDelayed(ConstantUtil.AUTO_PLAY_PICTURE, ConstantUtil.DELAY_TIME);
    }

    @Override
    public void connect() {
        Log.i(TAG, "connect()");
        isConnectEnable = true;
    }

    private void connectToFTPServer() {
        if (!isFTPManagerAvailable()) {
            mFTPManager = new FTPManager();
            mFTPManager.setDownloadFilesListener(AutoPlayImageActivity.this);
        }
        if (mFTPThread == null) {
            isFTPThreadRunning = true;
            mFTPThread = new Thread() {
                @Override
                public void run() {
                    while (isFTPThreadRunning) {
                        // TODO:connect to ftp server
                        if (isConnectEnable) {
                            boolean bool = mFTPManager.connect(AutoPlayImageActivity.this.getApplicationContext());
                            Log.i(TAG, "connect -> " + bool);
                            sendMessage(ConstantUtil.FTP_SERVER_CONNECT_START);
                            if (bool) {
                                sendMessage(ConstantUtil.FTP_SERVER_CONNECTED);
                            } else {
                                sendMessageDelayed(ConstantUtil.FTP_SERVER_CONNECT_FAILED, ConstantUtil.DELAY_TIME);
                            }
                            isConnectEnable = false;
                        }
                        try {
                            sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.i(TAG, "FTPThread is Running");
                    }
                    Log.i(TAG, "isFTPThreadRunning -> " + isFTPThreadRunning);
                }
            };
            mFTPThread.start();
        } else {
            Log.i(TAG, "mFTPThread.getState() -->> " + mFTPThread.getState());
        }
    }

    private void sendMessage(int what) {
        sendMessageDelayed(what, 0);
    }

    private void sendMessageDelayed(int what, int delayTime) {
        if (mAutoPlayHandler == null)
            return;
        mAutoPlayHandler.removeMessages(what);
        Message msg = mAutoPlayHandler.obtainMessage();
        msg.what = what;
        mAutoPlayHandler.sendMessageDelayed(msg, delayTime);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_btn_ok:
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_SETTINGS);
                startActivity(intent);
                mConfirmDialog.dismiss();
                break;
            case R.id.id_btn_cancel:
                mConfirmDialog.dismiss();
                if (!SharedPreferencesUtil.checkFTPServerInfo(AutoPlayImageActivity.this.getApplicationContext())) {
                    showFTPServerInfoDialog();
                }
                break;
            default:
                break;
        }
    }

    private static class AutoPlayHandler extends Handler {
        private final WeakReference<AutoPlayImageActivity> mWeakReference;

        public AutoPlayHandler(AutoPlayImageActivity activity) {
            mWeakReference = new WeakReference<AutoPlayImageActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            AutoPlayImageActivity activity = mWeakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case ConstantUtil.PLAY_NEXT_PICTURE:
                    case ConstantUtil.AUTO_PLAY_PICTURE:
                        activity.setLoadingVisibility(View.GONE);
                        activity.autoPlayPicture();
                        break;
                    case ConstantUtil.FTP_SERVER_CONNECT_START:
                        if (activity.mFTPServerInfoDialog != null)
                            activity.mFTPServerInfoDialog.setConnectState(false);
                        break;
                    case ConstantUtil.FTP_SERVER_CONNECTED:
                        if (activity.mFTPServerInfoDialog != null)
                            activity.mFTPServerInfoDialog.dismiss();
                        if (activity.isShowLoadingEnable)
                            activity.setLoadingVisibility(View.VISIBLE);
                        break;
                    case ConstantUtil.FTP_SERVER_CONNECT_FAILED:
                        Toast.makeText(activity, R.string.tip_connect_failed, Toast.LENGTH_SHORT).show();
                    case ConstantUtil.FTP_SERVER_DISCONNECTED:
                        activity.setLoadingVisibility(View.GONE);
                        activity.onDisconnectFTP();
                        break;
                    case ConstantUtil.FTP_SERVER_ERROR_HAS_NO_FILES:
                        Toast.makeText(activity, R.string.tip_error_has_no_files, Toast.LENGTH_SHORT).show();
                        break;
                    case ConstantUtil.FTP_SERVER_CHECK_STATE:
                        Log.i(TAG, "ConstantUtil.FTP_SERVER_CHECK_STATE");
                        activity.autoCheckFTPServerState();
                        activity.isConnectEnable = true;
                        activity.isShowLoadingEnable = false;
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void autoPlayPicture() {
        Log.i(TAG, "file_position -> " + file_position);
        Log.i(TAG, "mFileList.size() -> " + mFileList.size());
        if (mFileList.size() <= 0)
            return;
        if (file_position < mFileList.size() - 1) {
            file_position++;
        } else {
            file_position = 0;
        }
        if (mFileList.size() > 1) {
            playNextPicture(mFileList.get(file_position));
        }
        sendMessageDelayed(ConstantUtil.AUTO_PLAY_PICTURE, ConstantUtil.DELAY_TIME);
        if (isFirstPlay) {
            isFirstPlay = false;
            playNextPicture(mFileList.get(file_position));
        } else {
            // first call back this function skip
            isEnablePlayNextPicture = true;
        }
    }

    private void playNextPicture(String pathName) {
        Log.i(TAG, "pathName -> " + pathName);
        Bitmap bitmap = BitmapUtil.getBitmapByRatio(pathName, ConstantUtil.LIMIT_WIDTH, ConstantUtil.LIMIT_HEIGHT);
        if (bitmap != null) {
            SoftReference<Bitmap> softReference = new SoftReference<Bitmap>(bitmap);
            img_switcher.setImageDrawable(new BitmapDrawable(this.getResources(), softReference.get()));
        } else {
            Log.i(TAG, "bitmap = null");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_play_image);

        //TvCommonManager.getInstance().setInputSource(TvCommonManager.INPUT_SOURCE_STORAGE);

        mInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_in);
        mOutAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_out);

        initViews();

        mAutoPlayHandler = new AutoPlayHandler(this);

        playLocalImages();

        autoCheckFTPServerState();

        connectToFTPServer();
    }

    private void showFTPServerInfoDialog() {
        if (mFTPServerInfoDialog == null) {
            mFTPServerInfoDialog = new FTPServerInfoDialog(this, R.style.dialog_ftp_info);
            mFTPServerInfoDialog.setFTPConnectListener(this);
            mFTPServerInfoDialog.show();
        } else if(!mFTPServerInfoDialog.isShowing()) {
            mFTPServerInfoDialog.show();
        }
        isShowLoadingEnable = true;
    }

    private void initViews() {
        img_switcher = (ImageSwitcher) this.findViewById(R.id.id_img_switcher);
        pro_loading = (ProgressBar) this.findViewById(R.id.id_pro_loading);
        text_loading = (TextView) this.findViewById(R.id.id_text_loading);
        setLoadingVisibility(View.GONE);

        img_switcher.setFactory(this);

        setAnimation(img_switcher);

        img_switcher.setImageDrawable(this.getResources().getDrawable(R.drawable.background));
    }

    private void setAnimation(ImageSwitcher switcher) {
        switcher.setInAnimation(mInAnimation);
        switcher.setOutAnimation(mOutAnimation);
    }

    private void setLoadingVisibility(int visibility) {
        pro_loading.setVisibility(visibility);
        text_loading.setVisibility(visibility);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                setLoadingVisibility(View.GONE);
                showFTPServerInfoDialog();
                break;
            default:break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public View makeView() {
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        return imageView;
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (!NetworkManager.isWiFiOpen(this.getApplicationContext())) {
//            showJumpToSettingsDialog(R.string.str_wifi_was_closed);
//        } else
        if (!NetworkManager.isNetworkAvailable(this.getApplicationContext())){
            showJumpToSettingsDialog(R.string.str_network_unavailable);
        } else {
            if (!isFTPConnected()) {
                if (SharedPreferencesUtil.checkFTPServerInfo(AutoPlayImageActivity.this.getApplicationContext())) {
                    setLoadingVisibility(View.VISIBLE);
                    isConnectEnable = true;
                } else {
                    showFTPServerInfoDialog();
                }
            }
        }
    }

    private void showJumpToSettingsDialog(int resId) {
        if (mConfirmDialog == null) {
            mConfirmDialog = new ConfirmDialog(this, R.style.dialog_ftp_info, this);
            mConfirmDialog.show();
        } else if (!mConfirmDialog.isShowing()) {
            mConfirmDialog.show();
        }
        mConfirmDialog.setContent(resId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onDestroyFTPConnect();
        isFTPThreadRunning = false;
        if (mAutoPlayHandler != null) {
            mAutoPlayHandler.removeMessages(ConstantUtil.PLAY_NEXT_PICTURE);
            mAutoPlayHandler.removeMessages(ConstantUtil.AUTO_PLAY_PICTURE);
            mAutoPlayHandler.removeMessages(ConstantUtil.FTP_SERVER_CHECK_STATE);
        }
        mAutoPlayHandler = null;
    }

    private boolean isFTPManagerAvailable() {
        return mFTPManager != null;
    }

    private void onDestroyFTPConnect() {
        if (isFTPManagerAvailable()) {
            mFTPManager.onDestroyFTPConnect();
        }
    }

    private void onDisconnectFTP() {
        if (isFTPManagerAvailable()) {
            mFTPManager.onDisconnectFTP();
        }
    }

    private boolean isFTPConnected() {
        boolean bool = false;
        if (isFTPManagerAvailable()) {
            bool = mFTPManager.isConnected();
        }
        return bool;
    }

    private void playLocalImages() {
        File dir = new File(FileUtil.getCachePath(AutoPlayImageActivity.this.getApplicationContext(), FileUtil.IMAGE_PATH));
        for (File file:dir.listFiles()) {
            if (FileUtil.isImageType(file.getName()))
                mFileList.add(file.getAbsolutePath());
        }
        Log.i(TAG, "mFileList.size() -> " + mFileList.size());
        if (mFileList.size() >= 1) {
            sendMessageDelayed(ConstantUtil.AUTO_PLAY_PICTURE, ConstantUtil.DELAY_TIME);
        }
    }

//    private boolean loginFTP() {
//        if (!isFTPManagerAvailable())
//            return false;
//        try {
//            return mFTPManager.login();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

    @Deprecated
    private boolean downloadVersionFile() {
        if (!isFTPManagerAvailable())
            return false;
        return mFTPManager.downloadVersionFile(AutoPlayImageActivity.this.getApplicationContext());
    }

    private void startDownloadFiles() {
        file_position = 0;
        mFileList.clear();
        isDeleteDirEnable = true;
        if (isFTPManagerAvailable()) {
            mFTPManager.downloadFilesTask(FileUtil.getCachePath(AutoPlayImageActivity.this.getApplicationContext(), FileUtil.IMAGE_PATH),
                    "/" + SharedPreferencesUtil.getSharedPreferencesString(AutoPlayImageActivity.this.getApplicationContext(), ConstantUtil.KEY_DOWNLOAD_PATH));
        }
    }

    private void autoCheckFTPServerState() {
        sendMessageDelayed(ConstantUtil.FTP_SERVER_CHECK_STATE, ConstantUtil.DELAY_TIME_CHECK_SERVER_STATE);
    }
}
