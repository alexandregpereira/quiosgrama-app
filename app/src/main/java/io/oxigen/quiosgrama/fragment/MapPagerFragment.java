package io.oxigen.quiosgrama.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import java.lang.reflect.Field;
import java.util.ArrayList;

import io.oxigen.quiosgrama.Bill;
import io.oxigen.quiosgrama.Poi;
import io.oxigen.quiosgrama.ProductRequest;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.adapter.MapPagerAdapter;
import io.oxigen.quiosgrama.adapter.TablePanelAdapter;
import io.oxigen.quiosgrama.data.DataProviderContract;
import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.listener.BackFragmentListener;
import io.oxigen.quiosgrama.observer.TableNameObserver;

public class MapPagerFragment extends Fragment implements OnPageChangeListener, OnClickListener,
		TablePanelAdapter.OnItemClickListener{
    public static final String RECEIVER_FILTER = "io.oxigen.quiosgrama.filter.MAP_RECEIVER_FILTER";

    private ViewPager pagerMap;
    private GridView gridSearchTableLayout;
	private LinearLayout searchPoiLayout;
	private FrameLayout btnSearchMapHidden;
	private ScrollView scrollSearchPoiLayout;

	private Menu menu;
	private MapPagerAdapter mapAdapter;

	private ArrayList<MapFragment> fragments;
	private SharedPreferences preferences;

	private MapPagerListener mListener;
	private BackFragmentListener backListener;

	private TableNameObserver tableObserver;
	private RefleshMapReceiver mapReceiver;
	QuiosgramaApp app;

	private boolean searchTableLayoutVisible;
	private Animation searchTableShowAnim;
	private Animation searchTableDimissAnim;
	private Animation.AnimationListener animationListener;
	private Animation searchPoiDimissAnim;
	private Animation.AnimationListener animationPoiListener;
	private Animation searchPoiShowAnim;

	public interface MapPagerListener{
		public void onChangeMapTitle(String title);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
	}

	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (MapPagerListener) activity;
            backListener = (BackFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement MapPagerListener");
        }
    }

	@Override
	public void onResume() {
		super.onResume();
		app = (QuiosgramaApp) getActivity().getApplication();
		if(tableObserver == null)
			tableObserver = new TableNameObserver(null, getActivity());
		getActivity().getContentResolver().registerContentObserver(DataProviderContract.TableTable.TABLE_URI, true, tableObserver);

		if(mapReceiver == null)
			mapReceiver = new RefleshMapReceiver();
		getActivity().registerReceiver(mapReceiver, new IntentFilter(RECEIVER_FILTER));
	}

	@Override
	public void onPause() {
		getActivity().getContentResolver().unregisterContentObserver(tableObserver);
		getActivity().unregisterReceiver(mapReceiver);
		super.onPause();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_map_pager, container, false);
		app = (QuiosgramaApp) getActivity().getApplication();

		pagerMap = (ViewPager) v.findViewById(R.id.pagerMap);
		gridSearchTableLayout = (GridView) v.findViewById(R.id.gridSearchTableLayout);
		searchPoiLayout = (LinearLayout) v.findViewById(R.id.searchPoiLayout);
		btnSearchMapHidden = (FrameLayout) v.findViewById(R.id.btnSearchMapHidden);
		scrollSearchPoiLayout = (ScrollView) v.findViewById(R.id.scrollSearchPoiLayout);

		if(fragments == null){
			preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
			int pageNumbers = preferences.getInt(KeysContract.PAGE_NUMBERS_KEY, 1);

			fragments = new ArrayList<MapFragment>(pageNumbers);
			for (int i = 0; i < pageNumbers; i++) {
				fragments.add(MapFragment.newInstance(i+1));
			}

			for (Bill bill : app.getBillList()) {
				if(bill.table.mapPageNumber > pageNumbers){
					pageNumbers = bill.table.mapPageNumber;
					fragments.add(MapFragment.newInstance(pageNumbers));
				}
			}

			SharedPreferences.Editor editor = preferences.edit();
			editor.putInt(KeysContract.PAGE_NUMBERS_KEY, pageNumbers);
			editor.commit();
		}

		buildSearchTableLayout();
		buildSearchPoiLayout();

		mapAdapter = new MapPagerAdapter(getChildFragmentManager(), fragments);
		pagerMap.setAdapter(mapAdapter);
		pagerMap.setOnPageChangeListener(this);
		btnSearchMapHidden.setOnClickListener(this);

		changeMapTitle(pagerMap.getCurrentItem());

		return v;
	}

	private void buildSearchTableLayout() {
		if(gridSearchTableLayout != null){
			gridSearchTableLayout.setAdapter(new TablePanelAdapter(getActivity(), this, app.getBillList()));
		}
	}

	private void buildSearchPoiLayout() {
		LayoutInflater mapInflater = (LayoutInflater) getActivity().getSystemService
				(Context.LAYOUT_INFLATER_SERVICE);

		if(searchPoiLayout != null){
			searchPoiLayout.removeAllViews();
			for (Poi poi : app.getPoiList()) {
				View btnPoiMap = MapFragment.buildButtonSearchPoiMapComponents(mapInflater, poi);

				int padding = (int) getResources().getDimension(R.dimen.btn_map_padding);
				btnPoiMap.setPadding(padding, padding, padding, padding);

				btnPoiMap.setOnClickListener(this);

				searchPoiLayout.addView(btnPoiMap);
			}
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
        try {
            Field childFragmentManager = Fragment.class
                    .getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        backListener.backFragmentPressed();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		this.menu = menu;
		inflater.inflate(R.menu.map, menu);
		super.onCreateOptionsMenu(menu, inflater);

		checkActionNextPlus(pagerMap.getCurrentItem() + 1);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	        case R.id.action_search_poi:
				if(searchTableLayoutVisible) {
                    changeSearchTableLayoutVisibility();
                }
				changeSearchPoiLayoutVisibility();
	        break;

	        case R.id.action_search_table:
				if(scrollSearchPoiLayout.getVisibility() == View.VISIBLE) {
					changeSearchPoiLayoutVisibility();
				}
				changeSearchTableLayoutVisibility();
		    break;

	        case R.id.action_cancel:
	        	removeMap();
		    break;

	        case R.id.action_next_plus:
	        	int pageNumbers = fragments.size()+1;
	        	fragments.add(MapFragment.newInstance(pageNumbers));
	        	mapAdapter.notifyDataSetChanged();

	        	SharedPreferences.Editor editor = preferences.edit();
				editor.putInt(KeysContract.PAGE_NUMBERS_KEY, pageNumbers);
				editor.commit();

				pagerMap.setCurrentItem(pageNumbers-1);
		    break;
	        default:
	            return super.onOptionsItemSelected(item);
		}

		return true;
	}

	private void changeSearchTableLayoutVisibility(){
		Animation animation;
		if (searchTableLayoutVisible) {
			if (searchTableDimissAnim == null)
				searchTableDimissAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.search_hide);

			if (animationListener == null) {
				animationListener = new Animation.AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						changeSearchTableLayoutVisibilityOld();
					}
				};

				searchTableDimissAnim.setAnimationListener(animationListener);
			}

			btnSearchMapHidden.setVisibility(View.GONE);
			animation = searchTableDimissAnim;
		} else {
			if (searchTableShowAnim == null)
				searchTableShowAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.search_table_show);

			animation = searchTableShowAnim;
			changeSearchTableLayoutVisibilityOld();
		}
		gridSearchTableLayout.startAnimation(animation);
	}

	private void changeSearchTableLayoutVisibilityOld() {
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				(int) getResources().getDimension(R.dimen.search_table_map_width),
				RelativeLayout.LayoutParams.MATCH_PARENT);

		if (searchTableLayoutVisible) {
			params.addRule(RelativeLayout.RIGHT_OF, R.id.pagerMap);
		} else {
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
					RelativeLayout.TRUE);

			btnSearchMapHidden.setVisibility(View.VISIBLE);
		}

		gridSearchTableLayout.setLayoutParams(params);

		searchTableLayoutVisible = !searchTableLayoutVisible;
	}

	private void changeSearchPoiLayoutVisibility(){
		Animation animation;
		if (scrollSearchPoiLayout.getVisibility() == View.VISIBLE) {
			if (searchPoiDimissAnim == null)
				searchPoiDimissAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.search_hide);

			if (animationPoiListener == null) {
				animationPoiListener = new Animation.AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						changeSearchPoiLayoutVisibilityOld();
					}
				};

				searchPoiDimissAnim.setAnimationListener(animationPoiListener);
			}

			btnSearchMapHidden.setVisibility(View.GONE);
			animation = searchPoiDimissAnim;
		} else {
			if (searchPoiShowAnim == null)
				searchPoiShowAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.search_poi_show);

			animation = searchPoiShowAnim;
			changeSearchPoiLayoutVisibilityOld();
			btnSearchMapHidden.setVisibility(View.VISIBLE);
		}
		scrollSearchPoiLayout.startAnimation(animation);
	}

	private void changeSearchPoiLayoutVisibilityOld() {
		scrollSearchPoiLayout.setVisibility(
				scrollSearchPoiLayout.getVisibility() == View.GONE ? View.VISIBLE : View.GONE
		);
	}

	private void removeMap() {
		int currentItem = pagerMap.getCurrentItem();
    	fragments.remove( currentItem );
    	mapAdapter.notifyDataSetChanged();

    	SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(KeysContract.PAGE_NUMBERS_KEY, fragments.size());
		editor.commit();

		if(currentItem > 0)
    		pagerMap.setCurrentItem(currentItem - 1);
	}

	public void refleshOtherMaps(int pageNumber){
		for (MapFragment fragment : fragments) {
			if(fragment.getMapPageNumber() != pageNumber) {
				fragment.refresh();
			}
		}

		buildSearchTableLayout();
		buildSearchPoiLayout();
	}

	public void refreshMaps(){
		for (MapFragment fragment : fragments) {
			if(!fragment.movingTable)
				fragment.refresh();
			else
				break;
		}

		buildSearchTableLayout();
		buildSearchPoiLayout();
	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

	}

	@Override
	public void onPageSelected(int position) {
		changeMapTitle(position);

		MapFragment fragment = fragments.get(position);
		if(!fragment.hasTable && (fragment.getMapPageNumber() == fragments.size())){
			MenuItem menuItem = menu.findItem(R.id.action_cancel);
			if(menuItem != null){
				menuItem.setVisible(true);
			}
		}
		else{
			MenuItem menuItem = menu.findItem(R.id.action_cancel);
			if(menuItem != null){
				menuItem.setVisible(false);
			}
		}

		checkActionNextPlus(position + 1);
	}

	private void checkActionNextPlus(int pageNumberActual){
		if(pageNumberActual == fragments.size()){
			MenuItem menuItem = menu.findItem(R.id.action_next_plus);
			if(menuItem != null)
				menuItem.setVisible(true);
		}
		else{
			MenuItem menuItem = menu.findItem(R.id.action_next_plus);
			if(menuItem != null)
				menuItem.setVisible(false);
		}
	}

	private void changeMapTitle(int position){
		String title = getResources().getString(R.string.action_map) + " " +
				(position + 1) + " - " + fragments.size();
		mListener.onChangeMapTitle(title);
	}

	public void setViewPagerEnabled(boolean enabled){
		 pagerMap.setEnabled(enabled);
	 }

	public void changeTableToNextPage(View tableView) {
		int nextItem = pagerMap.getCurrentItem() + 1;
		if(nextItem < fragments.size()){
			pagerMap.setCurrentItem(nextItem);
			fragments.get(nextItem).receiveTableView(tableView);
		}
	}

	public void changeTableToPreviousPage(View tableView) {
		int previousItem = pagerMap.getCurrentItem() - 1;
		if(previousItem >= 0){
			pagerMap.setCurrentItem(previousItem);
			fragments.get(previousItem).receiveTableView(tableView);
		}
	}

	public int getCount() {
		return fragments.size();
	}

	public void onAddComplementListener(View v) {
		ProductRequest productRequest = (ProductRequest) v.getTag();
		ComplementDialogFragment complementFragment =
				ComplementDialogFragment.newInstance(productRequest, false);
		complementFragment.show(getChildFragmentManager(), ComplementDialogFragment.class.getName());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnSearchMapHidden:
				if(searchTableLayoutVisible) {
					changeSearchTableLayoutVisibility();
				}
				if(scrollSearchPoiLayout.getVisibility() == View.VISIBLE){
					changeSearchPoiLayoutVisibility();
				}
			break;

			case R.id.txtButtonSearchPoiMap:
                changeSearchPoiLayoutVisibility();

				for (MapFragment mapFragment : fragments) {
					mapFragment.refresh();
				}

				Poi poi = (Poi) v.getTag();
				int indexPoi = poi.mapPageNumber - 1;
				if(indexPoi >= 0){
					pagerMap.setCurrentItem(indexPoi);
				}
				else{
					indexPoi = pagerMap.getCurrentItem();
				}

				fragments.get(indexPoi).showPoiMapSelected(poi);
			break;
		}
	}

	@Override
	public void onItemClicked(Bill bill) {
		boolean isTablet = getResources().getBoolean(R.bool.is_tablet);
		if(!isTablet) changeSearchTableLayoutVisibility();

		for (MapFragment mapFragment : fragments) {
			mapFragment.refresh();
		}

		int index = bill.table.mapPageNumber - 1;
		if(index >= 0){
			pagerMap.setCurrentItem(index);
		}
		else{
			index = pagerMap.getCurrentItem();
		}

		fragments.get(index).showTableMapSelected(bill);
	}

	@Override
	public void onItemLongClicked(Bill bill) {
		TableInfoDialogFragment tableInfo = TableInfoDialogFragment.newInstance(bill);
		tableInfo.setTargetFragment(this, 1);
		tableInfo.show(getActivity().getSupportFragmentManager(),
				TableInfoDialogFragment.class.getName());
	}

	private class RefleshMapReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			refreshMaps();
		}

	}

	public void onAddProductRequestListener(View v){
		ProductRequest productRequest = (ProductRequest) v.getTag();
		++productRequest.product.quantity;

		FragmentManager fManager = getActivity().getSupportFragmentManager();
		SendRequestFragment requestFragment = (SendRequestFragment) fManager.findFragmentByTag(SendRequestFragment.class.getName());
		if(requestFragment != null){
			if(requestFragment.isResumed()){
				requestFragment.changeProduct(productRequest);
			}
		}
	}

	public void onRemoveProductRequestListener(View v){
		ProductRequest productRequest = (ProductRequest) v.getTag();

		--productRequest.product.quantity;

		FragmentManager fManager = getActivity().getSupportFragmentManager();
		SendRequestFragment requestFragment = (SendRequestFragment) fManager.findFragmentByTag(SendRequestFragment.class.getName());
		if(requestFragment != null && requestFragment.isResumed()){
			requestFragment.changeProduct(productRequest);
		}
	}
}
