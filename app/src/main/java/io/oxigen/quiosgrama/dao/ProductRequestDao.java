package io.oxigen.quiosgrama.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import io.oxigen.quiosgrama.Bill;
import io.oxigen.quiosgrama.Complement;
import io.oxigen.quiosgrama.Product;
import io.oxigen.quiosgrama.ProductRequest;
import io.oxigen.quiosgrama.ProductType;
import io.oxigen.quiosgrama.Request;
import io.oxigen.quiosgrama.data.DataBaseCursorBuild;
import io.oxigen.quiosgrama.data.DataProviderContract;
import io.oxigen.quiosgrama.data.DataProviderHelper;
import io.oxigen.quiosgrama.data.KeysContract;

public class ProductRequestDao {

	private static final String TAG = "ProductRequestDao";
	private static final String TABLE_NAME = DataProviderContract.ProductRequestTable.TABLE_NAME;

	public static final SQLiteQueryBuilder TABLE_JOIN_QUERY;

	static{
		TABLE_JOIN_QUERY = new SQLiteQueryBuilder();

		StringBuilder sb = new StringBuilder();
		sb.append(TABLE_NAME + " AS " + DataProviderContract.ProductRequestTable.NICKNAME);

		sb.append(" JOIN ");
		sb.append(DataProviderContract.ProductTable.TABLE_NAME + " AS " + DataProviderContract.ProductTable.NICKNAME);
		sb.append(" ON ");
		sb.append(DataProviderContract.ProductRequestTable.NICKNAME + "." + DataProviderContract.ProductRequestTable.PRODUCT_ID_COLUMN);
		sb.append(" = ");
		sb.append(DataProviderContract.ProductTable.NICKNAME + "." + DataProviderContract.ProductTable.ID_COLUMN);

		sb.append(" JOIN ");
		sb.append(DataProviderContract.RequestTable.TABLE_NAME + " AS " + DataProviderContract.RequestTable.NICKNAME);
		sb.append(" ON ");
		sb.append(DataProviderContract.ProductRequestTable.NICKNAME + "." + DataProviderContract.ProductRequestTable.REQUEST_ID_COLUMN);
		sb.append(" = ");
		sb.append(DataProviderContract.RequestTable.NICKNAME + "." + DataProviderContract.RequestTable.ID_COLUMN);

		sb.append(" LEFT OUTER JOIN ");
		sb.append(DataProviderContract.ComplementTable.TABLE_NAME + " AS " + DataProviderContract.ComplementTable.NICKNAME);
		sb.append(" ON ");
		sb.append(DataProviderContract.ProductRequestTable.NICKNAME + "." + DataProviderContract.ProductRequestTable.COMPLEMENT_ID_COLUMN);
		sb.append(" = ");
		sb.append(DataProviderContract.ComplementTable.NICKNAME + "." + DataProviderContract.ComplementTable.ID_COLUMN);

		sb.append(" JOIN ");
		sb.append(DataProviderContract.BillTable.TABLE_NAME + " AS " + DataProviderContract.BillTable.NICKNAME);
		sb.append(" ON ");
		sb.append(DataProviderContract.RequestTable.NICKNAME + "." + DataProviderContract.RequestTable.BILL_ID_COLUMN);
		sb.append(" = ");
		sb.append(DataProviderContract.BillTable.NICKNAME + "." + DataProviderContract.BillTable.ID_COLUMN);

		sb.append(" JOIN ");
		sb.append(DataProviderContract.TableTable.TABLE_NAME + " AS " + DataProviderContract.TableTable.NICKNAME);
		sb.append(" ON ");
		sb.append(DataProviderContract.BillTable.NICKNAME + "." + DataProviderContract.BillTable.TABLE_ID_COLUMN);
		sb.append(" = ");
		sb.append(DataProviderContract.TableTable.NICKNAME + "." + DataProviderContract.TableTable.ID_COLUMN);
		String query = sb.toString();

		TABLE_JOIN_QUERY.setTables(query);
	}

