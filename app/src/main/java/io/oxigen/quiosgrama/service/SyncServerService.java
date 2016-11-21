package io.oxigen.quiosgrama.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;

import io.oxigen.quiosgrama.Amount;
import io.oxigen.quiosgrama.Bill;
import io.oxigen.quiosgrama.Client;
import io.oxigen.quiosgrama.Complement;
import io.oxigen.quiosgrama.Container;
import io.oxigen.quiosgrama.Device;
import io.oxigen.quiosgrama.Functionary;
import io.oxigen.quiosgrama.ObjectContainer;
import io.oxigen.quiosgrama.Poi;
import io.oxigen.quiosgrama.Product;
import io.oxigen.quiosgrama.ProductRequest;
import io.oxigen.quiosgrama.ProductType;
import io.oxigen.quiosgrama.Push;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.Request;
import io.oxigen.quiosgrama.Table;
import io.oxigen.quiosgrama.activity.MainActivity;
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
import io.oxigen.quiosgrama.data.DataProviderContract;
import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.fragment.MapPagerFragment;
import io.oxigen.quiosgrama.fragment.TableRequestFragment;
import io.oxigen.quiosgrama.util.AndroidUtil;
import io.oxigen.quiosgrama.util.DateDeserializer;

public class SyncServerService extends IntentService {
	private static final String TAG = "SyncServerService";

	public static final int GET_OBJECT_CONTAINER = 1;
	public static final int SEND_PRODUCT_REQUEST = 3;
	public static final int SEND_REGISTRATION_ID = 4;
	public static final int SEND_TABLE = 5;
	public static final int SEND_DESYNCHRONIZED_DATA = 6;
	public static final int SEND_POI = 7;
	public static final int SEND_BILL = 8;
	public static final int SEND_AMOUNT = 9;
	public static final int GET_SHORT_OBJECT_CONTAINER = 10;

//	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private static final String PROPERTY_REG_ID = "registration_id";
	public static final String PROPERTY_REG_IP = "registration_ip";
	private static final String PROPERTY_APP_VERSION = "appVersion";

	QuiosgramaApp app;
	private GoogleCloudMessaging gcm;
	private String regid;
	private Bundle mData;

	public SyncServerService(){
		super(SyncServerService.class.getName());
	}
	
	public SyncServerService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		app = (QuiosgramaApp) getApplication();
		mData = intent.getExtras();
		int method = mData.getInt(KeysContract.METHOD_KEY);

		if(method == 0)
			throw new IllegalArgumentException("Intent sem method key: use os valores statics da classe SyncServerService");

