package io.oxigen.quiosgrama.activity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import io.oxigen.quiosgrama.Functionary;
import io.oxigen.quiosgrama.ProductRequest;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.fiscal.Fiscal;
import io.oxigen.quiosgrama.fragment.AmountStackFragment;
import io.oxigen.quiosgrama.fragment.CategoryFragment;
import io.oxigen.quiosgrama.fragment.CategoryFragment.CategoryFragmentListener;
import io.oxigen.quiosgrama.fragment.ComplementDialogFragment.ComplementDialogListener;
import io.oxigen.quiosgrama.fragment.MapPagerFragment;
import io.oxigen.quiosgrama.fragment.MapPagerFragment.MapPagerListener;
import io.oxigen.quiosgrama.fragment.QrCodeFragment;
import io.oxigen.quiosgrama.fragment.RequestFragment;
import io.oxigen.quiosgrama.fragment.RequestPagerFragment;
import io.oxigen.quiosgrama.fragment.SendRequestFragment.SendRequestListener;
import io.oxigen.quiosgrama.fragment.TableInfoDialogFragment;
import io.oxigen.quiosgrama.listener.BackFragmentListener;
import io.oxigen.quiosgrama.sat.EasySAT;
import io.oxigen.quiosgrama.service.SocketPushServerService;
import io.oxigen.quiosgrama.service.SyncServerService;
import io.oxigen.quiosgrama.util.AndroidUtil;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
		CategoryFragmentListener, ComplementDialogListener,
		SendRequestListener, MapPagerListener, BackFragmentListener, RequestFragment.RequestFragmentListener {

	public static final String RECEIVER_FILTER = "io.oxigen.quiosgrama.filter.MAIN_ACTIVITY_RECEIVER_FILTER";

	public static final int RESULT_SUCCESS_SYNC = 1;
	public static final int RESULT_START_SYNC = 10;
	public static final int RESULT_NO_GOOGLE_PLAY = 11;
	public static final int RESULT_MESSAGE = 12;
	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

	private DrawerLayout drawerLayout;
	private TextView txtWaiter;
	private TextView txtKiosk;
	private ActionBarDrawerToggle drawerToggle;
	private ProgressBar syncProgress;

	private CategoryFragment categoryFragment;
	private RequestFragment requestFragment;
	private MapPagerFragment mapPagerFragment;
	private RequestPagerFragment requestPagerFragment;
	private QrCodeFragment qrCodeFragment;
	private TableInfoDialogFragment tableInfoDialogFragment;

	QuiosgramaApp app;
	private SyncMainReceiver syncMainReceiver;
	private Menu mMenuNavigation;
	private Toolbar mToolbar;
	private Snackbar mSnackbar;
	private PendingIntent usbPermissionIntent;

	@Override
	public void onBackPressed() {
		if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
			drawerLayout.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		app = (QuiosgramaApp) getApplication();

//		getSupportActionBar().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.background_actionbar, null));
		mToolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		resolveIntent();

		boolean isTablet = getResources().getBoolean(R.bool.is_tablet);
		if(isTablet){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		else{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		registerReceiver(mUsbReceiver, new IntentFilter(ACTION_USB_PERMISSION));
		registerReceiver(mUsbReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));
		registerReceiver(mUsbReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED));
		usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		Fiscal.connectEasySat(app, usbPermissionIntent);

		buildWidgets();

		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.main, R.string.main);

		drawerLayout.setDrawerListener(drawerToggle);
		drawerToggle.syncState();

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		QuiosgramaApp.firstTime = preferences.getBoolean(getResources().getString(R.string.first_time_key), true);
		buildMenuNavigation();

		if(QuiosgramaApp.firstTime){
			QuiosgramaApp.connectionFailed = true;
		}

		categoryFragment = new CategoryFragment();
		FragmentManager fManager = getSupportFragmentManager();
		if (savedInstanceState == null) {
			fManager.beginTransaction().add(R.id.fragmentContent, categoryFragment, CategoryFragment.class.getName()).commit();
			onChangeMapTitle(getResources().getString(R.string.category));
		}

		Intent intentService = new Intent(this, SocketPushServerService.class);
		startService(intentService);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
		
		switch (item.getItemId()) {
			case R.id.action_sync:
				startSync(null);
				return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	private void startSync(String imei){
		if(!AndroidUtil.isMyServiceRunning(this, SyncServerService.class.getName())){
			syncProgress.setVisibility(View.GONE);
			Intent intentService = new Intent(this, SyncServerService.class);
			intentService.putExtra(KeysContract.METHOD_KEY, SyncServerService.GET_OBJECT_CONTAINER);
			if(imei != null){
				intentService.putExtra(KeysContract.IMEI_KEY, imei);

				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString(SyncServerService.PROPERTY_REG_IP, "");
				editor.commit();
			}
			startService(intentService);
		}
	}
	
	protected void onPostCreate(Bundle savedInstanceState) {
	    super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
	    drawerToggle.syncState();
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		if(syncMainReceiver == null) {
			syncMainReceiver = new SyncMainReceiver();
		}

		registerReceiver(syncMainReceiver, new IntentFilter(RECEIVER_FILTER));

		app = (QuiosgramaApp) getApplication();
	}
	
	@Override
	protected void onPause() {
		unregisterReceiver(syncMainReceiver);
		super.onPause();
	}
	
	private void buildWidgets() {
		NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);
		drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
		txtWaiter = (TextView) navigationView.findViewById(R.id.txtWaiter);
		txtKiosk = (TextView) navigationView.findViewById(R.id.txtKiosk);
		syncProgress = (ProgressBar) findViewById(R.id.syncProgress);

		if(app.getFunctionarySelected() != null) {
			txtWaiter.setText(app.getFunctionarySelected().toString());
		}
		else if(app.getBillSelected() != null){
			txtWaiter.setText(app.getBillSelected().toString());
		}

		navigationView.setNavigationItemSelectedListener(this);
		mMenuNavigation = navigationView.getMenu();
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		// Handle navigation view item clicks here.
		int id = item.getItemId();
		onNavigationItemSelected(id);

		return true;
	}

	private void onNavigationItemSelected(int id){
		FragmentManager fManager = getSupportFragmentManager();
		String actionBarTitle = (String) getSupportActionBar().getTitle();
		Fragment fragment = null;
		boolean fragmentChange = true;
		boolean home = false;

		if (id == R.id.nav_category) {
			actionBarTitle = getResources().getString(R.string.category);
			fragment = categoryFragment;
			home = true;
		} else if (id == R.id.nav_menu) {
			actionBarTitle = getResources().getString(R.string.menu);
			requestFragment = RequestFragment.newInstance(0);
			fragment = requestFragment;
		} else if (id == R.id.nav_map) {
			if(mapPagerFragment == null){
				mapPagerFragment = new MapPagerFragment();
			}
			actionBarTitle = getResources().getString(R.string.action_map);
			fragment = mapPagerFragment;
		} else if (id == R.id.nav_request) {
			if(requestPagerFragment == null){
				requestPagerFragment = new RequestPagerFragment();
			}

			fragment = requestPagerFragment;
			actionBarTitle = getResources().getString(R.string.request);
		} else if (id == R.id.nav_request_client) {
			tableInfoDialogFragment = TableInfoDialogFragment.newInstance(app.getBillSelected());
			tableInfoDialogFragment.show(getSupportFragmentManager(),
					TableInfoDialogFragment.class.getName());
			fragmentChange = false;
		} else if (id == R.id.nav_zombie) {
			if(qrCodeFragment == null){
				qrCodeFragment = new QrCodeFragment();
			}

			fragment = qrCodeFragment;
			actionBarTitle = getResources().getString(R.string.zombie);
		}
		else if (id == R.id.nav_report) {
			startActivity(new Intent(this, ReportActivity.class));
			fragmentChange = false;
		}
		else if (id == R.id.nav_settings) {
			startActivity(new Intent(this, AppPreferencesActivity.class));
			fragmentChange = false;
		}
		else if (id == R.id.nav_sat_test) {
			startActivity(new Intent(this, SatActivity.class));
			fragmentChange = false;
		}

		if(fragmentChange){
			onChangeMapTitle(actionBarTitle);

			if(fragment != null && !fragment.isResumed()) {
				fManager.popBackStack();

				FragmentTransaction transaction = fManager.beginTransaction();
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				transaction.add(R.id.fragmentContent, fragment, fragment.getClass().getName()).addToBackStack("fragStack").commit();
			}
			else if(home){
				fManager.popBackStack();
			}
		}

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
		drawer.closeDrawer(GravityCompat.START);
	}

	public void onSyncCompleted() {
		int functionaryType = app.getFunctionarySelectedType();

		if(functionaryType == Functionary.ADMIN) {
			txtWaiter.setText(app.getFunctionarySelected().name + " - " + getResources().getString(R.string.admin));
		}
		else if(functionaryType == Functionary.WAITER) {
			txtWaiter.setText(app.getFunctionarySelected().name + " - " + getResources().getString(R.string.waiter));
		}
		else if(functionaryType == Functionary.CLIENT_WAITER) {
			txtWaiter.setText(app.getFunctionarySelected().name + " - " + getResources().getString(R.string.client));
		}
		else if(app.getBillSelected() != null){
			txtWaiter.setText(app.getBillSelected().toString());
		}
		else{
			txtWaiter.setText("");
		}

		if(!QuiosgramaApp.getLicence().isEmpty()){
			txtKiosk.setText(QuiosgramaApp.getKioskName());
		}
		else{
			txtKiosk.setText("");
		}
		
		categoryFragment.onSyncCompleted();
		if(requestFragment != null && requestFragment.isResumed()){
			requestFragment.buildMenu();
		}
	}

	@Override
	public void onCategoryClicked(int position) {
		onChangeMapTitle(getResources().getString(R.string.menu));
		requestFragment = RequestFragment.newInstance(position);

		FragmentManager fManager = getSupportFragmentManager();
		FragmentTransaction transaction = fManager.beginTransaction();
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		fManager.popBackStack();
		transaction.add(R.id.fragmentContent, requestFragment, RequestFragment.class.getName()).addToBackStack("fragStack").commit();
		requestFragment.changeTabSelected(position);
	}

	public void onAddProductListener(View v){
		requestFragment.onAddProductListener(v);
	}
	
	public void onRemoveProductListener(View v){
		requestFragment.onRemoveProductListener(v);
	}
	
	public void onAddProductRequestListener(View v){
		if(requestFragment != null && requestFragment.isResumed()) requestFragment.onAddProductRequestListener(v);
		if(mapPagerFragment != null && mapPagerFragment.isResumed()) mapPagerFragment.onAddProductRequestListener(v);
	}
	
	public void onRemoveProductRequestListener(View v){
		if(requestFragment != null && requestFragment.isResumed()) requestFragment.onRemoveProductRequestListener(v);
		if(mapPagerFragment != null && mapPagerFragment.isResumed()) mapPagerFragment.onRemoveProductRequestListener(v);
	}
	
	public void onAddComplementListener(View v){
		if(requestFragment != null && requestFragment.isResumed())
			requestFragment.onAddComplementListener(v);
		else if(mapPagerFragment != null && mapPagerFragment.isResumed()){
			mapPagerFragment.onAddComplementListener(v);
		}
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(requestFragment != null && requestFragment.isResumed() ){
			if(requestFragment.onKeyUp(keyCode, event))
				return super.onKeyUp(keyCode, event);
			else
				return false;
		}
		
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onConfirmComplement(ProductRequest oldProductRequest, ProductRequest newProductRequest) {
		requestFragment.onConfirmComplement(oldProductRequest, newProductRequest);
	}

	@Override
	public void onCancelComplement() {
		requestFragment.onCancelComplement();
	}

	@Override
	public void onSendRequestResumed() {
		getSupportActionBar().hide();
	}

	@Override
	public void onSendRequestPaused() {
		getSupportActionBar().show();
	}

	@Override
	public void onConfirmRequest() {
		if(requestFragment != null && requestFragment.isResumed()) requestFragment.onConfirmRequest();
	}

	@Override
	public void onSaveRequest() {
		requestFragment.onSaveRequest();
	}

	@Override
	public void onChangeMapTitle(String title) {
		getSupportActionBar().setTitle(title);
	}

	@Override
	public void backFragmentPressed() {
		if((requestFragment == null || !requestFragment.isResumed())
				&& (mapPagerFragment == null || !mapPagerFragment.isResumed())
				&& (requestPagerFragment == null || !requestPagerFragment.isResumed())
				&& (qrCodeFragment == null || !qrCodeFragment.isResumed())){
			String actionBarTitle = getResources().getString(R.string.category);
			mToolbar.setBackgroundResource(R.color.colorPrimary);
			onChangeMapTitle(actionBarTitle);
			try {
				FragmentManager fManager = getSupportFragmentManager();
				fManager.popBackStack();
			} catch(Exception e){
				Log.e("MainActivity", "Erro no backFragmentPressed(): " + e.getMessage());
			}
		}
	}

	@Override
	public void onSearchToolbarClicked(){
		getSupportActionBar().hide();
	}

	@Override
	public void onSearchToolbarClose(){
		getSupportActionBar().show();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if (scanResult != null) {
			String content = scanResult.getContents();
			if(content != null){
				String[] split = content.split(":");
				if(split.length > 2) {
					String host = split[0];
					String port = split[1];
					String imei = split[2];
					String billId = null;
					if (split.length > 3) billId = split[3];

					if (host != null && port != null && imei != null) {
						SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
						SharedPreferences.Editor editor = preferences.edit();
						if (billId != null) {
							editor.putString(getResources().getString(R.string.bill_key), billId);
						}
						editor.putString(getResources().getString(R.string.preference_host_key), host);
						editor.putString(getResources().getString(R.string.preference_port_key), port);
						editor.commit();

						startSync(imei);
					}
				}
				else if(!QuiosgramaApp.connectionFailed){
					String imeiRegistraion = split[1];
					if(imeiRegistraion != null && imeiRegistraion.length() > 5){
						Intent intentService = new Intent(this, SyncServerService.class);
						intentService.putExtra(KeysContract.METHOD_KEY, SyncServerService.SEND_REGISTRATION_ID);
						intentService.putExtra(KeysContract.IMEI_KEY, AndroidUtil.getImei(this));
						intentService.putExtra(KeysContract.IMEI_REGISTRATION_KEY, imeiRegistraion);

						startService(intentService);
					}
					else{
						Snackbar.make(findViewById(R.id.drawerLayout), getResources().getString(R.string.qr_code_invalid), Snackbar.LENGTH_LONG).show();
					}
				}
				else{
					Snackbar.make(findViewById(R.id.drawerLayout), getResources().getString(R.string.qr_code_invalid), Snackbar.LENGTH_LONG).show();
				}

				FragmentManager fManager = getSupportFragmentManager();
				fManager.popBackStack();
				onChangeMapTitle(getResources().getString(R.string.category));
			}
		}
	}

	private void buildMenuNavigation(){
		if(!QuiosgramaApp.firstTime) {
			mMenuNavigation.findItem(R.id.nav_category).setVisible(true);
			mMenuNavigation.findItem(R.id.nav_menu).setVisible(true);

			int functionaryType = app.getFunctionarySelectedType();
			boolean clear = false;
			if (functionaryType != Functionary.ADMIN && functionaryType != Functionary.WAITER) {
				mMenuNavigation.findItem(R.id.nav_map).setVisible(false);
				if (mapPagerFragment != null && mapPagerFragment.isResumed()) {
					clear = true;
				}

				if (functionaryType == Functionary.CLIENT_WAITER
						&& app.getBillSelected() != null) {
					mMenuNavigation.findItem(R.id.nav_request).setVisible(true);
				} else {
					mMenuNavigation.findItem(R.id.nav_request).setVisible(false);
					if (requestPagerFragment != null && requestPagerFragment.isResumed()) {
						clear = true;
					}
				}

				mMenuNavigation.findItem(R.id.nav_report).setVisible(false);
				if (app.getBillSelected() != null) {
					mMenuNavigation.findItem(R.id.nav_request_client).setVisible(true);
					mMenuNavigation.findItem(R.id.nav_request_client).setTitle(app.getBillSelected().toString());
				} else {
					mMenuNavigation.findItem(R.id.nav_request_client).setVisible(false);
					if (tableInfoDialogFragment != null && tableInfoDialogFragment.isResumed()) {
						tableInfoDialogFragment.dismiss();
					}
				}
			} else {
				mMenuNavigation.findItem(R.id.nav_map).setVisible(true);
				mMenuNavigation.findItem(R.id.nav_request).setVisible(true);
				if (functionaryType == Functionary.ADMIN) {
					mMenuNavigation.findItem(R.id.nav_report).setVisible(true);
				} else {
					mMenuNavigation.findItem(R.id.nav_report).setVisible(false);
				}
				mMenuNavigation.findItem(R.id.nav_request_client).setVisible(false);
				if (tableInfoDialogFragment != null && tableInfoDialogFragment.isResumed()) {
					tableInfoDialogFragment.dismiss();
				}
			}

			if (clear) {
				FragmentManager fManager = getSupportFragmentManager();
				fManager.popBackStack();
				onChangeMapTitle(getResources().getString(R.string.category));
			}
		}
		else{
			mMenuNavigation.findItem(R.id.nav_category).setVisible(false);
			mMenuNavigation.findItem(R.id.nav_menu).setVisible(false);
			mMenuNavigation.findItem(R.id.nav_map).setVisible(false);
			mMenuNavigation.findItem(R.id.nav_request).setVisible(false);
			mMenuNavigation.findItem(R.id.nav_request_client).setVisible(false);
			mMenuNavigation.findItem(R.id.nav_report).setVisible(false);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private class SyncMainReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			int result = intent.getExtras().getInt(KeysContract.RESULT_SYNC_KEY);

			boolean pass = true;
			if(result == RESULT_START_SYNC) {
				syncProgress.setVisibility(View.VISIBLE);
			}
			else if(result == RESULT_SUCCESS_SYNC){
				onSyncCompleted();
				syncProgress.setVisibility(View.GONE);
			}
			else if(result == RESULT_NO_GOOGLE_PLAY){
				final Snackbar snackBar = Snackbar.make(findViewById(R.id.drawerLayout), getResources().getString(R.string.no_google_play_services), Snackbar.LENGTH_INDEFINITE);

				snackBar.setAction("OK", new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						snackBar.dismiss();
					}
				});
				snackBar.show();

				pass = false;
			}
			else if(result == RESULT_MESSAGE){
				if(!QuiosgramaApp.firstTime) {
					String message = intent.getExtras().getString(KeysContract.MESSAGE_KEY);
					if (message != null && !message.trim().isEmpty()) {
						mSnackbar = Snackbar.make(findViewById(R.id.drawerLayout), message, Snackbar.LENGTH_INDEFINITE);
						View snackbarView = mSnackbar.getView();
						TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
						textView.setMaxLines(3);

						mSnackbar.setAction("OK", new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								mSnackbar.dismiss();
							}
						});
						mSnackbar.show();
					} else if (mSnackbar != null) {
						mSnackbar.dismiss();
					}
				}

				pass = false;
			}
			else{
				syncProgress.setVisibility(View.GONE);
			}

			if(pass) {
				buildMenuNavigation();

				if (qrCodeFragment != null && qrCodeFragment.isResumed()) {
					qrCodeFragment.loadCode();
				}
			}
		}
		
	}

	/**
	 * Processa eventual intent dando permissão USB ao app.
	 *
	 * Registra dispositivo e exibe mensagem.
	 */
	private void resolveIntent() {
		UsbDevice device = getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
		if (device != null) {
			app.easySat.setDevice(device);
			Toast toast = Toast.makeText(getApplicationContext(), "Conectado com SAT", Toast.LENGTH_LONG);
			toast.show();
		}
	}

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if (device != null){
							app.easySat.setDevice(device);
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.sat_connect), Toast.LENGTH_LONG).show();
							sendBroadcast(new Intent(AmountStackFragment.SAT_USB_RECEIVER));
						}
					}
				}
			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
				if (EasySAT.isEasySATDevice(device)) {
					app.easySat.setDevice(null);
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.sat_disconnect), Toast.LENGTH_LONG).show();
				}
			}
			else if(UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)){
				Fiscal.connectEasySat(app, usbPermissionIntent);
			}
		}
	};
}
