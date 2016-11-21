package io.oxigen.quiosgrama.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import io.oxigen.quiosgrama.Bill;
import io.oxigen.quiosgrama.Functionary;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.Request;
import io.oxigen.quiosgrama.Table;
import io.oxigen.quiosgrama.data.DataBaseCursorBuild;
import io.oxigen.quiosgrama.data.DataProviderContract;
import io.oxigen.quiosgrama.data.DataProviderHelper;

public class BillDao {

	private static final String TAG = "BillDao";
	private static final String TABLE_NAME = DataProviderContract.BillTable.TABLE_NAME;

	public static final SQLiteQueryBuilder TABLE_JOIN_QUERY;

	static{
		TABLE_JOIN_QUERY = new SQLiteQueryBuilder();

		StringBuilder sb2 = new StringBuilder();
		sb2.append(TABLE_NAME + " AS " + DataProviderContract.BillTable.NICKNAME);
		sb2.append(" JOIN ");
		sb2.append(DataProviderContract.TableTable.TABLE_NAME + " AS " + DataProviderContract.TableTable.NICKNAME);
		sb2.append(" ON ");
		sb2.append(DataProviderContract.BillTable.NICKNAME + "." + DataProviderContract.BillTable.TABLE_ID_COLUMN);
		sb2.append(" = ");
		sb2.append(DataProviderContract.TableTable.NICKNAME + "." + DataProviderContract.TableTable.ID_COLUMN);
		String query = sb2.toString();

		TABLE_JOIN_QUERY.setTables(query);
	}