		boolean shortObject = false;
		switch (method) {
			case GET_SHORT_OBJECT_CONTAINER:
				shortObject = true;
			case GET_OBJECT_CONTAINER:
				Intent data = new Intent(MainActivity.RECEIVER_FILTER);
				if (!shortObject) {
					data.putExtra(KeysContract.RESULT_SYNC_KEY, MainActivity.RESULT_START_SYNC);
					sendBroadcast(data);
				}

				if (!shortObject) {
					verifyRegistrationId();
				}
				getObjectContainer(shortObject);

				data.putExtra(KeysContract.RESULT_SYNC_KEY, MainActivity.RESULT_SUCCESS_SYNC);
				sendBroadcast(data);

				Intent service = new Intent(this, PrintService.class);
				service.putExtra(KeysContract.METHOD_KEY, PrintService.PRINT_REQUEST);
				startService(service);
				break;

			case SEND_PRODUCT_REQUEST:
				sendProductRequest(this, true);
				break;

			case SEND_REGISTRATION_ID:
				verifyRegistrationId();
				break;

			case SEND_TABLE:
				sendTable();
				break;

			case SEND_DESYNCHRONIZED_DATA:
				sendDesynchronizedData();
				break;

			case SEND_POI:
				sendPoi();
				break;

			case SEND_BILL:
				sendBill();
				break;

			case SEND_AMOUNT:
				sendAmount();
				break;

			default:
				throw new IllegalArgumentException("Método invalido: use os valores statics da classe SyncServerService");
		}

	}

	private void sendAmount() {
		try{
			HashSet<Amount> amountList = AmountDao.getBySyncStatus(this);

			if(amountList != null && !amountList.isEmpty()){
				Log.i(TAG, getResources().getString(R.string.sending_amount_message));

				Gson gson = new GsonBuilder()
						.excludeFieldsWithoutExposeAnnotation()
						.registerTypeAdapter(Date.class, new DateDeserializer())
						.create();
				Push<Amount> push = new Push<>(AndroidUtil.getImei(this), amountList);
				String jsonString = gson.toJson(push);
				boolean sended = HttpService.post(this, getResources().getString(R.string.send_amount_url), jsonString,
						getResources().getString(R.string.amount_sended_error_message));

				if(sended){
					for (Amount amount : amountList) {
						amount.syncStatus = 0;
						AmountDao.update(this, amount);
					}

					Log.i(TAG, getResources().getString(R.string.amount_sended_message));
				}
			}
		}catch(Exception e){
			Log.e(TAG, "Erro ao mandar o Amount");

			sendBroadcastMessageError(this, getResources().getString(R.string.amount_sended_error_message));
		}
	}

	private void sendPoi() {
		try{
			HashSet<Poi> poiList = PoiDao.getBySyncStatus(this);
			
			if(poiList != null && !poiList.isEmpty()){
				Log.i(TAG, getResources().getString(R.string.sending_poi_message));

				Gson gson = new GsonBuilder()
						.excludeFieldsWithoutExposeAnnotation()
						.registerTypeAdapter(Date.class, new DateDeserializer())
						.create();
				Push<Poi> push = new Push<Poi>(AndroidUtil.getImei(this), poiList);
				String jsonString = gson.toJson(push);
				boolean sended = HttpService.post(this, getResources().getString(R.string.send_poi_url), jsonString,
						getResources().getString(R.string.poi_sended_error_message));
				
				if(sended){
					for (Poi poi : poiList) {
						poi.syncStatus = 0;
						PoiDao.update(this, poi);
					}
					
					Log.i(TAG, getResources().getString(R.string.poi_sended_message));
				}
			}
		}catch(Exception e){
			Log.e(TAG, "Erro ao mandar o POI");

			sendBroadcastMessageError(this, getResources().getString(R.string.poi_sended_error_message));
		}
	}

	private void sendRegistrationId(){
		String ipAddress = AndroidUtil.getIpAddress();
		String imei = mData.getString(KeysContract.IMEI_KEY);
		String imeiRegistration = mData.getString(KeysContract.IMEI_REGISTRATION_KEY);
		if(imeiRegistration == null) imeiRegistration = AndroidUtil.getImei(this);

		if(imei == null){
			imei = AndroidUtil.getImei(this);
		}

		Device device = new Device(imeiRegistration, regid, ipAddress, DataBaseService.getBillId(this));

		HashSet<Device> deviceList = new HashSet<>();
		deviceList.add(device);
		Push<Device> push = new Push<>(imei, deviceList);
		Gson gson = new Gson();
		String json = gson.toJson(push);

		Log.d(TAG, "Enviado Registration: " + json);
		boolean sended = HttpService.post(this, getResources().getString(R.string.send_registration_url), json, "");
		if (sended) {
			storeRegistrationIp(ipAddress);
		}
	}
	
	/**
	 * Envia a mesa alterada no mapa para o webService
	 */
	private void sendTable() {
		try{
			HashSet<Table> tableList = TableDao.getBySyncStatus(this);
			
			if(tableList != null && !tableList.isEmpty()){
				Log.i(TAG, getResources().getString(R.string.sending_table_message));

				Gson gson = new GsonBuilder()
						.excludeFieldsWithoutExposeAnnotation()
						.registerTypeAdapter(Date.class, new DateDeserializer())
						.create();
				Push<Table> push = new Push<Table>(AndroidUtil.getImei(this), tableList);
				String jsonString = gson.toJson(push);
				boolean sended = HttpService.post(this, getResources().getString(R.string.send_table_url), jsonString,
						getResources().getString(R.string.table_sended_error_message));
				
				if(sended){
					for (Table table : tableList) {
						table.syncStatus = 0;
						TableDao.update(this, table);
					}
					
					Log.i(TAG, getResources().getString(R.string.table_sended_message));
				}
			}
		}catch(Exception e){
			Log.e(TAG, "Erro ao mandar a mesa");
			sendBroadcastMessageError(this, getResources().getString(R.string.table_sended_error_message));
		}
	}

	public static void sendProductRequest(Context context, boolean print) {
		try{
			HashSet<ProductRequest> prodReqList = ProductRequestDao.getBySyncStatus(context);

			if(prodReqList != null && !prodReqList.isEmpty()){

				Log.i(TAG, context.getResources().getString(R.string.sending_request_message));
				
				Gson gson = new GsonBuilder()
						.excludeFieldsWithoutExposeAnnotation()
						.registerTypeAdapter(Date.class, new DateDeserializer())
						.create();
				Push<ProductRequest> push = new Push<ProductRequest>(AndroidUtil.getImei(context), prodReqList);
				String jsonString = gson.toJson(push);
				Log.i(TAG, jsonString);
				boolean sended = HttpService.post(context, context.getResources().getString(R.string.send_product_request_url), jsonString,
						context.getResources().getString(R.string.request_sended_error_message));
				
				if(sended){
					for (ProductRequest productRequest : prodReqList) {
						productRequest.request.syncStatus = KeysContract.SYNCHRONIZED_STATUS_KEY;
						productRequest.syncStatus = KeysContract.SYNCHRONIZED_STATUS_KEY;
						RequestDao.update(context, productRequest.request);
						ProductRequestDao.update(context, productRequest);
					}
					
					Log.i(TAG, context.getResources().getString(R.string.request_sended_message));
					if(print) {
						Intent intent = new Intent(context, PrintService.class);
						intent.putExtra(KeysContract.METHOD_KEY, PrintService.PRINT_REQUEST);
						context.startService(intent);
					}
				}
			}
		}
		catch (Exception e) {
			Log.e(TAG, "Erro ao mandar o ProductRequest");
			sendBroadcastMessageError(context, context.getResources().getString(R.string.request_sended_error_message));
		}
	}

	/**
	 * Envia a conta quando for fechada
	 */
	private void sendBill(){
		try{
			HashSet<Bill> billList = BillDao.getBySyncStatus(this);
			
			if(billList != null && !billList.isEmpty()){
				Log.i(TAG, getResources().getString(R.string.sending_bill_message));

				Gson gson = new GsonBuilder()
						.excludeFieldsWithoutExposeAnnotation()
						.registerTypeAdapter(Date.class, new DateDeserializer())
						.create();
				Push<Bill> push = new Push<Bill>(AndroidUtil.getImei(this), billList);
				String jsonString = gson.toJson(push);
				boolean sended = HttpService.post(this, getResources().getString(R.string.send_bill_url), jsonString,
						getResources().getString(R.string.bill_sended_error_message));
				
				if(sended){
					for (Bill bill : billList) {
						bill.syncStatus = 0;
						BillDao.update(this, bill);

						if(bill.closeTime != null && bill.paidTime == null) {
							Intent service = new Intent(this, PrintService.class);
							service.putExtra(KeysContract.METHOD_KEY, PrintService.PRINT_BILL);
							service.putExtra(KeysContract.BILL_KEY, bill);
							startService(service);
						}
					}

					Log.i(TAG, getResources().getString(R.string.bill_sended_message));
				}
			}
		}catch(Exception e){
			Log.e(TAG, "Erro ao mandar a conta");
			sendBroadcastMessageError(this, getResources().getString(R.string.bill_sended_error_message));
		}
	}

	private void getObjectContainer(boolean shortObject){
		long init = System.currentTimeMillis();
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		Push<Table> push = new Push<>(AndroidUtil.getImei(this), null);
		String jsonString = gson.toJson(push);

		long initRequest = System.currentTimeMillis();
		ObjectContainer obj = HttpService.getWithPost(this, ObjectContainer.class,
				getResources().getString(R.string.get_object_container), jsonString, HttpService.CONTENT_TYPE_JSON);
		long endRequest = System.currentTimeMillis();
		Log.d(TAG, "HttpService get: " + (endRequest - initRequest));

		if(obj != null && obj.container != null){
			QuiosgramaApp.setKiosk(obj.kiosk);
			deleteAllSynchronizedData(shortObject);
			insertObjectContainer(obj.container);
			if(QuiosgramaApp.firstTime){
				final SharedPreferences prefs = getGCMPreferences();
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean(getResources().getString(R.string.first_time_key), false);
				editor.commit();
				QuiosgramaApp.firstTime = false;
			}
		}

		DataBaseService.loadFromDb(this);
		sendBroadcast(new Intent(MapPagerFragment.RECEIVER_FILTER));
		sendBroadcast(new Intent(TableRequestFragment.RECEIVER_FILTER));
		long end = System.currentTimeMillis();
		Log.d(TAG, "getObjectContainer: " + (end - init));
	}

	private void insertObjectContainer(Container container) {
		ArrayList<ProductType> productTypeList = container.productTypeList;
		ArrayList<Product> productList = container.productList;
		ArrayList<Functionary> functionaryList = container.functionaryList;
		ArrayList<Table> tableList = container.tableList;
		ArrayList<Bill> billList = container.billList;
		ArrayList<Request> requestList = container.requestList;
		ArrayList<ProductRequest> productRequestList = container.productRequestList;
		ArrayList<Complement> complementList = container.complementList;
		ArrayList<Poi> poiList = container.poiList;
		ArrayList<Client> clientList = container.clientList;

		long init = System.currentTimeMillis();
		long finalInit = System.currentTimeMillis();
		app.setFunctionary(this, functionaryList);

		if (functionaryList != null && !functionaryList.isEmpty()) {
			FunctionaryDao.insertOrUpdate(this, functionaryList);
		}
		long end = System.currentTimeMillis();
		Log.d(TAG, "functionaryList: " + (end - init));
		init = end;

		if ((productTypeList != null && productList != null) && (!productTypeList.isEmpty() || !productList.isEmpty())) {
			ProductTypeDao.insertOrUpdate(this, productTypeList);
			end = System.currentTimeMillis();
			Log.d(TAG, "productTypeList: " + (end - init));
			init = end;

			ProductDao.insertOrUpdate(this, productList);
			end = System.currentTimeMillis();
			Log.d(TAG, "productList: " + (end - init));
			init = end;
		}

		if (tableList != null) {
			TableDao.insertOrUpdate(this, tableList);
		}
		end = System.currentTimeMillis();
		Log.d(TAG, "tableList: " + (end - init));
		init = end;

		if (poiList != null) {
			PoiDao.insertOrUpdate(this, poiList);
		}
		end = System.currentTimeMillis();
		Log.d(TAG, "poiList: " + (end - init));
		init = end;

		if (billList != null) {
			BillDao.insertOrUpdate(this, billList);
		}
		end = System.currentTimeMillis();
		Log.d(TAG, "billList: " + (end - init));
		init = end;

		if (requestList != null) {
			RequestDao.insertOrUpdate(this, requestList);
		}
		end = System.currentTimeMillis();
		Log.d(TAG, "requestList: " + (end - init));
		init = end;

		if (complementList != null) {
			ComplementDao.insertOrUpdate(this, complementList);
		}
		end = System.currentTimeMillis();
		Log.d(TAG, "complementList: " + (end - init));
		init = end;

		if (productRequestList != null) {
			ProductRequestDao.insertOrUpdate(this, productRequestList);
		}
		end = System.currentTimeMillis();
		Log.d(TAG, "productRequestList: " + (end - init));
		init = end;

		if (clientList != null) {
			for (Client client : clientList) {
				ClientDao.insertOrUpdate(this, client);
			}
		}
		end = System.currentTimeMillis();
		Log.d(TAG, "clientList: " + (end - init));
		init = end;
		long finalEnd = System.currentTimeMillis();
		Log.d(TAG, "insertObjectContainer: " + (finalEnd - finalInit));
	}
	
	/**
	 * Obtem os dados nao sincronizados do servidor e armazena temporiamente, deleta todos os dados,
	 * depois inclui os dados nao sincrozinados no banco novamente.
	 * Por fim, a criado uma nova instancia desse service para enviar esses dados para o servidor
	 * OBS: Nao sao incluidos dados que nao sao permitidos visualizaaao antes da data corrente.
	 * @param shortObject
	 */
	private void deleteAllSynchronizedData(boolean shortObject) {
		try{
			HashSet<ProductRequest> prodReqSet = ProductRequestDao.getBySyncStatus(this);
			HashSet<Table> tableSet = TableDao.getBySyncStatus(this);
			HashSet<Bill> billSet = BillDao.getBySyncStatus(this);
			HashSet<Poi> poiSet = PoiDao.getBySyncStatus(this);
			HashSet<Amount> amountSet = AmountDao.getBySyncStatus(this);

			getContentResolver().delete(DataProviderContract.AmountTable.TABLE_URI, null, null);
			getContentResolver().delete(DataProviderContract.ProductRequestTable.TABLE_URI, null, null);
			getContentResolver().delete(DataProviderContract.RequestTable.TABLE_URI, null, null);
			getContentResolver().delete(DataProviderContract.BillTable.TABLE_URI, null, null);
			getContentResolver().delete(DataProviderContract.TableTable.TABLE_URI, null, null);
			getContentResolver().delete(DataProviderContract.ProductTypeTable.TABLE_URI, null, null);
			getContentResolver().delete(DataProviderContract.ProductTable.TABLE_URI, null, null);
			getContentResolver().delete(DataProviderContract.FunctionaryTable.TABLE_URI, null, null);
			getContentResolver().delete(DataProviderContract.PoiTable.TABLE_URI, null, null);
			getContentResolver().delete(DataProviderContract.ComplementTable.TABLE_URI, null, null);
			getContentResolver().delete(DataProviderContract.ComplementTypeTable.TABLE_URI, null, null);

			if(tableSet != null){
				TableDao.insertOrUpdate(this, new ArrayList<Table>(tableSet));
			}

			if(prodReqSet != null){
				Calendar currentDate = Calendar.getInstance();
				for (ProductRequest productRequest : prodReqSet) {
					Calendar requestTime = new GregorianCalendar();
					requestTime.setTime(productRequest.request.requestTime);
					//Verifica se o Pedido realizado a de hoje
					if(currentDate.get(Calendar.DAY_OF_MONTH) == requestTime.get(Calendar.DAY_OF_MONTH)
							&& currentDate.get(Calendar.MONTH) == requestTime.get(Calendar.MONTH)
							&& currentDate.get(Calendar.YEAR) == requestTime.get(Calendar.YEAR)){
						BillDao.insertOrUpdate(this, productRequest.request.bill);
						RequestDao.insertOrUpdate(this, productRequest.request);
						ProductRequestDao.insertOrUpdate(this, productRequest);
					}
				}
			}
			
			if(billSet != null){
				Calendar currentDate = Calendar.getInstance();
				for (Bill bill : billSet) {
					Calendar billTime = new GregorianCalendar();
					billTime.setTime(bill.billTime);
					//Verifica se a conta a de hoje
					if(currentDate.get(Calendar.DAY_OF_MONTH) == billTime.get(Calendar.DAY_OF_MONTH)
							&& currentDate.get(Calendar.MONTH) == billTime.get(Calendar.MONTH)
							&& currentDate.get(Calendar.YEAR) == billTime.get(Calendar.YEAR)){
						BillDao.insertOrUpdate(this, bill);
					}
				}
			}
			
			if(poiSet != null){
				PoiDao.insertOrUpdate(this, new ArrayList<Poi>(poiSet));
			}

			if(amountSet != null){
				AmountDao.insert(this, new ArrayList<Amount>(amountSet));
			}
			
			//criado uma nova instancia desse service para enviar os dados nao sincrozinados para o servidor
			if(!shortObject && (!tableSet.isEmpty() || !prodReqSet.isEmpty() || !billSet.isEmpty() || !poiSet.isEmpty() || !amountSet.isEmpty())){
				Intent intent = new Intent(this, SyncServerService.class);
				intent.putExtra(KeysContract.METHOD_KEY, SEND_DESYNCHRONIZED_DATA);
				startService(intent);
			}
		}
		catch (Exception e) {
			Log.e(TAG, "Erro ao incluir dados antigos no sync: " + e.getMessage());
		}
	}
	
	/**
	 * Envia todos os dados que nao estao sincronizados com o servidor (syncStatus = 1)
	 */
	private void sendDesynchronizedData(){
		sendTable();
		sendProductRequest(this, true);
		sendPoi();
		sendBill();
		sendAmount();
	}

	private void verifyRegistrationId(){
		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);
			regid = getRegistrationId(this);

			if (regid.equals("")) {
				register();
			}
		}

		sendRegistrationId();
	}

	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
