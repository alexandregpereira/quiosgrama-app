package io.oxigen.quiosgrama.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;

import io.oxigen.quiosgrama.ProductType;
import io.oxigen.quiosgrama.data.DataBaseCursorBuild;
import io.oxigen.quiosgrama.data.DataProviderContract;
import io.oxigen.quiosgrama.data.DataProviderHelper;

public class ProductTypeDao {

//	private static final String TAG = "ProductTypeDao";
    private static final String TABLE_NAME = DataProviderContract.ProductTypeTable.TABLE_NAME;

	public static synchronized void insertOrUpdate(Context context, ArrayList<ProductType> typeList){
        DataProviderHelper mHelper = new DataProviderHelper(context);
        SQLiteDatabase db = mHelper.getWritableDatabase();

        db.beginTransaction();
        for(ProductType type : typeList){
            insertOrUpdate(db, type);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
	}

    private static synchronized void insertOrUpdate(SQLiteDatabase db, ProductType type) {
        ContentValues values = getValues(type);
        boolean updateFlag = false;
        try{
            long id = db.insert(TABLE_NAME, null, values);
            if(id == -1){
                updateFlag = true;
            }
        }catch(SQLiteException e){
            updateFlag = true;
        }

        if(updateFlag){
            values.remove(DataProviderContract.ProductTypeTable.ID_COLUMN);
            db.update(
                    TABLE_NAME,
                    values,
                    DataProviderContract.ProductTypeTable.ID_COLUMN + " = '" + type.id + "'",
                    null);
        }
    }

    private static ContentValues getValues(ProductType type){
		ContentValues values = new ContentValues();
		values.put(DataProviderContract.ProductTypeTable.ID_COLUMN, type.id);
		values.put(DataProviderContract.ProductTypeTable.NAME_COLUMN, type.name);
		values.put(DataProviderContract.ProductTypeTable.PRIORITY_COLUMN, type.priority);
		values.put(DataProviderContract.ProductTypeTable.BUTTON_IMAGE_COLUMN, type.buttonImage);
		values.put(DataProviderContract.ProductTypeTable.IMAGE_INFO_COLUMN, type.imageInfo);
		values.put(DataProviderContract.ProductTypeTable.COLOR_ID_COLUMN, type.colorId);
		values.put(DataProviderContract.ProductTypeTable.IMAGE_INFO_ID_COLUMN, type.imageInfoId);
		values.put(DataProviderContract.ProductTypeTable.DESTINATION_COLUMN, type.destination);
		values.put(DataProviderContract.ProductTypeTable.DESTINATION_NAME_COLUMN, type.destinationName);
		values.put(DataProviderContract.ProductTypeTable.DESTINATION_ICON_COLUMN, type.destinationIcon);
		values.put(DataProviderContract.ProductTypeTable.DESTINATION_IP_COLUMN, type.printerIp);

		return values;
	}

	public static ArrayList<ProductType> getAll(Context context, ArrayList<ProductType> productTypeList){
		if(productTypeList == null){
			productTypeList = new ArrayList<>();
		}

		Cursor c = context.getContentResolver().query(
				DataProviderContract.ProductTypeTable.TABLE_URI, null, null, null,
				DataProviderContract.ProductTypeTable.PRIORITY_COLUMN);

		if(c.moveToFirst()){
			do{
				productTypeList.add(DataBaseCursorBuild.buildProductTypeObject(c));
			}while(c.moveToNext());
		}

		c.close();

		return productTypeList;
	}
}
