package io.oxigen.quiosgrama.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataProviderHelper extends SQLiteOpenHelper {
	
	// Constants for building SQLite tables during initialization
    private static final String TEXT_TYPE = "TEXT";
    private static String PRIMARY_KEY_TYPE = "PRIMARY KEY";
    private static final String INTEGER_PRIMARY_KEY_TYPE = "INTEGER PRIMARY KEY";
    private static final String TEXT_PRIMARY_KEY_TYPE = "TEXT PRIMARY KEY";
    private static final String INTEGER_TYPE = "INTEGER";
    private static final String REAL_TYPE = "REAL";

    private static final String CREATE_AMOUNT_TABLE = "CREATE TABLE" + " " +
            DataProviderContract.AmountTable.TABLE_NAME + " " +
            "(" + " " +
            DataProviderContract.AmountTable.ID_COLUMN + " " + TEXT_PRIMARY_KEY_TYPE + " NOT NULL," +
            DataProviderContract.AmountTable.VALUE_COLUMN + " " + REAL_TYPE  + " NOT NULL," +
            DataProviderContract.AmountTable.PAID_METHOD_COLUMN + " " + INTEGER_TYPE  + " NOT NULL," +
            DataProviderContract.AmountTable.SYNC_STATUS_COLUMN + " " + INTEGER_TYPE  + " NOT NULL," +
            DataProviderContract.AmountTable.BILL_ID_COLUMN + " " + TEXT_TYPE  + " NOT NULL," +
            "FOREIGN KEY("+
            DataProviderContract.AmountTable.BILL_ID_COLUMN  + ") REFERENCES " +
            DataProviderContract.BillTable.TABLE_NAME + "(" +
            DataProviderContract.BillTable.ID_COLUMN  + ")"+
            ")";
    
    private static final String CREATE_BILL_TABLE = "CREATE TABLE" + " " +
            DataProviderContract.BillTable.TABLE_NAME + " " +
            "(" + " " +
            DataProviderContract.BillTable.ID_COLUMN + " " + TEXT_PRIMARY_KEY_TYPE + " NOT NULL," +
            DataProviderContract.BillTable.OPEN_TIME_COLUMN + " " + TEXT_TYPE  + " ,"  +
            DataProviderContract.BillTable.CLOSE_TIME_COLUMN + " " + TEXT_TYPE + " ," +
            DataProviderContract.BillTable.PAID_TIME_COLUMN + " " + TEXT_TYPE + " ," +
            DataProviderContract.BillTable.WAITER_OPEN_TABLE_ID_COLUMN + " " + INTEGER_TYPE  + " ," +
            DataProviderContract.BillTable.WAITER_CLOSE_TABLE_ID_COLUMN + " " + INTEGER_TYPE  + " ," +
            DataProviderContract.BillTable.BILL_TIME + " " + TEXT_TYPE  + " NOT NULL,"  +
            DataProviderContract.BillTable.SERVICE_PAID_COLUMN + " " + INTEGER_TYPE + " NOT NULL," +
            DataProviderContract.BillTable.SYNC_STATUS_COLUMN + " " + INTEGER_TYPE  + " NOT NULL,"  +
            DataProviderContract.BillTable.TABLE_ID_COLUMN + " " + INTEGER_TYPE  + " NOT NULL," +
            "FOREIGN KEY("+ 
            DataProviderContract.BillTable.WAITER_OPEN_TABLE_ID_COLUMN + ") REFERENCES " + 
            DataProviderContract.FunctionaryTable.TABLE_NAME + "(" + 
            DataProviderContract.FunctionaryTable.ID_COLUMN + ")"+ " ," +
            "FOREIGN KEY("+ 
            DataProviderContract.BillTable.WAITER_CLOSE_TABLE_ID_COLUMN + ") REFERENCES " + 
            DataProviderContract.FunctionaryTable.TABLE_NAME + "(" + 
            DataProviderContract.FunctionaryTable.ID_COLUMN + ")"+ " ," +
            "FOREIGN KEY("+ 
            DataProviderContract.BillTable.TABLE_ID_COLUMN  + ") REFERENCES " + 
            DataProviderContract.TableTable.TABLE_NAME + "(" + 
            DataProviderContract.TableTable.ID_COLUMN  + ")"+
            ")";
    
    private static final String CREATE_COMPLEMENT_TABLE = "CREATE TABLE" + " " +
            DataProviderContract.ComplementTable.TABLE_NAME + " " +
            "(" + " " +
            DataProviderContract.ComplementTable.ID_COLUMN + " " + TEXT_PRIMARY_KEY_TYPE + " NOT NULL," +
            DataProviderContract.ComplementTable.PRICE_COLUMN + " " + REAL_TYPE + " NOT NULL," +
            DataProviderContract.ComplementTable.DRAWABLE_ID_COLUMN + " " + INTEGER_TYPE +
            ")";
    
    private static final String CREATE_FUNCTION_TABLE = "CREATE TABLE" + " " +
            DataProviderContract.FunctionTable.TABLE_NAME + " " +
            "(" + " " +
            DataProviderContract.FunctionTable.ID_COLUMN + " " + INTEGER_PRIMARY_KEY_TYPE + " NOT NULL," +
            DataProviderContract.FunctionTable.NAME_COLUMN + " " + TEXT_TYPE + " NOT NULL" +
            ")";
    
    private static final String CREATE_FUNCTIONARY_TABLE = "CREATE TABLE" + " " +
            DataProviderContract.FunctionaryTable.TABLE_NAME + " " +
            "(" + " " +
            DataProviderContract.FunctionaryTable.ID_COLUMN + " " + INTEGER_PRIMARY_KEY_TYPE + " NOT NULL," +
            DataProviderContract.FunctionaryTable.NAME_COLUMN + " " + TEXT_TYPE + " NOT NULL," +
            DataProviderContract.FunctionaryTable.IMEI_COLUMN + " " + TEXT_TYPE + ", " +
            DataProviderContract.FunctionaryTable.ADMIN_FLAG_COLUMN + " " + INTEGER_TYPE + " NOT NULL," +
            DataProviderContract.FunctionaryTable.FUNCTION_ID_COLUMN + " " + INTEGER_TYPE + ", " +
            "FOREIGN KEY("+ 
            DataProviderContract.FunctionaryTable.FUNCTION_ID_COLUMN  + ") REFERENCES " + 
            DataProviderContract.FunctionTable.TABLE_NAME + "(" + 
            DataProviderContract.FunctionTable.ID_COLUMN  + ")"+
            ")";
    
    private static final String CREATE_PRODUCT_TABLE = "CREATE TABLE" + " " +
            DataProviderContract.ProductTable.TABLE_NAME + " " +
            "(" + " " +
            DataProviderContract.ProductTable.ID_COLUMN + " " + INTEGER_PRIMARY_KEY_TYPE + " NOT NULL," +
            DataProviderContract.ProductTable.NAME_COLUMN + " " + TEXT_TYPE + " NOT NULL," +
            DataProviderContract.ProductTable.PRICE_COLUMN + " " + REAL_TYPE + " NOT NULL," +
            DataProviderContract.ProductTable.DESCRIPTION_COLUMN + " " + TEXT_TYPE + ", " +
            DataProviderContract.ProductTable.POPULARITY_COLUMN + " " + INTEGER_TYPE + " NOT NULL," +
            DataProviderContract.ProductTable.PRODUCT_TYPE_ID_COLUMN + " " + TEXT_TYPE + " NOT NULL," +
            DataProviderContract.ProductTable.TAX_COLUMN + " " + TEXT_TYPE + "," +
            "FOREIGN KEY("+
            DataProviderContract.ProductTable.PRODUCT_TYPE_ID_COLUMN  + ") REFERENCES " + 
            DataProviderContract.ProductTypeTable.TABLE_NAME + "(" + 
            DataProviderContract.ProductTypeTable.ID_COLUMN  + ")"+
            ")";
    
    private static final String CREATE_PRODUCT_REQUEST_TABLE = "CREATE TABLE" + " " +
            DataProviderContract.ProductRequestTable.TABLE_NAME + " " +
            "(" + " " +
            DataProviderContract.ProductRequestTable.REQUEST_ID_COLUMN + " " + TEXT_TYPE + " NOT NULL," +
            DataProviderContract.ProductRequestTable.PRODUCT_ID_COLUMN + " " + INTEGER_TYPE + " NOT NULL," +
            DataProviderContract.ProductRequestTable.COMPLEMENT_ID_COLUMN + " " + TEXT_TYPE + "," +
            DataProviderContract.ProductRequestTable.QUANTITY_COLUMN + " " + INTEGER_TYPE + " NOT NULL," +
            DataProviderContract.ProductRequestTable.VALID_COLUMN + " " + INTEGER_TYPE + " NOT NULL," +
            DataProviderContract.ProductRequestTable.TRANSFER_ROUTE_COLUMN + " " + TEXT_TYPE + ", " +
            DataProviderContract.ProductRequestTable.PRODUCT_REQUEST_TIME_COLUMN + " " + TEXT_TYPE + " NOT NULL," +
            DataProviderContract.ProductRequestTable.STATUS_COLUMN + " " + INTEGER_TYPE + " NOT NULL," +
            DataProviderContract.ProductRequestTable.SYNC_STATUS_COLUMN + " " + INTEGER_TYPE + " NOT NULL," +
            "FOREIGN KEY("+
            DataProviderContract.ProductRequestTable.REQUEST_ID_COLUMN  + ") REFERENCES " + 
            DataProviderContract.RequestTable.TABLE_NAME + "(" + 
            DataProviderContract.RequestTable.ID_COLUMN  + "),"+
            "FOREIGN KEY("+ 
            DataProviderContract.ProductRequestTable.PRODUCT_ID_COLUMN  + ") REFERENCES " + 
            DataProviderContract.ProductTable.TABLE_NAME + "(" + 
            DataProviderContract.ProductTable.ID_COLUMN  + "),"+
            "FOREIGN KEY("+ 
            DataProviderContract.ProductRequestTable.COMPLEMENT_ID_COLUMN  + ") REFERENCES " + 
            DataProviderContract.ComplementTable.TABLE_NAME + "(" + 
            DataProviderContract.ComplementTable.ID_COLUMN  + "),"+
            PRIMARY_KEY_TYPE + "(" + 
			DataProviderContract.ProductRequestTable.REQUEST_ID_COLUMN + ", " + 
			DataProviderContract.ProductRequestTable.PRODUCT_ID_COLUMN + ", " + 
			DataProviderContract.ProductRequestTable.COMPLEMENT_ID_COLUMN + ")" +
            ")";
    
    private static final String CREATE_PRODUCT_TYPE_TABLE = "CREATE TABLE" + " " +
            DataProviderContract.ProductTypeTable.TABLE_NAME + " " +
            "(" + " " +
            DataProviderContract.ProductTypeTable.ID_COLUMN + " " + TEXT_PRIMARY_KEY_TYPE + " NOT NULL," +
            DataProviderContract.ProductTypeTable.NAME_COLUMN + " " + TEXT_TYPE + " NOT NULL," +
            DataProviderContract.ProductTypeTable.PRIORITY_COLUMN + " " + INTEGER_TYPE + " NOT NULL," +
            DataProviderContract.ProductTypeTable.BUTTON_IMAGE_COLUMN + " " + TEXT_TYPE + " NOT NULL," +
            DataProviderContract.ProductTypeTable.IMAGE_INFO_COLUMN + " " + TEXT_TYPE + " NOT NULL," +
            DataProviderContract.ProductTypeTable.COLOR_ID_COLUMN + " " + TEXT_TYPE + " NOT NULL," +
            DataProviderContract.ProductTypeTable.IMAGE_INFO_ID_COLUMN + " " + INTEGER_TYPE + " NOT NULL," +
            DataProviderContract.ProductTypeTable.DESTINATION_COLUMN + " " + INTEGER_TYPE + " NOT NULL," +
            DataProviderContract.ProductTypeTable.DESTINATION_NAME_COLUMN + " " + TEXT_TYPE + " NOT NULL," +
            DataProviderContract.ProductTypeTable.DESTINATION_ICON_COLUMN + " " + TEXT_TYPE + " NOT NULL," +
            DataProviderContract.ProductTypeTable.DESTINATION_IP_COLUMN + " " + TEXT_TYPE +
            ")";
    
    private static final String CREATE_REQUEST_TABLE = "CREATE TABLE" + " " +
            DataProviderContract.RequestTable.TABLE_NAME + " " +
            "(" + " " +
            DataProviderContract.RequestTable.ID_COLUMN + " " + TEXT_PRIMARY_KEY_TYPE + " NOT NULL," +
            DataProviderContract.RequestTable.REQUEST_TIME_COLUMN + " " + TEXT_TYPE + " NOT NULL," +
            DataProviderContract.RequestTable.BILL_ID_COLUMN + " " + TEXT_TYPE + " NOT NULL," +
            DataProviderContract.RequestTable.FUNCTIONARY_ID_COLUMN + " " + INTEGER_TYPE + " NOT NULL, " +
            DataProviderContract.RequestTable.SYNC_STATUS_COLUMN + " " + INTEGER_TYPE + " NOT NULL," +
            "FOREIGN KEY("+ 
            DataProviderContract.RequestTable.BILL_ID_COLUMN  + ") REFERENCES " + 
            DataProviderContract.BillTable.TABLE_NAME + "(" + 
            DataProviderContract.BillTable.ID_COLUMN  + "),"+
            "FOREIGN KEY("+ 
            DataProviderContract.RequestTable.FUNCTIONARY_ID_COLUMN  + ") REFERENCES " + 
            DataProviderContract.FunctionaryTable.TABLE_NAME + "(" + 
            DataProviderContract.FunctionaryTable.ID_COLUMN  + ")"+
            ")";
    
    private static final String CREATE_TABLE_TABLE = "CREATE TABLE" + " " +
            DataProviderContract.TableTable.TABLE_NAME + " " +
            "(" + " " +
            DataProviderContract.TableTable.ID_COLUMN + " " + INTEGER_PRIMARY_KEY_TYPE + " NOT NULL," +
            DataProviderContract.TableTable.X_POS_DPI_COLUMN + " " + INTEGER_TYPE + " NOT NULL," +
            DataProviderContract.TableTable.Y_POS_DPI_COLUMN + " " + INTEGER_TYPE + " NOT NULL," +
            DataProviderContract.TableTable.MAP_PAGE_NUMBER + " " + INTEGER_TYPE + " NOT NULL," +
            DataProviderContract.TableTable.TABLE_TIME + " " + TEXT_TYPE + " NOT NULL," +
            DataProviderContract.TableTable.FUNCTIONARY_ID_COLUMN + " " + INTEGER_TYPE + ", " +
            DataProviderContract.TableTable.CLIENT_ID_COLUMN + " " + INTEGER_TYPE + ", " +
            DataProviderContract.TableTable.CLIENT_TEMP_COLUMN + " " + TEXT_TYPE + ", " +
            DataProviderContract.TableTable.SYNC_STATUS_COLUMN + " " + INTEGER_TYPE + " NOT NULL," +
            DataProviderContract.TableTable.SHOW_COLUMN + " " + INTEGER_TYPE + " NOT NULL," +
            "FOREIGN KEY("+
            DataProviderContract.TableTable.CLIENT_ID_COLUMN  + ") REFERENCES " + 
            DataProviderContract.ClientTable.TABLE_NAME + "(" + 
            DataProviderContract.ClientTable.ID_COLUMN  + "),"+
            "FOREIGN KEY("+ 
            DataProviderContract.TableTable.FUNCTIONARY_ID_COLUMN  + ") REFERENCES " + 
            DataProviderContract.FunctionaryTable.TABLE_NAME + "(" + 
            DataProviderContract.FunctionaryTable.ID_COLUMN  + ")"+
            ")";
    
    private static final String CREATE_COMPLEMENT_TYPE_TABLE = "CREATE TABLE" + " " +
    		DataProviderContract.ComplementTypeTable.TABLE_NAME + " " +
    		"(" + " " +
    		DataProviderContract.ComplementTypeTable.ID_PRODUCT_TYPE_COLUMN + " " + INTEGER_TYPE + " NOT NULL," +
    		DataProviderContract.ComplementTypeTable.COMPLEMENT_COLUMN + " " + TEXT_TYPE + " NOT NULL," +
    		"FOREIGN KEY("+ 
            DataProviderContract.ComplementTypeTable.ID_PRODUCT_TYPE_COLUMN  + ") REFERENCES " + 
            DataProviderContract.ProductTypeTable.TABLE_NAME + "(" + 
            DataProviderContract.ProductTypeTable.ID_COLUMN  + "),"+
            "FOREIGN KEY("+ 
            DataProviderContract.ComplementTypeTable.COMPLEMENT_COLUMN  + ") REFERENCES " + 
            DataProviderContract.ComplementTable.TABLE_NAME + "(" + 
            DataProviderContract.ComplementTable.ID_COLUMN  + "),"+
            PRIMARY_KEY_TYPE + "(" + 
			DataProviderContract.ComplementTypeTable.ID_PRODUCT_TYPE_COLUMN + ", " + 
			DataProviderContract.ComplementTypeTable.COMPLEMENT_COLUMN + ")" +
            ")";
    
    private static final String CREATE_POI_TABLE = "CREATE TABLE" + " " +
            DataProviderContract.PoiTable.TABLE_NAME + " " +
            "(" + " " +
            DataProviderContract.PoiTable.ID_COLUMN + " " + INTEGER_PRIMARY_KEY_TYPE + " NOT NULL," +
            DataProviderContract.PoiTable.X_POS_DPI_COLUMN + " " + INTEGER_TYPE + " NOT NULL," +
            DataProviderContract.PoiTable.Y_POS_DPI_COLUMN + " " + INTEGER_TYPE + " NOT NULL," +
            DataProviderContract.PoiTable.NAME_COLUMN + " " + INTEGER_TYPE + " NOT NULL," +
            DataProviderContract.PoiTable.IMAGE_COLUMN + " " + TEXT_TYPE + " NOT NULL," +
            DataProviderContract.PoiTable.MAP_PAGE_NUMBER + " " + INTEGER_TYPE + " NOT NULL," +
            DataProviderContract.PoiTable.POI_TIME + " " + TEXT_TYPE + " NOT NULL," +
            DataProviderContract.PoiTable.FUNCTIONARY_ID_COLUMN + " " + INTEGER_TYPE + ", " +
            DataProviderContract.PoiTable.SYNC_STATUS_COLUMN + " " + INTEGER_TYPE + " NOT NULL," +
            "FOREIGN KEY("+ 
            DataProviderContract.PoiTable.FUNCTIONARY_ID_COLUMN  + ") REFERENCES " + 
            DataProviderContract.FunctionaryTable.TABLE_NAME + "(" + 
            DataProviderContract.FunctionaryTable.ID_COLUMN  + ")"+
            ")";
    
    private static final String CREATE_CLIENT_TABLE = "CREATE TABLE" + " " +
            DataProviderContract.ClientTable.TABLE_NAME + " " +
            "(" + " " +
            DataProviderContract.ClientTable.ID_COLUMN + " " + INTEGER_PRIMARY_KEY_TYPE + " NOT NULL," +
            DataProviderContract.ClientTable.NAME_COLUMN + " " + TEXT_TYPE + " NOT NULL," +
            DataProviderContract.ClientTable.CPF_COLUMN + " " + TEXT_TYPE + " ," +
            DataProviderContract.ClientTable.PHONE_COLUMN + " " + TEXT_TYPE + " ," +
            DataProviderContract.ClientTable.TEMP_FLAG_COLUMN + " " + INTEGER_TYPE + " NOT NULL," +
            DataProviderContract.ClientTable.PRESENT_FLAG_COLUMN + " " + INTEGER_TYPE + " NOT NULL" +
            ")";
	
    public DataProviderHelper(Context context) {
//    	super(context, "/storage/sdcard1/Quiosgrama/quiosque.db",
//    			null,
//    			DataProviderContract.DATABASE_VERSION);
    	super(context, DataProviderContract.DATABASE_NAME,
    			null,
    			DataProviderContract.DATABASE_VERSION);
    }

    /**
     * Executes the queries to drop all of the tables from the database.
     *
     * @param db A handle to the provider's backing database.
     */
    public void dropTables(SQLiteDatabase db) {

        // If the table doesn't exist, don't throw an error
    	db.execSQL("DROP TABLE IF EXISTS " + DataProviderContract.BillTable.TABLE_NAME);
    	db.execSQL("DROP TABLE IF EXISTS " + DataProviderContract.ComplementTable.TABLE_NAME);
    	db.execSQL("DROP TABLE IF EXISTS " + DataProviderContract.FunctionTable.TABLE_NAME);
    	db.execSQL("DROP TABLE IF EXISTS " + DataProviderContract.FunctionaryTable.TABLE_NAME);
    	db.execSQL("DROP TABLE IF EXISTS " + DataProviderContract.ProductTable.TABLE_NAME);
    	db.execSQL("DROP TABLE IF EXISTS " + DataProviderContract.ProductRequestTable.TABLE_NAME);
    	db.execSQL("DROP TABLE IF EXISTS " + DataProviderContract.ProductTypeTable.TABLE_NAME);
    	db.execSQL("DROP TABLE IF EXISTS " + DataProviderContract.RequestTable.TABLE_NAME);
    	db.execSQL("DROP TABLE IF EXISTS " + DataProviderContract.TableTable.TABLE_NAME);
    	db.execSQL("DROP TABLE IF EXISTS " + DataProviderContract.ComplementTypeTable.TABLE_NAME);
    	db.execSQL("DROP TABLE IF EXISTS " + DataProviderContract.PoiTable.TABLE_NAME);
    	db.execSQL("DROP TABLE IF EXISTS " + DataProviderContract.ClientTable.TABLE_NAME);
    	db.execSQL("DROP TABLE IF EXISTS " + DataProviderContract.AmountTable.TABLE_NAME);
    }

    /**
     * Does setup of the database. The system automatically invokes this method when
     * SQLiteDatabase.getWriteableDatabase() or SQLiteDatabase.getReadableDatabase() are
     * invoked and no db instance is available.
     *
     * @param db the database instance in which to create the tables.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creates the tables in the backing database for this provider
    	db.execSQL(CREATE_BILL_TABLE);
    	db.execSQL(CREATE_COMPLEMENT_TABLE);
    	db.execSQL(CREATE_FUNCTION_TABLE);
    	db.execSQL(CREATE_FUNCTIONARY_TABLE);
    	db.execSQL(CREATE_PRODUCT_TABLE);
    	db.execSQL(CREATE_PRODUCT_REQUEST_TABLE);
    	db.execSQL(CREATE_PRODUCT_TYPE_TABLE);
    	db.execSQL(CREATE_REQUEST_TABLE);
    	db.execSQL(CREATE_TABLE_TABLE);
    	db.execSQL(CREATE_COMPLEMENT_TYPE_TABLE);
    	db.execSQL(CREATE_POI_TABLE);
    	db.execSQL(CREATE_CLIENT_TABLE);
    	db.execSQL(CREATE_AMOUNT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int version1, int version2) {
        Log.w(DataProviderHelper.class.getName(),
                "Upgrading database from version " + version1 + " to "
                        + version2 + ", which will destroy all the existing data");

        // Drops all the existing tables in the database
        dropTables(db);

        // Invokes the onCreate callback to build new tables
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int version1, int version2) {
        Log.w(DataProviderHelper.class.getName(),
            "Downgrading database from version " + version1 + " to "
                    + version2 + ", which will destroy all the existing data");

        // Drops all the existing tables in the database
        dropTables(db);

        // Invokes the onCreate callback to build new tables
        onCreate(db);
    }
}
