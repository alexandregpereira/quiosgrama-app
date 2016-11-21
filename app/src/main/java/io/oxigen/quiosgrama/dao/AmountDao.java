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

import io.oxigen.quiosgrama.Amount;
import io.oxigen.quiosgrama.data.DataBaseCursorBuild;
import io.oxigen.quiosgrama.data.DataProviderContract;
import io.oxigen.quiosgrama.data.DataProviderHelper;
import io.oxigen.quiosgrama.data.KeysContract;

public class AmountDao {

	private static final String TAG = "AmountDao";
	private static final String TABLE_NAME = DataProviderContract.AmountTable.TABLE_NAME;

	public static final SQLiteQueryBuilder TABLE_JOIN_QUERY;

	static{
		TABLE_JOIN_QUERY = new SQLiteQueryBuilder();

		StringBuilder sb2 = new StringBuilder();
		sb2.append(TABLE_NAME + " AS " + DataProviderContract.AmountTable.NICKNAME);
		sb2.append(" JOIN ");
		sb2.append(DataProviderContract.BillTable.TABLE_NAME + " AS " + DataProviderContract.BillTable.NICKNAME);
		sb2.append(" ON ");
		sb2.append(DataProviderContract.AmountTable.NICKNAME + "." + DataProviderContract.AmountTable.BILL_ID_COLUMN);
		sb2.append(" = ");
		sb2.append(DataProviderContract.BillTable.NICKNAME + "." + DataProviderContract.BillTable.ID_COLUMN);
		String query = sb2.toString();

		TABLE_JOIN_QUERY.setTables(query);
	}

	private static final String[] PROJECTION = new String[]{
			DataProviderContract.AmountTable.ID_COLUMN,
			DataProviderContract.AmountTable.VALUE_COLUMN,
			DataProviderContract.AmountTable.PAID_METHOD_COLUMN,
			DataProviderContract.AmountTable.SYNC_STATUS_COLUMN,
			DataProviderContract.AmountTable.BILL_ID_COLUMN
	};
	
	private static final String SYNC_SELECTION = 
			DataProviderContract.AmountTable.SYNC_STATUS_COLUMN + " = " +
					KeysContract.NO_SYNCHRONIZED_STATUS_KEY;

	private static ContentValues getValues(Amount amount){
		ContentValues values = new ContentValues();
		values.put(DataProviderContract.AmountTable.ID_COLUMN, amount.id);
		values.put(DataProviderContract.AmountTable.VALUE_COLUMN, amount.value);
		values.put(DataProviderContract.AmountTable.PAID_METHOD_COLUMN, amount.paidMethod);
		values.put(DataProviderContract.AmountTable.SYNC_STATUS_COLUMN, amount.syncStatus);
		values.put(DataProviderContract.AmountTable.BILL_ID_COLUMN, amount.bill.id);
		
		return values;
	}

	public static synchronized void insert(Context context, ArrayList<Amount> amountList) {
		DataProviderHelper mHelper = new DataProviderHelper(context);
		SQLiteDatabase db = mHelper.getWritableDatabase();

		db.beginTransaction();
		for (Amount amount : amountList){
			insert(db, amount);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	public static synchronized void update(Context context, Amount amount) {
		try{
			context.getContentResolver().update(
					DataProviderContract.AmountTable.TABLE_URI,
					getValues(amount),
					DataProviderContract.AmountTable.ID_COLUMN + " = ?",
					new String[]{amount.id});
		}catch(SQLiteException e){
			Log.e(TAG, amount.toString());
		}
	}


	private static synchronized void insert(SQLiteDatabase db, Amount amount){
		try {
			long id = db.insert(TABLE_NAME, null, getValues(amount));
			if(id == -1){
				Log.e(TAG, "Erro ao incluir Amount");
			}
		}catch(SQLiteException e){
			Log.e(TAG, "Erro ao incluir Amount");
		}
	}

	public static HashSet<Amount> getBySyncStatus(Context context) {
		Cursor c = context.getContentResolver().query(
				DataProviderContract.AmountTable.TABLE_URI,
				PROJECTION,
				SYNC_SELECTION,
				null,
				null
				);

		HashSet<Amount> amountList = new HashSet<>();
		if(c.moveToFirst()){
			do{
				Amount amount = DataBaseCursorBuild.buildAmount(c);
				amountList.add(amount);
			}while(c.moveToNext());
		}
		c.close();
		
		return amountList;
	}
}
