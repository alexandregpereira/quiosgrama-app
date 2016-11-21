package io.oxigen.quiosgrama.fragment;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import io.oxigen.quiosgrama.Amount;
import io.oxigen.quiosgrama.Bill;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.fiscal.Fiscal;
import io.oxigen.quiosgrama.service.DataBaseService;
import io.oxigen.quiosgrama.util.AndroidUtil;
import io.oxigen.quiosgrama.util.ImageUtil;

/**
 * Created by Alexandre on 05/05/2016.
 *
 */
public class AmountStackFragment extends Fragment implements View.OnClickListener, TextWatcher {

    public static final String SAT_USB_RECEIVER = "io.oxigen.quiosgrama.filter.AMOUNT_FRAGMENT_SAT_USB_RECEIVER";

    private RadioButton chkMoneyOption;
    private RadioButton chkCardOption;
    private CheckBox chkServiceOption;
    private CheckBox chkCouponIssue;
    private TextView txtSatDisconnect;
    private EditText edtValue;
    private EditText edtDiscount;
    private EditText edtCpf;
    private LinearLayout linAmount;
    private ImageButton btnAddAmount;
    private TextView txtTotal;
    private TextView txtTotalLeft;
    private TextView txtTotalLeftLabel;
    private TextView txtTotalPaid;
    private TextView txtTotalPaidLabel;
    private Button btnConfirmPayment;
    private Button btnTotal;
    private View txtTotalLeftLayout;

    private Bill bill;
    private Context context;
    private Toolbar mToolbar;
    protected ArrayList<Amount> mAmountList;
    private double mTotalReceived;
    private double mDiscount;