	private static final String[] PROJECTION = new String[]{
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

	private static final String[] BILL_NOTIFICATION_PROJECTION = new String[]{
		DataProviderContract.BillTable.CLOSE_TIME_COLUMN
	};

	private static final String CLOSE_TIME_SELECTION =
			DataProviderContract.BillTable.CLOSE_TIME_COLUMN + " IS NOT NULL" +
					" and " +
					DataProviderContract.BillTable.PAID_TIME_COLUMN + " IS NULL";

	private static final String SYNC_SELECTION =
			DataProviderContract.BillTable.SYNC_STATUS_COLUMN +
			" = ? and " + DataProviderContract.BillTable.OPEN_TIME_COLUMN + " IS NOT NULL";

	private static final String BILL_SELECTION =
			DataProviderContract.BillTable.PAID_TIME_COLUMN +
			" IS NULL";
	private static final Uri URI = DataProviderContract.BillTable.TABLE_URI;

	public static ArrayList<Bill> getAll(Context context, ArrayList<Functionary> functionaryList, 
			ArrayList<Table> tableList, ArrayList<Bill> billList){
		if(billList == null){
			billList = new ArrayList<Bill>();
		}
		
		Cursor c = context.getContentResolver().query(
				URI, null, BILL_SELECTION, null, null);
		
		if(c.moveToFirst()){
			do{
				billList.add(DataBaseCursorBuild.buildBillObject(c, functionaryList, tableList));
			}while(c.moveToNext());
		}
		
		c.close();
		
		Collections.sort(billList);
		
		return billList;
	}

	public static int getCountByBillClosed(Context context){
		Cursor c = context.getContentResolver().query(
				DataProviderContract.BillTable.TABLE_URI,
				BILL_NOTIFICATION_PROJECTION,
				CLOSE_TIME_SELECTION,
				null,
				null
		);

		int i = 0;
		if(c.moveToFirst()) {
			do {
				++i;
			}while(c.moveToNext());
		}
		c.close();
		return i;
	}

	public static synchronized void update(Context context, Bill bill) {
		try{
			context.getContentResolver().update(URI,
					getValues(bill),
					DataProviderContract.BillTable.ID_COLUMN + " = ?",
					new String[]{bill.id});

			QuiosgramaApp app = (QuiosgramaApp) context.getApplicationContext();
			if(bill.paidTime != null){
				app.removeBill(bill);
				syncBillWithTable(context);
			}
			else
				app.addOrUpdateBill(bill);
		}catch(SQLiteException e){
			Log.e(TAG, "Mesa " + bill.toString());
		}
	}

	private static synchronized void update(Context context, QuiosgramaApp app, SQLiteDatabase db, Bill bill) {
		try{
			db.update(TABLE_NAME,
					getValues(bill),
					DataProviderContract.BillTable.ID_COLUMN + " = ?",
					new String[]{bill.id});

			if(bill.paidTime != null){
				app.removeBill(bill);
				syncBillWithTable(context, db);
			}
			else
				app.addOrUpdateBill(bill);
		}catch(SQLiteException e){
			Log.e(TAG, "Mesa " + bill.toString());
		}
	}
	
	private static ContentValues getValues(Bill bill){
		ContentValues values = new ContentValues();
		values.put(DataProviderContract.BillTable.ID_COLUMN, bill.id);
		if(bill.openTime != null)
			values.put(DataProviderContract.BillTable.OPEN_TIME_COLUMN, DataBaseCursorBuild.format.format(bill.openTime));
		if(bill.closeTime != null)
			values.put(DataProviderContract.BillTable.CLOSE_TIME_COLUMN, DataBaseCursorBuild.format.format(bill.closeTime));
		if(bill.paidTime != null)
			values.put(DataProviderContract.BillTable.PAID_TIME_COLUMN, DataBaseCursorBuild.format.format(bill.paidTime));
		if(bill.waiterOpenTable != null)
			values.put(DataProviderContract.BillTable.WAITER_OPEN_TABLE_ID_COLUMN, bill.waiterOpenTable.id);
		if(bill.waiterCloseTable != null)
			values.put(DataProviderContract.BillTable.WAITER_CLOSE_TABLE_ID_COLUMN, bill.waiterCloseTable.id);
		
		String time = DataBaseCursorBuild.format.format(bill.billTime);
		values.put(DataProviderContract.BillTable.BILL_TIME, time);
		
		values.put(DataProviderContract.BillTable.TABLE_ID_COLUMN, bill.table.number);
		values.put(DataProviderContract.BillTable.SYNC_STATUS_COLUMN, bill.syncStatus);
		values.put(DataProviderContract.BillTable.SERVICE_PAID_COLUMN, bill.servicePaid);

		return values;
	}

	public static synchronized void insertOrUpdate(Context context, ArrayList<Bill> billList) {
		QuiosgramaApp app = (QuiosgramaApp) context.getApplicationContext();
		DataProviderHelper mHelper = new DataProviderHelper(context);
		SQLiteDatabase db = mHelper.getWritableDatabase();

		db.beginTransaction();
		for (Bill bill : billList){
			insertOrUpdate(context, app, db, bill);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	public static synchronized void insertOrUpdate(Context context, Bill bill) {
		QuiosgramaApp app = (QuiosgramaApp) context.getApplicationContext();
		DataProviderHelper mHelper = new DataProviderHelper(context);
		SQLiteDatabase db = mHelper.getWritableDatabase();

		db.beginTransaction();
		insertOrUpdate(context, app, db, bill);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	private static synchronized void insertOrUpdate(Context context, QuiosgramaApp app, SQLiteDatabase db, Bill bill){
		if (!verifyBillId(app, db, bill)) {
			boolean updateFlag = false;
			try {
				long id = db.insert(TABLE_NAME, null, getValues(bill));
				if(id != -1){
					app.addOrUpdateBill(bill);
				}
				else{
					updateFlag = true;
				}
			}catch(SQLiteException e){
				updateFlag = true;
			}

			if(updateFlag){
				Bill bill2 = get(db, bill.id);

				if (bill2 != null && bill.billTime.compareTo(bill2.billTime) >= 0) {
					update(context, app , db, bill);
				}
			}
		}
	}

	private static boolean verifyBillId(QuiosgramaApp app, SQLiteDatabase db, Bill bill) {
		Cursor c = TABLE_JOIN_QUERY.query(
				db,
				PROJECTION,
				DataProviderContract.BillTable.TABLE_ID_COLUMN + " = ? and " + DataProviderContract.BillTable.PAID_TIME_COLUMN + " IS NULL",
				new String[]{String.valueOf(bill.table.number)}
				, null, null, null);

		Bill bill2 = getByNumber(c);
		if(bill2 != null && !bill2.equals(bill) 
				&& bill.billTime.compareTo(bill2.billTime) >= 0){

			db.insert(TABLE_NAME, null, getValues(bill));
			app.addOrUpdateBill(bill);
			
			HashSet<Request> requestList = RequestDao.getAllByBill(db, bill2.id);
			for (Request request : requestList) {
				request.bill = bill;
				RequestDao.update(db, request);
			}
			
			delete(db, bill2.id);
			return true;
		}
		
		return false;
	}

	public static void delete(SQLiteDatabase db, String id) {
		try{
			db.delete(TABLE_NAME,
					DataProviderContract.BillTable.ID_COLUMN + " = ?",
					new String[]{id});
		}
		catch (Exception e){
			Log.e(TAG, "Erro ao excluir");
		}
	}

	private static Bill get(SQLiteDatabase db, String id) {
		Cursor c = TABLE_JOIN_QUERY.query(
				db,
				PROJECTION, 
				DataProviderContract.BillTable.ID_COLUMN + " = ?", 
				new String[]{id}, 
				null, null, null);

		Bill bill = null;
		if(c.moveToFirst()){
			bill = DataBaseCursorBuild.buildBill(c);
		}
		c.close();
		
		return bill;
	}
	
	private static Bill getByNumber(Cursor c){
		Bill bill = null;
		if(c.moveToFirst()){
			bill =  DataBaseCursorBuild.buildBill(c);
		}
		c.close();

		return bill;
	}

	public static HashSet<Bill> getBySyncStatus(Context context) {
		Cursor c = context.getContentResolver().query(
				URI,
				PROJECTION,
				SYNC_SELECTION,
				new String[]{"1"},
				null
				);

		HashSet<Bill> billList = new HashSet<Bill>();
		if(c.moveToFirst()){
			do{
				Bill bill = DataBaseCursorBuild.buildBill(c);
				billList.add(bill);
			}while(c.moveToNext());
		}
		c.close();
		
		return billList;
	}
	
	public static void syncBillWithTable(Context context){
		DataProviderHelper mHelper = new DataProviderHelper(context);
		SQLiteDatabase db = mHelper.getWritableDatabase();
		db.beginTransaction();

		syncBillWithTable(context, db);

		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	private static void syncBillWithTable(Context context, SQLiteDatabase db){
		QuiosgramaApp app = (QuiosgramaApp) context.getApplicationContext();
		if(app.getTableList().size() > app.getBillList().size() ){
			for (Table table : app.getTableList()) {
				Bill bill = QuiosgramaApp.searchBill(table.number);
				if(bill == null){
					bill = new Bill(table, null, null, null);
					insertOrUpdate(context, app, db, bill);
					app.sortBillList();
				}
				else
					bill.table = table;
			}
		}
		else if(app.getBillList().size() > app.getTableList().size()){
			ArrayList<Bill> billList = new ArrayList<>();
			for (Table table : app.getTableList()) {
				Bill bill = QuiosgramaApp.searchBill(table.number);
				if(bill != null){
					billList.add(bill);
				}
			}

			app.createBillList(billList);
		}
		else{
			for (Table table : app.getTableList()) {
				Bill bill = QuiosgramaApp.searchBill(table.number);
				if(bill != null){
					bill.table = table;
				}
			}
		}
	}
}
