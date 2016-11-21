package io.oxigen.quiosgrama.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import io.oxigen.quiosgrama.dao.AmountDao;
import io.oxigen.quiosgrama.dao.BillDao;
import io.oxigen.quiosgrama.dao.ProductRequestDao;
import io.oxigen.quiosgrama.dao.RequestDao;

public class DataProvider extends ContentProvider{
	
	public static final int TABLE_URL_QUERY = 1;
	public static final int PRODUCT_TYPE_URL_QUERY = 2;
	public static final int PRODUCT_URL_QUERY = 3;
	public static final int REQUEST_URL_QUERY = 4;
	public static final int FUNCTIONARY_URL_QUERY = 5;
	public static final int FUNCTION_URL_QUERY = 6;
	public static final int BILL_URL_QUERY = 7;
	public static final int COMPLEMENT_URL_QUERY = 8;
	public static final int PRODUCT_REQUEST_URL_QUERY = 9;
	public static final int COMPLEMENT_TYPE_URL_QUERY = 10;
	public static final int POI_URL_QUERY = 11;
	public static final int CLIENT_URL_QUERY = 12;
	public static final int AMOUNT_URL_QUERY = 13;
	public static final int INVALID_URI = -1;
	
	private DataProviderHelper mHelper;
	private static final UriMatcher sUriMatcher;

	static {
		sUriMatcher = new UriMatcher(0);

		sUriMatcher.addURI(
				DataProviderContract.AUTHORITY,
				DataProviderContract.TableTable.TABLE_NAME,
				TABLE_URL_QUERY);
		
		sUriMatcher.addURI(
				DataProviderContract.AUTHORITY,
				DataProviderContract.ProductTypeTable.TABLE_NAME,
				PRODUCT_TYPE_URL_QUERY);
		
		sUriMatcher.addURI(
				DataProviderContract.AUTHORITY,
				DataProviderContract.ProductTable.TABLE_NAME,
				PRODUCT_URL_QUERY);
		
		sUriMatcher.addURI(
				DataProviderContract.AUTHORITY,
				DataProviderContract.RequestTable.TABLE_NAME,
				REQUEST_URL_QUERY);
		
		sUriMatcher.addURI(
				DataProviderContract.AUTHORITY,
				DataProviderContract.FunctionaryTable.TABLE_NAME,
				FUNCTIONARY_URL_QUERY);
		
		sUriMatcher.addURI(
				DataProviderContract.AUTHORITY,
				DataProviderContract.FunctionTable.TABLE_NAME,
				FUNCTION_URL_QUERY);
		
		sUriMatcher.addURI(
				DataProviderContract.AUTHORITY,
				DataProviderContract.BillTable.TABLE_NAME,
				BILL_URL_QUERY);
		
		sUriMatcher.addURI(
				DataProviderContract.AUTHORITY,
				DataProviderContract.ComplementTable.TABLE_NAME,
				COMPLEMENT_URL_QUERY);
		
		sUriMatcher.addURI(
				DataProviderContract.AUTHORITY,
				DataProviderContract.ProductRequestTable.TABLE_NAME,
				PRODUCT_REQUEST_URL_QUERY);
		
		sUriMatcher.addURI(
				DataProviderContract.AUTHORITY,
				DataProviderContract.ComplementTypeTable.TABLE_NAME,
				COMPLEMENT_TYPE_URL_QUERY);

		sUriMatcher.addURI(
				DataProviderContract.AUTHORITY,
				DataProviderContract.PoiTable.TABLE_NAME,
				POI_URL_QUERY);
		
		sUriMatcher.addURI(
				DataProviderContract.AUTHORITY,
				DataProviderContract.ClientTable.TABLE_NAME,
				CLIENT_URL_QUERY);

		sUriMatcher.addURI(
				DataProviderContract.AUTHORITY,
				DataProviderContract.AmountTable.TABLE_NAME,
				AMOUNT_URL_QUERY);
	}
	
