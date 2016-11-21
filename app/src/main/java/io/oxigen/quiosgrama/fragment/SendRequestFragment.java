package io.oxigen.quiosgrama.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

import io.oxigen.quiosgrama.Bill;
import io.oxigen.quiosgrama.Functionary;
import io.oxigen.quiosgrama.ProductRequest;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.Request;
import io.oxigen.quiosgrama.Table;
import io.oxigen.quiosgrama.adapter.ProductRequestListAdapter;
import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.service.DataBaseService;
import io.oxigen.quiosgrama.util.AndroidUtil;

public class SendRequestFragment extends DialogFragment implements OnCheckedChangeListener, Toolbar.OnMenuItemClickListener{

	public static final int NEW_REQUEST_MODE = 1;
	public static final int TRANSFER_MODE = 2;

	private ListView listViewProduct;
	private EditText edtTable;
	private RadioButton chkTable;
	private RadioButton chkClient;
	protected RadioButton chkTempClient;
	private RadioGroup radTableOptions;
	private Spinner spnClient;
	protected EditText edtTempClient;
	protected View tableOptionsLayout;

	protected Request request;
	protected ArrayList<ProductRequest> productRequestList;
	public ProductRequestListAdapter productRequestListAdapter;
	
	private SendRequestListener mListener;
	private double subTotal;
	private double serviceTotal;
	private double total;
	
	QuiosgramaApp app;
	private int mMode;
	protected ArrayList<ProductRequest> mPreviousProductRequestList;
	private Bill mPreviousBill;
	private boolean mDialogMode;
	private Toolbar toolbar;

	public interface SendRequestListener{
		public void onSendRequestResumed();
		public void onSendRequestPaused();
		public void onConfirmRequest();
		public void onSaveRequest();
	}

	public static SendRequestFragment newInstance(ArrayList<ProductRequest> productRequestList, int mode){
		SendRequestFragment f = new SendRequestFragment();
		Bundle b = new Bundle();
		b.putParcelableArrayList(KeysContract.PRODUCT_REQUEST_LIST_KEY, productRequestList);
		b.putInt(KeysContract.SEND_REQUEST_MODE, mode);
		f.setArguments(b);
		return f;
	}
	
	public void reflesh(ArrayList<ProductRequest> productRequestList){
		this.productRequestList = productRequestList;
		productRequestListAdapter = new ProductRequestListAdapter(getActivity(), productRequestList);
		
		listViewProduct.setAdapter(productRequestListAdapter);
		
		calculateTotals(productRequestList);
	}
	
	public void changeProduct(ProductRequest productRequest){
		int index = productRequestListAdapter.productRequestList.indexOf(productRequest);
		
		if(productRequest.product.quantity == 0)
			productRequestListAdapter.productRequestList.remove(productRequest);
		else
			productRequestListAdapter.productRequestList.set(index, productRequest);
		
		productRequestListAdapter.notifyDataSetChanged();
		
		calculateTotals(productRequestList);
	}
	
	private void calculateTotals(ArrayList<ProductRequest> productRequestList){
		subTotal = 0;
		serviceTotal = 0;
		total = 0;
		
		for (ProductRequest productReq : productRequestList) {
			subTotal += (productReq.product.price * productReq.product.quantity);
		}
		
		serviceTotal = subTotal*0.10;
		total = subTotal + serviceTotal;
		
		if(productRequestList.isEmpty()){
			remove();
		}
	}
	