//				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
//						PLAY_SERVICES_RESOLUTION_REQUEST).show();

				Intent data = new Intent(MainActivity.RECEIVER_FILTER);
				data.putExtra(KeysContract.RESULT_SYNC_KEY, MainActivity.RESULT_NO_GOOGLE_PLAY);
				sendBroadcast(data);
			} else {
				Log.i(TAG, "This device is not supported.");
			}
			return false;
		}
		return true;
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences();
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.equals("")) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing registration ID is not guaranteed to work with
		// the new app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	private String getRegistrationIp() {
		final SharedPreferences prefs = getGCMPreferences();
		String registrationIp = prefs.getString(PROPERTY_REG_IP, "");
			if (registrationIp.equals("")) {
			Log.i(TAG, "Registration not found.");
			return "";
		}

		return registrationIp;
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences() {
		// This sample app persists the registration ID in shared preferences, but
		// how you store the registration ID in your app is up to you.
		return PreferenceManager.getDefaultSharedPreferences(this);
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void register() {
		try {
			if (gcm == null) {
				gcm = GoogleCloudMessaging.getInstance(this);
			}
			regid = gcm.register(this.getResources().getString(R.string.sender_id));
			storeRegistrationId(this, regid);
		} catch (IOException ex) {
			Log.e(TAG + " register()", ex.getMessage());
		}
	}

	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 *
	 * @param context application's context.
	 * @param regId registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences();
		int appVersion = getAppVersion(context);
		Log.i(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	private void storeRegistrationIp(String ip) {
		final SharedPreferences prefs = getGCMPreferences();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_IP, ip);
		editor.commit();
	}

	public static void sendBroadcastMessageError(Context context, String message){
		Intent data = new Intent(MainActivity.RECEIVER_FILTER);
		data.putExtra(KeysContract.RESULT_SYNC_KEY, MainActivity.RESULT_MESSAGE);
		data.putExtra(KeysContract.MESSAGE_KEY, message);
		context.sendBroadcast(data);
	}
}
