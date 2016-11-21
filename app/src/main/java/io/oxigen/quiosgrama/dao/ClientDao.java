package io.oxigen.quiosgrama.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.ArrayList;

import io.oxigen.quiosgrama.Client;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.data.DataBaseCursorBuild;
import io.oxigen.quiosgrama.data.DataProviderContract;

public class ClientDao {

	private static final String TAG = "ClientDao";
	
	public static synchronized void insertOrUpdate(Context context, Client client){
		try{
			context.getContentResolver().insert(DataProviderContract.ClientTable.TABLE_URI, getValues(client));
			
			QuiosgramaApp app = (QuiosgramaApp) context.getApplicationContext();
			app.addClient(client);
		}catch(SQLiteException e){
			update(context, client);
		}
	}
	
	public static synchronized void update(Context context, Client client) {
		try{
			context.getContentResolver().update(
					DataProviderContract.ClientTable.TABLE_URI, 
					getValues(client), 
					DataProviderContract.ClientTable.ID_COLUMN + " = " + client.id, 
					null);
			
			insertOrUpdateList(context, client);
		}catch(SQLiteException e){
			Log.e(TAG, "POI " + client.toString());
		}
	}
	
	private static ContentValues getValues(Client client){
		ContentValues values = new ContentValues();
		
		values.put(DataProviderContract.ClientTable.ID_COLUMN, client.id);
		values.put(DataProviderContract.ClientTable.NAME_COLUMN, client.name);
		values.put(DataProviderContract.ClientTable.CPF_COLUMN, client.cpf);
		values.put(DataProviderContract.ClientTable.PHONE_COLUMN, client.phone);
		values.put(DataProviderContract.ClientTable.TEMP_FLAG_COLUMN, client.tempFlag ? 1 : 0);
		values.put(DataProviderContract.ClientTable.PRESENT_FLAG_COLUMN, client.presentFlag ? 1 : 0);
		
		return values;
	}

	public static ArrayList<Client> getAll(Context context, ArrayList<Client> clientList) {
		if(clientList == null){
			clientList = new ArrayList<Client>();
		}

		Cursor c = context.getContentResolver().query(
				DataProviderContract.ClientTable.TABLE_URI, null, null, null, null);

		if(c.moveToFirst()){
			do{
				clientList.add(DataBaseCursorBuild.buildClientObject(c));
			}while(c.moveToNext());
		}

		c.close();
		
		return clientList;
	}

	public synchronized static void insertOrUpdateList(Context context, Client client) {
		QuiosgramaApp app = (QuiosgramaApp) context.getApplicationContext();
		int index = app.getClientList().indexOf(client);
		if(index >= 0)
			app.updateClient(index, client);
		else
			app.addClient(client);
	}
}