	public static final String[] PRODUCT_REQUEST_PROJECTION = {
		DataProviderContract.ProductRequestTable.NICKNAME + "." +
				DataProviderContract.ProductRequestTable.PRODUCT_ID_COLUMN,
		DataProviderContract.ProductRequestTable.NICKNAME + "." +
				DataProviderContract.ProductRequestTable.REQUEST_ID_COLUMN,
		DataProviderContract.ProductRequestTable.NICKNAME + "." +
				DataProviderContract.ProductRequestTable.COMPLEMENT_ID_COLUMN,
		DataProviderContract.ProductRequestTable.QUANTITY_COLUMN,
		DataProviderContract.ProductRequestTable.VALID_COLUMN,
		DataProviderContract.ProductRequestTable.TRANSFER_ROUTE_COLUMN,
		DataProviderContract.ProductRequestTable.PRODUCT_REQUEST_TIME_COLUMN,
		DataProviderContract.ProductRequestTable.STATUS_COLUMN,
		DataProviderContract.ProductRequestTable.SYNC_STATUS_COLUMN,
		DataProviderContract.ComplementTable.PRICE_COLUMN,
		DataProviderContract.ProductTable.NAME_COLUMN,
		DataProviderContract.ProductTable.NICKNAME + "." +
				DataProviderContract.ProductTable.PRICE_COLUMN,
		DataProviderContract.RequestTable.REQUEST_TIME_COLUMN,
		DataProviderContract.RequestTable.NICKNAME + "." +
				DataProviderContract.RequestTable.FUNCTIONARY_ID_COLUMN,
		DataProviderContract.RequestTable.SYNC_STATUS_COLUMN,
		DataProviderContract.BillTable.NICKNAME + "." +
				DataProviderContract.BillTable.ID_COLUMN,
		DataProviderContract.BillTable.OPEN_TIME_COLUMN,
		DataProviderContract.BillTable.CLOSE_TIME_COLUMN,
		DataProviderContract.BillTable.PAID_TIME_COLUMN,
		DataProviderContract.BillTable.WAITER_OPEN_TABLE_ID_COLUMN,
		DataProviderContract.BillTable.WAITER_CLOSE_TABLE_ID_COLUMN,
		DataProviderContract.BillTable.BILL_TIME,
		DataProviderContract.BillTable.SYNC_STATUS_COLUMN,
		DataProviderContract.TableTable.NICKNAME + "." +
				DataProviderContract.TableTable.ID_COLUMN,
		DataProviderContract.TableTable.X_POS_DPI_COLUMN,
		DataProviderContract.TableTable.Y_POS_DPI_COLUMN,
		DataProviderContract.TableTable.MAP_PAGE_NUMBER,
		DataProviderContract.TableTable.TABLE_TIME,
		DataProviderContract.TableTable.NICKNAME + "." +
				DataProviderContract.TableTable.FUNCTIONARY_ID_COLUMN,
		DataProviderContract.TableTable.SYNC_STATUS_COLUMN,
		DataProviderContract.TableTable.CLIENT_TEMP_COLUMN,
		DataProviderContract.TableTable.SHOW_COLUMN
	};

	public static final String[] TABLE_INFO_REQUEST_PROJECTION = {
			DataProviderContract.ProductRequestTable.NICKNAME + "." +
					DataProviderContract.ProductRequestTable.PRODUCT_ID_COLUMN,
			DataProviderContract.ProductRequestTable.NICKNAME + "." +
					DataProviderContract.ProductRequestTable.REQUEST_ID_COLUMN,
			DataProviderContract.ProductRequestTable.NICKNAME + "." +
					DataProviderContract.ProductRequestTable.COMPLEMENT_ID_COLUMN,
			DataProviderContract.ProductRequestTable.QUANTITY_COLUMN,
			DataProviderContract.ProductRequestTable.VALID_COLUMN,
			DataProviderContract.ProductRequestTable.TRANSFER_ROUTE_COLUMN,
			DataProviderContract.ProductRequestTable.PRODUCT_REQUEST_TIME_COLUMN,
			DataProviderContract.ProductRequestTable.STATUS_COLUMN,
			DataProviderContract.ProductRequestTable.SYNC_STATUS_COLUMN,
			DataProviderContract.ComplementTable.PRICE_COLUMN,
			DataProviderContract.ProductTable.NAME_COLUMN,
			DataProviderContract.ProductTable.NICKNAME + "." +
					DataProviderContract.ProductTable.PRICE_COLUMN,
			DataProviderContract.ProductTable.POPULARITY_COLUMN,
			DataProviderContract.ProductTable.TAX_COLUMN,
			DataProviderContract.ProductTable.PRODUCT_TYPE_ID_COLUMN,
			DataProviderContract.RequestTable.REQUEST_TIME_COLUMN,
			DataProviderContract.RequestTable.SYNC_STATUS_COLUMN,
			DataProviderContract.RequestTable.NICKNAME + "." +
					DataProviderContract.RequestTable.FUNCTIONARY_ID_COLUMN,
			DataProviderContract.BillTable.NICKNAME + "." +
					DataProviderContract.BillTable.ID_COLUMN,
			DataProviderContract.BillTable.TABLE_ID_COLUMN,
			DataProviderContract.BillTable.PAID_TIME_COLUMN
	};

