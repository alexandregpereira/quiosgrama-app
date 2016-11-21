package io.oxigen.quiosgrama.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.ArrayList;

import io.oxigen.quiosgrama.Complement;
import io.oxigen.quiosgrama.ProductType;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.data.DataBaseCursorBuild;
import io.oxigen.quiosgrama.data.DataProviderContract;
import io.oxigen.quiosgrama.data.DataProviderHelper;

public class ComplementDao {

	private static final String TAG = "ComplementDao";
	private static final String TABLE_NAME = DataProviderContract.ComplementTable.TABLE_NAME;

	public static ArrayList<Complement> getAll(Context context, ArrayList<Complement> complementList){
		if(complementList == null){
			complementList = new ArrayList<>();
		}

		Cursor c = context.getContentResolver().query(
				DataProviderContract.ComplementTable.TABLE_URI, null, null, null, null);

		if(c.moveToFirst()){
			do{
				complementList.add(DataBaseCursorBuild.buildComplementObject(c));
			}while(c.moveToNext());
		}

		c.close();
		
		for (Complement complement : complementList) {
			complement.typeSet = ComplementTypeDao.getByComplement(context, complement.description);
		}

		return complementList;
	}

	public static synchronized void insertOrUpdate(Context context, ArrayList<Complement> complementList) {
		QuiosgramaApp app = (QuiosgramaApp) context.getApplicationContext();
		DataProviderHelper mHelper = new DataProviderHelper(context);
		SQLiteDatabase db = mHelper.getWritableDatabase();

		db.beginTransaction();
		for (Complement complement : complementList){
			insertOrUpdate(app, db, complement);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	public static synchronized void insertOrUpdate(Context context, Complement complement) {
		QuiosgramaApp app = (QuiosgramaApp) context.getApplicationContext();
		DataProviderHelper mHelper = new DataProviderHelper(context);
		SQLiteDatabase db = mHelper.getWritableDatabase();

		db.beginTransaction();
		insertOrUpdate(app, db, complement);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	private static synchronized void insertOrUpdate(QuiosgramaApp app, SQLiteDatabase db, Complement complement){
		if(complement.typeSet != null && !complement.typeSet.isEmpty()){
			for (ProductType type : complement.typeSet) {
				if(type.id > 0) ComplementTypeDao.insert(db, complement.description, type.id);
			}
		}

		ContentValues values = getValues(complement);
		boolean updateFlag = false;
		try {
			long id = db.insert(TABLE_NAME, null, values);
			if (id != -1) {
				app.addComplement(complement);
			}
			else{
				updateFlag = true;
			}
		} catch (SQLiteException e) {
			updateFlag = true;
		}

		if(updateFlag){
			update(db, app, values, complement);
		}
	}
	
	public static synchronized void update(SQLiteDatabase db, QuiosgramaApp app, ContentValues values, Complement complement){
		try{
			if(complement.typeSet != null && !complement.typeSet.isEmpty()){
				for (ProductType type : complement.typeSet) {
					if(type.id > 0) ComplementTypeDao.insert(db, complement.description, type.id);
				}
			}
			
			db.update(TABLE_NAME,
					values,
					DataProviderContract.ComplementTable.ID_COLUMN + " = ?",
					new String[]{complement.description});
			
			int index = app.getComplementList().indexOf(complement);
			if(index >= 0)
				app.updateComplement(index, complement);
			else
				app.addComplement(complement);
		}catch(Exception e){
			Log.e(TAG, "Complemento, erro ao fazer update");
		}
	}
	
	private static ContentValues getValues(Complement complement){
		ContentValues values = new ContentValues();
		values.put(DataProviderContract.ComplementTable.ID_COLUMN, complement.description);
		values.put(DataProviderContract.ComplementTable.PRICE_COLUMN, complement.price);
		values.put(DataProviderContract.ComplementTable.DRAWABLE_ID_COLUMN, complement.drawable);
		
		return values;
	}
}
