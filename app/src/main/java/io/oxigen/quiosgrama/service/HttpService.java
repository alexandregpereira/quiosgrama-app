package io.oxigen.quiosgrama.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.util.AndroidUtil;
import io.oxigen.quiosgrama.util.DateDeserializer;

public class HttpService {

	private static final String TAG = "HttpService";
	private static final int TIME_OUT = 10000;
	public static final String CONTENT_TYPE_JSON = "application/json";
	public static final String CONTENT_TYPE_TEXT = "text/plan";

	public static <T> T getWithPost(Context context, Class<T> classGen, String urlString, String json, String contentType){
		InputStream is = null;

		try {
		    URL url = new URL(buildUrl(context, urlString));
		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		    conn.setConnectTimeout(TIME_OUT);
		    conn.setRequestMethod("POST");
		    conn.setRequestProperty("Content-Type", contentType);
			conn.setRequestProperty("Accept", "application/json");
			conn.setDoInput(true);
			conn.setDoOutput(true);

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(
			        new OutputStreamWriter(os, "UTF-8"));
			writer.write(json);
			writer.flush();
			writer.close();
			os.close();
		    // Starts the query
		    conn.connect();
		    int response = conn.getResponseCode();

			is = conn.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line = reader.readLine();

			T object = null;
			if(line != null) {
				Log.d("HttService", line);

				if (contentType.equals(CONTENT_TYPE_JSON)) {
					long init = System.currentTimeMillis();
					Gson gson = new GsonBuilder()
							.registerTypeAdapter(Date.class, new DateDeserializer())
							.create();
					object = gson.fromJson(line, classGen);
					long end = System.currentTimeMillis();
					Log.d(TAG, "Json Des.: " + (end - init));
				} else {
					object = (T) line;
				}

				SyncServerService.sendBroadcastMessageError(context, object.toString());
			}

			if(response == 200 || response == 203){
				AndroidUtil.clearNotofication(context, 1);
				QuiosgramaApp.connectionFailed = false;

				return object;
			}
			else{
				QuiosgramaApp.connectionFailed = true;
			}
		} catch (Exception e) {
			Log.e(TAG, "Http get error " + e.getMessage());
			SyncServerService.sendBroadcastMessageError(context, context.getResources().getString(R.string.sync_error_message));
			QuiosgramaApp.connectionFailed = true;
		}

		return null;
	}
	
	public synchronized static boolean post(Context context, String urlString, String json, String errorMessage){
		try {
			URL url = new URL(buildUrl(context, urlString));
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(TIME_OUT);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Accept", "application/json");
			conn.setDoInput(true);
			conn.setDoOutput(true);

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(
			        new OutputStreamWriter(os, "UTF-8"));
			writer.write(json);
			writer.flush();
			writer.close();
			os.close();

			conn.connect();
			int response = conn.getResponseCode();

			InputStream is = conn.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line = reader.readLine();
			if(line != null && !line.isEmpty()){
				Log.d("HttService Post", line);
				try {
					JSONObject jsonObj = new JSONObject(line);
					String message = jsonObj.getString(KeysContract.MESSAGE_KEY);
					if (message != null)
						SyncServerService.sendBroadcastMessageError(context, message);
				} catch (JSONException e){
					Log.e(TAG, "Http JSON error: " + e.getMessage());
				}
			}

			if(response == 200 || response == 203){
				AndroidUtil.clearNotofication(context, 1);
				QuiosgramaApp.connectionFailed = false;
				return true;
			}
			else{
				Log.e(TAG, "Server error " + response + ": " + conn.getResponseMessage());
				return false;
			}
		} catch (IOException e) {
			Log.e(TAG, "Http post error: " + e.getMessage());
			e.printStackTrace();
			SyncServerService.sendBroadcastMessageError(context, errorMessage);
			QuiosgramaApp.connectionFailed = true;
			
			return false;
		}
	}

	public static String buildUrl(Context context, String urlString) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String host = preferences.getString(context.getResources().getString(R.string.preference_host_key), context.getResources().getString(R.string.preference_host_default));
		String port = preferences.getString(context.getResources().getString(R.string.preference_port_key), context.getResources().getString(R.string.preference_port_default));
		
		return String.format("http://%s:%s/%s", host, port, urlString);
	}
}
