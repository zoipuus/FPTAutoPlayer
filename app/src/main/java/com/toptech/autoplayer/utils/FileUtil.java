package com.toptech.autoplayer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.apache.http.util.EncodingUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {

	public static final String TAG = "FileUtil";

	/**
	 * 图片的存放位置：
	 * /data/data/包名/cache/Images/00
	 */
	public static final String IMAGE_PATH = "/Images/";
	public static final int TYPE_FILE = 0;
	public static final int TYPE_DIR = 1;

	/**
	 * 一、私有文件夹下的文件存取（/data/data/包名/files）
	 * 
	 * @param fileName
	 * @param message
	 */
	public static void writeFileData(String fileName, String message) {
		try {
			FileOutputStream fout = new FileOutputStream(fileName);
			byte[] bytes = message.getBytes();
			fout.write(bytes);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * //读文件在./data/data/包名/files/下面
	 * 
	 * @param fileName
	 * @return
	 */
	public static String readFileData(String fileName) {
		File file = new File(fileName);
		if (!file.exists())
			return null;
		String res = "";
		try {
			FileInputStream fin = new FileInputStream(fileName);
			int length = fin.available();
			byte[] buffer = new byte[length];
			fin.read(buffer);
			res = EncodingUtils.getString(buffer, "UTF-8");
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	public static File[] readFiles(String path) {
		File dir = new File(path);
		if (dir.isDirectory()) {
			return dir.listFiles();
		}
		return null;
	}

	public static boolean doSaveImage(Bitmap bitmap, String filename) {
		boolean isSuccess = true;
		File file = new File(filename);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				isSuccess = false;
				e.printStackTrace();
			}
		}
		File myCaptureFile = new File(filename);
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);

			// float sca = mImageSize / size;
			// if (sca > 1.0f) {
			// bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			// } else {
			// bitmap.compress(Bitmap.CompressFormat.JPEG,
			// (int) (sca * 100), bos);
			// }
			bos.flush();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			isSuccess = false;
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			isSuccess = false;
			e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					isSuccess = false;
					e.printStackTrace();
				}
			}
		}
		return isSuccess;
	}

	// only delete files
	public static void onDeleteFiles(File dir) {
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (File file : files) {
				onDeleteFiles(file);
			}
			// dir.delete();
		} else if (dir.isFile()) {
			Log.i(TAG, "delete " + dir.getPath());
			dir.delete();
		}
	}

	// only delete files
	public static void onDeleteFilesEx(File dir, String filePath) {
		Log.i(TAG, "filePath -> " + filePath);
		Log.i(TAG, "dir.getName() -> " + dir.getName());
		Log.i(TAG, "filePath.contains(dir.getName()) -> " + filePath.contains(dir.getName()));
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (File file : files) {
				onDeleteFilesEx(file, filePath);
			}
			// dir.delete();
		} else if (dir.isFile() && (!filePath.contains(dir.getName()))) {
			Log.i(TAG, "delete " + dir.getPath());
			dir.delete();
		}
	}

	// public static String getFilesPath(Context context, String path) {
	// // TODO Auto-generated method stub
	// path = context.getFilesDir().getAbsolutePath() + path;
	// File dir = new File(path);
	// if (!dir.exists())
	// dir.mkdir();
	// return path;
	// }

	public static String getCachePath(Context context, String path) {
		path = context.getCacheDir().getAbsolutePath() + path;
		File dir = new File(path);
		if (!dir.exists())
			dir.mkdir();
		Log.i(TAG, "CachePath -->>> " + dir);
		return path;
	}

	public static String getCachePath(Context context, String path, String dir_name) {
		path = getCachePath(context, path);
		File dir = new File(path + "/" + dir_name);
		if (!dir.exists())
			dir.mkdir();
		return path;
	}

	public static boolean isImageType(String fileName) {
		fileName = fileName.toLowerCase();
		Log.i(TAG, "fileName -> " + fileName);
		Log.i(TAG, "isImageType -> " + (fileName.endsWith(".png") || fileName.endsWith(".jpg")
				|| fileName.endsWith(".jpeg") || fileName.endsWith(".bmp")));
		return fileName.endsWith(".png") || fileName.endsWith(".jpg")
				|| fileName.endsWith(".jpeg") || fileName.endsWith(".bmp");
	}

}
