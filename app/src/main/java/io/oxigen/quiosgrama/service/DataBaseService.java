package io.oxigen.quiosgrama.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import io.oxigen.quiosgrama.Amount;
import io.oxigen.quiosgrama.Bill;
import io.oxigen.quiosgrama.Client;
import io.oxigen.quiosgrama.Complement;
import io.oxigen.quiosgrama.Functionary;
import io.oxigen.quiosgrama.Poi;
import io.oxigen.quiosgrama.Product;
import io.oxigen.quiosgrama.ProductRequest;
import io.oxigen.quiosgrama.ProductType;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.Request;
import io.oxigen.quiosgrama.Table;
import io.oxigen.quiosgrama.dao.AmountDao;
import io.oxigen.quiosgrama.dao.BillDao;
import io.oxigen.quiosgrama.dao.ClientDao;
import io.oxigen.quiosgrama.dao.ComplementDao;
import io.oxigen.quiosgrama.dao.FunctionaryDao;
import io.oxigen.quiosgrama.dao.PoiDao;
import io.oxigen.quiosgrama.dao.ProductDao;
import io.oxigen.quiosgrama.dao.ProductRequestDao;
import io.oxigen.quiosgrama.dao.ProductTypeDao;
import io.oxigen.quiosgrama.dao.RequestDao;
import io.oxigen.quiosgrama.dao.TableDao;
import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.fragment.MapPagerFragment;
import io.oxigen.quiosgrama.util.AndroidUtil;

public class DataBaseService extends IntentService{

	private static final String TAG = "DataBaseService";

	public static final int INSERT_REQUEST = 1;
	public static final int UPDATE_TABLE = 2;
//	public static final int INSERT_OBJECT_CONTAINER = 3;
	public static final int UPDATE_POI = 3;
	public static final int UPDATE_BILL = 4;
	public static final int INSERT_PRODUCT_REQUEST = 5;
	public static final int UPDATE_PRODUCT_REQUEST = 6;
	public static final int INSERT_AMOUNT = 7;

	QuiosgramaApp app;

