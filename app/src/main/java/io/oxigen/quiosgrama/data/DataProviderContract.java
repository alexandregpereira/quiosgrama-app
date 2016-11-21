package io.oxigen.quiosgrama.data;

import android.net.Uri;

import java.util.UUID;

public final class DataProviderContract {

	public static final String DATABASE_NAME = "Quiosgrama.db";
	public static final int DATABASE_VERSION = 29;
	public static final String SCHEME = "content";
	public static final String AUTHORITY = "io.oxigen.quiosgrama";
	private static final Uri CONTENT_URI = Uri.parse(SCHEME + "://" + AUTHORITY);
	
	public static final String ACCOUNT_TYPE = "io.oxigen";
    public static final String ACCOUNT_NAME = "QuiosqueAccount";
	
	public static String idGenerator(){
		return UUID.randomUUID().toString();
	}

	public static final class AmountTable{
		public static final String TABLE_NAME = "Amount";
		public static final Uri TABLE_URI =
				Uri.withAppendedPath(CONTENT_URI, TABLE_NAME);
		public static final String NICKNAME = "amount";

		public static final String ID_COLUMN = "idAmount";
		public static final String VALUE_COLUMN = "amountValue";
		public static final String PAID_METHOD_COLUMN = "paidMethod";
		public static final String SYNC_STATUS_COLUMN = "syncStatusAmount";
		public static final String BILL_ID_COLUMN = "idBillAmount";
	}
	
	public static final class BillTable{
		public static final String TABLE_NAME = "Bill";
		public static final Uri TABLE_URI =
				Uri.withAppendedPath(CONTENT_URI, TABLE_NAME);
		public static final String NICKNAME = "bill";
		
		public static final String ID_COLUMN = "idBill";
		public static final String OPEN_TIME_COLUMN = "openTime";
		public static final String CLOSE_TIME_COLUMN = "closeTime";
		public static final String PAID_TIME_COLUMN = "paidTime";
		public static final String WAITER_OPEN_TABLE_ID_COLUMN = "idWaiterOpenTable";
		public static final String WAITER_CLOSE_TABLE_ID_COLUMN = "idWaiterCloseTable";
		public static final String BILL_TIME = "billTime";
		public static final String SERVICE_PAID_COLUMN = "servicePaid";
		public static final String TABLE_ID_COLUMN = "billTableNumber";
		public static final String SYNC_STATUS_COLUMN = "syncStatusBill";
	}
	
	public static final class ComplementTable{
		public static final String TABLE_NAME = "Complement";
		public static final Uri TABLE_URI =
				Uri.withAppendedPath(CONTENT_URI, TABLE_NAME);
		public static final String NICKNAME = "complement";
		
		public static final String ID_COLUMN = "description";
		public static final String PRICE_COLUMN = "complementPrice";
		public static final String DRAWABLE_ID_COLUMN = "drawableId";
	}
	
	public static final class ComplementTypeTable{
		public static final String TABLE_NAME = "ComplementType";
		public static final Uri TABLE_URI =
				Uri.withAppendedPath(CONTENT_URI, TABLE_NAME);
		public static final String NICKNAME = "complementType";
		
		public static final String COMPLEMENT_COLUMN = "complement";
		public static final String ID_PRODUCT_TYPE_COLUMN= "idProductType";
	}
	
	public static final class FunctionTable{
		public static final String TABLE_NAME = "Function";
		public static final Uri TABLE_URI =
				Uri.withAppendedPath(CONTENT_URI, TABLE_NAME);
		
		public static final String ID_COLUMN = "idFunction";
		public static final String NAME_COLUMN = "functionName";
	}
	
	public static final class FunctionaryTable{
		public static final String TABLE_NAME = "Functionary";
		public static final Uri TABLE_URI =
				Uri.withAppendedPath(CONTENT_URI, TABLE_NAME);
		public static final String NICKNAME = "waiter";
		
		public static final String ID_COLUMN = "idFunctionary";
		public static final String NAME_COLUMN = "functionaryName";
		public static final String FUNCTION_ID_COLUMN = FunctionTable.ID_COLUMN;
		public static final String IMEI_COLUMN = "imei";
		public static final String ADMIN_FLAG_COLUMN = "adminFlag";
	}
	
	public static final class ProductTable{
		public static final String TABLE_NAME = "Product";
		public static final Uri TABLE_URI =
				Uri.withAppendedPath(CONTENT_URI, TABLE_NAME);
		public static final String NICKNAME = "product";
		
		public static final String ID_COLUMN = "idProduct";
		public static final String NAME_COLUMN = "productName";
		public static final String PRICE_COLUMN = "price";
		public static final String DESCRIPTION_COLUMN = "description";
		public static final String POPULARITY_COLUMN = "popularity";
		public static final String TAX_COLUMN = "tax";
		public static final String PRODUCT_TYPE_ID_COLUMN = "productTypeId";
	}
	
