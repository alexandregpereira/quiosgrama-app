package io.oxigen.quiosgrama.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;

import io.oxigen.quiosgrama.Request;
import io.oxigen.quiosgrama.data.DataBaseCursorBuild;
import io.oxigen.quiosgrama.data.DataProviderContract;
import io.oxigen.quiosgrama.data.DataProviderHelper;

public class RequestDao {

	private static final String TAG = "RequestDao";
	private static final String TABLE_NAME = DataProviderContract.RequestTable.TABLE_NAME;

	public static final SQLiteQueryBuilder TABLE_JOIN_QUERY;
	public static final SQLiteQueryBuilder TABLE_JOIN_BILL_QUERY;

	static{
		TABLE_JOIN_QUERY = new SQLiteQueryBuilder();

		StringBuilder sb1 = new StringBuilder();
		sb1.append(TABLE_NAME + " AS " + DataProviderContract.RequestTable.NICKNAME);
		sb1.append(" JOIN ");
		sb1.append(DataProviderContract.FunctionaryTable.TABLE_NAME + " AS " + DataProviderContract.FunctionaryTable.NICKNAME);
		sb1.append(" ON ");
		sb1.append(DataProviderContract.RequestTable.NICKNAME + "." + DataProviderContract.RequestTable.FUNCTIONARY_ID_COLUMN);
		sb1.append(" = ");
		sb1.append(DataProviderContract.FunctionaryTable.NICKNAME + "." + DataProviderContract.FunctionaryTable.ID_COLUMN);
		String query = sb1.toString();
		TABLE_JOIN_QUERY.setTables(query);

		TABLE_JOIN_BILL_QUERY = new SQLiteQueryBuilder();
		sb1.append(" JOIN ");
		sb1.append(DataProviderContract.BillTable.TABLE_NAME + " AS " + DataProviderContract.BillTable.NICKNAME);
		sb1.append(" ON ");
		sb1.append(DataProviderContract.RequestTable.NICKNAME + "." + DataProviderContract.RequestTable.BILL_ID_COLUMN);
		sb1.append(" = ");
		sb1.append(DataProviderContract.BillTable.NICKNAME + "." + DataProviderContract.BillTable.ID_COLUMN);
		sb1.append(" JOIN ");
		sb1.append(DataProviderContract.TableTable.TABLE_NAME + " AS " + DataProviderContract.TableTable.NICKNAME);
		sb1.append(" ON ");
		sb1.append(DataProviderContract.BillTable.NICKNAME + "." + DataProviderContract.BillTable.TABLE_ID_COLUMN);
		sb1.append(" = ");
		sb1.append(DataProviderContract.TableTable.NICKNAME + "." + DataProviderContract.TableTable.ID_COLUMN);
		String query2 = sb1.toString();
		TABLE_JOIN_BILL_QUERY.setTables(query2);
	}
	
	public static final String[] REQUEST_PROJECTION = {
		DataProviderContract.RequestTable.ID_COLUMN,
		DataProviderContract.RequestTable.REQUEST_TIME_COLUMN,
		DataProviderContract.RequestTable.SYNC_STATUS_COLUMN,
		DataProviderContract.RequestTable.NICKNAME + "." +
				DataProviderContract.RequestTable.FUNCTIONARY_ID_COLUMN,
		DataProviderContract.FunctionaryTable.NAME_COLUMN,
		DataProviderContract.FunctionaryTable.ADMIN_FLAG_COLUMN
	};
	
	public static final String REQUEST_SELECTION = 
			DataProviderContract.RequestTable.BILL_ID_COLUMN +
			" = ?";
	
	private static final String[] PROJECTION = {
		DataProviderContract.RequestTable.ID_COLUMN,
		DataProviderContract.RequestTable.REQUEST_TIME_COLUMN,
		DataProviderContract.RequestTable.SYNC_STATUS_COLUMN,
		DataProviderContract.RequestTable.BILL_ID_COLUMN,
		DataProviderContract.RequestTable.FUNCTIONARY_ID_COLUMN,
		DataProviderContract.FunctionaryTable.NAME_COLUMN,
	};

	private static final String[] JOIN_PROJECTION = {
			DataProviderContract.RequestTable.ID_COLUMN,
			DataProviderContract.RequestTable.REQUEST_TIME_COLUMN,
			DataProviderContract.RequestTable.SYNC_STATUS_COLUMN,
			DataProviderContract.RequestTable.FUNCTIONARY_ID_COLUMN,
			DataProviderContract.FunctionaryTable.NAME_COLUMN,
			DataProviderContract.BillTable.ID_COLUMN,
			DataProviderContract.BillTable.OPEN_TIME_COLUMN,
			DataProviderContract.BillTable.CLOSE_TIME_COLUMN,
			DataProviderContract.BillTable.PAID_TIME_COLUMN,
			DataProviderContract.BillTable.WAITER_OPEN_TABLE_ID_COLUMN,
			DataProviderContract.BillTable.WAITER_CLOSE_TABLE_ID_COLUMN,
			DataProviderContract.BillTable.SERVICE_PAID_COLUMN,
			DataProviderContract.BillTable.BILL_TIME,
			DataProviderContract.BillTable.SYNC_STATUS_COLUMN,
			DataProviderContract.TableTable.ID_COLUMN,
			DataProviderContract.TableTable.X_POS_DPI_COLUMN,
			DataProviderContract.TableTable.Y_POS_DPI_COLUMN,
			DataProviderContract.TableTable.MAP_PAGE_NUMBER,
			DataProviderContract.TableTable.TABLE_TIME,
			DataProviderContract.TableTable.FUNCTIONARY_ID_COLUMN,
			DataProviderContract.TableTable.CLIENT_TEMP_COLUMN,
			DataProviderContract.TableTable.SYNC_STATUS_COLUMN,
			DataProviderContract.TableTable.SHOW_COLUMN
	};

