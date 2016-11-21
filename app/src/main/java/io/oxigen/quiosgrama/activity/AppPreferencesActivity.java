package io.oxigen.quiosgrama.activity;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import io.oxigen.quiosgrama.R;

@SuppressWarnings("deprecation")
public class AppPreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener  {

	private AppCompatDelegate mDelegate;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_preference);
        
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.action_settings));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        addPreferencesFromResource(R.xml.preferences);
        
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        
        EditTextPreference hostPref = (EditTextPreference) findPreference(getResources().getString(R.string.preference_host_key));
        hostPref.setSummary(sharedPreferences.getString(getResources().getString(R.string.preference_host_key), ""));
        
        EditTextPreference portPref = (EditTextPreference) findPreference(getResources().getString(R.string.preference_port_key));
        portPref.setSummary(sharedPreferences.getString(getResources().getString(R.string.preference_port_key), ""));
        
        EditTextPreference mapxPref = (EditTextPreference) findPreference(getResources().getString(R.string.preference_map_x_key));
        mapxPref.setSummary(sharedPreferences.getString(getResources().getString(R.string.preference_map_x_key), ""));
        
        EditTextPreference mapyPref = (EditTextPreference) findPreference(getResources().getString(R.string.preference_map_y_key));
        mapyPref.setSummary(sharedPreferences.getString(getResources().getString(R.string.preference_map_y_key), ""));
        
        EditTextPreference mapMarginRightPref = (EditTextPreference) findPreference(getResources().getString(R.string.preference_map_margin_right_key));
        mapMarginRightPref.setSummary(sharedPreferences.getString(getResources().getString(R.string.preference_map_margin_right_key), ""));
        
        EditTextPreference mapMarginLeftPref = (EditTextPreference) findPreference(getResources().getString(R.string.preference_map_margin_left_key));
        mapMarginLeftPref.setSummary(sharedPreferences.getString(getResources().getString(R.string.preference_map_margin_left_key), ""));

        EditTextPreference printerIpPref = (EditTextPreference) findPreference(getResources().getString(R.string.preference_printer_ip_key));
        printerIpPref.setSummary(sharedPreferences.getString(getResources().getString(R.string.preference_printer_ip_key), ""));

        EditTextPreference satSerialNumberPref = (EditTextPreference) findPreference(getResources().getString(R.string.preference_sat_serial_number_key));
        satSerialNumberPref.setSummary(sharedPreferences.getString(getResources().getString(R.string.preference_sat_serial_number_key), ""));
        
        getPreferenceScreen().getSharedPreferences()
        	.registerOnSharedPreferenceChangeListener(this);
    }
    
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        Preference pref = findPreference(key);
        if (pref instanceof EditTextPreference) {
            EditTextPreference etp = (EditTextPreference) pref;
            pref.setSummary(etp.getText());
        }
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

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().onPostCreate(savedInstanceState);
    }

    public ActionBar getSupportActionBar() {
        return getDelegate().getSupportActionBar();
    }

    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        getDelegate().setSupportActionBar(toolbar);
    }

    @Override
    public MenuInflater getMenuInflater() {
        return getDelegate().getMenuInflater();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        getDelegate().setContentView(layoutResID);
    }

    @Override
    public void setContentView(View view) {
        getDelegate().setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().setContentView(view, params);
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().addContentView(view, params);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        getDelegate().setTitle(title);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getDelegate().onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getDelegate().onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }

    public void invalidateOptionsMenu() {
        getDelegate().invalidateOptionsMenu();
    }

    private AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }
    
}