	public DataBaseService() {
		super(DataBaseService.class.getName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		app = (QuiosgramaApp) getApplication();
		Bundle data = intent.getExtras();
		int method = data.getInt(KeysContract.METHOD_KEY);

		if(method == 0)
			throw new IllegalArgumentException("Intent sem method key: use os valores statics da classe InsertDbService");

		switch (method) {
			case INSERT_REQUEST:
				insertRequest(data);
			break;

			case UPDATE_TABLE:
				updateTable(data);
			break;

			case UPDATE_POI:
				updatePoi(data);
			break;

			case UPDATE_BILL:
				updateBill(data);
			break;

			case INSERT_PRODUCT_REQUEST:
				ArrayList<ProductRequest> productRequestList = data.getParcelableArrayList(KeysContract.PRODUCT_REQUEST_LIST_KEY);
                Bill previousBill = data.getParcelable(KeysContract.BILL_KEY);

                insertProductRequest(this, productRequestList);
                validatePreviousBill(previousBill);
                sendBroadcast(new Intent(MapPagerFragment.RECEIVER_FILTER));

                Intent intentService = new Intent(this, SyncServerService.class);
				intentService.putExtra(KeysContract.METHOD_KEY, SyncServerService.SEND_PRODUCT_REQUEST);
				startService(intentService);
			break;

			case UPDATE_PRODUCT_REQUEST:
				updateProductRequest(data);
			break;

			case INSERT_AMOUNT:
				insertAmount(data);
			break;
		}
	}

	private void insertAmount(Bundle data) {
		ArrayList<Amount> amountList = data.getParcelableArrayList(KeysContract.AMOUNT_KEY);
		AmountDao.insert(this, amountList);

		Intent syncServerIntent = new Intent(this, SyncServerService.class);
		syncServerIntent.putExtra(KeysContract.METHOD_KEY, SyncServerService.SEND_AMOUNT);
		startService(syncServerIntent);
	}

	private void updateProductRequest(Bundle data) {
		ArrayList<ProductRequest> productRequestList = data.getParcelableArrayList(KeysContract.PRODUCT_REQUEST_LIST_KEY);
		for (ProductRequest productRequest: productRequestList) {
			ProductRequestDao.update(this, productRequest);
		}

		Intent syncServerIntent = new Intent(this, SyncServerService.class);
		syncServerIntent.putExtra(KeysContract.METHOD_KEY, SyncServerService.SEND_PRODUCT_REQUEST);
		startService(syncServerIntent);
	}

	private void updatePoi(Bundle data) {
		Poi poi = data.getParcelable(KeysContract.TABLE_KEY);
		PoiDao.update(this, poi);

		Intent syncServerIntent = new Intent(this, SyncServerService.class);
		syncServerIntent.putExtra(KeysContract.METHOD_KEY, SyncServerService.SEND_POI);
		startService(syncServerIntent);
	}

	private void updateTable(Bundle data) {
		Table table = data.getParcelable(KeysContract.TABLE_KEY);
		TableDao.update(this, table);

		if(!table.show){
			Bill bill = QuiosgramaApp.searchBill(table.number);
			bill.table = table;
			app.addOrUpdateBill(bill);
			sendBroadcast(new Intent(MapPagerFragment.RECEIVER_FILTER));
		}
	}

	private void insertRequest(Bundle data){
		Request request = data.getParcelable(KeysContract.REQUEST_KEY);
		ArrayList<ProductRequest> productRequestList =
				data.getParcelableArrayList(KeysContract.PRODUCT_REQUEST_LIST_KEY);

		Table table = request.bill.table;
		TableDao.insertOrUpdate(this, table);

		Bill bill = request.bill;

		BillDao.insertOrUpdate(this, bill);

		app.sortBillList();

		RequestDao.insertOrUpdate(this, request);

		for (ProductRequest productRequest : productRequestList) {
			if(productRequest.complement != null ){
				Complement complement = productRequest.complement;
				ComplementDao.insertOrUpdate(this, complement);
			}

			ProductRequestDao.insertOrUpdate(this, productRequest);
		}

		Intent intent = new Intent(this, SyncServerService.class);
		intent.putExtra(KeysContract.METHOD_KEY, SyncServerService.SEND_PRODUCT_REQUEST);
		startService(intent);
	}

	private void updateBill(Bundle data) {
		Bill bill = data.getParcelable(KeysContract.BILL_KEY);
		if(bill.table.syncStatus == KeysContract.NO_SYNCHRONIZED_STATUS_KEY){
			TableDao.update(this, bill.table);
		}
		BillDao.update(this, bill);

		sendBroadcast(new Intent(MapPagerFragment.RECEIVER_FILTER));

		Intent intent = new Intent(this, SyncServerService.class);
		intent.putExtra(KeysContract.METHOD_KEY, SyncServerService.SEND_BILL);
		startService(intent);
	}

	public static void insertOrUpdateTable(Context context, ArrayList<Table> tableList){
		if(tableList != null){
			TableDao.insertOrUpdate(context, tableList);
		}
	}

	public static void insertProductRequest(Context context, ArrayList<ProductRequest> prodReqList) {
		QuiosgramaApp app = (QuiosgramaApp) context.getApplicationContext();
		for (ProductRequest productRequest : prodReqList) {
			Product product = productRequest.product;
			//ProductType type = product.type;
			Request request = productRequest.request;
			Bill bill = request.bill;
			Table table= bill.table;
			Complement complement = productRequest.complement;

			if(productRequest.quantity == 0){
				productRequest.quantity = product.quantity;
			}

			ArrayList<Functionary> waiterList = new ArrayList<Functionary>(3);
			waiterList.add(request.waiter);
			if(bill.waiterOpenTable != null)
				waiterList.add(bill.waiterOpenTable);
			if(bill.waiterCloseTable != null)
				waiterList.add(bill.waiterCloseTable);

			boolean syncFlag = false;
			ArrayList<Functionary> functionaryListTemp = app.getFunctionaryList();
			for (Functionary functionary : waiterList) {
				Functionary searchWaiter = null;
				for (Functionary functionaryTemp : functionaryListTemp) {
					if(functionaryTemp.id == functionary.id){
						searchWaiter = functionaryTemp;
						break;
					}
				}

				if(searchWaiter == null){
					syncFlag = true;
					break;
				}
				//FunctionaryDao.insertOrUpdate(context, functionary);
			}

			if(!syncFlag){
				if(complement != null)
					ComplementDao.insertOrUpdate(context, complement);
				//ProductTypeDao.insertOrUpdate(context, type);
				//ProductDao.insertOrUpdate(context, product);
				TableDao.insertOrUpdate(context, table);
				BillDao.insertOrUpdate(context, bill);
				RequestDao.insertOrUpdate(context, request);
				try{
					ProductRequestDao.insertOrUpdate(context, productRequest);
				} catch(Exception e){
					Intent intentService = new Intent(context, SyncServerService.class);
					intentService.putExtra(KeysContract.METHOD_KEY, SyncServerService.GET_OBJECT_CONTAINER);
					context.startService(intentService);
				}
			}
			else{
				Intent intentService = new Intent(context, SyncServerService.class);
				intentService.putExtra(KeysContract.METHOD_KEY, SyncServerService.GET_OBJECT_CONTAINER);
				context.startService(intentService);
			}
		}
	}

	public static void loadFromDb(Context context){
		long finalInit = System.currentTimeMillis();
		QuiosgramaApp app = (QuiosgramaApp) context.getApplicationContext();

		ArrayList<ProductType> productTypeList = new ArrayList<ProductType>();
		ArrayList<Product> productList = new ArrayList<Product>();
		ArrayList<Functionary> functionaryList = new ArrayList<Functionary>();
		ArrayList<Bill> billList = new ArrayList<Bill>();
		ArrayList<Table> tableList = new ArrayList<Table>();
		ArrayList<Complement> complementList = new ArrayList<Complement>();
		ArrayList<Poi> poiList = new ArrayList<Poi>();
		ArrayList<Client> clientList = new ArrayList<Client>();

		ProductTypeDao.getAll(context, productTypeList);
		buildProductTypeImagesValue(productTypeList);

		ProductDao.getAll(context, productTypeList, productList);
		FunctionaryDao.getAll(context, functionaryList);
		ClientDao.getAll(context, clientList);
		TableDao.getAll(context, clientList, functionaryList, tableList);
		BillDao.getAll(context, functionaryList, tableList, billList);
		ComplementDao.getAll(context, complementList);
		PoiDao.getAll(context, functionaryList, poiList);

		app.createProductList(productList);
		app.createProductTypeList(productTypeList);

		app.createBillList(billList);
		app.createTableList(tableList);
		app.createComplementList(complementList);
		app.createFunctionaryList(functionaryList);
		app.createPoiList(poiList);
		app.createClientList(clientList);

		app.setFunctionary(context, functionaryList);

		long init = System.currentTimeMillis();
		BillDao.syncBillWithTable(context);
		app.setBill(getBillId(context));
		long end = System.currentTimeMillis();
		Log.d(TAG, "syncBillWithTable: " + (end - init));

		long finalEnd = System.currentTimeMillis();
		Log.d(TAG, "loadFromDb: " + (finalEnd - finalInit));
	}

	public static String getBillId(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(context.getResources().getString(R.string.bill_key), null);
	}

	public static void loadTablesFromDb(Context context){
		QuiosgramaApp app = (QuiosgramaApp) context.getApplicationContext();

		ArrayList<Functionary> functionaryList = new ArrayList<Functionary>();
		ArrayList<Table> tableList = new ArrayList<Table>();
		ArrayList<Client> clientList = new ArrayList<Client>();
		ArrayList<Bill> billList = new ArrayList<Bill>();
		ArrayList<Poi> poiList = new ArrayList<Poi>();

		FunctionaryDao.getAll(context, functionaryList);
		ClientDao.getAll(context, clientList);
		TableDao.getAll(context, clientList, functionaryList, tableList);
		BillDao.getAll(context, functionaryList, tableList, billList);
		PoiDao.getAll(context, functionaryList, poiList);

		app.createFunctionaryList(functionaryList);
		app.createTableList(tableList);
		app.createClientList(clientList);
		app.createBillList(billList);
		app.createPoiList(poiList);

		BillDao.syncBillWithTable(context);
		app.setBill(getBillId(context));
	}

	private static void buildProductTypeImagesValue(ArrayList<ProductType> productTypeList) {
		for (ProductType productType : productTypeList) {
			productType.imageInfoId = AndroidUtil.buildImagesValue(productType.imageInfo);
		}
	}

	private void validatePreviousBill(Bill previousBill){
		if(previousBill != null) {
			HashSet<ProductRequest> allPreviousProdReqList = ProductRequestDao.getByBill(this, previousBill);
			int count = 0;
			for (ProductRequest previousProdReq : allPreviousProdReqList) {
				if (!previousProdReq.valid) ++count;
			}

			if (count == allPreviousProdReqList.size() && !allPreviousProdReqList.isEmpty()) {
				previousBill.billTime = new Date();
				previousBill.paidTime = new Date();
				previousBill.closeTime = new Date();
				previousBill.waiterCloseTable = app.getFunctionarySelected();

				BillDao.update(this, previousBill);
			}
		}
    }

	public static void buildNotification(Context context) {
		QuiosgramaApp app = (QuiosgramaApp) context.getApplicationContext();
		Functionary functionary = app.getFunctionarySelected();
		if(functionary != null){
			String title = null;
			String text = null;
			if (functionary.adminFlag == Functionary.ADMIN) {
				int prodReqCount = ProductRequestDao.getCountByStatusSent(context);
				int billCount = BillDao.getCountByBillClosed(context);

				title = context.getResources().getString(R.string.app_name);
				if (prodReqCount > 0 && billCount > 0) {
					String prodReqString = String.format(context.getResources().getString(R.string.notification_request), prodReqCount);
					String billString = String.format(context.getResources().getString(R.string.notification_bill), billCount);
					text = String.format("%s e %s", prodReqString, billString);
				} else if (prodReqCount > 0) {
					text = String.format(context.getResources().getString(R.string.notification_request), prodReqCount);
				} else if (billCount > 0) {
					text = String.format(context.getResources().getString(R.string.notification_bill), billCount);
				} else {
					AndroidUtil.clearNotofication(context, 2);
				}
			}
			else{

			}

			if (text != null && title != null) AndroidUtil.createNotification(context, 2, title, text);
		}
	}
}