	@Override
	public boolean onCreate() {
		 mHelper = new DataProviderHelper(getContext());

	     return true;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		SQLiteDatabase localSQLiteDatabase = mHelper.getWritableDatabase();
		String tableName = null;
		switch (sUriMatcher.match(uri)) {
			case TABLE_URL_QUERY:
				tableName = DataProviderContract.TableTable.TABLE_NAME;
			break;
			
			case BILL_URL_QUERY:
				tableName = DataProviderContract.BillTable.TABLE_NAME;
			break;

			case AMOUNT_URL_QUERY:
				tableName = DataProviderContract.AmountTable.TABLE_NAME;
			break;
			
			case REQUEST_URL_QUERY:
				tableName = DataProviderContract.RequestTable.TABLE_NAME;
			break;
			
			case PRODUCT_REQUEST_URL_QUERY:
				tableName = DataProviderContract.ProductRequestTable.TABLE_NAME;
			break;
			
			case PRODUCT_TYPE_URL_QUERY:
				tableName = DataProviderContract.ProductTypeTable.TABLE_NAME;
			break;

			case PRODUCT_URL_QUERY:
				tableName = DataProviderContract.ProductTable.TABLE_NAME;
			break;
			
			case FUNCTIONARY_URL_QUERY:
				tableName = DataProviderContract.FunctionaryTable.TABLE_NAME;
			break;
			
			case COMPLEMENT_URL_QUERY:
				tableName = DataProviderContract.ComplementTable.TABLE_NAME;
			break;
				
			case COMPLEMENT_TYPE_URL_QUERY:
				tableName = DataProviderContract.ComplementTypeTable.TABLE_NAME;
			break;
			
			case POI_URL_QUERY:
				tableName = DataProviderContract.PoiTable.TABLE_NAME;
			break;
			
			case CLIENT_URL_QUERY:
				tableName = DataProviderContract.ClientTable.TABLE_NAME;
				break;
		}
		
		int rows = 0;
		if(tableName != null){
			rows = localSQLiteDatabase.delete(
					tableName,
	                selection,
	                selectionArgs);
		}
        
        if (0 != rows) 
            getContext().getContentResolver().notifyChange(uri, null);
            
        return rows;
    }

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		SQLiteDatabase localSQLiteDatabase = mHelper.getWritableDatabase();
		String tableName = null;
        switch (sUriMatcher.match(uri)) {
            case TABLE_URL_QUERY:
            	tableName = DataProviderContract.TableTable.TABLE_NAME;
            break;
            
            case PRODUCT_TYPE_URL_QUERY:
            	tableName = DataProviderContract.ProductTypeTable.TABLE_NAME;
            break;
            
            case PRODUCT_URL_QUERY:
            	tableName = DataProviderContract.ProductTable.TABLE_NAME;
            break;
            
            case REQUEST_URL_QUERY:
            	tableName = DataProviderContract.RequestTable.TABLE_NAME;
            break;
            
            case FUNCTIONARY_URL_QUERY:
            	tableName = DataProviderContract.FunctionaryTable.TABLE_NAME;
            break;
            
            case FUNCTION_URL_QUERY:
            	tableName = DataProviderContract.FunctionTable.TABLE_NAME;
            break;
            
            case BILL_URL_QUERY:
            	tableName = DataProviderContract.BillTable.TABLE_NAME;
            break;
            
            case COMPLEMENT_URL_QUERY:
            	tableName = DataProviderContract.ComplementTable.TABLE_NAME;
            break;
            
            case PRODUCT_REQUEST_URL_QUERY:
            	tableName = DataProviderContract.ProductRequestTable.TABLE_NAME;
            break;
            
            case COMPLEMENT_TYPE_URL_QUERY:
            	tableName = DataProviderContract.ComplementTypeTable.TABLE_NAME;
            break;
            
            case POI_URL_QUERY:
            	tableName = DataProviderContract.PoiTable.TABLE_NAME;
            	break;
            	
            case CLIENT_URL_QUERY:
            	tableName = DataProviderContract.ClientTable.TABLE_NAME;
            	break;
        }
        
        long id = localSQLiteDatabase.insert(
        		tableName,
        		null,
        		values
        		);

