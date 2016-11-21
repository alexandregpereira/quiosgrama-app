package io.oxigen.quiosgrama.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;

import io.oxigen.quiosgrama.Functionary;
import io.oxigen.quiosgrama.data.DataBaseCursorBuild;
import io.oxigen.quiosgrama.data.DataProviderContract;
import io.oxigen.quiosgrama.data.DataProviderHelper;

public class FunctionaryDao {

//	private static final String TAG = "FunctionaryDao";
	private static final String TABLE_NAME = DataProviderContract.FunctionaryTable.TABLE_NAME;

	public static synchronized void insertOrUpdate(Context context, ArrayList<Functionary> functionaryList){
		DataProviderHelper mHelper = new DataProviderHelper(context);
		SQLiteDatabase db = mHelper.getWritableDatabase();

		db.beginTransaction();
		for(Functionary functionary : functionaryList){
			insertOrUpdate(db, functionary);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	private static synchronized void insertOrUpdate(SQLiteDatabase db, Functionary functionary) {
		ContentValues values = getValues(functionary);
		boolean updateFlag = false;
		try{
			long id = db.insert(TABLE_NAME, null, values);
			if(id == -1){
				updateFlag = true;
			}
		}catch(SQLiteException e){
			updateFlag = true;
		}

		if(updateFlag) {
			db.update(
					TABLE_NAME,
					values,
					DataProviderContract.FunctionaryTable.ID_COLUMN + " = " + functionary.id,
					null);
		}
	}

	private static ContentValues getValues(Functionary functionary) {
		ContentValues values = new ContentValues();
		values.put(DataProviderContract.FunctionaryTable.ID_COLUMN, functionary.id);
		values.put(DataProviderContract.FunctionaryTable.NAME_COLUMN, functionary.name);
		values.put(DataProviderContract.FunctionaryTable.IMEI_COLUMN, functionary.imei);
		values.put(DataProviderContract.FunctionaryTable.ADMIN_FLAG_COLUMN, functionary.adminFlag);

		return values;
	}
	
	public static ArrayList<Functionary> getAll(Context context, ArrayList<Functionary> functionaryList){
		if(functionaryList == null){
			functionaryList = new ArrayList<Functionary>();
		}
		
		Cursor c = context.getContentResolver().query(
				DataProviderContract.FunctionaryTable.TABLE_URI, null, null, null, null);
		
		if(c.moveToFirst()){
			do{
				functionaryList.add(DataBaseCursorBuild.buildFunctionaryObject(c));
			}while(c.moveToNext());
		}
		
		c.close();
		
		return functionaryList;
	}
}
