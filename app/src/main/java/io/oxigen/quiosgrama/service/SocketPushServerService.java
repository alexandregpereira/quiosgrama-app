package io.oxigen.quiosgrama.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import io.oxigen.quiosgrama.data.KeysContract;

public class SocketPushServerService extends IntentService {

	private static final String TAG = "PushServerSocketService";

	//static ServerSocket variable
	private static ServerSocket server;
	//socket server port on which it will listen
	private static int port = 9876;
	
	public SocketPushServerService() {
		super(SocketPushServerService.class.getSimpleName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		//keep listens indefinitely until receives 'exit' call or program terminates
		try {
			server = new ServerSocket(port);
			while(true){
				Socket socket = null;
				BufferedReader in = null;
				PrintWriter out = null;
				try {

					Log.i(TAG, "Waiting for client request");
					//creating socket and waiting for client connection
					socket = server.accept();

					//read from socket to ObjectInputStream object
					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

					//convert ObjectInputStream object to String
					String json = in.readLine();
					Log.i(TAG, "Message Received: " + json);

					//							out = new PrintWriter(socket.getOutputStream(), true);
					//write object to Socket
					//							out.write("foi");

					//							ois.close();
					//							oos.close();
					//							socket.close();

					if(json!= null && !json.isEmpty() ){
						JSONObject jsonObj = new JSONObject(json);

						int resultCode = jsonObj.getInt(KeysContract.GCM_RESULT_CODE_KEY);
						String dataJson = jsonObj.getString(KeysContract.GCM_DATA_JSON_KEY);
						String licence = jsonObj.getString(KeysContract.LICENCE_KEY);

						GcmIntentService.insertData(this, resultCode, dataJson, licence);

					}
				} catch (IOException e) {
					Log.e(TAG, e.getMessage() != null ? e.getMessage() : "IOException");
					out = new PrintWriter(socket.getOutputStream());
					out.write("IOException");
				} 
				catch (JSONException e) {
					Log.e(TAG, e.getMessage());
				}
				finally{
					try{
						if(in != null)
							in.close();
					} catch(IOException e){}
					if(out != null)
						out.close();
					try{
						if(socket != null)
							socket.close();
					} catch(IOException e){}
				}
				//        server.close();
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage() != null ? e.getMessage() : "Server IOException");
		}
		
		Intent intentService = new Intent(this, SocketPushServerService.class);
		startService(intentService);
	}
}
