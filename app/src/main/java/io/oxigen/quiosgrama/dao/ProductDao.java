package io.oxigen.quiosgrama.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;

import io.oxigen.quiosgrama.Product;
import io.oxigen.quiosgrama.ProductType;
import io.oxigen.quiosgrama.data.DataBaseCursorBuild;
import io.oxigen.quiosgrama.data.DataProviderContract;
import io.oxigen.quiosgrama.data.DataProviderHelper;

public class ProductDao {

//	private static final String TAG = "ProductDao";
	private static final String TABLE_NAME = DataProviderContract.ProductTable.TABLE_NAME;

	private static final String[] SIMPLE_PROJECTION = {
			DataProviderContract.ProductTable.ID_COLUMN
	};

	public static void insertOrUpdate(Context context, ArrayList<Product> productList){
		DataProviderHelper mHelper = new DataProviderHelper(context);
		SQLiteDatabase db = mHelper.getWritableDatabase();

		db.beginTransaction();
		for (Product product : productList){
			ContentValues values = getValues(product);

			try{
				String selection = DataProviderContract.ProductTable.ID_COLUMN + " = " + product.code;
				Cursor c = db.query(DataProviderContract.ProductTable.TABLE_NAME, SIMPLE_PROJECTION, selection, null, null, null, null);
				if(c.moveToFirst()){
					db.update(TABLE_NAME, values, selection, null);
				}
				else{
					db.insert(TABLE_NAME, null, values);
				}

				c.close();
			}catch (SQLiteException e){
				e.printStackTrace();
			}
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	private static ContentValues getValues(Product product){
		ContentValues values = new ContentValues();
		values.put(DataProviderContract.ProductTable.ID_COLUMN, product.code);
		values.put(DataProviderContract.ProductTable.NAME_COLUMN, product.name);
		values.put(DataProviderContract.ProductTable.PRICE_COLUMN, product.price);
		values.put(DataProviderContract.ProductTable.DESCRIPTION_COLUMN, product.description);
		values.put(DataProviderContract.ProductTable.POPULARITY_COLUMN, product.popularity);
		values.put(DataProviderContract.ProductTable.TAX_COLUMN, product.tax);
		values.put(DataProviderContract.ProductTable.PRODUCT_TYPE_ID_COLUMN, product.type.id);

		return values;
	}

	public static void getAll(Context context, ArrayList<ProductType> productTypeList, ArrayList<Product> productList) {
		if(productList == null)
			productList = new ArrayList<>();

		Cursor c = context. getContentResolver().query(
				DataProviderContract.ProductTable.TABLE_URI, null, null, null,
				DataProviderContract.ProductTable.POPULARITY_COLUMN + " DESC");
		
		if(c.moveToFirst()){
			do{
				productList.add(DataBaseCursorBuild.buildProductObject(c, productTypeList));
			}while(c.moveToNext());
		}
		
		c.close();
	}
}
