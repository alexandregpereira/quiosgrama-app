package io.oxigen.quiosgrama.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;

import io.oxigen.quiosgrama.Functionary;
import io.oxigen.quiosgrama.Poi;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.data.DataBaseCursorBuild;
import io.oxigen.quiosgrama.data.DataProviderContract;
import io.oxigen.quiosgrama.data.DataProviderHelper;

public class PoiDao {

	private static final String TAG = "PoiDao";
	private static final String TABLE_NAME = DataProviderContract.PoiTable.TABLE_NAME;
	
	private static final String[] POI_PROJECTION = {
		DataProviderContract.PoiTable.ID_COLUMN,
		DataProviderContract.PoiTable.NAME_COLUMN,
		DataProviderContract.PoiTable.X_POS_DPI_COLUMN,
		DataProviderContract.PoiTable.Y_POS_DPI_COLUMN,
		DataProviderContract.PoiTable.IMAGE_COLUMN,
		DataProviderContract.PoiTable.MAP_PAGE_NUMBER,
		DataProviderContract.PoiTable.POI_TIME,
		DataProviderContract.PoiTable.FUNCTIONARY_ID_COLUMN,
		DataProviderContract.PoiTable.SYNC_STATUS_COLUMN
	};
	
	private static final String SYNC_SELECTION = 
			DataProviderContract.PoiTable.SYNC_STATUS_COLUMN + 
			" = ?";

	public static synchronized void insertOrUpdate(Context context, ArrayList<Poi> poiList){
		QuiosgramaApp app = (QuiosgramaApp) context.getApplicationContext();
		DataProviderHelper mHelper = new DataProviderHelper(context);
		SQLiteDatabase db = mHelper.getWritableDatabase();

		db.beginTransaction();
		for(Poi poi : poiList){
			insertOrUpdate(app, db, poi);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}
	
	public static synchronized void insertOrUpdate(Context context, Poi poi){
		QuiosgramaApp app = (QuiosgramaApp) context.getApplicationContext();
		DataProviderHelper mHelper = new DataProviderHelper(context);
		SQLiteDatabase db = mHelper.getWritableDatabase();

		db.beginTransaction();
		insertOrUpdate(app, db, poi);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	private static synchronized void insertOrUpdate(QuiosgramaApp app, SQLiteDatabase db, Poi poi){
		ContentValues values = getValues(poi);
		boolean updateFlag = false;
		try{
			long id = db.insert(TABLE_NAME, null, values);
			if(id != -1){
				app.addPoi(poi);
			}
			else{
				updateFlag = true;
			}
		}catch (SQLiteException e){
			updateFlag = true;
		}

		if(updateFlag) {
			update(app, db, values, poi);
		}
	}

	private static synchronized void update(Context context, SQLiteDatabase db, ContentValues values, Poi poi) {
		db.update(
				TABLE_NAME,
				values,
				DataProviderContract.PoiTable.ID_COLUMN + " = " + poi.idPoi,
				null);

		insertOrUpdateList(context, poi);
	}

	public static synchronized void update(Context context, Poi poi) {
		try{
			context.getContentResolver().update(
					DataProviderContract.PoiTable.TABLE_URI, 
					getValues(poi), 
					DataProviderContract.PoiTable.ID_COLUMN + " = " + poi.idPoi, 
					null);
			
			insertOrUpdateList(context, poi);
		}catch(SQLiteException e){
			Log.e(TAG, "POI " + poi.toString());
		}
	}
	
	private static ContentValues getValues(Poi poi){
		ContentValues values = new ContentValues();
		
		values.put(DataProviderContract.PoiTable.ID_COLUMN, poi.idPoi);
		values.put(DataProviderContract.PoiTable.NAME_COLUMN, poi.name);
		values.put(DataProviderContract.PoiTable.X_POS_DPI_COLUMN, poi.xPosDpi);
		values.put(DataProviderContract.PoiTable.Y_POS_DPI_COLUMN, poi.yPosDpi);
		values.put(DataProviderContract.PoiTable.IMAGE_COLUMN, poi.image);
		values.put(DataProviderContract.PoiTable.MAP_PAGE_NUMBER, poi.mapPageNumber);
		
		String time = DataBaseCursorBuild.format.format(poi.poiTime);
		values.put(DataProviderContract.PoiTable.POI_TIME, time);
		values.put(DataProviderContract.PoiTable.FUNCTIONARY_ID_COLUMN, poi.waiterAlterPoi != null ? poi.waiterAlterPoi.id : null);
		values.put(DataProviderContract.PoiTable.SYNC_STATUS_COLUMN, poi.syncStatus);

		return values;
	}

	public static HashSet<Poi> getBySyncStatus(Context context) {
		Cursor c = context.getContentResolver().query(
				DataProviderContract.PoiTable.TABLE_URI,
				POI_PROJECTION,
				SYNC_SELECTION,
				new String[]{"1"},
				null
				);

		HashSet<Poi> poiList = new HashSet<Poi>();
		if(c.moveToFirst()){
			do{
				Poi poi = DataBaseCursorBuild.buildPoi(c);
				poiList.add(poi);
			}while(c.moveToNext());
		}
		c.close();
		
		return poiList;
	}

	public static ArrayList<Poi> getAll(Context context, ArrayList<Functionary> functionaryList, ArrayList<Poi> poiList) {
		if(poiList == null){
			poiList = new ArrayList<Poi>();
		}

		Cursor c = context.getContentResolver().query(
				DataProviderContract.PoiTable.TABLE_URI, null, null, null, null);

		if(c.moveToFirst()){
			do{
				poiList.add(DataBaseCursorBuild.buildPoiObject(c, functionaryList));
			}while(c.moveToNext());
		}

		c.close();
		
		return poiList;
	}

	public synchronized static void insertOrUpdateList(Context context, Poi poi) {
		QuiosgramaApp app = (QuiosgramaApp) context.getApplicationContext();
		int index = app.getPoiList().indexOf(poi);
		if(index >= 0)
			app.updatePoi(index, poi);
		else
			app.addPoi(poi);
	}
}
