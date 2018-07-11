package com.toptech.autoplayer.utils;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zoipuus on 2017/12/6.
 */

public class XmlUtil {
    private static final String TAG = "XmlUtil";

    public static boolean pullParserXml(Context context, String filePath) {
        boolean bool = false;
        File file = new File(filePath);
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            XmlPullParser pullParser = Xml.newPullParser();
            pullParser.setInput(inputStream, "UTF-8");

            int eventType = pullParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        Log.i(TAG, "getName -->> " + pullParser.getName());
                        if ("Version".equals(pullParser.getName())) {
                            eventType = pullParser.next();
                            Log.i(TAG, "getText -->> " + pullParser.getText());
                            // TODO:set remote version
                            SharedPreferencesUtil.setSharedPreferencesString(context, ConstantUtil.KEY_FTP_REMOTE_VERSION, pullParser.getText());
                            bool = true;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                    default:
                        break;
                }
                eventType = pullParser.next();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "FileNotFoundException");
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            bool = false;
            Log.e(TAG, "XmlPullParserException");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IOException");
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return bool;
    }
}
