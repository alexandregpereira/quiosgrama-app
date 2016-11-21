package io.oxigen.quiosgrama.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import io.oxigen.quiosgrama.Bill;
import io.oxigen.quiosgrama.Functionary;
import io.oxigen.quiosgrama.ProductRequest;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.adapter.ProductRequestListAdapter;
import io.oxigen.quiosgrama.dao.ProductRequestDao;
import io.oxigen.quiosgrama.data.DataBaseCursorBuild;
import io.oxigen.quiosgrama.data.DataProviderContract;
import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.listener.BackFragmentListener;
import io.oxigen.quiosgrama.service.DataBaseService;
import io.oxigen.quiosgrama.service.PrintService;
import io.oxigen.quiosgrama.util.AndroidUtil;

public class TableInfoDialogFragment extends DialogFragment implements
        LoaderCallbacks<Cursor>, OnClickListener, AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener, Toolbar.OnMenuItemClickListener {

    private static TableInfoDialogFragment tableInfoDialogFragment;

    private ListView listViewProduct;
    private View emptyView;

    private Bill bill;
    private ProductRequestListAdapter prodReqAdapter;
    private TextView txtSubTotal;
    private TextView txtServiceTotal;
    private TextView txtTotal;
    QuiosgramaApp app;
    private Context context;

    private boolean mActionMode;
    private Toolbar mToolbar;
    private MenuItem mMenuItemDelete;
    private MenuItem mMenuItemForward;
    private MenuItem mMenuItemRemove;
    private MenuItem mMenuItemZombieBill;
    private MenuItem mMenuItemReset;

    private boolean mDialogMode;
    private Button btnCloseTable;
    private Button btnConfirmCloseTable;
    private TextView txtWaiterOpen;
    private TextView txtWaiterOpenLabel;
    private TextView txtWaiterClose;
    private TextView txtWaiterCloseLabel;
    private TextView txtLastMod;
    private TextView txtLastModLabel;
    private LinearLayout scannerLayout;
    private LinearLayout billLayout;
    private Button btnStartScanner;

    private BackFragmentListener backListener;
    private SyncReceiver receiver;

    public static TableInfoDialogFragment newInstance(Bill bill) {
        if(tableInfoDialogFragment == null){
            tableInfoDialogFragment = new TableInfoDialogFragment();
            tableInfoDialogFragment.setArguments(new Bundle());
        }
        Bundle data = tableInfoDialogFragment.getArguments();
        data.putParcelable(KeysContract.BILL_KEY, bill);
        return tableInfoDialogFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(mDialogMode) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
        else{
            View v = inflater.inflate(R.layout.dialog_table_info, container, false);

            mToolbar = (Toolbar) v.findViewById(R.id.tool_bar);
            listViewProduct = (ListView) v.findViewById(R.id.listViewProduct);
            btnCloseTable = (Button) v.findViewById(R.id.btnCloseTable);
            btnConfirmCloseTable = (Button) v.findViewById(R.id.btnConfirmCloseTable);
            emptyView = v.findViewById(R.id.progress);
            txtSubTotal = (TextView) v.findViewById(R.id.txtSubTotal);
            txtServiceTotal = (TextView) v.findViewById(R.id.txtServiceTotal);
            txtTotal = (TextView) v.findViewById(R.id.txtTotal);
            txtWaiterOpen = (TextView) v.findViewById(R.id.txtWaiterOpen);
            txtWaiterOpenLabel = (TextView) v.findViewById(R.id.txtWaiterOpenLabel);
            txtWaiterClose = (TextView) v.findViewById(R.id.txtWaiterClose);
            txtWaiterCloseLabel = (TextView) v.findViewById(R.id.txtWaiterCloseLabel);
            txtLastMod = (TextView) v.findViewById(R.id.txtLastMod);
            txtLastModLabel = (TextView) v.findViewById(R.id.txtLastModLabel);
            scannerLayout = (LinearLayout) v.findViewById(R.id.scannerLayout);
            billLayout = (LinearLayout) v.findViewById(R.id.billLayout);
            btnStartScanner = (Button) v.findViewById(R.id.btnStartScanner);

            mToolbar.setVisibility(View.GONE);
            LinearLayout parentContainer = (LinearLayout) v.findViewById(R.id.parentContainer);
            int padding = (int) getResources().getDimension(R.dimen.default_layout_margin);
            parentContainer.setPadding(padding, padding, padding, padding);
            parentContainer.setBackgroundResource(android.R.color.transparent);

            Bundle data = getArguments();
            Bill bill = data.getParcelable(KeysContract.BILL_KEY);
            build(bill);

            return v;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog tableDialog = AndroidUtil.createCustomDialog(
                getActivity(),
                R.style.Dialog,
                R.layout.dialog_table_info);

        mDialogMode = true;

        mToolbar = (Toolbar) tableDialog.findViewById(R.id.tool_bar);
        listViewProduct = (ListView) tableDialog.findViewById(R.id.listViewProduct);
        btnCloseTable = (Button) tableDialog.findViewById(R.id.btnCloseTable);
        btnConfirmCloseTable = (Button) tableDialog.findViewById(R.id.btnConfirmCloseTable);
        emptyView = tableDialog.findViewById(R.id.progress);
        txtSubTotal = (TextView) tableDialog.findViewById(R.id.txtSubTotal);
        txtServiceTotal = (TextView) tableDialog.findViewById(R.id.txtServiceTotal);
        txtTotal = (TextView) tableDialog.findViewById(R.id.txtTotal);
        txtWaiterOpen = (TextView) tableDialog.findViewById(R.id.txtWaiterOpen);
        txtWaiterOpenLabel = (TextView) tableDialog.findViewById(R.id.txtWaiterOpenLabel);
        txtWaiterClose = (TextView) tableDialog.findViewById(R.id.txtWaiterClose);
        txtWaiterCloseLabel = (TextView) tableDialog.findViewById(R.id.txtWaiterCloseLabel);
        txtLastMod = (TextView) tableDialog.findViewById(R.id.txtLastMod);
        txtLastModLabel = (TextView) tableDialog.findViewById(R.id.txtLastModLabel);

        Bundle data = getArguments();
        Bill bill = data.getParcelable(KeysContract.BILL_KEY);
        build(bill);

        return tableDialog;
    }

    private void build(Bill bill){
        context = getActivity();
        app = (QuiosgramaApp) getActivity().getApplication();
        if(bill != null) {
            if(!mDialogMode){
                billLayout.setVisibility(View.VISIBLE);
                scannerLayout.setVisibility(View.GONE);
            }

            this.bill = bill;

            mToolbar.setNavigationIcon(R.drawable.abc_ic_clear_mtrl_alpha);

            mToolbar.setOnMenuItemClickListener(this);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });

            mToolbar.setBackgroundResource(R.drawable.shape_title_top);
            mToolbar.inflateMenu(R.menu.table_info_dialog);

            Menu menu = mToolbar.getMenu();
            mMenuItemDelete = menu.findItem(R.id.action_delete);
            mMenuItemForward = menu.findItem(R.id.action_forward);
            mMenuItemReset = menu.findItem(R.id.action_reset);
            MenuItem printItem = menu.findItem(R.id.action_print);
            printItem.setVisible(false);
            if (bill.openTime == null) {
                mMenuItemRemove = menu.findItem(R.id.action_remove_map);
                mMenuItemRemove.setVisible(true);
            } else {
                mMenuItemZombieBill = menu.findItem(R.id.action_zombie_bill);
                if (app.getFunctionarySelected() != null){
                    if (app.getFunctionarySelected().adminFlag == Functionary.WAITER) {
                        mMenuItemZombieBill.setVisible(true);
                    }
                    else if(app.getFunctionarySelected().adminFlag == Functionary.ADMIN){
                        printItem.setVisible(true);
                    }
                }
            }

            int type = app.getFunctionarySelectedType();
            if(type == Functionary.WAITER || type == Functionary.ADMIN){
                mMenuItemReset.setVisible(true);
            }

            buildToolbarTitle();

            listViewProduct.setOnItemLongClickListener(this);
            listViewProduct.setOnItemClickListener(this);

            int functionaryType = app.getFunctionarySelected() != null ? app.getFunctionarySelected().adminFlag : -1;

            if (bill.openTime == null || bill.closeTime != null) {
                btnCloseTable.setVisibility(View.GONE);
                if (bill.closeTime != null && bill.paidTime == null
                        && functionaryType == Functionary.ADMIN) {
                    btnConfirmCloseTable.setOnClickListener(this);
                    btnConfirmCloseTable.setVisibility(View.VISIBLE);
                }
            }
            else if(functionaryType != Functionary.ADMIN && functionaryType != Functionary.WAITER){
                btnCloseTable.setVisibility(View.GONE);
            }
            else {
                btnCloseTable.setOnClickListener(this);
            }

            listViewProduct.setEmptyView(emptyView);

            if (bill.waiterOpenTable != null) {
                String shortName = bill.waiterOpenTable.name;
                if(bill.waiterOpenTable.name != null && bill.waiterOpenTable.name.length() > 10){
                    shortName = bill.waiterOpenTable.name.substring(0, 9);
                }
                txtWaiterOpen.setText(shortName + " - há " + AndroidUtil.calculateDate(getActivity(), bill.openTime, new Date()));
                txtWaiterOpenLabel.setVisibility(View.VISIBLE);
                txtWaiterOpen.setVisibility(View.VISIBLE);
            } else {
                txtWaiterOpenLabel.setVisibility(View.GONE);
                txtWaiterOpen.setVisibility(View.GONE);
            }

            if (bill.waiterCloseTable != null) {
                String shortName = bill.waiterCloseTable.name;
                if(bill.waiterCloseTable.name != null && bill.waiterCloseTable.name.length() > 10){
                    shortName = bill.waiterCloseTable.name.substring(0, 9);
                }
                txtWaiterClose.setText(shortName + " - há " + AndroidUtil.calculateDate(getActivity(), bill.closeTime, new Date()));
                txtWaiterCloseLabel.setVisibility(View.VISIBLE);
                txtWaiterClose.setVisibility(View.VISIBLE);
            } else {
                txtWaiterCloseLabel.setVisibility(View.GONE);
                txtWaiterClose.setVisibility(View.GONE);
            }

            if (bill.table.waiterAlterTable != null) {
                String shortName = bill.table.waiterAlterTable.name;
                if(bill.table.waiterAlterTable.name != null && bill.table.waiterAlterTable.name.length() > 10){
                    shortName = bill.table.waiterAlterTable.name.substring(0, 9);
                }
                txtLastMod.setText(shortName + " - há " + AndroidUtil.calculateDate(getActivity(), bill.table.tableTime, new Date()));
                txtLastModLabel.setVisibility(View.VISIBLE);
                txtLastMod.setVisibility(View.VISIBLE);
            } else {
                txtLastModLabel.setVisibility(View.GONE);
                txtLastMod.setVisibility(View.GONE);
            }

            getLoaderManager().restartLoader(0, null, this);
        }
        else if(!mDialogMode){
            buildScannerMessage();
        }
    }

    private void buildScannerMessage() {
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.tool_bar);
        toolbar.setBackgroundResource(R.color.colorPrimary);
        toolbar.setTitle(getResources().getString(R.string.request));

        billLayout.setVisibility(View.GONE);
        scannerLayout.setVisibility(View.VISIBLE);
        btnStartScanner.setOnClickListener(this);
    }

    private void buildToolbarTitle(){
        Toolbar toolbar = null;
        if(mDialogMode) {
            GradientDrawable shapeDrawable = (GradientDrawable) mToolbar.getBackground();
            if (bill.openTime != null && bill.closeTime == null) {
                shapeDrawable.setColor(getResources().getColor(R.color.green));
            } else if (bill.openTime != null && bill.closeTime != null) {
                shapeDrawable.setColor(getResources().getColor(R.color.yellow_orange));
            } else {
                shapeDrawable.setColor(getResources().getColor(R.color.red));
            }

            toolbar = mToolbar;
        }
        else{
            toolbar = (Toolbar) getActivity().findViewById(R.id.tool_bar);

            if (bill.openTime != null && bill.closeTime == null) {
                toolbar.setBackgroundResource(R.color.green);
            } else if (bill.openTime != null && bill.closeTime != null) {
                toolbar.setBackgroundResource(R.color.yellow_orange);
            } else {
                toolbar.setBackgroundResource(R.color.red);
            }
        }

        if (bill.table.client == null && (bill.table.clientTemp == null || bill.table.clientTemp.isEmpty())) {
            String titleFormat = getResources().getString(R.string.dialog_table_title);
            toolbar.setTitle(String.format(Locale.US, titleFormat, bill.table.number));
        } else if (bill.table.client != null) {
            String titleFormat = getResources().getString(R.string.dialog_table_client_title);
            toolbar.setTitle(String.format(Locale.US, titleFormat, bill.table.number + " - " + bill.table.client.name));
        } else if (bill.table.clientTemp != null) {
            String titleFormat = getResources().getString(R.string.dialog_table_client_title);
            toolbar.setTitle(String.format(Locale.US, titleFormat, bill.table.number + " - " + bill.table.clientTemp));
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item.getItemId() == R.id.action_forward) {
            if (prodReqAdapter.mProductRequestActionModeList.size() > 0) {
                dismiss();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();

                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                SendRequestFragment fragment = SendRequestFragment.newInstance(prodReqAdapter.mProductRequestActionModeList, SendRequestFragment.TRANSFER_MODE);
                transaction.add(android.R.id.content, fragment, SendRequestFragment.class.getName()).addToBackStack(null).commit();
            } else {
                Toast.makeText(context, getResources().getString(R.string.no_item_selected), Toast.LENGTH_LONG).show();
            }
        }
        else if(item.getItemId() == R.id.action_delete) {
            invalidateProductRrequest();
        }
        else if(item.getItemId() == R.id.action_remove_map){
            if(bill.openTime == null) {
                bill.table.show = false;
                bill.table.tableTime = new Date();
                bill.table.clientTemp = null;
                bill.table.syncStatus = 1;

                Intent intent = new Intent(getActivity(), DataBaseService.class);
                intent.putExtra(KeysContract.METHOD_KEY, DataBaseService.UPDATE_TABLE);
                intent.putExtra(KeysContract.TABLE_KEY, bill.table);
                getActivity().startService(intent);

                dismiss();
            }
        }
        else if(item.getItemId() == R.id.action_zombie_bill){
            dismiss();

            QrCodeFragment qrCodeFragment = QrCodeFragment.newInstance(bill);
            qrCodeFragment.setTargetFragment(this, 1);
            qrCodeFragment.show(getActivity().getSupportFragmentManager(),
                    QrCodeFragment.class.getName());
        }
        else if(item.getItemId() == R.id.action_reset){
            resetMapItem();
        }
        else if(item.getItemId() == R.id.action_print){
            print();
        }

        return false;
    }

    private void print() {
        Intent intent = new Intent(getActivity(), PrintService.class);
        intent.putExtra(KeysContract.METHOD_KEY, PrintService.PRINT_BILL);
        intent.putExtra(KeysContract.BILL_KEY, bill);
        getActivity().startService(intent);
    }

    private void resetMapItem(){
        String title =
                String.format(getResources().getString(R.string.confirm_alter_table_title),
                        bill.table.number);
        String message =
                String.format(getResources().getString(R.string.confirm_reset_table_message),
                        bill.table.number);
        AndroidUtil.createDialog(getActivity(), title, message)
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        bill.table.xPosInDpi = 25;
                        bill.table.yPosDpi = 0;
                        bill.table.mapPageNumber = 0;
                        bill.table.waiterAlterTable = app.getFunctionarySelected();
                        bill.table.tableTime = new Date();
                        bill.table.syncStatus = 1;

                        app.addOrUpdateBill(bill);
                        getActivity().sendBroadcast(new Intent(MapPagerFragment.RECEIVER_FILTER));

                        Intent intent = new Intent(getActivity(), DataBaseService.class);
                        intent.putExtra(KeysContract.METHOD_KEY,
                                DataBaseService.UPDATE_TABLE);
                        intent.putExtra(KeysContract.TABLE_KEY, bill.table);

                        getActivity().startService(intent);

                        dismiss();
                    }
                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.cancel();
                    }
                })
                .create().show();
    }

    private void invalidateProductRrequest(){
        if (prodReqAdapter.mProductRequestActionModeList.size() > 0) {
            for(ProductRequest prodReq : prodReqAdapter.mProductRequestActionModeList){
                prodReq.valid = false;
                prodReq.productRequestTime = new Date();
                prodReq.syncStatus = KeysContract.NO_SYNCHRONIZED_STATUS_KEY;
            }

            int count = 0;
            for(ProductRequest prodReq : prodReqAdapter.productRequestList){
                if(!prodReq.valid) ++count;
            }

            if(count == prodReqAdapter.productRequestList.size()){
                dismiss();
            }

            Intent intent = new Intent(getActivity(), DataBaseService.class);
            intent.putExtra(KeysContract.METHOD_KEY, DataBaseService.INSERT_PRODUCT_REQUEST);
            intent.putExtra(KeysContract.PRODUCT_REQUEST_LIST_KEY, prodReqAdapter.mProductRequestActionModeList);
            intent.putExtra(KeysContract.BILL_KEY, bill);
            getActivity().startService(intent);
            clearActionMode();
        } else {
            Toast.makeText(context, getResources().getString(R.string.no_item_selected), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (mActionMode) {
            actionModeListChange(position);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        if (!mActionMode) {
            if(actionModeListChange(position)) {
                setMenuActionModeVisibility(true);
                GradientDrawable shapeDrawable = (GradientDrawable) mToolbar.getBackground();
                shapeDrawable.setColor(getResources().getColor(R.color.colorPrimaryDark));
                mActionMode = true;
                mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clearActionMode();
                    }
                });
            }
            else{
                return false;
            }
        }

        return true;
    }

    private void clearActionMode(){
        setMenuActionModeVisibility(false);
        prodReqAdapter.mProductRequestActionModeList.clear();
        prodReqAdapter.notifyDataSetChanged();
        buildToolbarTitle();
        mActionMode = false;
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private boolean actionModeListChange(int position){
        ProductRequest prodReq = prodReqAdapter.getItem(position);
        int functionaryType = app.getFunctionarySelectedType();
        if(prodReq.valid && mDialogMode
                && (functionaryType == Functionary.ADMIN || functionaryType == Functionary.WAITER)) {
            if (prodReqAdapter.mProductRequestActionModeList.contains(prodReq)) {
                prodReqAdapter.mProductRequestActionModeList.remove(prodReq);
            } else {
                prodReqAdapter.mProductRequestActionModeList.add(prodReq);
            }
            prodReqAdapter.notifyDataSetChanged();

            String title = (String) mToolbar.getTitle();
            title = getResources().getString(R.string.forward);

            if (prodReqAdapter.mProductRequestActionModeList.size() == 1) {
                title += " - 1 Item";
            } else if (prodReqAdapter.mProductRequestActionModeList.size() > 1) {
                title += " - " + prodReqAdapter.mProductRequestActionModeList.size() + " Itens";
            }

            mToolbar.setTitle(title);
            return true;
        }

        return false;
    }

    private void setMenuActionModeVisibility(boolean visible){
        if(!visible || app.getFunctionarySelectedType() == Functionary.ADMIN) {
            mMenuItemDelete.setVisible(visible);
        }
        mMenuItemForward.setVisible(visible);
        mMenuItemReset.setVisible(!visible);
        if(mMenuItemRemove != null) mMenuItemRemove.setVisible(!visible);
        if(mMenuItemZombieBill != null && app.getFunctionarySelectedType() == Functionary.WAITER
                || visible) {
            mMenuItemZombieBill.setVisible(!visible);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        return new CursorLoader(
                getActivity(),                          // Context
                DataProviderContract.ProductRequestTable.TABLE_URI,  // Table to query
                ProductRequestDao.TABLE_INFO_REQUEST_PROJECTION,                             // Projection to return
                ProductRequestDao.BILL_SELECTION,                                   // No selection clause
                new String[]{String.valueOf(bill.id)},                                 // No selection arguments
                null                                    // Default sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        ArrayList<ProductRequest> prodRequestList = new ArrayList<ProductRequest>();
        if (c.moveToFirst()) {
            do {
                ProductRequest prodReq = DataBaseCursorBuild.buildProductRequestObject(c, bill);
                prodRequestList.add(prodReq);
            }
            while (c.moveToNext());

            prodReqAdapter = new ProductRequestListAdapter(getActivity(),
                    prodRequestList,
                    true);

            calculateBillTotal(prodRequestList);

            listViewProduct.setAdapter(prodReqAdapter);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    private void calculateBillTotal(ArrayList<ProductRequest> prodRequestList) {
        if (prodRequestList != null) {
            double noServiceTotal = 0;
            for (ProductRequest productRequest : prodRequestList) {
                if(productRequest.valid) {
                    noServiceTotal += productRequest.product.price * productRequest.quantity + productRequest.complement.price * productRequest.product.quantity;
                }
            }

            double serviceTotal = noServiceTotal * 0.1;
            double total = noServiceTotal + serviceTotal;

            txtSubTotal.setText(String.format("%.2f", noServiceTotal));
            txtServiceTotal.setText(String.format("%.2f", serviceTotal));
            txtTotal.setText(String.format("%.2f", total));
            bill.amountPaid = noServiceTotal;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnCloseTable) {
            String title =
                    getResources().getString(R.string.close_table) + " " + bill.table.number;

            String message =
                    String.format(Locale.US,
                            getResources().getString(R.string.close_table_message),
                            bill.table.number);

            AndroidUtil.createDialog(getActivity(),
                    title,
                    message)
                    .setNegativeButton("Não", null)
                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            bill.closeTime = new Date();
                            bill.waiterCloseTable = app.getFunctionarySelected();
                            bill.syncStatus = 1;
                            bill.billTime = new Date();

                            Intent intent = new Intent(context, DataBaseService.class);
                            intent.putExtra(KeysContract.METHOD_KEY, DataBaseService.UPDATE_BILL);
                            intent.putExtra(KeysContract.BILL_KEY, bill);
                            context.startService(intent);
                        }
                    })
                    .show();

            dismiss();
        } else if (v.getId() == R.id.btnConfirmCloseTable) {
            AmountStackFragment amountFragment = AmountStackFragment.newInstance(bill);

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.add(android.R.id.content, amountFragment, AmountStackFragment.class.getName()).addToBackStack(null).commit();

            dismiss();
        }
        else if(v.getId() == R.id.btnStartScanner){
            IntentIntegrator integrator = new IntentIntegrator(getActivity());
            integrator.initiateScan();
        }
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
        mActionMode = false;
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.tool_bar);
        toolbar.setBackgroundResource(R.color.colorPrimary);
        super.onDetach();
        backListener.backFragmentPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(receiver == null)
            receiver = new SyncReceiver();
        getActivity().registerReceiver(receiver, new IntentFilter(MapPagerFragment.RECEIVER_FILTER));
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(receiver);
        super.onPause();
    }

    private class SyncReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            build(app.getBillSelected());
        }

    }
}