	public void remove(){
		FragmentManager fManager = getActivity().getSupportFragmentManager();
		FragmentTransaction transaction = fManager.beginTransaction();
		transaction.remove(this).commit();
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = AndroidUtil.createCustomDialog(
				getActivity(),
				R.style.Dialog,
				R.layout.fragment_send_request);

		mDialogMode = true;

		buildWidgets(dialog.findViewById(R.id.infoFragmentParent));

		toolbar.setNavigationIcon(R.drawable.abc_ic_clear_mtrl_alpha);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		toolbar.setBackgroundResource(R.drawable.shape_title_top);

		GradientDrawable shapeDrawable = (GradientDrawable) toolbar.getBackground();
		shapeDrawable.setColor(getResources().getColor(R.color.colorPrimary));

		createView();

		return dialog;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v;
		if(!mDialogMode) {
			v = buildWidgets(inflater.inflate(R.layout.fragment_send_request, container, false));

			toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);

			toolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					getActivity().getSupportFragmentManager().popBackStackImmediate();
				}
			});

			createView();
		}
		else{
			v = super.onCreateView(inflater, container, savedInstanceState);
		}

		return v;
	}

	private void createView() {
		toolbar.inflateMenu(R.menu.send_request);
		toolbar.setOnMenuItemClickListener(this);

		Bundle b = getArguments();
		if (b == null)
			throw new IllegalArgumentException("Sem argumentos: use o metodo newInstance para instaciar a classe " + SendRequestFragment.class.getName());

		productRequestList = b.getParcelableArrayList(KeysContract.PRODUCT_REQUEST_LIST_KEY);
		mMode = b.getInt(KeysContract.SEND_REQUEST_MODE);

		if (mMode == NEW_REQUEST_MODE) {
			toolbar.setTitle(getResources().getString(R.string.send_request));

			int functionaryType = app.getFunctionarySelectedType();
			if (functionaryType == Functionary.ADMIN || functionaryType == Functionary.WAITER) {
				tableOptionsLayout.setVisibility(View.VISIBLE);
			} else {
				if (app.getBillSelected() != null) {
					tableOptionsLayout.setVisibility(View.GONE);
					chkTable.setChecked(true);
					edtTable.setText(String.valueOf(app.getBillSelected().table.number));
				}
			}
		} else {
			Bill bill = productRequestList.get(0).request.bill;
			toolbar.setTitle(getResources().getString(R.string.forward) + " - " + bill.toString());
			createPreviousProductRequestList();
		}

		reflesh(productRequestList);

		request = productRequestList.get(0).request;

		if (!mDialogMode) {
			mListener.onSendRequestResumed();
		}
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_send_request:
				int tableNumber = 0;
				String number = edtTable.getText().toString();
				if(number != null && !number.equals(""))
					tableNumber = Integer.parseInt(number);

				if(chkClient.isChecked()){
					Table table = (Table) spnClient.getSelectedItem();
					tableNumber = table.number;
					edtTable.setText(String.valueOf(tableNumber));
				}

				sendRequest(tableNumber);
				break;
		}
		return false;
	}

	protected void sendRequest(int tableNumber){
		if(tableNumber != 0){
			syncProductRequeListTransfer();

			Bill bill = QuiosgramaApp.searchBill(tableNumber);
			boolean tempClientOk = false;

			if(bill == null){
				Table table = QuiosgramaApp.searchTable(tableNumber);
				if(table == null){
					table = new Table(tableNumber, app.getFunctionarySelected());
				}

				request.bill = new Bill(
						table,
						app.getFunctionarySelected(),
						null,
						new Date());

				tempClientOk = checkTempClient();
			}
			else{
				request.bill = bill;
				tempClientOk = checkTempClient();

				if(tempClientOk){
					if(bill.openTime == null){
						bill.openTime = new Date();
						bill.billTime = new Date();
					}
					if(bill.waiterOpenTable == null){
						bill.waiterOpenTable = app.getFunctionarySelected();
						bill.billTime = new Date();
					}

				}
			}

			if(tempClientOk){
				if(request.bill.closeTime != null){
					request.bill.closeTime = null;
					request.bill.waiterOpenTable = app.getFunctionarySelected();
					request.bill.billTime = new Date();
				}

				Intent intent = new Intent(getActivity(), DataBaseService.class);
				if(mMode == SendRequestFragment.TRANSFER_MODE){
					syncProductRequestListWithRequest();
					intent.putExtra(KeysContract.BILL_KEY, mPreviousBill);
					intent.putExtra(KeysContract.METHOD_KEY, DataBaseService.INSERT_PRODUCT_REQUEST);
				}
				else{
					intent.putExtra(KeysContract.METHOD_KEY, DataBaseService.INSERT_REQUEST);
					intent.putExtra(KeysContract.REQUEST_KEY, request);
				}

				intent.putExtra(KeysContract.PRODUCT_REQUEST_LIST_KEY, productRequestList);

				getActivity().startService(intent);

				AndroidUtil.hideKeyBoard(getActivity(), edtTable);
				if(mDialogMode){
					dismiss();
				}
				else{
					getActivity().getSupportFragmentManager().popBackStackImmediate();
				}
				mListener.onConfirmRequest();
			}
			else{
				String client;
				if(request.bill.table.client != null)
					client = request.bill.table.client.name;
				else
					client = request.bill.table.clientTemp;
				Toast.makeText(getActivity(), String.format(getResources().getString(R.string.client_already_table), client),
						Toast.LENGTH_SHORT).show();
			}
		}
		else{
			Toast.makeText(getActivity(),
					getResources().getString(R.string.table_number_empty),
					Toast.LENGTH_SHORT).show();
		}
	}

	protected void createPreviousProductRequestList(){
		mPreviousProductRequestList = new ArrayList<>(productRequestList.size());
		for(ProductRequest prodReq: productRequestList){
			mPreviousProductRequestList.add(new ProductRequest(prodReq.request, prodReq.product, prodReq.complement, prodReq.valid, prodReq.transferRoute, new Date(), prodReq.status, prodReq.syncStatus));
		}
	}

	protected void syncProductRequeListTransfer(){
		if(mPreviousProductRequestList != null) {
			Request request = new Request(app.getFunctionarySelected());
			Request previousRequestInvalid = new Request(app.getFunctionarySelected());

			mPreviousBill = mPreviousProductRequestList.get(0).request.bill;
			previousRequestInvalid.bill = mPreviousBill;

			ArrayList<ProductRequest> newPreviousProdReqInvalidList = new ArrayList<>();
			for (ProductRequest previousProdReq : mPreviousProductRequestList) {
				for (ProductRequest prodReq : productRequestList) {
					if (previousProdReq.equals(prodReq)) {
						changePreviousProdReqWithDifference(previousProdReq, prodReq, previousRequestInvalid, newPreviousProdReqInvalidList);

						int i = productRequestList.indexOf(prodReq);
						prodReq = new ProductRequest(request, prodReq.product, prodReq.complement, true, prodReq.transferRoute, new Date(), ProductRequest.NOT_VISUALIZED_STATUS, KeysContract.SYNCHRONIZED_STATUS_KEY);
						productRequestList.set(i, prodReq);
					}
				}
			}

			verifyNewPreviousProdReqInvalidList(newPreviousProdReqInvalidList);

			this.request = productRequestList.get(0).request;
			productRequestList.addAll(mPreviousProductRequestList);
		}
	}

	private void changePreviousProdReqWithDifference(ProductRequest previousProdReq, ProductRequest prodReq, Request previousRequestInvalid,
													 ArrayList<ProductRequest> newPreviousProdReqInvalidList){
		int difference = previousProdReq.product.quantity - prodReq.product.quantity;
		if (difference > 0) {
			previousProdReq.product.quantity = difference;

			ProductRequest previousProdReqInvalid = new ProductRequest(previousRequestInvalid, previousProdReq.product, previousProdReq.complement, false, previousProdReq.transferRoute, new Date(), ProductRequest.NOT_VISUALIZED_STATUS, KeysContract.SYNCHRONIZED_STATUS_KEY);
			previousProdReqInvalid.product.quantity = prodReq.product.quantity;
			newPreviousProdReqInvalidList.add(previousProdReqInvalid);
		} else {
			previousProdReq.valid = false;
		}

		previousProdReq.request.syncStatus = 1;
		int i = mPreviousProductRequestList.indexOf(previousProdReq);
		mPreviousProductRequestList.set(i, previousProdReq);
	}

	private void verifyNewPreviousProdReqInvalidList(ArrayList<ProductRequest> newPreviousProdReqInvalidList){
		if(!newPreviousProdReqInvalidList.isEmpty()){
			mPreviousProductRequestList.addAll(newPreviousProdReqInvalidList);
		}
	}

	private void syncProductRequestListWithRequest(){
		for (ProductRequest prodReq : productRequestList) {
			if(prodReq.request.id.equals(request.id)){
				prodReq.setTransferRoute(mPreviousBill, request.bill);
			}
			else{
				prodReq.setTransferRoute(request.bill);
			}
		}

		verifyProductRequestDuplicate();
	}

	private void verifyProductRequestDuplicate(){
		ArrayList<ProductRequest> newProdReqList = new ArrayList<>();

		for (ProductRequest prodReq : productRequestList) {
			int i = newProdReqList.indexOf(prodReq);
			if(i >= 0){
				newProdReqList.get(i).product.quantity += prodReq.product.quantity;
			}
			else{
				newProdReqList.add(prodReq);
			}
		}

		productRequestList = newProdReqList;
	}

	@Override
	public void onPause() {
		mListener.onSendRequestPaused();
		super.onPause();
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (SendRequestListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SendRequestListener");
        }
    }
	
	private View buildWidgets(View v){
		app = (QuiosgramaApp) getActivity().getApplication();

		listViewProduct = (ListView) v.findViewById(R.id.listViewProduct);
		edtTable = (EditText) v.findViewById(R.id.edtTable);
		chkClient = (RadioButton) v.findViewById(R.id.chkClient);
		chkTable = (RadioButton) v.findViewById(R.id.chkTable);
		chkTempClient = (RadioButton) v.findViewById(R.id.chkTempClient);
		radTableOptions = (RadioGroup) v.findViewById(R.id.radTableOptions);
		spnClient = (Spinner) v.findViewById(R.id.spnClient);
		edtTempClient = (EditText) v.findViewById(R.id.edtTempClient);
		tableOptionsLayout = v.findViewById(R.id.tableOptionsLayout);
		toolbar = (Toolbar) v.findViewById(R.id.tool_bar);

		radTableOptions.setOnCheckedChangeListener(this);
		
		v.setOnTouchListener(new OnTouchListener(){
	    	
	        @Override
	        public boolean onTouch(View v, MotionEvent event){
		        return true;
	        }
	    });
		
		ArrayList<Table> tableListTemp = new ArrayList<Table>(app.getTableList().size()+1);
		tableListTemp.add(new Table(0, null));
		tableListTemp.addAll(app.getTableList());
		SpinnerAdapter mSpinnerAdapter = new ArrayAdapter<Table>(getActivity(), 
				android.R.layout.simple_spinner_dropdown_item, tableListTemp);
		spnClient.setAdapter(mSpinnerAdapter);
		
		return v;
	}

	public static String convertToPrice(double price){
		return String.format("%.2f", price);
	}

	private boolean checkTempClient() {
		if(chkTempClient != null) {
			if (chkTempClient.isChecked()) {
				String clientTemp = edtTempClient.getText().toString();
				if (!clientTemp.isEmpty()) {
					if (request.bill.table.client == null){
						request.bill.table.clientTemp = clientTemp;
						return true;
					}
				}
			} else
				return true;
		}
		else{
			//For test porpose
			return true;
		}

		return false;
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
			case R.id.chkTable:
				edtTable.setVisibility(View.VISIBLE);
				spnClient.setVisibility(View.GONE);
				edtTempClient.setVisibility(View.GONE);
			break;

			case R.id.chkClient:
				edtTable.setVisibility(View.GONE);
				spnClient.setVisibility(View.VISIBLE);
				edtTempClient.setVisibility(View.GONE);
			break;
				
			case R.id.chkTempClient:
				edtTable.setVisibility(View.VISIBLE);
				spnClient.setVisibility(View.GONE);
				edtTempClient.setVisibility(View.VISIBLE);
			break;
		}
	}
	
	
	
}
