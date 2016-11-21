package io.oxigen.quiosgrama.fragment;


import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.service.HttpService;
import io.oxigen.quiosgrama.service.PrintService;
import io.oxigen.quiosgrama.util.AndroidUtil;


public class ReportFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, DatePickerDialog.OnDateSetListener {

    TextView txtReport;
    TextView txtDate;
    SwipeRefreshLayout swipeRefreshLayout;
    ProgressBar syncProgress;
    private String mDateString;
    private int mDay;
    private int mMonth;
    private int mYear;
    private Menu mMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_report, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        txtReport = (TextView) view.findViewById(R.id.txtReport);
        txtDate = (TextView) view.findViewById(R.id.txtDate);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        syncProgress = (ProgressBar) view.findViewById(R.id.syncProgress);

        Date date = new Date();
        mDateString = new SimpleDateFormat(KeysContract.SIMPLE_DATE_FORMAT_KEY, Locale.US).format(date);
        txtDate.setText(new SimpleDateFormat(getResources().getString(R.string.date_format), Locale.US).format(date));

        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        swipeRefreshLayout.setOnRefreshListener(this);
        txtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });
        new ReportTask(getActivity()).execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mMenu = menu;
        inflater.inflate(R.menu.report, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_print:
                print();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void print() {
        Intent intent = new Intent(getActivity(), PrintService.class);
        intent.putExtra(KeysContract.METHOD_KEY, PrintService.PRINT_REPORT);
        intent.putExtra(KeysContract.REPORT_KEY, txtReport.getText().toString());
        getActivity().startService(intent);
    }

    private void showDatePicker() {


        // Create a new instance of DatePickerDialog and return it
        new DatePickerDialog(getActivity(), this, mYear, mMonth, mDay).show();
    }

    @Override
    public void onRefresh() {
        if(syncProgress.getVisibility() != View.VISIBLE) {
            new ReportTask(getActivity()).execute();
        }
        else{
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        mDay = i2;
        mMonth = i1;
        mYear = i;

        txtDate.setText(String.format("%s/%s/%s", i2, i1+1, i));
        mDateString = String.format("%s-%s-%s", i, i1+1, i2);
        onRefresh();
    }

    private class ReportTask extends AsyncTask<Void, Void, String>{

        private Context mContext;

        public ReportTask(Context context){
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

            Container push = new Container(AndroidUtil.getImei(mContext), mDateString);
            String jsonString = gson.toJson(push);

            return HttpService.getWithPost(mContext, String.class,
                    getResources().getString(R.string.get_report), jsonString, HttpService.CONTENT_TYPE_TEXT);
        }

        @Override
        protected void onPostExecute(String result) {
            if(result == null){
                txtReport.setText("");
            }
            else{
                txtReport.setText(Html.fromHtml(result));
            }
            swipeRefreshLayout.setRefreshing(false);
            syncProgress.setVisibility(View.GONE);
            mMenu.findItem(R.id.action_print).setVisible(true);
        }

        private class Container{
            @Expose
            public String imei;
            @Expose
            public String date;

            public Container(String imei, String date){
                this.imei = imei;
                this.date = date;
            }
        }
    }
}
