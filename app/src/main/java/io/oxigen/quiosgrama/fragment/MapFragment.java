package io.oxigen.quiosgrama.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

import io.oxigen.quiosgrama.Bill;
import io.oxigen.quiosgrama.Poi;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.Table;
import io.oxigen.quiosgrama.dao.PoiDao;
import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.service.DataBaseService;
import io.oxigen.quiosgrama.util.AndroidUtil;

public class MapFragment extends Fragment implements OnLongClickListener, OnTouchListener, OnClickListener{

	private static final int X_ADJUST_LDPI = 30;
	private static final int Y_ADJUST_LDPI = 140;

	private RelativeLayout mapLayout;
	private Table beforeTableAltered;
	private Table afterTableAltered;
	private Poi beforePoiAltered;
	private Poi afterPoiAltered;
	private int left;
	private int top;
	private int mapPageNumber;
	public boolean hasTable;
	private MapPagerFragment fragmentParent;
	private boolean pageNext = true;
	
	public boolean movingTable;
	
	QuiosgramaApp app;
	private int x_precision = 50;
	private int y_precision = 200;
	private int rightMapLimit = 280;
	private int leftMapLimit = -2;
	private float mDesnsity;
	private boolean isTablet;

	public static MapFragment newInstance(int pageNumber){
		MapFragment fragment = new MapFragment();
		Bundle data = new Bundle();
		data.putInt(KeysContract.MAP_PAGE_NUMBER_KEY, pageNumber);
		fragment.setArguments(data);
		return fragment;
	}
	
	public int getMapPageNumber(){
		return mapPageNumber;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.fragment_map, container, false);
		app = (QuiosgramaApp) getActivity().getApplication();
		isTablet = getResources().getBoolean(R.bool.is_tablet);

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		mDesnsity = metrics.density;

		mapLayout = (RelativeLayout) v.findViewById(R.id.mapLayout);
		
		Bundle data = getArguments();
		mapPageNumber = data.getInt(KeysContract.MAP_PAGE_NUMBER_KEY);
		fragmentParent = (MapPagerFragment) getParentFragment();
		
		buildMapObjects();
		
