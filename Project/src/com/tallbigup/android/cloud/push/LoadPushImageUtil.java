package com.tallbigup.android.cloud.push;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

/**
 * 下载push时通知的图标
 * @author mch
 *
 */
public class LoadPushImageUtil {
	
	public static void getImageStream(Context context,String path,final ImageCallback callback) throws IOException{
		new AsyncTask<String, Integer, Bitmap>() {

			@Override
			protected Bitmap doInBackground(String... params) {
				InputStream input = null;
				try {
					URL url = new URL(params[0]);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.setDoInput(true);
					connection.setReadTimeout(10*1000);
					connection.setConnectTimeout(10*1000);
					connection.connect();
					Bitmap bitmap = null;
					if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
						input = connection.getInputStream();
						bitmap = BitmapFactory.decodeStream(input);
					}
					return bitmap;
				} catch (MalformedURLException e) {
					e.printStackTrace();
					return null;
				} catch (ProtocolException e) {
					e.printStackTrace();
					return null;
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			}	
			
			@Override
			protected void onPostExecute(Bitmap bitmap) {
				callback.result(bitmap);
			}
		}.execute(path);
	}
}
