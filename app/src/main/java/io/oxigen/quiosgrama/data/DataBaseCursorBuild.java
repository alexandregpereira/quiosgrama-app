package io.oxigen.quiosgrama.data;

import android.database.Cursor;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import io.oxigen.quiosgrama.Amount;
import io.oxigen.quiosgrama.Bill;
import io.oxigen.quiosgrama.Client;
import io.oxigen.quiosgrama.Complement;
import io.oxigen.quiosgrama.Functionary;
import io.oxigen.quiosgrama.Poi;
import io.oxigen.quiosgrama.Product;
import io.oxigen.quiosgrama.ProductRequest;
import io.oxigen.quiosgrama.ProductType;
import io.oxigen.quiosgrama.Request;
import io.oxigen.quiosgrama.Table;

public class DataBaseCursorBuild {
	
	public static final SimpleDateFormat format = new SimpleDateFormat(KeysContract.DATE_FORMAT_KEY, Locale.US);
	private static final String TAG = "DataBaseUtil";

	public static Functionary buildWaiter(Cursor c, final String ID_COLUMN) {
		long id = c.getLong(
				c.getColumnIndex(ID_COLUMN));
		if(id > 0)
			return new Functionary(id, null, Functionary.WAITER);
		else
			return null;
	}

	public static Request buildRequest(Cursor c) {
		Bill bill = null;
		try{
			bill = buildBill(c);
		}
		catch(Exception e){
			Log.e(TAG, "buildRequest.buildBill: " + e.getMessage());
		}
		Functionary waiter = buildWaiter(c, DataProviderContract.RequestTable.FUNCTIONARY_ID_COLUMN);
		
		if(waiter != null){
			String requestId = c.getString(
					c.getColumnIndex(DataProviderContract.RequestTable.ID_COLUMN));

			String requestTime = c.getString(
					c.getColumnIndex(DataProviderContract.RequestTable.REQUEST_TIME_COLUMN));

			int syncStatus = c.getInt(
					c.getColumnIndex(DataProviderContract.RequestTable.SYNC_STATUS_COLUMN)); 

			try {
				return new Request(requestId, format.parse(requestTime), bill, waiter, syncStatus);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public static Bill buildBill(Cursor c) {
		String id = c.getString(
				c.getColumnIndex(DataProviderContract.BillTable.ID_COLUMN));

		String openTime = c.getString(
				c.getColumnIndex(DataProviderContract.BillTable.OPEN_TIME_COLUMN));
		
		String closeTime = c.getString(
				c.getColumnIndex(DataProviderContract.BillTable.CLOSE_TIME_COLUMN));
		
		String paidTime = c.getString(
				c.getColumnIndex(DataProviderContract.BillTable.PAID_TIME_COLUMN));
		
		String time = c.getString(
				c.getColumnIndex(DataProviderContract.BillTable.BILL_TIME));
		Date billTime = null;
		try {
			billTime = format.parse(time);
		} catch (ParseException e1) {
			Log.e(TAG, "Erro parse");
		}

		Functionary waiterOpenTable = buildWaiter(c, DataProviderContract.BillTable.WAITER_OPEN_TABLE_ID_COLUMN);
		Functionary waiterCloseTable = buildWaiter(c, DataProviderContract.BillTable.WAITER_CLOSE_TABLE_ID_COLUMN);
		Table table = buildTable(c);
		
		int syncStatus = c.getInt(
				c.getColumnIndex(DataProviderContract.BillTable.SYNC_STATUS_COLUMN));

		boolean servicePaid = false;
		try{
			servicePaid = c.getInt(c.getColumnIndex(DataProviderContract.BillTable.SERVICE_PAID_COLUMN)) == 1;
		} catch (Exception e){}

		try {
			Date closeDate = closeTime == null ? null : format.parse(closeTime);
			Date openDate = openTime == null ? null : format.parse(openTime);
			Date paidDate = paidTime == null ? null : format.parse(paidTime);
			
			return new Bill(id, openDate, closeDate, paidDate, waiterOpenTable, waiterCloseTable, billTime, table, syncStatus, servicePaid);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public static Table buildTable(Cursor c) {
		int tableNumber = c.getInt(
				c.getColumnIndex(DataProviderContract.TableTable.ID_COLUMN));

		int xPosInDpi = c.getInt(
				c.getColumnIndex(DataProviderContract.TableTable.X_POS_DPI_COLUMN));
		
		int yPosDpi = c.getInt(
				c.getColumnIndex(DataProviderContract.TableTable.Y_POS_DPI_COLUMN));
		
		int mapPageNumber = c.getInt(
				c.getColumnIndex(DataProviderContract.TableTable.MAP_PAGE_NUMBER));
		
		String time = c.getString(c.getColumnIndex(DataProviderContract.TableTable.TABLE_TIME));
		Date tableTime = null;
		if(time != null)
			try {
				tableTime = format.parse(time);
			} catch (ParseException e) {
				Log.e(TAG, "Erro de parse");
			}
		
		Functionary waiterAlterTable = buildWaiter(c, DataProviderContract.TableTable.FUNCTIONARY_ID_COLUMN);
		
		int syncStatus = c.getInt(
				c.getColumnIndex(DataProviderContract.TableTable.SYNC_STATUS_COLUMN));
		
		String clientTemp = c.getString(
				c.getColumnIndex(DataProviderContract.TableTable.CLIENT_TEMP_COLUMN));

		int show = c.getInt(
				c.getColumnIndex(DataProviderContract.TableTable.SHOW_COLUMN));
		
		return new Table(tableNumber, xPosInDpi, yPosDpi, mapPageNumber, tableTime, waiterAlterTable, syncStatus, null, clientTemp, show == 1);
	}

	public static Product buildProduct(Cursor c) {
		long productId = c.getLong(
				c.getColumnIndex(DataProviderContract.ProductTable.ID_COLUMN));

		String productName = c.getString(
				c.getColumnIndex(DataProviderContract.ProductTable.NAME_COLUMN));

		double productPrice = c.getDouble(
				c.getColumnIndex(
						DataProviderContract.ProductTable.PRICE_COLUMN));

		int quantity = c.getInt(
				c.getColumnIndex(
						DataProviderContract.ProductRequestTable.QUANTITY_COLUMN));

		return new Product(productId, productName, productPrice, quantity, 0, new ProductType());
	}

	public static Complement buildComplement(Cursor c) {
		String complementDesc = c.getString(
				c.getColumnIndex(
						DataProviderContract.ComplementTable.ID_COLUMN));
		
		if(complementDesc != null)
			return new Complement(complementDesc, 0, null);
		
		return null;
	}
	
//	public static Request buildRequestObject(Cursor c) {
//		return null;
//	}

	public static ProductType buildProductTypeObject(Cursor c){
		long id = c.getLong(
				c.getColumnIndex(DataProviderContract.ProductTypeTable.ID_COLUMN));
		
		ProductType type = new ProductType(id);
		
		type.name = c.getString(
				c.getColumnIndex(DataProviderContract.ProductTypeTable.NAME_COLUMN));
		
		type.priority = c.getInt(
				c.getColumnIndex(DataProviderContract.ProductTypeTable.PRIORITY_COLUMN));

		type.buttonImage = c.getString(
				c.getColumnIndex(DataProviderContract.ProductTypeTable.BUTTON_IMAGE_COLUMN));
		
		type.imageInfo = c.getString(
				c.getColumnIndex(DataProviderContract.ProductTypeTable.IMAGE_INFO_COLUMN));
		
		type.colorId = c.getString(
				c.getColumnIndex(DataProviderContract.ProductTypeTable.COLOR_ID_COLUMN));
		
		type.imageInfoId = c.getInt(
				c.getColumnIndex(DataProviderContract.ProductTypeTable.IMAGE_INFO_ID_COLUMN));

		type.destination = c.getInt(
				c.getColumnIndex(DataProviderContract.ProductTypeTable.DESTINATION_COLUMN));

		type.destinationName = c.getString(
				c.getColumnIndex(DataProviderContract.ProductTypeTable.DESTINATION_NAME_COLUMN));

		type.destinationIcon = c.getString(
				c.getColumnIndex(DataProviderContract.ProductTypeTable.DESTINATION_ICON_COLUMN));

		type.printerIp = c.getString(
				c.getColumnIndex(DataProviderContract.ProductTypeTable.DESTINATION_IP_COLUMN));
		
		return type;
	}
	
	public static Product buildProductObject(Cursor c, ArrayList<ProductType>productTypeList){
		long code = c.getLong(
				c.getColumnIndex(DataProviderContract.ProductTable.ID_COLUMN));
		
		String name = c.getString(
				c.getColumnIndex(DataProviderContract.ProductTable.NAME_COLUMN));
		
		double price = c.getDouble(
				c.getColumnIndex(DataProviderContract.ProductTable.PRICE_COLUMN));
		
		String description = c.getString(
				c.getColumnIndex(DataProviderContract.ProductTable.DESCRIPTION_COLUMN));
		
		int popularity = c.getInt(
				c.getColumnIndex(
						DataProviderContract.ProductTable.POPULARITY_COLUMN));
		
		long typeId = c.getLong(
				c.getColumnIndex(DataProviderContract.ProductTable.PRODUCT_TYPE_ID_COLUMN));

		String tax = c.getString(
				c.getColumnIndex(DataProviderContract.ProductTable.TAX_COLUMN));
		
		Product product = new Product(code, name, price, 0, popularity, tax, new ProductType());
		product.description = description;
		
		for (ProductType type : productTypeList) {
			if(type.id == typeId)
				product.type = type;
		}
		
		return product;
	}
	
	public static Functionary buildFunctionaryObject(Cursor c){
		Functionary waiter = new Functionary(
				c.getLong(
						c.getColumnIndex(DataProviderContract.FunctionaryTable.ID_COLUMN)));
		
		waiter.name = c.getString(
				c.getColumnIndex(DataProviderContract.FunctionaryTable.NAME_COLUMN));
		
		waiter.imei = c.getString(
				c.getColumnIndex(DataProviderContract.FunctionaryTable.IMEI_COLUMN));
		
		waiter.adminFlag = c.getInt(
				c.getColumnIndex(DataProviderContract.FunctionaryTable.ADMIN_FLAG_COLUMN));
		
		return waiter;
	}
	
	public static Bill buildBillObject(Cursor c, ArrayList<Functionary> functionaryList, 
			ArrayList<Table> tableList){
		SimpleDateFormat dateFormat = new SimpleDateFormat(KeysContract.DATE_FORMAT_KEY, Locale.US);
		try {
			int tableNumber = 
					c.getInt(c.getColumnIndex(DataProviderContract.BillTable.TABLE_ID_COLUMN));
			
			Table table = null;
			for (Table tableTemp : tableList) {
				if(tableTemp.number == tableNumber){
					table = tableTemp;
					break;
				}
			}
			
			long idWaiterOpenTable = 
					c.getLong(c.getColumnIndex(DataProviderContract.BillTable.WAITER_OPEN_TABLE_ID_COLUMN));
			
			Functionary waiterOpenTable = null;
			for (Functionary waiterTemp : functionaryList) {
				if(waiterTemp.id == idWaiterOpenTable){
					waiterOpenTable = waiterTemp;
					break;
				}
			}
			
			long idWaiterCloseTable = 
					c.getLong(c.getColumnIndex(DataProviderContract.BillTable.WAITER_CLOSE_TABLE_ID_COLUMN));
			
			Functionary waiterCloseTable = null;
			for (Functionary waiterTemp : functionaryList) {
				if(waiterTemp.id == idWaiterCloseTable){
					waiterCloseTable = waiterTemp;
					break;
				}
			}
			
			String closeTimeString = 
					c.getString(c.getColumnIndex(DataProviderContract.BillTable.CLOSE_TIME_COLUMN));
			Date closeTime = null;
			if(closeTimeString != null)
				closeTime = dateFormat.parse(closeTimeString);
			
			String paidTimeString = 
					c.getString(c.getColumnIndex(DataProviderContract.BillTable.PAID_TIME_COLUMN));
			Date paidTime = null;
			if(paidTimeString != null)
				paidTime = dateFormat.parse(paidTimeString);
			
			String openTimeString = 
					c.getString(c.getColumnIndex(DataProviderContract.BillTable.OPEN_TIME_COLUMN));
			Date openTime = null;
			if(openTimeString != null)
				openTime = dateFormat.parse(openTimeString);
			
			String time = c.getString(c.getColumnIndex(DataProviderContract.BillTable.BILL_TIME));
			Date billTime = format.parse(time);
			
			int syncStatus = c.getInt(c.getColumnIndex(DataProviderContract.BillTable.SYNC_STATUS_COLUMN));

			boolean servicePaid = false;
			try{
				servicePaid = c.getInt(c.getColumnIndex(DataProviderContract.BillTable.SERVICE_PAID_COLUMN)) == 1;
			} catch (Exception e){}

			Bill bill = new Bill(
					c.getString(c.getColumnIndex(DataProviderContract.BillTable.ID_COLUMN)), 
					openTime, 
					closeTime, 
					paidTime,
					waiterOpenTable, 
					waiterCloseTable,
					billTime,
					table,
					syncStatus, servicePaid);
			
			return bill;
		} catch (ParseException e) {
			Log.e(TAG, "buildBillObject: " + e.getMessage());
		}
		
		return null;
	}
	
	public static Table buildTableObject(Cursor c, ArrayList<Client> clientList,
			ArrayList<Functionary> functionaryList) {

		long idWaiter = 
				c.getLong(c.getColumnIndex(DataProviderContract.TableTable.FUNCTIONARY_ID_COLUMN));
		
		Functionary waiter = null;
		for (Functionary waiterTemp : functionaryList) {
			if(waiterTemp.id == idWaiter){
				waiter = waiterTemp;
				break;
			}
		}
		
		long idClient = 
				c.getLong(c.getColumnIndex(DataProviderContract.TableTable.CLIENT_ID_COLUMN));
		
		Client client = null;
		for (Client clientTemp : clientList) {
			if(clientTemp.id == idClient){
				client = clientTemp;
				break;
			}
		}
		
		String time = c.getString(c.getColumnIndex(DataProviderContract.TableTable.TABLE_TIME));
		Date tableTime = null;
		if(time != null)
			try {
				tableTime = format.parse(time);
			} catch (ParseException e) {
				Log.e(TAG, "Erro de parse");
			}
				
		Table table = new Table(
				c.getInt(c.getColumnIndex(DataProviderContract.TableTable.ID_COLUMN)), 
				c.getInt(c.getColumnIndex(DataProviderContract.TableTable.X_POS_DPI_COLUMN)),
				c.getInt(c.getColumnIndex(DataProviderContract.TableTable.Y_POS_DPI_COLUMN)),
				c.getInt(c.getColumnIndex(DataProviderContract.TableTable.MAP_PAGE_NUMBER)),
				tableTime,
				waiter,
				c.getInt(c.getColumnIndex(DataProviderContract.TableTable.SYNC_STATUS_COLUMN)),
				client,
				c.getString(c.getColumnIndex(DataProviderContract.TableTable.CLIENT_TEMP_COLUMN)),
				c.getInt(c.getColumnIndex(DataProviderContract.TableTable.SHOW_COLUMN)) == 1);
		
		return table;
	}
	
	public static Complement buildComplementObject(Cursor c){
		Complement complement = new Complement(
				c.getString(c.getColumnIndex(DataProviderContract.ComplementTable.ID_COLUMN)),
				c.getDouble(c.getColumnIndex(DataProviderContract.ComplementTable.PRICE_COLUMN)),
				c.getString(c.getColumnIndex(DataProviderContract.ComplementTable.DRAWABLE_ID_COLUMN)));

		return complement;
	}

	public static Poi buildPoi(Cursor c) {
		int idPoi = c.getInt(
				c.getColumnIndex(DataProviderContract.PoiTable.ID_COLUMN));
		
		String name = c.getString(
				c.getColumnIndex(DataProviderContract.PoiTable.NAME_COLUMN));

		int xPosInDpi = c.getInt(
				c.getColumnIndex(DataProviderContract.PoiTable.X_POS_DPI_COLUMN));
		
		int yPosDpi = c.getInt(
				c.getColumnIndex(DataProviderContract.PoiTable.Y_POS_DPI_COLUMN));
		
		String image = c.getString(
				c.getColumnIndex(DataProviderContract.PoiTable.IMAGE_COLUMN));
		
		int mapPageNumber = c.getInt(
				c.getColumnIndex(DataProviderContract.PoiTable.MAP_PAGE_NUMBER));
		
		String time = c.getString(c.getColumnIndex(DataProviderContract.PoiTable.POI_TIME));
		Date poiTime = null;
		if(time != null)
			try {
				poiTime = format.parse(time);
			} catch (ParseException e) {
				Log.e(TAG, "Erro de parse");
			}
		
		Functionary waiterAlterPoi = buildWaiter(c, DataProviderContract.PoiTable.FUNCTIONARY_ID_COLUMN);
		
		int syncStatus = c.getInt(
				c.getColumnIndex(DataProviderContract.PoiTable.SYNC_STATUS_COLUMN));
		
		return new Poi(idPoi,name, xPosInDpi, yPosDpi, image, mapPageNumber, waiterAlterPoi, poiTime, syncStatus);
	}

	public static Poi buildPoiObject(Cursor c, ArrayList<Functionary> functionaryList) {
		long idWaiter = 
				c.getLong(c.getColumnIndex(DataProviderContract.PoiTable.FUNCTIONARY_ID_COLUMN));
		
		Functionary waiter = null;
		for (Functionary waiterTemp : functionaryList) {
			if(waiterTemp.id == idWaiter){
				waiter = waiterTemp;
				break;
			}
		}
		
		String time = c.getString(c.getColumnIndex(DataProviderContract.PoiTable.POI_TIME));
		Date poiTime = null;
		if(time != null)
			try {
				poiTime = format.parse(time);
			} catch (ParseException e) {
				Log.e(TAG, "Erro de parse");
			}
				
		Poi poi = new Poi(
				c.getInt(c.getColumnIndex(DataProviderContract.PoiTable.ID_COLUMN)),
				c.getString(c.getColumnIndex(DataProviderContract.PoiTable.NAME_COLUMN)),
				c.getInt(c.getColumnIndex(DataProviderContract.PoiTable.X_POS_DPI_COLUMN)),
				c.getInt(c.getColumnIndex(DataProviderContract.PoiTable.Y_POS_DPI_COLUMN)),
				c.getString(c.getColumnIndex(DataProviderContract.PoiTable.IMAGE_COLUMN)),
				c.getInt(c.getColumnIndex(DataProviderContract.PoiTable.MAP_PAGE_NUMBER)),
				waiter,
				poiTime,
				c.getInt(c.getColumnIndex(DataProviderContract.PoiTable.SYNC_STATUS_COLUMN)));
		
		return poi;
	}

	public static Client buildClientObject(Cursor c) {
		return new Client(
			c.getLong(c.getColumnIndex(DataProviderContract.ClientTable.ID_COLUMN)),
			c.getString(c.getColumnIndex(DataProviderContract.ClientTable.NAME_COLUMN)),
			c.getString(c.getColumnIndex(DataProviderContract.ClientTable.CPF_COLUMN)),
			c.getString(c.getColumnIndex(DataProviderContract.ClientTable.PHONE_COLUMN)),
			c.getInt(c.getColumnIndex(DataProviderContract.ClientTable.TEMP_FLAG_COLUMN)),
			c.getInt(c.getColumnIndex(DataProviderContract.ClientTable.PRESENT_FLAG_COLUMN)));
	}

	public static ProductRequest buildProductRequestObject(Cursor c, Bill bill){
		String idRequest = c.getString(
				c.getColumnIndex(
						DataProviderContract.ProductRequestTable.REQUEST_ID_COLUMN));

		String requestTime = c.getString(
				c.getColumnIndex(
						DataProviderContract.RequestTable.REQUEST_TIME_COLUMN));

		long idWaiter = c.getLong(
				c.getColumnIndex(
						DataProviderContract.RequestTable.FUNCTIONARY_ID_COLUMN));

		int syncStatus = c.getInt(
				c.getColumnIndex(
						DataProviderContract.RequestTable.SYNC_STATUS_COLUMN));

		Functionary waiter = new Functionary(idWaiter);

		Request request = null;
		try {
			request = new Request(idRequest,
					new SimpleDateFormat(KeysContract.DATE_FORMAT_KEY, Locale.US).parse(requestTime),
					bill,
					waiter, syncStatus);
		} catch (ParseException e) {
			Log.e("buildProductRequest", e.getMessage());
		}

		long idProduct = c.getLong(
				c.getColumnIndex(
						DataProviderContract.ProductTable.ID_COLUMN));

		String productName = c.getString(
				c.getColumnIndex(
						DataProviderContract.ProductTable.NAME_COLUMN));

		double productPrice = c.getDouble(
				c.getColumnIndex(
						DataProviderContract.ProductTable.PRICE_COLUMN));

		int popularity = c.getInt(
				c.getColumnIndex(
						DataProviderContract.ProductTable.POPULARITY_COLUMN));

		String tax = null;
		try {
			tax = c.getString(
					c.getColumnIndex(
							DataProviderContract.ProductTable.TAX_COLUMN));
		}catch (Exception e){}

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

		int prodReqSyncStatus = c.getInt(
				c.getColumnIndex(
						DataProviderContract.ProductRequestTable.SYNC_STATUS_COLUMN));

		ProductType type = new ProductType(c.getLong(
				c.getColumnIndex(
						DataProviderContract.ProductTable.PRODUCT_TYPE_ID_COLUMN)));

		Product product = new Product(idProduct, productName, productPrice, quantity, popularity, tax, type);

		String complementDesc = c.getString(
				c.getColumnIndex(
						DataProviderContract.ComplementTable.ID_COLUMN));

		double complementPrice = c.getDouble(
				c.getColumnIndex(
						DataProviderContract.ComplementTable.PRICE_COLUMN));

		Complement complement = null;
		if (complementDesc != null)
			complement = new Complement(complementDesc, complementPrice, null);

		ProductRequest prodReq = new ProductRequest(request, product, complement, valid, transferRoute, productRequestTime, status, prodReqSyncStatus);
		prodReq.quantity = quantity;

		return prodReq;
	}

	public static Amount buildAmount(Cursor c) {
		String id = c.getString(
				c.getColumnIndex(
						DataProviderContract.AmountTable.ID_COLUMN));

		double value = c.getDouble(
				c.getColumnIndex(
						DataProviderContract.AmountTable.VALUE_COLUMN));

		int paidMethod = c.getInt(
				c.getColumnIndex(
						DataProviderContract.AmountTable.PAID_METHOD_COLUMN));

		int syncStatusAmount = c.getInt(
				c.getColumnIndex(
						DataProviderContract.AmountTable.SYNC_STATUS_COLUMN));

		String idBill = c.getString(
				c.getColumnIndex(
						DataProviderContract.AmountTable.BILL_ID_COLUMN));

		Bill bill = new Bill(idBill, null, null, null, null, null, null, null, 0, false);

		return new Amount(id, value, paidMethod, bill, syncStatusAmount);
	}
}