	public static synchronized void update(Context context, Request request) {
		try{
			context.getContentResolver().update(
					DataProviderContract.RequestTable.TABLE_URI,
					getValues(request),
					DataProviderContract.RequestTable.ID_COLUMN + " = " + "'" + request.id + "'",
					null);
		}catch(Exception e){
			Log.e(TAG, e.getMessage());
		}
	}

	public static synchronized void update(SQLiteDatabase db, Request request) {
		try{
			db.update(
					DataProviderContract.RequestTable.TABLE_NAME,
					getValues(request),
					DataProviderContract.RequestTable.ID_COLUMN + " = " + "'" + request.id + "'",
					null);
		}catch(Exception e){
			Log.e(TAG, e.getMessage());
		}
	}

	public static synchronized void insertOrUpdate(Context context, Request request){
		DataProviderHelper mHelper = new DataProviderHelper(context);
		SQLiteDatabase db = mHelper.getWritableDatabase();

		db.beginTransaction();
		insertOrUpdate(db, request);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	public static synchronized void insertOrUpdate(Context context, ArrayList<Request> requestList){
		DataProviderHelper mHelper = new DataProviderHelper(context);
		SQLiteDatabase db = mHelper.getWritableDatabase();

		db.beginTransaction();
		for (Request request : requestList){
			insertOrUpdate(db, request);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	private static synchronized void insertOrUpdate(SQLiteDatabase db, Request request) {
		boolean updateFlag = false;
		try {
			long id = db.insert(TABLE_NAME, null, getValues(request));
			if(id == -1){
				updateFlag = true;
			}
		}catch(SQLiteException e){
			updateFlag = true;
		}

		if(updateFlag){
			Request request2 = get(db, request.id);

			if(request2 != null && request.compareTo(request2) >= 0){
				update(db, request);
			}
			else{
				Log.e(TAG, "Erro ao tentar atualizar o Request");
			}
		}
	}

	private static Request get(SQLiteDatabase db, String id) {
		Cursor c = TABLE_JOIN_QUERY.query(
				db,
				PROJECTION,
				DataProviderContract.RequestTable.ID_COLUMN + " = ?",
				new String[]{id},
				null, null, null);

		if (c.moveToFirst()) {
			return DataBaseCursorBuild.buildRequest(c);
		}

		c.close();

		return null;
	}

	public static synchronized Request get(Context context, String id){
		DataProviderHelper mHelper = new DataProviderHelper(context);
		SQLiteDatabase db = mHelper.getWritableDatabase();

		db.beginTransaction();

		Cursor c = TABLE_JOIN_BILL_QUERY.query(
				db,
				JOIN_PROJECTION,
				DataProviderContract.RequestTable.ID_COLUMN + " = ?",
				new String[]{id},
				null, null, null);

		Request request = null;
		if (c.moveToFirst()) {
			request = DataBaseCursorBuild.buildRequest(c);
		}

		c.close();

		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();

		return request;
	}
	
	public static HashSet<Request> getAllByBill(SQLiteDatabase db, String billId) {
		Cursor c = TABLE_JOIN_QUERY.query(
				db,
				null, 
				DataProviderContract.RequestTable.BILL_ID_COLUMN + " = ?", 
				new String[]{billId}, 
				null, null, null);

		HashSet<Request> requestList = new HashSet<>();
		if(c.moveToFirst()){
			do{
				requestList.add(DataBaseCursorBuild.buildRequest(c));
			}while(c.moveToNext());
		}
		
		c.close();
		
		return requestList;
	}

	private static ContentValues getValues(Request request) {
		ContentValues values = new ContentValues();
		values.put(DataProviderContract.RequestTable.ID_COLUMN, request.id);
		values.put(DataProviderContract.RequestTable.REQUEST_TIME_COLUMN, DataBaseCursorBuild.format.format(request.requestTime));
		values.put(DataProviderContract.RequestTable.SYNC_STATUS_COLUMN, request.syncStatus);
		values.put(DataProviderContract.RequestTable.BILL_ID_COLUMN, request.bill.id);
		values.put(DataProviderContract.RequestTable.FUNCTIONARY_ID_COLUMN, request.waiter.id);
		return values;
	}
}
