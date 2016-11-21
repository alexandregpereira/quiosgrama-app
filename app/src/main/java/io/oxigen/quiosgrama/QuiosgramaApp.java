package io.oxigen.quiosgrama;

import android.accounts.Account;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import io.oxigen.quiosgrama.data.DataProviderContract;
import io.oxigen.quiosgrama.sat.EasySAT;
import io.oxigen.quiosgrama.util.AndroidUtil;

public class QuiosgramaApp extends Application{
	
	public static final String APP_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath() +
			File.separator + "Quiosgrama";
	
	private static ArrayList<Bill> billList;
	private static ArrayList<Table> tableList;
	private static ArrayList<Complement> complementList;
	private static ArrayList<Functionary> functionaryList;
	private static ArrayList<Product> productList;
	private ArrayList<ProductType> productTypeList;
	private static ArrayList<Poi> poiList;
	private static ArrayList<Client> clientList;
	
	private static Functionary functionarySelected;
	private Bill billSelected;

	private static Kiosk kiosk;
	private static final Object kioskLock = new Object();
	private static final Object complementListLock = new Object();
	private static final Object billSelectedLock = new Object();
	private static final Object billListLock = new Object();
	private static final Object productListLock = new Object();
	private static final Object functionarySelectedLock = new Object();

	public static TableObserver observer;
	
	// A content URI for the content provider's data table
    Uri mUri;
    // A content resolver for accessing the provider
    ContentResolver mResolver;
	public static boolean connectionFailed;
	public static boolean firstTime;
	public EasySAT easySat;

	@Override
	public void onCreate() {
		super.onCreate();
		
		new File(APP_FOLDER).mkdir();
		
		if(billList == null)
			billList = new ArrayList<>();
		
		tableList = new ArrayList<>();
		complementList = new ArrayList<>();
		poiList = new ArrayList<>();
		clientList = new ArrayList<>();
		
		// Get the content resolver object for your app
        mResolver = getContentResolver();
        // Construct a URI that points to the content provider data table
        mUri = new Uri.Builder()
                  .scheme(DataProviderContract.SCHEME)
                  .authority(DataProviderContract.AUTHORITY)
                  .path(DataProviderContract.RequestTable.TABLE_NAME)
                  .build();

		easySat = new EasySAT((UsbManager) getSystemService(Context.USB_SERVICE));
        /*
         * Create a content observer object.
         * Its code does not mutate the provider, so set
         * selfChange to "false"
         */
//        observer = new TableObserver();
        /*
         * Register the observer for the data table. The table's path
         * and any of its subpaths trigger the observer.
         */
//        mResolver.registerContentObserver(mUri, true, observer);
//        mResolver.registerContentObserver(DataProviderContract.TableTable.TABLE_URI, true, new TableNameObserver(null, this));
	}
	
	public static Bill searchBill(int tableNumber){
		synchronized (billListLock) {
			for (Bill bill : billList) {
				if (bill.table != null && bill.table.number == tableNumber) {
					return bill;
				}
			}

			return null;
		}
	}

	public ProductType searchProductType(long id){
		for (ProductType type : getProductTypeList()) {
			if(type.id == id)
				return type;
		}

		return null;
	}

	public static Table searchTable(int tableNumber) {
		for (Table table : tableList) {
			if(table.number == tableNumber)
				return table;
		}
		
		return null;
	}

	public static Product searchProduct(long code){
		synchronized (productListLock){
			if(productList != null) {
				for (Product product : productList) {
					if (product.code == code) {
						return product;
					}
				}
			}

			return null;
		}
	}
	
	public synchronized void setFunctionary(Context context, ArrayList<Functionary> functionaryList){
		synchronized (functionarySelectedLock) {
			functionarySelected = null;
			if (functionaryList != null && !functionaryList.isEmpty()) {
				String deviceId = AndroidUtil.getImei(context);
				for (Functionary functionary : functionaryList) {
					if (functionary.imei != null && functionary.imei.equals(deviceId)) {
						functionarySelected = functionary;
					}
				}
			}
		}
	}
	
	public synchronized Functionary getFunctionarySelected(){
		synchronized (functionarySelectedLock) {
			return functionarySelected;
		}
	}

	public synchronized int getFunctionarySelectedType(){
		synchronized (functionarySelectedLock) {
			return functionarySelected != null ? functionarySelected.adminFlag : -1;
		}
	}

