package com.toptech.autoplayer.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zoipuus on 2017/12/8.
 */

public class BitmapUtil {

    private static final String TAG = "BitmapUtil";

    public static Bitmap getBitmapByRatio(String imgPath, int width, int height) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true，即只读边不读内容
        newOpts.inJustDecodeBounds = true;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        // Get bitmap info, but notice that bitmap is null now
        BitmapFactory.decodeFile(imgPath,newOpts);

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 想要缩放的目标尺寸
        float hh = height;
        float ww = width;
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int inSampleSize = 1;//be=1表示不缩放
        Log.i(TAG, "(w / ww) -> " + (w / ww));
        Log.i(TAG, "(h / hh) -> " + (h / hh));
        boolean isSaveEnable = false;
        if ((w > 0) && (h > 0)
                &&(w > ww || h > hh)) {
            inSampleSize = Math.max((int)(w / ww), (int)(h / hh));
            isSaveEnable = true;
        }
        Log.i(TAG, "inSampleSize -> " + inSampleSize);
        /*
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            inSampleSize = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            inSampleSize = (int) (newOpts.outHeight / hh);
        }
        */
        if (inSampleSize <= 0)
            inSampleSize = 1;
        newOpts.inSampleSize = inSampleSize;//设置缩放比例
        // 开始压缩图片，注意此时已经把options.inJustDecodeBounds 设回false了
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, newOpts);
        // TODO:save bitmap to local
        if (isSaveEnable && inSampleSize != 1)
            saveBitmapFile(bitmap, imgPath);
        // 压缩好比例大小后再进行质量压缩
//        return compress(bitmap, maxSize); // 这里再进行质量压缩的意义不大，反而耗资源，删除
        return bitmap;
    }

    private static void saveBitmapFile(Bitmap bitmap, String imgPath) {
        if (bitmap == null || TextUtils.isEmpty(imgPath))
            return;
        File file = new File(imgPath);
        if (file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        BufferedOutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(imgPath));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            Log.i(TAG, "save " + imgPath + " success!");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
