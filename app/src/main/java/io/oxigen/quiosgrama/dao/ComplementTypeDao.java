package io.oxigen.quiosgrama.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashSet;

import io.oxigen.quiosgrama.ProductType;
import io.oxigen.quiosgrama.data.DataProviderContract;

public class ComplementTypeDao {
	
	private static final String SELECTION = DataProviderContract.ComplementTypeTable.COMPLEMENT_COLUMN + " = ?";

	public static synchronized void insert(SQLiteDatabase db, String complement, long productType) {
		try{
			db.insert(DataProviderContract.ComplementTypeTable.TABLE_NAME, null, getValues(complement, productType));
		}catch(Exception e){
			
		}
	}
	
	public static HashSet<ProductType> getByComplement(Context context, String complement){
		HashSet<ProductType> typeSet = new HashSet<>();
		
		Cursor c = context.getContentResolver().query(
				DataProviderContract.ComplementTypeTable.TABLE_URI, null, SELECTION, new String[]{complement}, null);

		if(c.moveToFirst()){
			do{
				typeSet.add(new ProductType(c.getLong((c.getColumnIndex(DataProviderContract.ComplementTypeTable.ID_PRODUCT_TYPE_COLUMN)))));
			}while(c.moveToNext());
		}

		c.close();
		
		return typeSet;
	}
	
	private static ContentValues getValues(String complement, long productType){
		ContentValues values = new ContentValues();
		values.put(DataProviderContract.ComplementTypeTable.COMPLEMENT_COLUMN, complement);
		values.put(DataProviderContract.ComplementTypeTable.ID_PRODUCT_TYPE_COLUMN, productType);
		
		return values;
	}
}
