package io.oxigen.quiosgrama.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.Date;

import io.oxigen.quiosgrama.Functionary;
import io.oxigen.quiosgrama.ProductRequest;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.adapter.WaiterRequestAdapter;
import io.oxigen.quiosgrama.dao.ProductRequestDao;
import io.oxigen.quiosgrama.data.DataBaseCursorBuild;
import io.oxigen.quiosgrama.data.DataProviderContract;
import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.service.DataBaseService;
import io.oxigen.quiosgrama.util.AndroidUtil;

/**
 * Created by Alexandre on 14/04/2016.
 *
 */
public class WaiterRequestFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener, WaiterRequestAdapter.OnItemLongClickListener, View.OnClickListener {

    private String mSelection;
    private String[] mSelectionArgs;

    private RecyclerView recyWaiterRequest;
    private ProgressBar emptyView;
    private QuiosgramaApp app;
    private SwipeRefreshLayout swipeRefreshLayout;
    private WaiterRequestFragment waiterRequestFragment;
    private RefreshTableRequestReceiver tableRequestReceiver;
    private Menu menu;
    private RadioButton radioAllRequests;
    private RadioButton radioMyRequests;
    private RadioButton radioAll;
    private RadioButton radioSent;
    private RadioButton radioVisualized;
    private RadioButton radioReady;
    private Dialog filterDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        waiterRequestFragment = this;
        app = (QuiosgramaApp) getActivity().getApplication();
        return inflater.inflate(R.layout.fragment_waiter_request, container, false);
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        recyWaiterRequest = (RecyclerView) v.findViewById(R.id.recyWaiterRequest);
        emptyView = (ProgressBar) v.findViewById(R.id.progress);
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeRefreshLayout);
        filterDialog = AndroidUtil.createCustomDialog(getActivity(), R.style.Dialog, R.layout.dialog_product_request_filter);
        radioAllRequests = (RadioButton) filterDialog.findViewById(R.id.radioAllRequests);
        radioMyRequests = (RadioButton) filterDialog.findViewById(R.id.radioMyRequests);
        radioAll = (RadioButton) filterDialog.findViewById(R.id.radioAll);
        radioSent = (RadioButton) filterDialog.findViewById(R.id.radioSent);
        radioVisualized = (RadioButton) filterDialog.findViewById(R.id.radioVisualized);
        radioReady = (RadioButton) filterDialog.findViewById(R.id.radioReady);

        swipeRefreshLayout.setOnRefreshListener(this);
        radioAllRequests.setOnClickListener(this);
        radioMyRequests.setOnClickListener(this);
        radioAll.setOnClickListener(this);
        radioSent.setOnClickListener(this);
        radioVisualized.setOnClickListener(this);
        radioReady.setOnClickListener(this);

        recyWaiterRequest.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyWaiterRequest.setLayoutManager(layoutManager);

        Functionary functionary = app.getFunctionarySelected();
        if(functionary != null && functionary.adminFlag == Functionary.WAITER
                || functionary.adminFlag == Functionary.CLIENT_WAITER){
            radioAllRequests.setEnabled(false);
            radioMyRequests.setChecked(true);
        }
        else if(functionary != null){
            radioAllRequests.setChecked(true);
        }

        checkRadios();
    }

    private void checkRadios(){
        mSelection = null;
        mSelectionArgs = null;
        if(radioMyRequests.isChecked()){
            mSelection = ProductRequestDao.WAITER_SELECTION;
            mSelectionArgs = new String[]{String.valueOf(app.getFunctionarySelected().id)};
        }

        if(radioSent.isChecked()){
            if(mSelection == null){
                mSelection = ProductRequestDao.SENT_SELECTION;
            }
            else{
                mSelection += " and " + ProductRequestDao.SENT_SELECTION;
            }
        }
        else if(radioVisualized.isChecked()){
            if(mSelection == null){
                mSelection = ProductRequestDao.VISUALIZED_SELECTION;
            }
            else{
                mSelection += " and " + ProductRequestDao.VISUALIZED_SELECTION;
            }
        }
        else if(radioReady.isChecked()){
            if(mSelection == null){
                mSelection = ProductRequestDao.READY_SELECTION;
            }
            else{
                mSelection += " and " + ProductRequestDao.READY_SELECTION;
            }
        }

        filterDialog.dismiss();
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        inflater.inflate(R.menu.waiter_request, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_filter) {
            filterDialog.show();
        }
        else if(item.getItemId() == R.id.action_filter_category){

        }
        else{
            return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void onRefresh() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        if(mSelection != null){
            mSelection += " and " + DataProviderContract.BillTable.PAID_TIME_COLUMN + " IS NULL";
        }
        else{
            mSelection = DataProviderContract.BillTable.PAID_TIME_COLUMN + " IS NULL";
        }

        return new CursorLoader(
                getActivity(),                          // Context
                DataProviderContract.ProductRequestTable.TABLE_URI,  // Table to query
                ProductRequestDao.TABLE_INFO_REQUEST_PROJECTION,                             // Projection to return
                mSelection,                                   // No selection clause
                mSelectionArgs,                                 // No selection arguments
                DataProviderContract.ProductRequestTable.SYNC_STATUS_COLUMN + " desc, " +
                        DataProviderContract.RequestTable.SYNC_STATUS_COLUMN + " desc, " +
                        DataProviderContract.RequestTable.REQUEST_TIME_COLUMN + " desc"
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        ArrayList<ProductRequest> prodRequestList = new ArrayList<>();
        if(c.moveToFirst()){
            do{
                int tableNumber = c.getInt(
                        c.getColumnIndex(
                                DataProviderContract.BillTable.TABLE_ID_COLUMN));

                long typeId = c.getLong(
                        c.getColumnIndex(
                                DataProviderContract.ProductTable.PRODUCT_TYPE_ID_COLUMN));

                ProductRequest prodReq = DataBaseCursorBuild.buildProductRequestObject(c, QuiosgramaApp.searchBill(tableNumber));
                prodReq.product.type = app.searchProductType(typeId);
                prodRequestList.add(prodReq);
            }
            while(c.moveToNext());

        }
        c.close();

        WaiterRequestAdapter prodReqAdapter = new WaiterRequestAdapter(getActivity(), this,
                prodRequestList);

        recyWaiterRequest.setAdapter(prodReqAdapter);

        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    @Override
    public String toString() {
        return "Histórico";
    }

    @Override
    public void onResume() {
        super.onResume();

        if(tableRequestReceiver == null)
            tableRequestReceiver = new RefreshTableRequestReceiver();
        getActivity().registerReceiver(tableRequestReceiver, new IntentFilter(TableRequestFragment.RECEIVER_FILTER));
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(tableRequestReceiver);
        super.onPause();
    }

    @Override
    public void onDetach() {
        MenuItem item = menu.findItem(R.id.action_filter);
        if(item != null) item.setVisible(false);
        super.onDetach();
    }

    @Override
    public ProductRequest onItemLongClicked(ProductRequest productRequest) {
        if(app.getFunctionarySelected().adminFlag == Functionary.ADMIN) {
            if (productRequest.request.syncStatus == KeysContract.NO_SYNCHRONIZED_STATUS_KEY) {
                return null;
            } else if (productRequest.status == ProductRequest.NOT_VISUALIZED_STATUS) {
                productRequest.status = ProductRequest.VISUALIZED_STATUS;
            } else if (productRequest.status == ProductRequest.VISUALIZED_STATUS) {
                productRequest.status = ProductRequest.READY_STATUS;
            } else {
                return null;
            }

            productRequest.syncStatus = KeysContract.NO_SYNCHRONIZED_STATUS_KEY;
            productRequest.productRequestTime = new Date();
            ArrayList<ProductRequest> productRequestList = new ArrayList<>(1);
            productRequestList.add(productRequest);

            Intent intent = new Intent(getActivity(), DataBaseService.class);
            intent.putExtra(KeysContract.METHOD_KEY, DataBaseService.UPDATE_PRODUCT_REQUEST);
            intent.putExtra(KeysContract.PRODUCT_REQUEST_LIST_KEY, productRequestList);
            getActivity().startService(intent);

            return productRequest;
        }

        return null;
    }

    @Override
    public void onClick(View view) {
        checkRadios();
    }

    private class RefreshTableRequestReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(waiterRequestFragment != null) {
                getLoaderManager().restartLoader(0, null, waiterRequestFragment);
            }
        }

    }
}