    public static AmountStackFragment newInstance(Bill bill){
        AmountStackFragment fragment = new AmountStackFragment();
        Bundle data = new Bundle();
        data.putParcelable(KeysContract.BILL_KEY, bill);
        fragment.setArguments(data);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_amount_stack, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        chkMoneyOption = (RadioButton) view.findViewById(R.id.chkMoneyOption);
        chkCardOption = (RadioButton) view.findViewById(R.id.chkCardOption);
        chkServiceOption = (CheckBox) view.findViewById(R.id.chkServiceOption);
        chkCouponIssue = (CheckBox) view.findViewById(R.id.chkCouponIssue);
        txtSatDisconnect = (TextView) view.findViewById(R.id.txtSatDisconnect);
        edtValue = (EditText) view.findViewById(R.id.edtValue);
        edtDiscount = (EditText) view.findViewById(R.id.edtDiscount);
        edtCpf = (EditText) view.findViewById(R.id.edtCpf);
        linAmount = (LinearLayout) view.findViewById(R.id.linAmount);
        btnAddAmount = (ImageButton) view.findViewById(R.id.btnAddAmount);
        txtTotal = (TextView) view.findViewById(R.id.txtTotal);
        txtTotalLeft = (TextView) view.findViewById(R.id.txtTotalLeft);
        txtTotalLeftLabel = (TextView) view.findViewById(R.id.txtTotalLeftLabel);
        txtTotalPaid = (TextView) view.findViewById(R.id.txtTotalPaid);
        txtTotalPaidLabel = (TextView) view.findViewById(R.id.txtTotalPaidLabel);
        btnConfirmPayment = (Button) view.findViewById(R.id.btnConfirmPayment);
        btnTotal = (Button) view.findViewById(R.id.btnTotal);
        txtTotalLeftLayout = view.findViewById(R.id.txtTotalLeftLayout);

        mToolbar = (Toolbar) view.findViewById(R.id.tool_bar);
        mToolbar.setNavigationIcon(R.drawable.abc_ic_clear_mtrl_alpha);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        });

        mToolbar.setBackgroundResource(R.drawable.shape_title_top);

        changeStateCouponIssue();

        changeStateCustomRadio(chkMoneyOption, chkCardOption);
        chkMoneyOption.setOnClickListener(this);
        chkCardOption.setOnClickListener(this);
        btnAddAmount.setOnClickListener(this);
        edtValue.addTextChangedListener(this);
        edtDiscount.addTextChangedListener(this);
        btnConfirmPayment.setOnClickListener(this);
        btnTotal.setOnClickListener(this);
        chkServiceOption.setOnClickListener(this);
        chkCouponIssue.setOnClickListener(this);

        context = getActivity();
        Bundle data = getArguments();
        bill = data.getParcelable(KeysContract.BILL_KEY);

        mToolbar.setTitle(bill.toString());
        calculateFixedTotals();
        calculateTotalPaid();
    }

    private void changeStateCouponIssue() {
        QuiosgramaApp app = (QuiosgramaApp) getActivity().getApplicationContext();

        if(app.easySat.getDevice() == null){
            chkCouponIssue.setChecked(false);
            chkCouponIssue.setEnabled(false);

            txtSatDisconnect.setVisibility(View.VISIBLE);
        }
        else{
            chkCouponIssue.setEnabled(true);

            txtSatDisconnect.setVisibility(View.INVISIBLE);
        }

        if(chkCouponIssue.isChecked()){
            edtCpf.setVisibility(View.VISIBLE);
        }
        else{
            edtCpf.setVisibility(View.GONE);
            edtCpf.setText("");
        }
    }

    private void calculateFixedTotals() {
        mDiscount = 0;
        try{
            mDiscount = Double.valueOf(edtDiscount.getText().toString());
        }catch (NumberFormatException e){}
        if(chkServiceOption.isChecked()) {
            txtTotal.setText(String.format("R$ %.2f", (bill.amountPaid * 1.1) - mDiscount));
        }
        else{
            txtTotal.setText(String.format("R$ %.2f", (bill.amountPaid - mDiscount)));
        }
    }

    private void changeStateCustomRadio(RadioButton radio, RadioButton anotherRadio){
        if(radio.isChecked()){
            radio.setTextColor(getResources().getColor(android.R.color.white));
            anotherRadio.setTextColor(getResources().getColor(R.color.dark_green));
        }
        else{
            radio.setTextColor(getResources().getColor(R.color.dark_green));
            anotherRadio.setTextColor(getResources().getColor(android.R.color.white));
        }
    }

    private void addAmount(){
        String valueString = edtValue.getText().toString();
        double value = 0;
        try {
            if (!valueString.isEmpty())
                value = Double.valueOf(edtValue.getText().toString().replace(",", "."));
            if (value > 0) {
                int paidMethod = getPaidMethod();
                bill.servicePaid = chkServiceOption.isChecked();
                Amount amount = new Amount(value, paidMethod, bill);

                if (mAmountList == null) mAmountList = new ArrayList<>();
                mAmountList.add(amount);

                LayoutInflater inflater = getActivity().getLayoutInflater();
                LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.item_amount_stack, linAmount, false);
                TextView txtAmount = (TextView) layout.findViewById(R.id.txtAmount);
                ImageButton btnDelAmount = (ImageButton) layout.findViewById(R.id.btnDelAmount);

                btnDelAmount.setOnClickListener(this);
                btnDelAmount.setTag(amount);

                int imageWidth = (int) getResources().getDimension(R.dimen.icon_amount_width);
                int imageHeight = (int) getResources().getDimension(R.dimen.icon_amount_height);
                if (amount.paidMethod == Amount.PAID_METHOD_MONEY) {
                    txtAmount.setCompoundDrawables(ImageUtil.changeColorIconDrawable(getActivity(), R.drawable.ic_coin, R.color.colorPrimary, imageWidth, imageHeight), null, null, null);
                } else {
                    txtAmount.setCompoundDrawables(ImageUtil.changeColorIconDrawable(getActivity(), R.drawable.ic_credit_card, R.color.colorPrimary, imageWidth, imageHeight), null, null, null);
                }

                txtAmount.setText(String.format("R$ %.2f", amount.value));

                linAmount.addView(layout);
                edtValue.setText("");
            }
        }catch (NumberFormatException e){}
    }

    private int getPaidMethod() {
        if (chkMoneyOption.isChecked()) {
            return Amount.PAID_METHOD_MONEY;
        } else {
            return Amount.PAID_METHOD_CARD;
        }
    }

    private void calculateTotalPaid() {
        calculateFixedTotals();
        mTotalReceived = 0;
        if(mAmountList != null && !mAmountList.isEmpty()) {
            for (Amount amount : mAmountList) {
                mTotalReceived += amount.value;
            }
        }
        else{
            String value = edtValue.getText().toString();
            if(value.isEmpty()){
                mTotalReceived = 0;
            }
            else{
                try {
                    mTotalReceived = Double.valueOf(value.replace(",", "."));
                }catch (NumberFormatException e){

                }
            }
        }

        txtTotalPaid.setText(String.format("R$ %.2f", mTotalReceived));
        double totalLeft;

        boolean servicePaid = chkServiceOption.isChecked();

        if(servicePaid) {
            totalLeft = bill.amountPaid * 0.1 + bill.amountPaid - mTotalReceived - mDiscount;
        }
        else{
            totalLeft = bill.amountPaid - mTotalReceived - mDiscount;
        }

        txtTotalLeftLayout.setVisibility(View.VISIBLE);
        if(totalLeft < 0){
            txtTotalLeftLabel.setText(getResources().getString(R.string.total_change));
            txtTotalLeft.setText(String.format("R$ %.2f", totalLeft * -1));
        }
        else if(totalLeft > 0){
            txtTotalLeftLabel.setText(getResources().getString(R.string.total_left));
            txtTotalLeft.setText(String.format("R$ %.2f", totalLeft));
        }
        else{
            txtTotalLeftLayout.setVisibility(View.GONE);
        }

        double totalWithService = bill.amountPaid * 0.1 + bill.amountPaid - mDiscount;
        if(!servicePaid && mTotalReceived < bill.amountPaid - mDiscount){
            txtTotalPaid.setTextColor(getResources().getColor(R.color.red));
            txtTotalPaidLabel.setTextColor(getResources().getColor(R.color.red));

            txtTotalLeft.setTextColor(getResources().getColor(R.color.red));
            txtTotalLeftLabel.setTextColor(getResources().getColor(R.color.red));
        }
        else if(servicePaid && mTotalReceived < totalWithService){
            txtTotalPaid.setTextColor(getResources().getColor(R.color.red));
            txtTotalPaidLabel.setTextColor(getResources().getColor(R.color.red));

            txtTotalLeft.setTextColor(getResources().getColor(R.color.red));
            txtTotalLeftLabel.setTextColor(getResources().getColor(R.color.red));
        }
        else if(!servicePaid && mTotalReceived > bill.amountPaid - mDiscount){
            txtTotalPaid.setTextColor(getResources().getColor(R.color.dark_green));
            txtTotalPaidLabel.setTextColor(getResources().getColor(R.color.dark_green));

            txtTotalLeft.setTextColor(getResources().getColor(R.color.dark_green));
            txtTotalLeftLabel.setTextColor(getResources().getColor(R.color.dark_green));
        }
        else if(servicePaid && mTotalReceived > totalWithService){
            txtTotalPaid.setTextColor(getResources().getColor(R.color.dark_green));
            txtTotalPaidLabel.setTextColor(getResources().getColor(R.color.dark_green));

            txtTotalLeft.setTextColor(getResources().getColor(R.color.dark_green));
            txtTotalLeftLabel.setTextColor(getResources().getColor(R.color.dark_green));
        }
        else{
            txtTotalPaid.setTextColor(getResources().getColor(android.R.color.black));
            txtTotalPaidLabel.setTextColor(getResources().getColor(android.R.color.black));
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.chkMoneyOption){
            changeStateCustomRadio(chkMoneyOption, chkCardOption);
        }
        else if(v.getId() == R.id.chkCardOption){
            changeStateCustomRadio(chkCardOption, chkMoneyOption);
        }
        else if(v.getId() == R.id.chkServiceOption){
            calculateTotalPaid();
        }
        else if(v.getId() == R.id.btnAddAmount){
            addAmount();
        }
        else if(v.getId() == R.id.chkCouponIssue){
            if(chkCouponIssue.isChecked()){
                edtCpf.setVisibility(View.VISIBLE);
            }
            else{
                edtCpf.setVisibility(View.GONE);
                edtCpf.setText("");
            }
        }
        else if (v.getId() == R.id.btnConfirmPayment) {
            confirmPayment();
        }
        else if(v.getId() == R.id.btnDelAmount){
            Amount amount = (Amount) v.getTag();
            int index = mAmountList.indexOf(amount);
            mAmountList.remove(index);
            linAmount.removeViewAt(index);
            calculateTotalPaid();
        }
        else if(v.getId() == R.id.btnTotal){
            fillValueTotal();
        }
    }

    private void fillValueTotal() {
        double value;
        if(chkServiceOption.isChecked()){
            value = bill.amountPaid * 1.1 - mDiscount;
        }
        else{
            value = bill.amountPaid - mDiscount;
        }

        if(value > 0) {
            edtValue.setText(String.format(Locale.US, "%.2f", value));
        }
    }

    private void confirmPayment(){
        bill.servicePaid = chkServiceOption.isChecked();
        if(mAmountList == null || mAmountList.isEmpty()){
            if(mAmountList == null) mAmountList = new ArrayList<>();
            Amount amount = new Amount(mTotalReceived, getPaidMethod(), bill);
            mAmountList.add(amount);
        }

        if(chkCouponIssue.isChecked()){
            new CouponTask().execute();
        }
        else{
            closeBill();
        }
    }

    private void closeBill(){
        bill.syncStatus = KeysContract.NO_SYNCHRONIZED_STATUS_KEY;
        bill.billTime = new Date();
        bill.paidTime = new Date();
        bill.table.clientTemp = null;
        bill.table.tableTime = new Date();
        bill.table.syncStatus = KeysContract.NO_SYNCHRONIZED_STATUS_KEY;

        Intent intent = new Intent(context, DataBaseService.class);
        intent.putExtra(KeysContract.METHOD_KEY, DataBaseService.UPDATE_BILL);
        intent.putExtra(KeysContract.BILL_KEY, bill);
        context.startService(intent);

        intent.putExtra(KeysContract.METHOD_KEY, DataBaseService.INSERT_AMOUNT);
        intent.putExtra(KeysContract.AMOUNT_KEY, mAmountList);
        context.startService(intent);

        getActivity().getSupportFragmentManager().popBackStackImmediate();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        calculateTotalPaid();
    }

    class CouponTask extends AsyncTask<Void, Void, Integer>{

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("Aguarde...");
            dialog.setIndeterminate(true);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            if(discountOk(mDiscount)) {
                return Fiscal.execute(getActivity(), edtCpf.getText().toString(), bill, mAmountList, mDiscount);
            }
            else{
                return Fiscal.SAT_INVALID_DISCOUNT_ERROR;
            }
        }

        private boolean discountOk(double discount) {
            if(chkServiceOption.isChecked() &&
                    discount <= bill.amountPaid * 1.1){
                return true;
            }
            else if(!chkServiceOption.isChecked() &&
                    discount <= bill.amountPaid){
                return true;
            }

            return false;
        }

        @Override
        protected void onPostExecute(Integer result) {
            showSatMessage(result);

            if(linAmount.getChildCount() <= 0){
                mAmountList.clear();
            }

            dialog.dismiss();
        }
    }

    private void showSatMessage(int result){
        if(result == Fiscal.SAT_CFE_SENT){
            Toast.makeText(getActivity(), Fiscal.easySatMessage, Toast.LENGTH_LONG).show();
            closeBill();
        }
        else if(result == Fiscal.SAT_NOT_CONNECTED){
            changeStateCouponIssue();
            AndroidUtil
                    .createDialog(getActivity(),
                            getResources().getString(R.string.coupon_send_error_title),
                            getResources().getString(R.string.sat_not_connected_error_message))
                    .setPositiveButton("OK", null).show();
        }
        else if(result == Fiscal.SAT_PRINT_ERROR){
            Toast.makeText(getActivity(), Fiscal.easySatMessage, Toast.LENGTH_LONG).show();
            AndroidUtil
                    .createDialog(getActivity(),
                            getResources().getString(R.string.coupon_print_error_title),
                            getResources().getString(R.string.coupon_print_error))
                    .setPositiveButton("OK", null).show();
            closeBill();
        }
        else if(result == Fiscal.SAT_CREATE_XML_ERROR){
            AndroidUtil
                    .createDialog(getActivity(),
                            getResources().getString(R.string.coupon_send_error_title),
                            getResources().getString(R.string.sat_xml_create_error))
                    .setPositiveButton("OK", null).show();
        }
        else if(result == Fiscal.SAT_GET_PRODUCTS_ERROR){
            AndroidUtil
                    .createDialog(getActivity(),
                            getResources().getString(R.string.coupon_send_error_title),
                            getResources().getString(R.string.sat_get_products_error))
                    .setPositiveButton("OK", null).show();
        }
        else if(result == Fiscal.SAT_UNKNOWN_ERROR){
            AndroidUtil
                    .createDialog(getActivity(),
                            getResources().getString(R.string.coupon_send_error_title),
                            getResources().getString(R.string.sat_unknown_error))
                    .setPositiveButton("OK", null).show();
        }
        else if(result == Fiscal.SAT_CFE_SENT_ERROR){
            AndroidUtil
                    .createDialog(getActivity(),
                            getResources().getString(R.string.coupon_send_error_title),
                            Fiscal.easySatMessage)
                    .setPositiveButton("OK", null).show();
        }
        else if(result == Fiscal.SAT_BUFFER_ERROR){
            AndroidUtil
                    .createDialog(getActivity(),
                            getResources().getString(R.string.coupon_send_error_title),
                            Fiscal.easySatMessage)
                    .setPositiveButton("OK", null).show();
        }
        else if(result == Fiscal.SAT_SERIAL_NUMBER_ERROR){
            AndroidUtil
                    .createDialog(getActivity(),
                            getResources().getString(R.string.coupon_send_error_title),
                            getResources().getString(R.string.sat_serial_number_error))
                    .setPositiveButton("OK", null).show();
        }
        else if(result == Fiscal.SAT_INVALID_DISCOUNT_ERROR){
            AndroidUtil
                    .createDialog(getActivity(),
                            getResources().getString(R.string.coupon_send_error_title),
                            getResources().getString(R.string.sat_invalid_discount_error))
                    .setPositiveButton("OK", null).show();
        }
        else if(result == Fiscal.SAT_TAX_NOT_FOUND_ERROR){
            AndroidUtil
                    .createDialog(getActivity(),
                            getResources().getString(R.string.coupon_send_error_title),
                            String.format(Locale.US, getResources().getString(R.string.sat_tax_not_found_error), Fiscal.LAST_PRODUCT))
                    .setPositiveButton("OK", null).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mUsbReceiver, new IntentFilter(SAT_USB_RECEIVER));
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(mUsbReceiver);
        super.onPause();
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            changeStateCouponIssue();
        }
    };
}