	public static final String[] PRODUCT_REQUEST_NOTIFICATION_PROJECTION = {
			DataProviderContract.ProductRequestTable.STATUS_COLUMN
	};

	public static final String PRODUCT_REQUEST_SYNC_SELECTION =
			DataProviderContract.RequestTable.SYNC_STATUS_COLUMN +
			" = ? or " +
			DataProviderContract.ProductRequestTable.SYNC_STATUS_COLUMN + " = " + KeysContract.NO_SYNCHRONIZED_STATUS_KEY;

	public static final String PROD_REQUEST_SELECTION =
			DataProviderContract.ProductRequestTable.NICKNAME + "." +
			DataProviderContract.ProductRequestTable.REQUEST_ID_COLUMN +
			" = ?";

	public static final String BILL_SELECTION =
			DataProviderContract.BillTable.NICKNAME + "." +
					DataProviderContract.BillTable.ID_COLUMN +
					" = ?";

	public static final String WAITER_SELECTION =
			DataProviderContract.RequestTable.NICKNAME + "." +
					DataProviderContract.RequestTable.FUNCTIONARY_ID_COLUMN +
					" = ?";

	public static final String SENT_SELECTION =
			DataProviderContract.ProductRequestTable.STATUS_COLUMN +
			" = " + ProductRequest.NOT_VISUALIZED_STATUS;

	public static final String VISUALIZED_SELECTION =
			DataProviderContract.ProductRequestTable.STATUS_COLUMN +
					" = " + ProductRequest.VISUALIZED_STATUS;

	public static final String READY_SELECTION =
			DataProviderContract.ProductRequestTable.STATUS_COLUMN +
					" = " + ProductRequest.READY_STATUS;

