package io.oxigen.quiosgrama.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import io.oxigen.quiosgrama.Bill;
import io.oxigen.quiosgrama.Functionary;
import io.oxigen.quiosgrama.Poi;
import io.oxigen.quiosgrama.Product;
import io.oxigen.quiosgrama.ProductRequest;
import io.oxigen.quiosgrama.ProductType;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.Table;
import io.oxigen.quiosgrama.activity.MainActivity;
import io.oxigen.quiosgrama.dao.BillDao;
import io.oxigen.quiosgrama.dao.PoiDao;
import io.oxigen.quiosgrama.dao.ProductDao;
import io.oxigen.quiosgrama.dao.ProductTypeDao;
import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.fragment.MapPagerFragment;
import io.oxigen.quiosgrama.fragment.TableRequestFragment;
import io.oxigen.quiosgrama.receiver.GcmBroadcastReceiver;
import io.oxigen.quiosgrama.util.AndroidUtil;
import io.oxigen.quiosgrama.util.DateDeserializer;

public class GcmIntentService extends IntentService {
	private static final String TAG = "GcmIntentService";
	public static final int SYNC_ALL_RESULT_CODE = -1;
	public static final int PRODUCT_REQUEST_RESULT_CODE = 1;
	public static final int TABLE_RESULT_CODE = 2;
	public static final int POI_RESULT_CODE = 3;
	public static final int BILL_RESULT_CODE = 4;
	public static final int PRODUCT_TYPE_RESULT_CODE = 5;
	public static final int PRODUCT_RESULT_CODE = 6;
	public static final int CLIENT_RESULT_CODE = 7;
	public static final int COMPLEMENT_RESULT_CODE = 8;
	public static final int FUNCTIONARY_RESULT_CODE = 10;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
            	SyncServerService.sendBroadcastMessageError(this, "Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
            	SyncServerService.sendBroadcastMessageError(this, "Deleted messages on server: " + extras.toString());
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Log.i(TAG, "Received: " + extras.toString());
                insertData(extras);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void insertData(Bundle extras) {
    	try{
    		Integer resultCode = Integer.valueOf(extras.getString(KeysContract.GCM_RESULT_CODE_KEY));
			String dataJson = extras.getString(KeysContract.GCM_DATA_JSON_KEY);
			String licence = extras.getString(KeysContract.LICENCE_KEY);

			insertData(this, resultCode, dataJson, licence);
    	} catch(NumberFormatException e){
    		Log.e(TAG, e.getMessage());
    	}
	}

    public static synchronized void insertData(Context context, Integer resultCode, String dataJson, String licence){
		if(licence.equals(QuiosgramaApp.getLicence())) {
			Gson gson = new GsonBuilder()
					.registerTypeAdapter(Date.class, new DateDeserializer())
					.excludeFieldsWithoutExposeAnnotation().create();

			if (resultCode == PRODUCT_REQUEST_RESULT_CODE) {
				ArrayList<ProductRequest> prodReqList = gson.fromJson(dataJson,
						new TypeToken<ArrayList<ProductRequest>>() {
						}.getType());

				if (prodReqList != null) {
					try {
						DataBaseService.insertProductRequest(context, prodReqList);
						DataBaseService.loadFromDb(context);

						Log.i(TAG, context.getResources().getString(R.string.gcm_insert_success));

						buildNotification(context, prodReqList);
						context.sendBroadcast(new Intent(TableRequestFragment.RECEIVER_FILTER));
						context.sendBroadcast(new Intent(MapPagerFragment.RECEIVER_FILTER));
					} catch (Exception e) {
						SyncServerService.sendBroadcastMessageError(context, context.getResources().getString(R.string.gcm_insert_error));
					}
				}
			} else if (resultCode == TABLE_RESULT_CODE) {
				ArrayList<Table> tableList = gson.fromJson(dataJson,
						new TypeToken<ArrayList<Table>>() {
						}.getType());

				if (tableList != null) {
					try {
						DataBaseService.insertOrUpdateTable(context, tableList);
						DataBaseService.loadTablesFromDb(context);

						Log.i(TAG, context.getResources().getString(R.string.gcm_insert_success));
						context.sendBroadcast(new Intent(MapPagerFragment.RECEIVER_FILTER));
					} catch (Exception e) {
						SyncServerService.sendBroadcastMessageError(context, context.getResources().getString(R.string.gcm_insert_error));
					}
				}
			} else if (resultCode == POI_RESULT_CODE) {
				ArrayList<Poi> poiList = gson.fromJson(dataJson,
						new TypeToken<ArrayList<Poi>>() {
						}.getType());

				if (poiList != null) {
					try {
						PoiDao.insertOrUpdate(context, poiList);
						DataBaseService.loadTablesFromDb(context);

						Log.i(TAG, context.getString(R.string.gcm_insert_success));
						context.sendBroadcast(new Intent(MapPagerFragment.RECEIVER_FILTER));
					} catch (Exception e) {
						SyncServerService.sendBroadcastMessageError(context, context.getResources().getString(R.string.gcm_insert_error));
					}
				}
			} else if (resultCode == BILL_RESULT_CODE) {
				ArrayList<Bill> billList = gson.fromJson(dataJson,
						new TypeToken<ArrayList<Bill>>() {
						}.getType());

				if (billList != null) {
					try {
						BillDao.insertOrUpdate(context, billList);
						DataBaseService.loadTablesFromDb(context);

						Log.i(TAG, context.getResources().getString(R.string.gcm_insert_success));
						context.sendBroadcast(new Intent(MapPagerFragment.RECEIVER_FILTER));

						Intent data = new Intent(MainActivity.RECEIVER_FILTER);
						data.putExtra(KeysContract.RESULT_SYNC_KEY, MainActivity.RESULT_SUCCESS_SYNC);
						context.sendBroadcast(data);
					} catch (Exception e) {
						SyncServerService.sendBroadcastMessageError(context, context.getResources().getString(R.string.gcm_insert_error));
						context.sendBroadcast(new Intent(MapPagerFragment.RECEIVER_FILTER));
					}
				}
			} else if (resultCode == PRODUCT_TYPE_RESULT_CODE) {
				ArrayList<ProductType> typeList = gson.fromJson(dataJson,
						new TypeToken<ArrayList<ProductType>>() {
						}.getType());

				if (typeList != null) {
					try {
						ProductTypeDao.insertOrUpdate(context, typeList);
						DataBaseService.loadFromDb(context);

						Log.i(TAG, context.getResources().getString(R.string.gcm_insert_success));
						Intent intent = new Intent(MainActivity.RECEIVER_FILTER);
						intent.putExtra(KeysContract.RESULT_SYNC_KEY, MainActivity.RESULT_SUCCESS_SYNC);
						context.sendBroadcast(intent);
					} catch (Exception e) {
						SyncServerService.sendBroadcastMessageError(context, context.getResources().getString(R.string.gcm_insert_error));
					}
				}
			} else if (resultCode == SYNC_ALL_RESULT_CODE) {
				Log.i(TAG, context.getResources().getString(R.string.gcm_insert_success));
				Intent intent = new Intent(context, SyncServerService.class);
				intent.putExtra(KeysContract.METHOD_KEY, SyncServerService.GET_OBJECT_CONTAINER);
				context.startService(intent);
			} else if (resultCode == PRODUCT_RESULT_CODE) {
				ArrayList<Product> productList = gson.fromJson(dataJson,
						new TypeToken<ArrayList<Product>>() {
						}.getType());

				if (productList != null) {
					try {
						ProductDao.insertOrUpdate(context, productList);
						DataBaseService.loadFromDb(context);

						Log.i(TAG, context.getResources().getString(R.string.gcm_insert_success));
						Intent intent = new Intent(MainActivity.RECEIVER_FILTER);
						intent.putExtra(KeysContract.RESULT_SYNC_KEY, MainActivity.RESULT_SUCCESS_SYNC);
						context.sendBroadcast(intent);
					} catch (Exception e) {
						SyncServerService.sendBroadcastMessageError(context, context.getResources().getString(R.string.gcm_insert_error));
					}
				}
			} else if (resultCode == CLIENT_RESULT_CODE) {
				Log.i(TAG, context.getResources().getString(R.string.gcm_insert_success));
				Intent intent = new Intent(context, SyncServerService.class);
				intent.putExtra(KeysContract.METHOD_KEY, SyncServerService.GET_OBJECT_CONTAINER);
				context.startService(intent);
			} else if (resultCode == COMPLEMENT_RESULT_CODE) {
				Log.i(TAG, context.getResources().getString(R.string.gcm_insert_success));
				Intent intent = new Intent(context, SyncServerService.class);
				intent.putExtra(KeysContract.METHOD_KEY, SyncServerService.GET_OBJECT_CONTAINER);
				context.startService(intent);
			} else if (resultCode == FUNCTIONARY_RESULT_CODE) {
				Log.i(TAG, context.getResources().getString(R.string.gcm_insert_success));
				Intent intent = new Intent(context, SyncServerService.class);
				intent.putExtra(KeysContract.METHOD_KEY, SyncServerService.GET_OBJECT_CONTAINER);
				context.startService(intent);
			}
		}
    }

	private static void buildNotification(Context context, ArrayList<ProductRequest> prodReqList) {
		QuiosgramaApp app = (QuiosgramaApp) context.getApplicationContext();
		Functionary functionary = app.getFunctionarySelected();
		if(functionary != null && functionary.adminFlag == Functionary.WAITER) {
			HashSet<Bill> billList = new HashSet<>();
			for(ProductRequest prodReq : prodReqList){
				if(prodReq.status == ProductRequest.READY_STATUS && prodReq.request != null && prodReq.request.bill != null) {
					billList.add(prodReq.request.bill);
				}
			}

			if(!billList.isEmpty()){
				String title = context.getResources().getString(R.string.delivery);
				String text = "";
				for (Bill bill : billList){
					text += bill.toString() + ", ";
				}

				text = text.substring(0, text.length() - 2);

				AndroidUtil.createNotification(context, 2, title, text);
			}
		}
	}
}