        // If the insert succeeded, notify a change and return the new row's content URI.
        if (-1 != id) {
            getContext().getContentResolver().notifyChange(uri, null);
            return Uri.withAppendedPath(uri, Long.toString(id));
        } else {
            throw new SQLiteException("Insert error:" + uri);
        }
    }

	@Override
	public Cursor query(Uri uri,
	        String[] projection,
	        String selection,
	        String[] selectionArgs,
	        String sortOrder) {

        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor returnCursor = null;
        String tableName = null;
        SQLiteQueryBuilder queryBuilder = null;
//        SQLiteQueryBuilder queryBuilder = null;
//        StringBuilder sb = null;
        switch (sUriMatcher.match(uri)) {
        	case TABLE_URL_QUERY:
        		tableName = DataProviderContract.TableTable.TABLE_NAME;
            	
            	returnCursor = db.query(
            			tableName,
            			projection,
            			selection, selectionArgs, null, null, sortOrder);
        	break;
        	
        	case PRODUCT_TYPE_URL_QUERY:
        		tableName = DataProviderContract.ProductTypeTable.TABLE_NAME;
            	
            	returnCursor = db.query(
            			tableName,
            			projection,
            			selection, selectionArgs, null, null, sortOrder);
        	break;
        	
        	case PRODUCT_URL_QUERY:
        		tableName = DataProviderContract.ProductTable.TABLE_NAME;
            	
            	returnCursor = db.query(
            			tableName,
            			projection,
            			selection, selectionArgs, null, null, sortOrder);
        	break;
        	
        	case REQUEST_URL_QUERY:
        		returnCursor = RequestDao.TABLE_JOIN_QUERY.query(
        				db,
            			projection,
            			selection, selectionArgs, null, null, sortOrder);
        	break;
        	
        	case FUNCTIONARY_URL_QUERY:
        		tableName = DataProviderContract.FunctionaryTable.TABLE_NAME;
            	
            	returnCursor = db.query(
            			tableName,
            			projection,
            			selection, selectionArgs, null, null, sortOrder);
        	break;
        	
        	case BILL_URL_QUERY:
        		returnCursor = BillDao.TABLE_JOIN_QUERY.query(
						db,
						projection,
						selection, selectionArgs, null, null, sortOrder);
        	break;

			case AMOUNT_URL_QUERY:
				returnCursor = AmountDao.TABLE_JOIN_QUERY.query(
						db,
						projection,
						selection, selectionArgs, null, null, sortOrder);
			break;
        	
        	case COMPLEMENT_URL_QUERY:
        		tableName = DataProviderContract.ComplementTable.TABLE_NAME;
            	
            	returnCursor = db.query(
            			tableName,
            			projection,
            			selection, selectionArgs, null, null, sortOrder);
        	break;
        	
        	case COMPLEMENT_TYPE_URL_QUERY:
        		tableName = DataProviderContract.ComplementTypeTable.TABLE_NAME;
            	
            	returnCursor = db.query(
            			tableName,
            			projection,
            			selection, selectionArgs, null, null, sortOrder);
        	break;

        	case POI_URL_QUERY:
        		tableName = DataProviderContract.PoiTable.TABLE_NAME;
        		
        		returnCursor = db.query(
        				tableName,
        				projection,
        				selection, selectionArgs, null, null, sortOrder);
        		break;
        		
        	case CLIENT_URL_QUERY:
        		tableName = DataProviderContract.ClientTable.TABLE_NAME;
        		
        		returnCursor = db.query(
        				tableName,
        				projection,
        				selection, selectionArgs, null, null, sortOrder);
        		break;
        	
        	case PRODUCT_REQUEST_URL_QUERY:
        		returnCursor = ProductRequestDao.TABLE_JOIN_QUERY.query(
						db,
						projection,
						selection, selectionArgs, null, null, sortOrder);
        	break;
        
            case INVALID_URI:
                throw new IllegalArgumentException("Query -- Invalid URI:" + uri);
                
        }

        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);

		return returnCursor;
	}

	@Override
	public int update(Uri uri, ContentValues contentValues, String selection,
            String[] selectionArgs) {
		
		String tableName = null;
        // Decodes the content URI and choose which insert to use
        switch (sUriMatcher.match(uri)) {
        	case TABLE_URL_QUERY:
        		tableName = DataProviderContract.TableTable.TABLE_NAME;
        	break;
        	
        	case BILL_URL_QUERY:
        		tableName = DataProviderContract.BillTable.TABLE_NAME;
        	break;

			case AMOUNT_URL_QUERY:
				tableName = DataProviderContract.AmountTable.TABLE_NAME;
			break;
        	
        	case COMPLEMENT_URL_QUERY:
        		tableName = DataProviderContract.ComplementTable.TABLE_NAME;
        	break;
        	
        	case PRODUCT_TYPE_URL_QUERY:
        		tableName = DataProviderContract.ProductTypeTable.TABLE_NAME;
        	break;
        	
        	case PRODUCT_URL_QUERY:
        		tableName = DataProviderContract.ProductTable.TABLE_NAME;
        	break;
        	
        	case FUNCTIONARY_URL_QUERY:
        		tableName = DataProviderContract.FunctionaryTable.TABLE_NAME;
        	break;
        	
        	case REQUEST_URL_QUERY:
        		tableName = DataProviderContract.RequestTable.TABLE_NAME;
        	break;
        	
        	case PRODUCT_REQUEST_URL_QUERY:
        		tableName = DataProviderContract.ProductRequestTable.TABLE_NAME;
        	break;
        	
        	case COMPLEMENT_TYPE_URL_QUERY:
        		tableName = DataProviderContract.ComplementTypeTable.TABLE_NAME;
        	break;
        	
        	case POI_URL_QUERY:
        		tableName = DataProviderContract.PoiTable.TABLE_NAME;
        		break;
        		
        	case CLIENT_URL_QUERY:
        		tableName = DataProviderContract.ClientTable.TABLE_NAME;
        		break;
        }
        
        // Creats a new writeable database or retrieves a cached one
        SQLiteDatabase localSQLiteDatabase = mHelper.getWritableDatabase();

        if(tableName == null){
        	 throw new SQLiteException("Update error:" + uri);
        }
        
        // Updates the table
        int rows = localSQLiteDatabase.update(
        		tableName,
                contentValues,
                selection,
                selectionArgs);

        // If the update succeeded, notify a change and return the number of updated rows.
        if (0 != rows) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        
        return rows;
    }
	
}