	public static HashSet<ProductRequest> getBySyncStatus(Context context) {
		Cursor c = context.getContentResolver().query(
				DataProviderContract.ProductRequestTable.TABLE_URI,
				PRODUCT_REQUEST_PROJECTION,
				PRODUCT_REQUEST_SYNC_SELECTION,
				new String[]{"1"},
				null
				);

		HashSet<ProductRequest> prodReqList = new HashSet<ProductRequest>();
		if(c.moveToFirst()){
			do{
				Product product = DataBaseCursorBuild.buildProduct(c);

				Request request = DataBaseCursorBuild.buildRequest(c);

				Complement complement = DataBaseCursorBuild.buildComplement(c);

				Double complementPrice = c.getDouble(
						c.getColumnIndex(
								DataProviderContract.ComplementTable.PRICE_COLUMN));
				complement.price = complementPrice;

				boolean valid = c.getInt(
						c.getColumnIndex(
								DataProviderContract.ProductRequestTable.VALID_COLUMN)) == 1;

				String transferRoute = c.getString(
						c.getColumnIndex(
								DataProviderContract.ProductRequestTable.TRANSFER_ROUTE_COLUMN));

				String productRequestTimeString = c.getString(
						c.getColumnIndex(
								DataProviderContract.ProductRequestTable.PRODUCT_REQUEST_TIME_COLUMN));

				Date productRequestTime = null;
				try{
					productRequestTime = DataBaseCursorBuild.format.parse(productRequestTimeString);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				int status = c.getInt(
						c.getColumnIndex(
								DataProviderContract.ProductRequestTable.STATUS_COLUMN));

				int syncStatus = c.getInt(
						c.getColumnIndex(
								DataProviderContract.ProductRequestTable.SYNC_STATUS_COLUMN));

				ProductRequest prodReq = new ProductRequest(request, product, complement, valid, transferRoute, productRequestTime, status, syncStatus);
				prodReqList.add(prodReq);
			}while(c.moveToNext());
		}
		c.close();

		return prodReqList;
	}

	public static HashSet<ProductRequest> getByRequest(Context context, Request request){
		if(request != null){
			HashSet<ProductRequest> prodReqList = new HashSet<ProductRequest>();

			Cursor c = context.getContentResolver().query(
					DataProviderContract.ProductRequestTable.TABLE_URI,
					TABLE_INFO_REQUEST_PROJECTION,
					PROD_REQUEST_SELECTION,
					new String[]{request.id},
					null
					);

			if(c.moveToFirst()){
				do{
					long productId = c.getLong(
							c.getColumnIndex(DataProviderContract.ProductTable.ID_COLUMN));

					String productName = c.getString(
							c.getColumnIndex(DataProviderContract.ProductTable.NAME_COLUMN));

					double productPrice = c.getDouble(
							c.getColumnIndex(
									DataProviderContract.ProductTable.PRICE_COLUMN));

					int popularity = c.getInt(
							c.getColumnIndex(
									DataProviderContract.ProductTable.POPULARITY_COLUMN));

					int quantity = c.getInt(
							c.getColumnIndex(
									DataProviderContract.ProductRequestTable.QUANTITY_COLUMN));

					boolean valid = c.getInt(
							c.getColumnIndex(
									DataProviderContract.ProductRequestTable.VALID_COLUMN)) == 1;

					String transferRoute = c.getString(
							c.getColumnIndex(
									DataProviderContract.ProductRequestTable.TRANSFER_ROUTE_COLUMN));

					String productRequestTimeString = c.getString(
							c.getColumnIndex(
									DataProviderContract.ProductRequestTable.PRODUCT_REQUEST_TIME_COLUMN));

					Date productRequestTime = null;
					try{
						productRequestTime = DataBaseCursorBuild.format.parse(productRequestTimeString);
					} catch (ParseException e) {
						e.printStackTrace();
					}

					int status = c.getInt(
							c.getColumnIndex(
									DataProviderContract.ProductRequestTable.STATUS_COLUMN));

					int syncStatus = c.getInt(
							c.getColumnIndex(
									DataProviderContract.ProductRequestTable.SYNC_STATUS_COLUMN));

					ProductType type = new ProductType(c.getLong(
							c.getColumnIndex(
									DataProviderContract.ProductTable.PRODUCT_TYPE_ID_COLUMN)));

					Product product = new Product(productId, productName, productPrice, quantity, popularity, type);

					String complementDesc = c.getString(
							c.getColumnIndex(
									DataProviderContract.ComplementTable.ID_COLUMN));

					Complement complement = null;
					if(complementDesc != null)
						complement = new Complement(complementDesc, 0, null);

					ProductRequest prodReq = new ProductRequest(request, product, complement, valid, transferRoute, productRequestTime, status, syncStatus);
					prodReqList.add(prodReq);
				}while(c.moveToNext());

			}

			c.close();

			return prodReqList;
		}

		return null;
	}

	public static HashSet<ProductRequest> getByBill(Context context, Bill bill){
		if(bill != null) {
			HashSet<ProductRequest> prodReqList = new HashSet<ProductRequest>();

			Cursor c = context.getContentResolver().query(
					DataProviderContract.ProductRequestTable.TABLE_URI,
					TABLE_INFO_REQUEST_PROJECTION,
					BILL_SELECTION,
					new String[]{bill.id},
					null
			);

			if(c.moveToFirst()) {
				do {
					ProductRequest prodReq = DataBaseCursorBuild.buildProductRequestObject(c, bill);
					prodReqList.add(prodReq);
				}while(c.moveToNext());
			}
			c.close();
			return prodReqList;
		}

		return null;
	}

	public static int getCountByStatusSent(Context context){
		Cursor c = context.getContentResolver().query(
				DataProviderContract.ProductRequestTable.TABLE_URI,
				PRODUCT_REQUEST_NOTIFICATION_PROJECTION,
				SENT_SELECTION,
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

	public static synchronized void insertOrUpdate(Context context, ArrayList<ProductRequest> productRequestList){
		DataProviderHelper mHelper = new DataProviderHelper(context);
		SQLiteDatabase db = mHelper.getWritableDatabase();

		db.beginTransaction();
		for (ProductRequest productRequest : productRequestList){
			insertOrUpdate(db, productRequest);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	public static synchronized void insertOrUpdate(Context context, ProductRequest productRequest){
		DataProviderHelper mHelper = new DataProviderHelper(context);
		SQLiteDatabase db = mHelper.getWritableDatabase();

		db.beginTransaction();
		insertOrUpdate(db, productRequest);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	private static synchronized void insertOrUpdate(SQLiteDatabase db, ProductRequest productRequest) {
		boolean updateFlag = false;
		try{
			if(productRequest.complement == null)
				productRequest.complement = new Complement(" ");
			long id = db.insert(TABLE_NAME, null, getValues(productRequest));
			if(id == -1){
				updateFlag = true;
			}
		}catch(SQLiteException e){
			updateFlag = true;
		}

		if(updateFlag){
			ProductRequest productRequest2 = get(db, productRequest.request.id,
					productRequest.product.code,
					productRequest.complement.description);

			if(productRequest.productRequestTime.compareTo(productRequest2.productRequestTime) >= 0){
				update(db, productRequest);
			}
		}
	}

	private static ProductRequest get(SQLiteDatabase db, String requestId, long productCode, String complement) {
		Cursor c = TABLE_JOIN_QUERY.query(
				db,
				TABLE_INFO_REQUEST_PROJECTION,
				DataProviderContract.ProductRequestTable.NICKNAME + "." +
						DataProviderContract.ProductRequestTable.PRODUCT_ID_COLUMN + " = " + productCode + " and " +
						DataProviderContract.ProductRequestTable.NICKNAME + "." +
						DataProviderContract.ProductRequestTable.REQUEST_ID_COLUMN + " = " + "'" + requestId + "' and " +
						DataProviderContract.ProductRequestTable.NICKNAME + "." +
						DataProviderContract.ProductRequestTable.COMPLEMENT_ID_COLUMN + " = " + "'" + complement + "'",
				null,
				null, null, null
		);

		ProductRequest prodReq = null;
		if(c.moveToFirst()){
			prodReq = DataBaseCursorBuild.buildProductRequestObject(c, null);
		}

		c.close();

		return prodReq;
	}

	public static void update(Context context, ProductRequest productRequest){
		try{
			context.getContentResolver().update(
					DataProviderContract.ProductRequestTable.TABLE_URI,
					getValues(productRequest),
					DataProviderContract.ProductRequestTable.REQUEST_ID_COLUMN + " = " + "'"+ productRequest.request.id + "' and " +
							DataProviderContract.ProductRequestTable.PRODUCT_ID_COLUMN + " = " + productRequest.product.code + " and " +
							DataProviderContract.ProductRequestTable.COMPLEMENT_ID_COLUMN + " = " +"'"+productRequest.complement.description+"'",
							null);
		}catch(Exception e){
			Log.e(TAG, "Produto Request " + productRequest.toString() + ": " + e.getMessage());
		}
	}

	public static synchronized void update(SQLiteDatabase db, ProductRequest productRequest) {
		try{
			db.update(
					TABLE_NAME,
					getValues(productRequest),
					DataProviderContract.ProductRequestTable.REQUEST_ID_COLUMN + " = " + "'" + productRequest.request.id + "' and " +
							DataProviderContract.ProductRequestTable.PRODUCT_ID_COLUMN + " = " + productRequest.product.code + " and " +
							DataProviderContract.ProductRequestTable.COMPLEMENT_ID_COLUMN + " = " + "'" + productRequest.complement.description + "'",
					null);
		}catch(Exception e){
			Log.e(TAG, "Produto Request " + productRequest.toString() + ": " + e.getMessage());
		}
	}

	private static ContentValues getValues(ProductRequest productRequest) {
		ContentValues values = new ContentValues();
		values.put(DataProviderContract.ProductRequestTable.REQUEST_ID_COLUMN, productRequest.request.id);
		values.put(DataProviderContract.ProductRequestTable.PRODUCT_ID_COLUMN, productRequest.product.code);
		values.put(DataProviderContract.ProductRequestTable.VALID_COLUMN, productRequest.valid);
		values.put(DataProviderContract.ProductRequestTable.TRANSFER_ROUTE_COLUMN, productRequest.transferRoute);
		values.put(DataProviderContract.ProductRequestTable.PRODUCT_REQUEST_TIME_COLUMN, DataBaseCursorBuild.format.format(productRequest.productRequestTime));
		values.put(DataProviderContract.ProductRequestTable.STATUS_COLUMN, productRequest.status);
		values.put(DataProviderContract.ProductRequestTable.SYNC_STATUS_COLUMN, productRequest.syncStatus);
		if(productRequest.complement != null)
			values.put(DataProviderContract.ProductRequestTable.COMPLEMENT_ID_COLUMN, productRequest.complement.description);
		if(productRequest.quantity > 0)
			values.put(DataProviderContract.ProductRequestTable.QUANTITY_COLUMN, productRequest.quantity);
		else
			values.put(DataProviderContract.ProductRequestTable.QUANTITY_COLUMN, productRequest.product.quantity);
		return values;
	}
}