	public synchronized void setBill(String billId){
		synchronized (billSelectedLock) {
			billSelected = null;
			if (billId != null) {
				ArrayList<Bill> billList = getBillList();
				if (billList != null && !billList.isEmpty()) {
					for (Bill bill : billList) {
						if (bill.id != null && bill.id.equals(billId)) {
							billSelected = bill;
							break;
						}
					}
				}
			}
		}
	}

	public synchronized Bill getBillSelected(){
		synchronized (billSelectedLock) {
			return billSelected;
		}
	}

	public static void setKiosk(Kiosk kioskTmp) {
		synchronized (kioskLock) {
			kiosk = kioskTmp;
		}
	}

	public static String getLicence(){
		synchronized (kioskLock) {
			return kiosk == null ? "" : kiosk.licence;
		}
	}

	public static String getKioskName(){
		synchronized (kioskLock) {
			return kiosk == null ? "" : kiosk.name;
		}
	}

	public static Kiosk getKiosk(){
		synchronized (kioskLock) {
			return new Kiosk(kiosk);
		}
	}
	
	public class TableObserver extends ContentObserver {
		
        public TableObserver() {
			super(null);
		}
		/*
         * Define a method that's called when data in the
         * observed content provider changes.
         * This method signature is provided for compatibility with
         * older platforms.
         */
        @Override
        public void onChange(boolean selfChange) {
            /*
             * Invoke the method signature available as of
             * Android platform version 4.1, with a null URI.
             */
            onChange(selfChange, null);
        }
        /*
         * Define a method that's called when data in the
         * observed content provider changes.
         */
        @Override
        public void onChange(boolean selfChange, Uri changeUri) {
            /*
             * Ask the framework to run your sync adapter.
             * To maintain backward compatibility, assume that
             * changeUri is null.*/
        	Account account = new Account(DataProviderContract.ACCOUNT_NAME, 
        			DataProviderContract.ACCOUNT_TYPE);
        	
        	if (ContentResolver.isSyncPending(account, DataProviderContract.AUTHORITY)  ||
        		    ContentResolver.isSyncActive(account, DataProviderContract.AUTHORITY)) {
        		
        		Log.i("QuiosgramaApp", "Sync is running");
//        		  ContentResolver.cancelSync(account, DataProviderContract.AUTHORITY);
        	}
        	
        	Bundle b = new Bundle();
            // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
            b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            ContentResolver.requestSync(
            		account, 
            		DataProviderContract.AUTHORITY, b);
        }
    }
	
	public synchronized ArrayList<Bill> getBillList(){
		synchronized (billListLock) {
			return new ArrayList<>(billList);
		}
	}
	
	public synchronized void addOrUpdateBill(Bill bill){
		synchronized (billListLock) {
			int index = billList.indexOf(bill);
			if(index < 0)
				billList.add(bill);
			else{
				if(bill.table.show) {
					billList.set(index, bill);
				}
				else{
					billList.remove(index);
				}
			}
		}
	}
	
	public synchronized void removeBill(Bill bill){
		synchronized (billListLock) {
			int index = billList.indexOf(bill);
			if(index >= 0)
				billList.remove(index);
		}
	}
	
	public synchronized void createBillList(ArrayList<Bill> billListTemp){
		if(billListTemp != null){
			synchronized (billListLock) {
				billList = billListTemp;
			}
		}
	}
	
	public synchronized void sortBillList(){
		synchronized (billListLock) {
			Collections.sort(billList);
		}
	}
	
	public synchronized ArrayList<Table> getTableList(){
		synchronized (tableList) {
			return new ArrayList<Table>(tableList);
		}
	}
	
	public synchronized void addTable(Table table){
		if(table.show) {
			synchronized (tableList) {
				tableList.add(table);
			}
		}
	}
	
	public synchronized void updateTable(int index, Table table){
		synchronized (tableList) {
			if(table.show) {
				tableList.set(index, table);
			}
			else{
				tableList.remove(index);
			}
		}
	}
	
	public synchronized void createTableList(ArrayList<Table> tableListTemp){
		if(tableListTemp != null){
			synchronized (tableList) {
				tableList = tableListTemp;
			}
		}
	}
	
	public synchronized void sortTableList(){
		synchronized (tableList) {
			Collections.sort(tableList);
		}
	}
	
	public synchronized void addComplement(Complement complement){
		synchronized (complementListLock) {
			complementList.add(complement);
		}
	}

