package com.tallbigup.android.cloud.push;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 下载push时通知的图标
 * @author mch
 *
 */
public class LoadPushImageUtil {
	
	public static InputStream getImageStream(String path) throws IOException{
		InputStream input = null;
		URL url = new URL(path);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setDoInput(true);
		connection.setReadTimeout(10*1000);
		connection.setConnectTimeout(10*1000);
		connection.connect();
		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
			input = connection.getInputStream();
		}
		return input;
	}
}
