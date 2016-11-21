package io.oxigen.quiosgrama.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import io.oxigen.quiosgrama.Client;
import io.oxigen.quiosgrama.Functionary;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.Table;
import io.oxigen.quiosgrama.data.DataBaseCursorBuild;
import io.oxigen.quiosgrama.data.DataProviderContract;
import io.oxigen.quiosgrama.data.DataProviderHelper;

public class TableDao {

	private final static Uri URI = DataProviderContract.TableTable.TABLE_URI;
	private static final String TABLE_NAME = DataProviderContract.TableTable.TABLE_NAME;
	private static final String TAG = "TableDao";

	private static final String[] TABLE_PROJECTION = {
		DataProviderContract.TableTable.ID_COLUMN,
		DataProviderContract.TableTable.X_POS_DPI_COLUMN,
		DataProviderContract.TableTable.Y_POS_DPI_COLUMN,
		DataProviderContract.TableTable.MAP_PAGE_NUMBER,
		DataProviderContract.TableTable.TABLE_TIME,
		DataProviderContract.TableTable.FUNCTIONARY_ID_COLUMN,
		DataProviderContract.TableTable.SYNC_STATUS_COLUMN,
		DataProviderContract.TableTable.CLIENT_TEMP_COLUMN,
		DataProviderContract.TableTable.SHOW_COLUMN
	};

	private static final String SYNC_SELECTION = 
			DataProviderContract.TableTable.SYNC_STATUS_COLUMN + 
			" = ?";

	private static final String ALL_SELECTION =
			DataProviderContract.TableTable.SHOW_COLUMN +
					" = 1";
	
	public static Table get(Context context, int tableNumber){
		Cursor c = context.getContentResolver().query(URI, null, 
				DataProviderContract.TableTable.ID_COLUMN + " = ?", 
				new String[]{String.valueOf(tableNumber)}, 
				null);

		return get(c);
	}

	public static Table get(Cursor c){
		Table table = null;
		if(c.moveToFirst()) {
			table = DataBaseCursorBuild.buildTable(c);
		}
		c.close();

		return table;
	}

	public static ArrayList<Table> getAll(Context context, ArrayList<Client> clientList, ArrayList<Functionary> functionaryList, ArrayList<Table> tableList){
		if(tableList == null){
			tableList = new ArrayList<Table>();
		}

		Cursor c = context.getContentResolver().query(
				DataProviderContract.TableTable.TABLE_URI, null, ALL_SELECTION, null, null);

		if(c.moveToFirst()){
			do{
				tableList.add(DataBaseCursorBuild.buildTableObject(c, clientList, functionaryList));
			}while(c.moveToNext());
		}

		c.close();
		
		Collections.sort(tableList);

		return tableList;
	}

	public static synchronized void insertOrUpdate(Context context, ArrayList<Table> tableList){
		QuiosgramaApp app = (QuiosgramaApp) context.getApplicationContext();
		DataProviderHelper mHelper = new DataProviderHelper(context);
		SQLiteDatabase db = mHelper.getWritableDatabase();

		db.beginTransaction();
		for (Table table : tableList){
			insertOrUpdate(app, db, table);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	public static synchronized void insertOrUpdate(Context context, Table table){
		QuiosgramaApp app = (QuiosgramaApp) context.getApplicationContext();
		DataProviderHelper mHelper = new DataProviderHelper(context);
		SQLiteDatabase db = mHelper.getWritableDatabase();

		db.beginTransaction();
		insertOrUpdate(app, db, table);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	private static synchronized void insertOrUpdate(QuiosgramaApp app, SQLiteDatabase db, Table table){
		ContentValues values = getValues(table);
		boolean updateFlag = false;
		try {
			long id = db.insert(TABLE_NAME, null, values);
			if(id != -1){
				app.addTable(table);
			}
			else{
				updateFlag = true;
			}
		}catch(SQLiteException e){
			updateFlag = true;
		}

		if(updateFlag) {
			Cursor c = db.query(TABLE_NAME, null,
					DataProviderContract.TableTable.ID_COLUMN + " = " + table.number,
					null,
					null, null, null);

			Table table2 = get(c);
			if(table.tableTime.compareTo(table2.tableTime) >= 0){
				db.update(
						TABLE_NAME,
						values,
						DataProviderContract.TableTable.ID_COLUMN + " = " + table.number,
						null);

				int index = app.getTableList().indexOf(table);
				if(index >= 0)
					app.updateTable(index, table);
				else
					app.addTable(table);
			}
		}
	}

	public static synchronized void update(Context context, Table table) {
		try{
			context.getContentResolver().update(
					DataProviderContract.TableTable.TABLE_URI, 
					getValues(table), 
					DataProviderContract.TableTable.ID_COLUMN + " = " + table.number, 
					null);
			
			QuiosgramaApp app = (QuiosgramaApp) context.getApplicationContext();
			int index = app.getTableList().indexOf(table);
			if(index >= 0)
				app.updateTable(index, table);
			else
				app.addTable(table);
		}catch(SQLiteException e){
			Log.e(TAG, "Mesa " + table.toString());
		}
	}

	public static synchronized void insert(Context context, Table table) {
		try{
			context.getContentResolver().insert(DataProviderContract.TableTable.TABLE_URI, getValues(table));
			
			QuiosgramaApp app = (QuiosgramaApp) context.getApplicationContext();
			app.addTable(table);
		}catch(SQLiteException e){
			Log.e(TAG, "Mesa " + table.toString());
		}
	}
	
	private static ContentValues getValues(Table table){
		ContentValues values = new ContentValues();
		
		values.put(DataProviderContract.TableTable.ID_COLUMN, table.number);
		values.put(DataProviderContract.TableTable.X_POS_DPI_COLUMN, table.xPosInDpi);
		values.put(DataProviderContract.TableTable.Y_POS_DPI_COLUMN, table.yPosDpi);
		values.put(DataProviderContract.TableTable.MAP_PAGE_NUMBER, table.mapPageNumber);

		String time = DataBaseCursorBuild.format.format(table.tableTime);
		values.put(DataProviderContract.TableTable.TABLE_TIME, time);
		values.put(DataProviderContract.TableTable.FUNCTIONARY_ID_COLUMN, table.waiterAlterTable != null ? table.waiterAlterTable.id : null);
		values.put(DataProviderContract.TableTable.SYNC_STATUS_COLUMN, table.syncStatus);
		values.put(DataProviderContract.TableTable.CLIENT_TEMP_COLUMN, table.clientTemp);
		values.put(DataProviderContract.TableTable.CLIENT_ID_COLUMN, table.client != null ? table.client.id : null);
		values.put(DataProviderContract.TableTable.SHOW_COLUMN, table.show);

		return values;
	}

	public static HashSet<Table> getBySyncStatus(Context context) {
		Cursor c = context.getContentResolver().query(
				DataProviderContract.TableTable.TABLE_URI,
				TABLE_PROJECTION,
				SYNC_SELECTION,
				new String[]{"1"},
				null
				);

		HashSet<Table> tableList = new HashSet<Table>();
		if(c.moveToFirst()){
			do{
				Table table = DataBaseCursorBuild.buildTable(c);
				tableList.add(table);
			}while(c.moveToNext());
		}
		c.close();
		
		return tableList;
	}
}