		return v;
	}

	@Override
	public void onResume() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		try{
			x_precision = Integer.valueOf(preferences.getString(getResources().getString(R.string.preference_map_x_key), "50"));
			y_precision = Integer.valueOf(preferences.getString(getResources().getString(R.string.preference_map_y_key), "200"));
			rightMapLimit = AndroidUtil.dpToPx(Integer.valueOf(preferences.getString(getResources().getString(R.string.preference_map_margin_right_key), "280")), getActivity());
			leftMapLimit = AndroidUtil.dpToPx(Integer.valueOf(preferences.getString(getResources().getString(R.string.preference_map_margin_left_key), "-2")), getActivity());
		} catch(NumberFormatException e){}
		super.onResume();
	}

	private void buildMapObjects(){
		FragmentActivity activity = getActivity();
		if(activity != null){
			LayoutInflater mapInflater = (LayoutInflater) getActivity().getSystemService
					(Context.LAYOUT_INFLATER_SERVICE);

			buildMapTables(mapInflater);
			buildMapPois(mapInflater);
		}
	}
	
	private void buildMapTables(LayoutInflater mapInflater){
		for (Bill bill: app.getBillList()) {
			Table table = bill.table;
			if(table != null) {
				boolean hasTable = table.mapPageNumber == mapPageNumber;
				if (hasTable)
					this.hasTable = hasTable;

				if (hasTable ||
						table.mapPageNumber == 0) {

					View btnTableMap = buildButtonTableMapComponents(mapInflater, bill,
							R.drawable.selector_ic_table_opened,
							R.drawable.selector_ic_table_closed,
							R.drawable.selector_ic_table_semi_closed);

					if (table.moved && mDesnsity < 1 && !isTablet) {
						table.xPosInDpi += X_ADJUST_LDPI;
						table.yPosDpi += Y_ADJUST_LDPI;
						table.moved = false;
					}
					setBtnMapParams(btnTableMap, table.xPosInDpi, table.yPosDpi);

					mapLayout.addView(btnTableMap);
				}
			}
		}
	}
	
	private void buildMapPois(LayoutInflater mapInflater){
		for (Poi poi : app.getPoiList()) {
			boolean hasTable = poi.mapPageNumber == mapPageNumber;
			if(hasTable)
				this.hasTable = hasTable;
			
			if( hasTable ||
					poi.mapPageNumber == 0){

				View btnPoiMap = buildButtonPoiMapComponents(mapInflater, poi, false);

				if(poi.moved && mDesnsity < 1 && !isTablet) {
					poi.xPosDpi += X_ADJUST_LDPI;
					poi.yPosDpi += Y_ADJUST_LDPI;
					poi.moved = false;
				}
				setBtnMapParams(btnPoiMap, poi.xPosDpi, poi.yPosDpi);

				mapLayout.addView(btnPoiMap);
			}
		}
	}

	/**
	 * Constroi uma View para ser apresentado no mapa
	 * @param mapInflater inflater para criar a View
	 * @param poi Objeto poi que ser usado na construcaoo da View
	 * @param selected Variavel que difini se a View sera marcada como selecioanda
	 * @return botao/icone que sera apresentado no mapa
	 */
	@SuppressLint("InflateParams")
	public static View buildButtonPoiMapComponents(LayoutInflater mapInflater, Poi poi, boolean selected) {
		View btnPoiMap = mapInflater.inflate(R.layout.button_poi_map, null);
		ImageView imgPoi = (ImageView) btnPoiMap.findViewById(R.id.imgPoi);
		TextView txtIdPoi = (TextView) btnPoiMap.findViewById(R.id.txtIdPoi);
		
		imgPoi.setBackgroundResource(AndroidUtil.buildImagesValue(poi.image));
		txtIdPoi.setText(String.valueOf(poi.idPoi));
		
		if(selected)
			btnPoiMap.setBackgroundResource(R.drawable.shape_color_primary);
		else
			btnPoiMap.setBackgroundResource(R.drawable.selector_gridview_complement);
		
		btnPoiMap.setTag(poi);
		
		return btnPoiMap;
	}
	
	@SuppressLint("InflateParams")
	public static View buildButtonSearchPoiMapComponents(LayoutInflater mapInflater, Poi poi) {
		View btnPoiMap = mapInflater.inflate(R.layout.button_search_poi_map, null);
		TextView txtPoi = (TextView) btnPoiMap.findViewById(R.id.txtButtonSearchPoiMap);
		
		txtPoi.setCompoundDrawablesWithIntrinsicBounds(AndroidUtil.buildImagesValue(poi.image), 0, 0, 0);
		txtPoi.setText(poi.name);
		btnPoiMap.setTag(poi);
		
		return btnPoiMap;
	}

	private void setBtnMapParams(View btnMap, int xPosDpi, int yPosDpi) {
		btnMap.setOnLongClickListener(this);
		btnMap.setOnClickListener(this);

		RelativeLayout.LayoutParams params =
				new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);

		int xDpi = xPosDpi;
		int yDpi = yPosDpi;
		if (mDesnsity < 1 && !isTablet) {
			xDpi -= X_ADJUST_LDPI;
			yDpi -= Y_ADJUST_LDPI;
			if (xDpi < 0) xDpi = 0;
			if (yDpi < 0) yDpi = 0;
		}

		params.setMargins(AndroidUtil.dpToPx(xDpi, getActivity()), AndroidUtil.dpToPx(yDpi, getActivity()), 0, 0);
		btnMap.setLayoutParams(params);
	}

	@SuppressLint("InflateParams")
	public static View buildButtonTableMapComponents(LayoutInflater inflater, Bill bill, 
			int selectorOpened, int selectorClosed, int selectorSemiClosed) {

		View btnTableMap = inflater.inflate(R.layout.button_table_map, null);
		TextView txtTableNumber = (TextView) btnTableMap.findViewById(R.id.txtTableNumber);
		ImageView imgCategory = (ImageView) btnTableMap.findViewById(R.id.imgCategory);
		
		txtTableNumber.setText(String.valueOf(bill.table.number));
		if(bill.paidTime != null)
			imgCategory.setBackgroundResource(selectorClosed);
		else if(bill.openTime != null && bill.closeTime != null)
			imgCategory.setBackgroundResource(selectorSemiClosed);
		else if(bill.openTime != null)
			imgCategory.setBackgroundResource(selectorOpened);
		else
			imgCategory.setBackgroundResource(selectorClosed);
		
		btnTableMap.setTag(bill);
		
		return btnTableMap;
	}

	@Override
	public boolean onLongClick(View v) {
		movingTable = true;
		Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(100);
		v.setOnTouchListener(this);
		v.setOnClickListener(null);
		
		fragmentParent.setViewPagerEnabled(false);
		
		return false;
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getActionMasked()){
			case MotionEvent.ACTION_MOVE:
				moveMapObject(v, event);
			break;
			case MotionEvent.ACTION_UP:
				if(v.getId() == R.id.btnTableMap)
					dropTable(v);
				else if(v.getId() == R.id.btnPoiMap)
					dropPoi(v);
			break;
		}
		return false;
	}
	
	private void dropPoi(View v) {
		v.setOnTouchListener(null);
		fragmentParent.setViewPagerEnabled(true);
		pageNext = true;
		
		Poi poi = (Poi) v.getTag();
		beforePoiAltered = new Poi(poi);

		if(mDesnsity < 1 && !isTablet){
			beforePoiAltered.xPosDpi -= X_ADJUST_LDPI;
			beforePoiAltered.yPosDpi -= Y_ADJUST_LDPI;
		}

		poi.xPosDpi = AndroidUtil.pxToDp(left, getActivity());
		poi.yPosDpi = AndroidUtil.pxToDp(top, getActivity());
		poi.mapPageNumber = mapPageNumber;
		poi.waiterAlterPoi = app.getFunctionarySelected();
		poi.poiTime = new Date();
		poi.syncStatus = 1;
		poi.moved = true;
		
		afterPoiAltered = poi;
		if(poi.xPosDpi >= 0 && poi.yPosDpi >= 0){
			confirmAlterPoi(v, poi);
		}
		else{
			undoPoiMovement(v);
			Toast.makeText(getActivity(), getResources().getString(R.string.invalid_map_object_position), Toast.LENGTH_LONG).show();
		}
	}

	private void dropTable(View v) {
		v.setOnTouchListener(null);
		fragmentParent.setViewPagerEnabled(true);
		pageNext = true;
		
		Bill bill = (Bill) v.getTag();
		beforeTableAltered = new Table(bill.table);

		if(mDesnsity < 1 && !isTablet){
			beforeTableAltered.xPosInDpi -= X_ADJUST_LDPI;
			beforeTableAltered.yPosDpi -= Y_ADJUST_LDPI;
		}

		bill.table.xPosInDpi = AndroidUtil.pxToDp(left, getActivity());
		bill.table.yPosDpi = AndroidUtil.pxToDp(top, getActivity());
		bill.table.mapPageNumber = mapPageNumber;
		bill.table.waiterAlterTable = app.getFunctionarySelected();
		bill.table.tableTime = new Date();
		bill.table.syncStatus = 1;
		bill.table.moved = true;
		
		afterTableAltered = bill.table;
		if(bill.table.xPosInDpi >= 0 && bill.table.yPosDpi >= 0){
			confirmAlterTable(v, bill.table);
		}
		else{
			undoTableMovement(v);
			Toast.makeText(getActivity(), getResources().getString(R.string.invalid_map_object_position), Toast.LENGTH_LONG).show();
		}
	}

	private void moveMapObject(View v, MotionEvent event) {
		left = (int) event.getRawX() - x_precision;
		top = (int) event.getRawY() - y_precision;

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(left, top, 0, 0);
		v.setLayoutParams(lp);
		
		if(left >= rightMapLimit && 
				pageNext && mapPageNumber < fragmentParent.getCount()){
			v.setOnTouchListener(null);
//			fragmentParent.setViewPagerEnabled(true);
			
			mapLayout.removeView(v);
			fragmentParent.changeTableToNextPage(v);
		}
		else if(left <= leftMapLimit && 
				pageNext && mapPageNumber > 1){
			v.setOnTouchListener(null);
			
			mapLayout.removeView(v);
			fragmentParent.changeTableToPreviousPage(v);
		}
	}

	private void confirmAlterPoi(final View v, final Poi poi){
		String title = poi.toString();
		String message = getResources().getString(R.string.confirm_alter_poi_message); 
		AndroidUtil.createDialog(getActivity(), title, message)
			.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					PoiDao.insertOrUpdateList(getActivity(), poi);
					refresh();
					fragmentParent.refleshOtherMaps(mapPageNumber);
					
					Intent intent = new Intent(getActivity(), DataBaseService.class);
					intent.putExtra(KeysContract.METHOD_KEY, 
							DataBaseService.UPDATE_POI);
					intent.putExtra(KeysContract.TABLE_KEY, afterPoiAltered);
					
					v.setOnClickListener(MapFragment.this);
					
					movingTable = false;
					getActivity().startService(intent);
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
					undoPoiMovement(v);
				}

			})
			.create().show();
	}
	
	private void undoPoiMovement(View v) {
		afterPoiAltered.xPosDpi = beforePoiAltered.xPosDpi;
		afterPoiAltered.yPosDpi = beforePoiAltered.yPosDpi;
		afterPoiAltered.waiterAlterPoi = beforePoiAltered.waiterAlterPoi;
		afterPoiAltered.mapPageNumber = beforePoiAltered.mapPageNumber;
    	refresh();
    	fragmentParent.refleshOtherMaps(mapPageNumber);
    	
    	v.setOnClickListener(MapFragment.this);
    	
    	movingTable = false;
	}
	
	private void confirmAlterTable(final View v, Table table){
		String title = 
				String.format(getResources().getString(R.string.confirm_alter_table_title), 
				table.number);
		String message = 
				String.format(getResources().getString(R.string.confirm_alter_table_message), 
				table.number);
		AndroidUtil.createDialog(getActivity(), title, message)
			.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					refresh();
					fragmentParent.refleshOtherMaps(mapPageNumber);
					
					Intent intent = new Intent(getActivity(), DataBaseService.class);
					intent.putExtra(KeysContract.METHOD_KEY, 
							DataBaseService.UPDATE_TABLE);
					intent.putExtra(KeysContract.TABLE_KEY, afterTableAltered);
					
					v.setOnClickListener(MapFragment.this);
					
					movingTable = false;
					getActivity().startService(intent);
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
					undoTableMovement(v);
				}
			})
			.create().show();
	}
	
	private void undoTableMovement(View v) {
		afterTableAltered.xPosInDpi = beforeTableAltered.xPosInDpi;
    	afterTableAltered.yPosDpi = beforeTableAltered.yPosDpi;
    	afterTableAltered.waiterAlterTable = beforeTableAltered.waiterAlterTable;
    	afterTableAltered.mapPageNumber = beforeTableAltered.mapPageNumber;
    	refresh();
    	fragmentParent.refleshOtherMaps(mapPageNumber);
    	
    	v.setOnClickListener(MapFragment.this);
    	
    	movingTable = false;
	}
	
	public void refresh(){
		if(mapLayout != null){
			mapLayout.removeAllViews();
	    	buildMapObjects();
		}
	}
	
	public void receiveTableView(View tableView){
		tableView.setOnLongClickListener(this);
		tableView.setOnTouchListener(this);
		pageNext = false;
		mapLayout.addView(tableView);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnTableMap:
				Bill bill = (Bill) v.getTag();
				TableInfoDialogFragment tableInfo = TableInfoDialogFragment.newInstance(bill);
				tableInfo.setTargetFragment(this, 1);
				tableInfo.show(getActivity().getSupportFragmentManager(), 
						TableInfoDialogFragment.class.getName());
			break;
			
			case R.id.btnPoiMap:
				Poi poi = (Poi) v.getTag();
				PoiInfoDialogFragment poiInfo = PoiInfoDialogFragment.newInstance(poi);
				poiInfo.show(getActivity().getSupportFragmentManager(), 
						PoiInfoDialogFragment.class.getName());
			break;
		}
		
	}
	
	public void showTableMapSelected(Bill bill) {
		int tableNumber = bill.table.number;
		for(int i = 0; i < mapLayout.getChildCount(); i++){
			View v = mapLayout.getChildAt(i);
			TextView txtTableNumber = (TextView) v.findViewById(R.id.txtTableNumber);
			int tableNumberTemp = Integer.valueOf(txtTableNumber.getText().toString());
			
			if(tableNumberTemp == tableNumber){
				mapLayout.removeViewAt(i);
				
				LayoutInflater mapInflater = (LayoutInflater) getActivity().getSystemService
						(Context.LAYOUT_INFLATER_SERVICE);
				
				View btnMapTable = buildButtonTableMapComponents(mapInflater, bill,
						R.drawable.selector_ic_table_search_opened,
						R.drawable.selector_ic_table_search_closed,
						R.drawable.selector_ic_table_search_semi_closed);

				if(bill.table.moved && mDesnsity < 1 && !isTablet) {
					bill.table.xPosInDpi += X_ADJUST_LDPI;
					bill.table.yPosDpi += Y_ADJUST_LDPI;
					bill.table.moved = false;
				}
				setBtnMapParams(btnMapTable, bill.table.xPosInDpi, bill.table.yPosDpi);
				
				mapLayout.addView(btnMapTable);
				
				break;
			}
		}
	}
	
	/**
	 * Destaca um poi no mapa
	 * @param poi
	 * poi que sera selecionado no mapa
	 */
	public void showPoiMapSelected(Poi poi) {
		int idPoi = poi.idPoi;
		for(int i = 0; i < mapLayout.getChildCount(); i++){
			View v = mapLayout.getChildAt(i);
			if(v.getId() == R.id.btnPoiMap){
				TextView txtIdPoi = (TextView) v.findViewById(R.id.txtIdPoi);
				int idPoiTemp = Integer.valueOf(txtIdPoi.getText().toString());

				if(idPoiTemp == idPoi){
					mapLayout.removeViewAt(i);

					LayoutInflater mapInflater = (LayoutInflater) getActivity().getSystemService
							(Context.LAYOUT_INFLATER_SERVICE);

					View btnMapPoi = buildButtonPoiMapComponents(mapInflater, poi, true);

					if(poi.moved && mDesnsity < 1 && !isTablet) {
						poi.xPosDpi += X_ADJUST_LDPI;
						poi.yPosDpi += Y_ADJUST_LDPI;
						poi.moved = false;
					}
					setBtnMapParams(btnMapPoi, poi.xPosDpi, poi.yPosDpi);

					mapLayout.addView(btnMapPoi);

					break;
				}
			}
		}
	}
}
