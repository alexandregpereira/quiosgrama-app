package io.oxigen.quiosgrama.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.google.zxing.integration.android.IntentIntegrator;

import io.oxigen.quiosgrama.Bill;
import io.oxigen.quiosgrama.Functionary;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.listener.BackFragmentListener;
import io.oxigen.quiosgrama.service.SyncServerService;
import io.oxigen.quiosgrama.util.AndroidUtil;
import io.oxigen.quiosgrama.util.QrCodeUtil;

public class QrCodeFragment extends DialogFragment implements View.OnClickListener{

    private BackFragmentListener backListener;

    private ImageView imgQrCode;
    private ImageView imgQrCodeNoCamera;
    private TextView txtZombie;
    private TextView txtClientZombieNoCamera;
    private TextView txtResetZombie;
    private Button btnStartScanner;

    QuiosgramaApp app;
    private Bill mBill;

    public static QrCodeFragment newInstance(Bill bill) {
        QrCodeFragment f = new QrCodeFragment();
        Bundle data = new Bundle();
        data.putParcelable(KeysContract.BILL_KEY, bill);
        f.setArguments(data);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(mBill != null){
            return super.onCreateView(inflater, container, savedInstanceState);
        }
        else {
            View v = inflater.inflate(R.layout.fragment_qr_code, container, false);
            app = (QuiosgramaApp) getActivity().getApplicationContext();

            imgQrCode = (ImageView) v.findViewById(R.id.imgQrCode);
            imgQrCodeNoCamera = (ImageView) v.findViewById(R.id.imgQrCodeNoCamera);
            txtZombie = (TextView) v.findViewById(R.id.txtZombie);
            txtResetZombie = (TextView) v.findViewById(R.id.txtResetZombie);
            txtClientZombieNoCamera = (TextView) v.findViewById(R.id.txtClientZombieNoCamera);
            btnStartScanner = (Button) v.findViewById(R.id.btnStartScanner);
            Toolbar toolbar = (Toolbar) v.findViewById(R.id.tool_bar);

            btnStartScanner.setOnClickListener(this);

            toolbar.setVisibility(View.GONE);

            return v;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = AndroidUtil.createCustomDialog(
                getActivity(),
                R.style.Dialog,
                R.layout.fragment_qr_code);

        app = (QuiosgramaApp) getActivity().getApplicationContext();

        imgQrCode = (ImageView) dialog.findViewById(R.id.imgQrCode);
        imgQrCodeNoCamera = (ImageView) dialog.findViewById(R.id.imgQrCodeNoCamera);
        txtZombie = (TextView) dialog.findViewById(R.id.txtZombie);
        txtResetZombie = (TextView) dialog.findViewById(R.id.txtResetZombie);
        txtClientZombieNoCamera = (TextView) dialog.findViewById(R.id.txtClientZombieNoCamera);
        btnStartScanner = (Button) dialog.findViewById(R.id.btnStartScanner);
        Toolbar toolbar = (Toolbar) dialog.findViewById(R.id.tool_bar);

        btnStartScanner.setOnClickListener(this);

        Bundle data = getArguments();
        mBill = data.getParcelable(KeysContract.BILL_KEY);

        toolbar.setNavigationIcon(R.drawable.abc_ic_clear_mtrl_alpha);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        toolbar.setTitle(mBill.toString() + " - " + getResources().getString(R.string.zombie));
        toolbar.setBackgroundResource(R.drawable.shape_title_top);
        GradientDrawable shapeDrawable = (GradientDrawable) toolbar.getBackground();
        shapeDrawable.setColor(getResources().getColor(R.color.colorPrimary));

        LinearLayout mainLayout = (LinearLayout) dialog.findViewById(R.id.mainLayout);
        mainLayout.setBackgroundResource(R.drawable.shape_bottom);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(0, 0, 0, 0);
        mainLayout.setLayoutParams(params);

        dialog.findViewById(R.id.parentContainer).setBackgroundResource(android.R.color.transparent);

        loadCode();

        return dialog;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        loadCode();
    }

    public void loadCode() {
        Handler handler = new Handler();
        if(!AndroidUtil.isMyServiceRunning(getActivity(), SyncServerService.class.getName())) {
            btnStartScanner.setVisibility(View.VISIBLE);

            Functionary functionary = app.getFunctionarySelected();
            if (!QuiosgramaApp.connectionFailed) {
                if (functionary != null && functionary.adminFlag == Functionary.ADMIN) {
                    txtZombie.setText(getResources().getString(R.string.functionary_zombie_description));
                } else {
                    txtZombie.setText(getResources().getString(R.string.client_zombie_description));
                }

                if(mBill != null){
                    txtZombie.setText(String.format(
                            getResources().getString(R.string.bill_zombie_description),
                            mBill));
                    btnStartScanner.setVisibility(View.GONE);
                }
                else{
                    txtResetZombie.setVisibility(View.VISIBLE);
                }

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        String host = preferences.getString(getResources().getString(R.string.preference_host_key), "127.0.0.1");
                        String port = preferences.getString(getResources().getString(R.string.preference_port_key), "8080");

                        String pattern = null;
                        if(mBill != null){
                            pattern = String.format("%s:%s:%s:%s", host, port, AndroidUtil.getImei(getActivity()), mBill.id);
                        }
                        else{
                            pattern = String.format("%s:%s:%s", host, port, AndroidUtil.getImei(getActivity()));
                        }

                        QrCodeTask task = new QrCodeTask(imgQrCode);
                        task.execute(pattern);

                    }
                }, 100);
            } else if (functionary == null) {
                txtZombie.setText(getResources().getString(R.string.new_client_zombie));
                txtClientZombieNoCamera.setVisibility(View.VISIBLE);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        QrCodeTask task = new QrCodeTask(imgQrCodeNoCamera);
                        task.execute();
                    }
                }, 100);
            } else {
                txtZombie.setText(getResources().getString(R.string.zombie_no_connection));
            }
        } else {
            txtZombie.setText(getResources().getString(R.string.loading));
            imgQrCode.setVisibility(View.GONE);
            imgQrCodeNoCamera.setVisibility(View.GONE);
            txtResetZombie.setVisibility(View.GONE);
            txtClientZombieNoCamera.setVisibility(View.GONE);
            btnStartScanner.setVisibility(View.GONE);
        }
    }

    private void startQrCodeScanner() {
        IntentIntegrator integrator = new IntentIntegrator(getActivity());
        integrator.initiateScan();
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
    public void onClick(View view) {
        startQrCodeScanner();
    }

    class QrCodeTask extends AsyncTask<String, Void, Bitmap> {

        private final ImageView mImageView;

        public QrCodeTask(ImageView imageView){
            mImageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                String data = null;
                if (strings != null && strings.length > 0) {
                    data = strings[0];
                }
                if (data == null) {
                    data = "0:" + AndroidUtil.getImei(getActivity());
                }
                return QrCodeUtil.encodeAsBitmap(getActivity(), data, txtZombie.getWidth(), txtZombie.getWidth());
            } catch (WriterException e) {
                e.printStackTrace();
            } catch (Exception e){

            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mImageView.setImageBitmap(bitmap);
            mImageView.setVisibility(View.VISIBLE);
        }
    }
}
