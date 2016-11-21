package io.oxigen.quiosgrama.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.fragment.ReportFragment;

public class ReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.report));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ReportFragment fragment = new ReportFragment();
        FragmentManager fManager = getSupportFragmentManager();
        fManager.beginTransaction().add(R.id.fragmentContent, fragment, ReportFragment.class.getName()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