	public static final class ProductRequestTable{
		public static final String TABLE_NAME = "ProductRequest";
		public static final Uri TABLE_URI =
				Uri.withAppendedPath(CONTENT_URI, TABLE_NAME);
		public static final String NICKNAME = "prodReq";
		
		public static final String REQUEST_ID_COLUMN = RequestTable.ID_COLUMN;
		public static final String PRODUCT_ID_COLUMN = ProductTable.ID_COLUMN;
		public static final String COMPLEMENT_ID_COLUMN = ComplementTable.ID_COLUMN;
		public static final String QUANTITY_COLUMN = "quantity";
		public static final String VALID_COLUMN = "valid";
		public static final String TRANSFER_ROUTE_COLUMN = "transferRoute";
		public static final String PRODUCT_REQUEST_TIME_COLUMN = "productRequestTime";
		public static final String STATUS_COLUMN = "status";
		public static final String SYNC_STATUS_COLUMN = "prodReqSyncStatus";
	}
	
	public static final class ProductTypeTable{
		public static final String TABLE_NAME = "ProductType";
		public static final Uri TABLE_URI =
				Uri.withAppendedPath(CONTENT_URI, TABLE_NAME);

		public static final String ID_COLUMN = "idProductType";
		public static final String NAME_COLUMN = "typeName";
		public static final String PRIORITY_COLUMN = "priority";
		public static final String BUTTON_IMAGE_COLUMN = "buttonImage";
		public static final String IMAGE_INFO_COLUMN = "imageInfo";
		public static final String COLOR_ID_COLUMN = "COLOR_ID";
		public static final String IMAGE_INFO_ID_COLUMN = "IMAGE_INFO_ID";
		public static final String DESTINATION_COLUMN = "destination";
		public static final String DESTINATION_NAME_COLUMN = "destinationName";
		public static final String DESTINATION_ICON_COLUMN = "destinationIcon";
		public static final String DESTINATION_IP_COLUMN = "printerIp";
	}
	
	public static final class RequestTable{
		public static final String TABLE_NAME = "Request";
		public static final Uri TABLE_URI =
				Uri.withAppendedPath(CONTENT_URI, TABLE_NAME);
		public static final String NICKNAME = "request";
		
		public static final String ID_COLUMN = "idRequest";
		public static final String REQUEST_TIME_COLUMN = "requestTime";
		public static final String SYNC_STATUS_COLUMN = "syncStatus";
		public static final String BILL_ID_COLUMN = "idBillRequest";
		public static final String FUNCTIONARY_ID_COLUMN = "idRequestFunctionary";
	}
	
	public static final class TableTable{
		public static final String TABLE_NAME = "TableName";
		public static final Uri TABLE_URI =
				Uri.withAppendedPath(CONTENT_URI, TABLE_NAME);
		
		public static final String ID_COLUMN = "tableNumber";
		public static final String X_POS_DPI_COLUMN = "xPosInDpi";
		public static final String Y_POS_DPI_COLUMN = "yPosInDpi";
		public static final String MAP_PAGE_NUMBER = "mapPageNumber";
		public static final String TABLE_TIME = "tableTime";
		public static final String FUNCTIONARY_ID_COLUMN = "idWaiterAlterTable";
		public static final String NICKNAME = "tableName";
		public static final String SYNC_STATUS_COLUMN = "syncStatusTable";
		public static final String CLIENT_ID_COLUMN = "idTableClient";
		public static final String CLIENT_TEMP_COLUMN = "clientTemp";
		public static final String SHOW_COLUMN = "showTable";
	}
	
	public static final class PoiTable{
		public static final String TABLE_NAME = "Poi";
		public static final Uri TABLE_URI =
				Uri.withAppendedPath(CONTENT_URI, TABLE_NAME);
		
		public static final String ID_COLUMN = "idPoi";
		public static final String X_POS_DPI_COLUMN = "xPosInDpi";
		public static final String Y_POS_DPI_COLUMN = "yPosInDpi";
		public static final String NAME_COLUMN = "poiName";
		public static final String IMAGE_COLUMN = "poiImage";
		public static final String MAP_PAGE_NUMBER = "mapPageNumber";
		public static final String POI_TIME = "poiTime";
		public static final String FUNCTIONARY_ID_COLUMN = "waiterAlterPoi";
		public static final String SYNC_STATUS_COLUMN = "poiSyncStatus";
	}
	
	public static final class ClientTable{
		public static final String TABLE_NAME = "Client";
		public static final Uri TABLE_URI =
				Uri.withAppendedPath(CONTENT_URI, TABLE_NAME);
		
		public static final String ID_COLUMN = "idClient";
		public static final String NAME_COLUMN = "clientName";
		public static final String CPF_COLUMN = "cpfName";
		public static final String PHONE_COLUMN = "phone";
		public static final String TEMP_FLAG_COLUMN = "tempFlag";
		public static final String PRESENT_FLAG_COLUMN = "presentFlag";
	}
}
