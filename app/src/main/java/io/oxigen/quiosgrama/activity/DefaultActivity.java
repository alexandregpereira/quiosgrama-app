package io.oxigen.quiosgrama.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import io.oxigen.quiosgrama.Bill;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.fragment.AmountStackFragment;

public class DefaultActivity extends AppCompatActivity {

    public static final int AMOUNT_STACK_FRAGMENT_KEY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);

        Bundle bundle = getIntent().getExtras();
        int fragmentKey = bundle.getInt(KeysContract.METHOD_KEY);

        Fragment fragment = getFragmentByKey(fragmentKey, bundle);

        if(fragment != null){
            FragmentManager fManager = getSupportFragmentManager();
            fManager.beginTransaction().add(R.id.fragmentContent, fragment, fragment.getClass().getName()).commit();
        }
    }

    private Fragment getFragmentByKey(int fragmentKey, Bundle bundle) {
        switch (fragmentKey){
            case AMOUNT_STACK_FRAGMENT_KEY:
                Bill bill = bundle.getParcelable(KeysContract.BILL_KEY);
                return AmountStackFragment.newInstance(bill);
        }

        return null;
    }
}
