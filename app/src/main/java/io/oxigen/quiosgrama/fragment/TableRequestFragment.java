package io.oxigen.quiosgrama.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.TreeSet;

import io.oxigen.quiosgrama.Bill;
import io.oxigen.quiosgrama.Functionary;
import io.oxigen.quiosgrama.ProductRequest;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.Request;
import io.oxigen.quiosgrama.adapter.TableRequestAdapter;
import io.oxigen.quiosgrama.dao.ProductRequestDao;
import io.oxigen.quiosgrama.dao.RequestDao;
import io.oxigen.quiosgrama.data.DataProviderContract;
import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.listener.BackFragmentListener;

public class TableRequestFragment extends Fragment implements 
        LoaderCallbacks<Cursor>, OnItemClickListener, AdapterView.OnItemSelectedListener{

	public static final String RECEIVER_FILTER = "io.oxigen.quiosgrama.filter.TABLE_REQUEST_RECEIVER_FILTER";
	private static final String TAG = "TableRequestFragment";

	private ListView listViewTableRequest;
	private Bill bill;
	private ProgressBar emptyView;
    private TableRequestAdapter tableRequestAdapter;
    
    HashMap<Request, HashSet<ProductRequest>> mapProdRequest;
	private BackFragmentListener backListener;
	private TableRequestFragment tableRequestFragment;
	private RefreshTableRequestReceiver tableRequestReceiver;
	private Spinner spnBillOpened;
	private QuiosgramaApp app;
	private LoaderManager mLoaderManager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		tableRequestFragment = this;
		app = (QuiosgramaApp) getActivity().getApplication();
		mLoaderManager = getLoaderManager();
		
		View v = inflater.inflate(R.layout.fragment_table_request, container, false);
		
		listViewTableRequest = (ListView) v.findViewById(R.id.listViewTableRequest);
		emptyView = (ProgressBar) v.findViewById(R.id.progress);
		spnBillOpened = (Spinner) v.findViewById(R.id.spnBillOpened);

		SpinnerAdapter mSpinnerAdapter = new ArrayAdapter<>(getActivity(),
				android.R.layout.simple_spinner_dropdown_item, getOpenedBill());

		spnBillOpened.setAdapter(mSpinnerAdapter);
		spnBillOpened.setOnItemSelectedListener(this);

		Bundle data = getArguments();
		if(data != null){
			bill = getArguments().getParcelable(KeysContract.BILL_KEY);
			if(bill != null) search(bill);
		}

		listViewTableRequest.setOnItemClickListener(this);

		return v;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	    try {
	        backListener = (BackFragmentListener) activity;
	    } catch (ClassCastException e) {
	        throw new ClassCastException(activity.toString() + " must implement Listener");
	    }
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		backListener.backFragmentPressed();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
		if(bill != null && bill.id != null) {
			return new CursorLoader(
					getActivity(),                          // Context
					DataProviderContract.RequestTable.TABLE_URI,  // Table to query
					RequestDao.REQUEST_PROJECTION,                             // Projection to return
					RequestDao.REQUEST_SELECTION,                                   // No selection clause
					new String[]{String.valueOf(bill.id)},                                 // No selection arguments
					null                                    // Default sort order
			);
		}

		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		TreeSet<Request> requestList = new TreeSet<Request>();
		if(c.moveToFirst()){
			do{
				//					HashMap<Request, ArrayList<ProductRequest>> map = new HashMap<Request, ArrayList<ProductRequest>>();

				String id = c.getString(	
						c.getColumnIndex(DataProviderContract.RequestTable.ID_COLUMN));

				Date requestTime = null;
				try {
					requestTime = new SimpleDateFormat(KeysContract.DATE_FORMAT_KEY, Locale.US)
					.parse( c.getString(
							c.getColumnIndex(DataProviderContract.RequestTable.REQUEST_TIME_COLUMN)));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
                long functionaryId = c.getLong(
						c.getColumnIndex(DataProviderContract.RequestTable.FUNCTIONARY_ID_COLUMN));

                String functionaryName = c.getString(
						c.getColumnIndex(DataProviderContract.FunctionaryTable.NAME_COLUMN));

                int adminFlag = c.getInt(
                        c.getColumnIndex(DataProviderContract.FunctionaryTable.ADMIN_FLAG_COLUMN));

                int syncStatus = c.getInt(
                        c.getColumnIndex(DataProviderContract.RequestTable.SYNC_STATUS_COLUMN));

				Request request = new Request(
                        id,
                        requestTime,
                        bill,
                        new Functionary(functionaryId, functionaryName, adminFlag), syncStatus);

                requestList.add(request);
			}
			while(c.moveToNext());
			
			c.close();
			
			new LoadProductRequestTask().execute(requestList.descendingSet().toArray(new Request[requestList.size()]));

			//				for (Request request : requestList) {
			//					Bundle data = new Bundle();
			//					data.putParcelable(KeysContract.REQUEST_KEY, request);
			//					mLoaderManager().restartLoader(0, data, this);
			//				}
		}
		else{
			listViewTableRequest.setEmptyView(null);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		
	}

	public void search(Bill bill) {
		this.bill = bill;
		listViewTableRequest.setEmptyView(emptyView);
		if(tableRequestAdapter != null){
			listViewTableRequest.setAdapter(null);
		}

		if(mLoaderManager != null) mLoaderManager.restartLoader(0, null, this);
	}
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        Request request = (Request) view.getTag();
        
        HashSet<ProductRequest> prodRequestSet = mapProdRequest.get(request);
        RequestInfoDialogFragment requestInfo = RequestInfoDialogFragment.newInstance(
        		prodRequestSet.toArray(
        				new ProductRequest[prodRequestSet.size()]));
        
		requestInfo.show(getActivity().getSupportFragmentManager(), 
				RequestInfoDialogFragment.class.getName());
    }

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		ArrayList<Bill> billList = getOpenedBill();
		if(billList != null && !billList.isEmpty()){
			try{
				search(billList.get(position));
			} catch(ArrayIndexOutOfBoundsException e){
				Log.e(TAG, "Erro ao selecionar mesa");
			}
		}
	}

	public void onNothingSelected(AdapterView<?> arg0) {}
	
	private class LoadProductRequestTask extends AsyncTask<Request, Void, Boolean>{

		Request[] requestList = null;;
    
		@Override
		protected Boolean doInBackground(Request... requestList) {
			this.requestList = requestList;
            mapProdRequest = new HashMap<Request, HashSet<ProductRequest>>();
			
			for (Request request : requestList) {
				HashSet<ProductRequest> prodReqList = new HashSet<ProductRequest>(ProductRequestDao.getByRequest(getActivity(), request));
                
				if(prodReqList != null)
					mapProdRequest.put(request, prodReqList);
			}
			
			return true;
		}
		
        @Override
        protected void onPostExecute (Boolean result){
            if(result){
                tableRequestAdapter = new TableRequestAdapter(getActivity(),
                                                requestList, 
                                                mapProdRequest);
                                                
                listViewTableRequest.setAdapter(tableRequestAdapter);
            }
        }
	}

	private ArrayList<Bill> getOpenedBill() {
		if(app.getBillList() != null){
			ArrayList<Bill> openedBillList = new ArrayList<Bill>();
			for (Bill bill : app.getBillList()) {
				if(bill.openTime != null && bill.paidTime == null){
					openedBillList.add(bill);
				}
			}
			return openedBillList;
		}
		return null;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if(tableRequestReceiver == null)
			tableRequestReceiver = new RefreshTableRequestReceiver();
		getActivity().registerReceiver(tableRequestReceiver, new IntentFilter(RECEIVER_FILTER));
	}
	
	@Override
	public void onPause() {
		getActivity().unregisterReceiver(tableRequestReceiver);
		super.onPause();
	}
	
	private class RefreshTableRequestReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(tableRequestFragment != null) {
				SpinnerAdapter mSpinnerAdapter = new ArrayAdapter<>(getActivity(),
						android.R.layout.simple_spinner_dropdown_item, getOpenedBill());

				spnBillOpened.setAdapter(mSpinnerAdapter);
				mLoaderManager.restartLoader(0, null, tableRequestFragment);
			}
		}
		
	}

	@Override
	public String toString() {
		return "Por Mesa";
	}
}