	public synchronized void updateComplement(int index, Complement complement){
		synchronized (complementListLock) {
			complementList.set(index, complement);
		}
	}

	public synchronized ArrayList<Complement> getComplementList(){
		synchronized (complementListLock){
			return complementList;
		}
	}

	public synchronized void createComplementList(ArrayList<Complement> complementListTemp){
		if(complementListTemp != null){
			synchronized (complementListLock) {
				complementList = complementListTemp;
			}
		}
	}

	public synchronized ArrayList<Functionary> getFunctionaryList(){
		return functionaryList;
	}
	
	public synchronized void addFunctionary(Functionary functionary){
		getFunctionaryList().add(functionary);
	}
	
	public synchronized void updateFunctionary(int index, Functionary functionary){
		getFunctionaryList().set(index, functionary);
	}
	
	public synchronized void createFunctionaryList(ArrayList<Functionary> functionaryListTemp){
		if(functionaryListTemp != null){
			functionaryList = functionaryListTemp;
		}
	}
	
	public synchronized ArrayList<Product> getProductList(){
		synchronized (productListLock) {
			return productList;
		}
	}
	
	public synchronized void addProduct(Product product){
		getProductList().add(product);
	}
	
	public synchronized void updateProduct(int index, Product product){
		getProductList().set(index, product);
	}
	
	public synchronized void createProductList(ArrayList<Product> productListTemp){
		synchronized (productListLock) {
			if (productListTemp != null) {
				productList = productListTemp;
			}
		}
	}
	
	public synchronized ArrayList<ProductType> getProductTypeList(){
		if(productTypeList != null) {
			synchronized (productTypeList) {
				return new ArrayList<>(productTypeList);
			}
		}

		return null;
	}
	
	public synchronized void addProductType(ProductType productType){
		getProductTypeList().add(productType);
	}
	
	public synchronized void updateProductType(int index, ProductType productType){
		getProductTypeList().set(index, productType);
	}
	
	public synchronized void createProductTypeList(ArrayList<ProductType> productTypeListTemp){
		if(productTypeListTemp != null){
			if(productTypeList != null) {
				synchronized (productTypeList) {
					productTypeList = productTypeListTemp;
				}
			}
			else{
				productTypeList = productTypeListTemp;
			}
		}
	}
	
	
	public synchronized ArrayList<Poi> getPoiList(){
		synchronized (poiList) {
			return new ArrayList<Poi>(poiList);
		}
	}
	
	public synchronized void addPoi(Poi poi){
		synchronized (poiList) {
			poiList.add(poi);
		}
	}
	
	public synchronized void updatePoi(int index, Poi poi){
		synchronized (poiList) {
			poiList.set(index, poi);
		}
	}
	
	public synchronized void createPoiList(ArrayList<Poi> poiListTemp){
		if(poiListTemp != null){
			synchronized (poiList) {
				poiList = poiListTemp;
			}
		}
	}
	
	public synchronized void sortPoiList(){
		synchronized (poiList) {
			Collections.sort(poiList);
		}
	}
	
	public synchronized ArrayList<Client> getClientList(){
		return clientList;
	}
	
	public synchronized void addClient(Client client){
		getClientList().add(client);
	}
	
	public synchronized void updateClient(int index, Client client){
		getClientList().set(index, client);
	}
	
	public synchronized void createClientList(ArrayList<Client> clientListTemp){
		if(clientListTemp != null){
			clientList = clientListTemp;
		}
	}
	
//	public static Account CreateSyncAccount(Context context) {
//        // Create the account type and default account
//        Account newAccount = new Account(
//                DataProviderContract.ACCOUNT, DataProviderContract.ACCOUNT_TYPE);
//        // Get an instance of the Android account manager
//        AccountManager accountManager =
//                (AccountManager) context.getSystemService(
//                        ACCOUNT_SERVICE);
//        /*
//         * Add the account and account type, no password or user data
//         * If successful, return the Account object, otherwise report an error.
//         */
//        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
//            /*
//             * If you don't set android:syncable="true" in
//             * in your <provider> element in the manifest,
//             * then call context.setIsSyncable(account, AUTHORITY, 1)
//             * here.
//             */
//        	return newAccount;
//        } else {
//            /*
//             * The account exists or some other error occurred. Log this, report it,
//             * or handle it internally.
//             */
//        	return null;
//        }
//    }
	
}
