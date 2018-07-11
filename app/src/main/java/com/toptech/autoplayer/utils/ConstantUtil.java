package com.toptech.autoplayer.utils;

public class ConstantUtil {

	// FTP SERVER INFO
	/*
	public static String FTP_SERVER_HOST = "52.26.124.196";
	public static int FTP_SERVER_PORT = 21;
	public static String  FTP_SERVER_USERNAME = "toptech";
	public static String  FTP_SERVER_PASSWORD = "lsy654321";
	public static String  FTP_SERVER_DOWNLOAD_PATH= "/Test";
	*/
	public static final String VERSION_FILE_NAME = "FTPServerState.xml";

	public static final int FTP_SERVER_CONNECT_START = 0x30;
	public static final int FTP_SERVER_CONNECTED = 0x31;
	public static final int FTP_SERVER_DISCONNECTED = 0x32;
	public static final int FTP_SERVER_CONNECT_FAILED = 0x33;

	public static final int FTP_SERVER_ERROR_HAS_NO_FILES = 0x50;
	public static final int FTP_SERVER_CHECK_STATE = 0x60;

	public static final String KEY_IP_ADDRESS = "KEY_IP_ADDRESS";
	public static final String KEY_PORT = "KEY_PORT";
	public static final String KEY_USER_NAME = "KEY_USER_NAME";
	public static final String KEY_PASSWORD = "KEY_PASSWORD";
	public static final String KEY_DOWNLOAD_PATH = "KEY_DOWNLOAD_PATH";
	public static final String KEY_FTP_REMOTE_VERSION = "KEY_FTP_REMOTE_VERSION";
	public static final String KEY_FTP_LOCAL_VERSION = "KEY_FTP_LOCAL_VERSION";

	public static final int AUTO_PLAY_PICTURE = 0x01;
	public static final int PLAY_NEXT_PICTURE = 0x02;
	public static final int DELAY_TIME = 5 * 1000;
	public static final int DELAY_TIME_CHECK_SERVER_STATE = 60 * 1000;

	public static final int LIMIT_WIDTH = 1920;
	public static final int LIMIT_HEIGHT = 1080;

}
