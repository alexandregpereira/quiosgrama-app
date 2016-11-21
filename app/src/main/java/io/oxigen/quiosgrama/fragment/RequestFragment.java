package io.oxigen.quiosgrama.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import io.oxigen.quiosgrama.Functionary;
import io.oxigen.quiosgrama.Product;
import io.oxigen.quiosgrama.ProductRequest;
import io.oxigen.quiosgrama.ProductType;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.Request;
import io.oxigen.quiosgrama.adapter.AutoCompleteProductAdapter;
import io.oxigen.quiosgrama.adapter.ProductListAdapter;
import io.oxigen.quiosgrama.adapter.ProductPagerAdapter;
import io.oxigen.quiosgrama.adapter.ProductRequestListAdapter;
import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.listener.BackFragmentListener;
import io.oxigen.quiosgrama.util.AndroidUtil;

public class RequestFragment extends Fragment implements OnClickListener, OnPageChangeListener{

	private static final String RECEIVER_FILTER = "io.oxigen.quiosgrama.filter.AUTO_COMPLETE_PRODUCT_RECEIVER_FILTER";

	private static RequestFragment requestFragment;

	private ArrayList<Product> productList;
	private ArrayList<ProductType> typeList;
	private ArrayList<ProductRequest> productRequestList;
	private int positionSelected;
	private boolean changePageSelected;

	private ViewPager pagerProduct;
	private LinearLayout tabsCategory;
	private HorizontalScrollView scrollTop;
	private ProductPagerAdapter pagerAdapter;

	private ProductRequestFragment productRequestFragment;

	private Menu menu;
	private Request request;
	private ComplementDialogFragment complementFragment;
	private BackFragmentListener backListener;

	QuiosgramaApp app;
	private Toolbar toolbarSearch;
	private AutoCompleteTextView edtSearchProduct;
	private AutoCompleteProductReceiver autoCompleteProductReceiver;
	private AutoCompleteProductAdapter completeAdapter;

	private boolean refreshBuildMenu;

	public interface RequestFragmentListener{
		public void onSearchToolbarClicked();
		public void onSearchToolbarClose();
	}

	RequestFragmentListener mListener;

	public RequestFragment() {
	}

	public static RequestFragment newInstance(int prioritySelected){
		if(requestFragment == null){
			requestFragment = new RequestFragment();
			Bundle data = new Bundle();
			data.putInt(KeysContract.POSITION_KEY, prioritySelected);
			requestFragment.setArguments(data);
		}
		else if(!requestFragment.isResumed()){
			Bundle data = requestFragment.getArguments();
			data.putInt(KeysContract.POSITION_KEY, prioritySelected);
			requestFragment.setArguments(data);
		}

		return requestFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(productRequestList == null)
			productRequestList = new ArrayList<>();

		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_request, container, false);
		app = (QuiosgramaApp) getActivity().getApplication();
		changePageSelected = true;

		toolbarSearch = (Toolbar) v.findViewById(R.id.tool_bar_search);
		pagerProduct = (ViewPager) v.findViewById(R.id.pagerProduct);
		tabsCategory = (LinearLayout) v.findViewById(R.id.tabsCategory);
		scrollTop = (HorizontalScrollView) v.findViewById(R.id.scrollTop);

		View btnProductRequestListHidden = v.findViewById(R.id.btnProductRequestListHidden);
		if(btnProductRequestListHidden != null) btnProductRequestListHidden.setOnClickListener(this);

		toolbarSearch.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);

		toolbarSearch.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				searchToolbarClose();
			}
		});

		if(productRequestFragment == null)
			productRequestFragment = new ProductRequestFragment();

		productRequestFragment.setContainerLayout(v.findViewById(R.id.registerFragmentContent));

		FragmentManager fManager = getChildFragmentManager();
		fManager.beginTransaction().add(R.id.registerFragmentContent, productRequestFragment, ProductRequestFragment.class.getName()).commit();

		pagerProduct.setOnPageChangeListener(this);

		Bundle data = getArguments();
		positionSelected = data.getInt(KeysContract.POSITION_KEY);

		buildMenu();

		return v;
	}

	private void searchToolbarOpen(){
		toolbarSearch.setVisibility(View.VISIBLE);
		edtSearchProduct = null;
		AndroidUtil.showKeyBoard(getActivity(), null);
		edtSearchProduct = (AutoCompleteTextView) toolbarSearch.findViewById(R.id.edtSearchProduct);
		edtSearchProduct.requestFocus();

		Button btnClearSearch = (Button) toolbarSearch.findViewById(R.id.btnClearSearch);
		btnClearSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				edtSearchProduct.setText("");
			}
		});

		if(autoCompleteProductReceiver == null)
			autoCompleteProductReceiver = new AutoCompleteProductReceiver();

		getActivity().registerReceiver(autoCompleteProductReceiver, new IntentFilter(RECEIVER_FILTER));

		completeAdapter = new AutoCompleteProductAdapter(
				getActivity(),
				productList,
				RECEIVER_FILTER);

		edtSearchProduct.setAdapter(completeAdapter);

		if(productRequestFragment.isDetailShowing()){
			productRequestFragment.setShowAllDetail();
		}

		mListener.onSearchToolbarClicked();
	}

	private void searchToolbarClose(){
		toolbarSearch.setVisibility(View.GONE);
		if(edtSearchProduct != null){
			AndroidUtil.hideKeyBoard(getActivity(), edtSearchProduct);
			edtSearchProduct.setText("");
		}
		if(autoCompleteProductReceiver != null){
			getActivity().unregisterReceiver(autoCompleteProductReceiver);
			autoCompleteProductReceiver = null;
		}
		completeAdapter = null;
		mListener.onSearchToolbarClose();
	}

	public void buildMenu() {
		if(!doingRequest() || productList == null || productList.isEmpty() || typeList == null || typeList.isEmpty()){
			refreshBuildMenu = false;
			productList = app.getProductList();
			typeList = app.getProductTypeList();
		}
		else{
			refreshBuildMenu = true;
		}

		if(productList != null || typeList != null){
			pagerAdapter = new ProductPagerAdapter(getActivity(), productList, typeList);

			pagerProduct.setAdapter(pagerAdapter);

			buildTabsCategory();

			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					if(productRequestList.isEmpty())
						hideInfoFragment();

					if(positionSelected <=2 )
						scrollTop.scrollTo(0, 0);
					else{
						int scrollDimen = (int) getResources().getDimension(R.dimen.scroll);
						scrollTop.scrollTo(scrollDimen*positionSelected, 0);
					}
				}
			}, 100);
		}
	}

	public boolean doingRequest(){
		return productRequestList != null && !productRequestList.isEmpty();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		this.menu = menu;
		inflater.inflate(R.menu.register, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_search:
				searchToolbarOpen();
				break;
	        case R.id.action_cancel:
	        	clearRequest();
	        break;
	        case R.id.action_finish:
	        	showSendRequestFragment();
	        break;
	        default:
	            return super.onOptionsItemSelected(item);
		}

		return true;
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if (productRequestList != null && !productRequestList.isEmpty()) {
			menu.findItem(R.id.action_cancel).setVisible(true);
			setActionFinishVisibility();
		}

		super.onPrepareOptionsMenu(menu);
	}

	private void setActionFinishVisibility(){
		int functionaryType = app.getFunctionarySelectedType();
		if(functionaryType == Functionary.ADMIN || functionaryType == Functionary.WAITER
				|| (functionaryType == Functionary.CLIENT_WAITER && app.getBillSelected() != null)){
			menu.findItem(R.id.action_finish).setVisible(true);
		}
		else{
			menu.findItem(R.id.action_finish).setVisible(false);
		}
	}

	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
			mListener = (RequestFragmentListener) activity;
            backListener = (BackFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement BacktListener");
        }
    }

	@Override
	public void onDetach() {
	    super.onDetach();

	    try {
	        Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
	        childFragmentManager.setAccessible(true);
	        childFragmentManager.set(this, null);

	    } catch (NoSuchFieldException e) {
	        throw new RuntimeException(e);
	    } catch (IllegalAccessException e) {
	        throw new RuntimeException(e);
	    }

	    backListener.backFragmentPressed();
		searchToolbarClose();
	}

	@Override
	public void onClick(View v) {
		int position = v.getId();
		changeTabSelected(position, true);
	}

	@Override
	public void onPageSelected(int position) {
		if(changePageSelected)
			changeTabSelected(position, false);
	}

	public void onAddProductListener(View v){
		if(request == null)
			request = new Request(app.getFunctionarySelected());

		Product p = (Product) v.getTag();
		ProductListAdapter listAdapter = pagerAdapter.getProductListAdapter(p);
		int index = listAdapter.productList.indexOf(p);

		ProductRequest productRequest = new ProductRequest(request, p, null);
		int requestIndex = productRequestList.indexOf(productRequest);
		if(requestIndex >= 0)
			productRequest = productRequestList.get(requestIndex);
		else{
			productRequest.product.quantity = 0;
		}

		++p.quantity;
		++productRequest.product.quantity;

		if(productRequest.product.quantity == 1 || requestIndex < 0){
			productRequestList.add(productRequest);
		}
		else{
			productRequestList.set(requestIndex, productRequest);
		}

		if(index >= 0)
			listAdapter.productList.set(index, p);

		listAdapter.notifyDataSetChanged();
		if(completeAdapter != null){
			completeAdapter.updateProduct(p);
		}

//		FragmentManager fManager = getChildFragmentManager();
//		ProductRequestFragment infoFragment = (ProductRequestFragment) fManager.findFragmentByTag(ProductRequestFragment.class.getName());
		if(!productRequestFragment.isDetailShowing()){
			productRequestFragment.refresh(productRequestList);
		}
		else{
			productRequestFragment.changeProduct(productRequest);
		}

		FragmentManager fManager = getChildFragmentManager();
		SendRequestFragment requestFragment = (SendRequestFragment) fManager.findFragmentByTag(SendRequestFragment.class.getName());
		if(requestFragment != null){
			if(!requestFragment.isResumed()){
				requestFragment.reflesh(productRequestList);
			}
			else{
				requestFragment.changeProduct(productRequest);
			}
		}

		if(productRequestList.size() > 0)
			showInfoFragment();

		setActionFinishVisibility();
		menu.findItem(R.id.action_cancel).setVisible(true);
	}

	public void onRemoveProductListener(View v){
		Product p = (Product) v.getTag();
		if(p.quantity > 0){
			ProductListAdapter listAdapter = pagerAdapter.getProductListAdapter(p);
			int index = listAdapter.productList.indexOf(p);

			ProductRequest productRequest = new ProductRequest(request, p, null);
			int requestIndex = productRequestList.indexOf(productRequest);
			if (requestIndex >= 0){
				if(--p.quantity < 0)
					p.quantity = 0;

				productRequest = productRequestList.get(requestIndex);

				--productRequest.product.quantity;

				if(productRequest.product.quantity <= 0){
					productRequestList.remove(productRequest);
				}
				else{
					productRequestList.set(requestIndex, productRequest);
				}

				listAdapter.productList.set(index, p);
				listAdapter.notifyDataSetChanged();
				if(completeAdapter != null){
					completeAdapter.updateProduct(p);
				}

				FragmentManager fManager = getChildFragmentManager();
				ProductRequestFragment infoFragment = (ProductRequestFragment) fManager.findFragmentByTag(ProductRequestFragment.class.getName());
				if(!infoFragment.isDetailShowing()){
					infoFragment.refresh(productRequestList);
				}
				else{
					infoFragment.changeProduct(productRequest);
				}

				SendRequestFragment requestFragment = (SendRequestFragment) fManager.findFragmentByTag(SendRequestFragment.class.getName());
				if(requestFragment != null && requestFragment.isResumed()){
					requestFragment.reflesh(productRequestList);
				}

				if(productRequestList.size() <= 0){
					request = null;
					hideInfoFragment();
					menu.findItem(R.id.action_finish).setVisible(false);
					menu.findItem(R.id.action_cancel).setVisible(false);
				}

			}
//			else if(!productRequestList.isEmpty()){
//				productRequest = productRequestList.get(0);
//			}
		}
	}

	public void onAddProductRequestListener(View v){
		ProductRequest productRequest = (ProductRequest) v.getTag();
		ProductListAdapter listAdapter = pagerAdapter.getProductListAdapter(productRequest.product);
		int index = listAdapter.productList.indexOf(productRequest.product);
		Product product = listAdapter.productList.get(index);

		++product.quantity;
		++productRequest.product.quantity;

		if(productRequest.product.quantity == 1){
			productRequestList.add(productRequest);
		}
		else{
			int requestIndex = productRequestList.indexOf(productRequest);
			productRequestList.set(requestIndex, productRequest);
		}

		listAdapter.notifyDataSetChanged();

		if(!productRequestFragment.isDetailShowing()){
			productRequestFragment.refresh(productRequestList);
		}
		else{
			productRequestFragment.changeProduct(productRequest);
		}

		FragmentManager fManager = getActivity().getSupportFragmentManager();
		SendRequestFragment requestFragment = (SendRequestFragment) fManager.findFragmentByTag(SendRequestFragment.class.getName());
		if(requestFragment != null){
			if(!requestFragment.isResumed()){
				requestFragment.reflesh(productRequestList);
			}
			else{
				requestFragment.changeProduct(productRequest);
			}
		}
	}

	public void onRemoveProductRequestListener(View v){
		ProductRequest productRequest = (ProductRequest) v.getTag();
		ProductListAdapter listAdapter = pagerAdapter.getProductListAdapter(productRequest.product);
		int index = listAdapter.productList.indexOf(productRequest.product);
		Product product = listAdapter.productList.get(index);

		if(--product.quantity < 0)
			product.quantity = 0;

		--productRequest.product.quantity;

		if(productRequest.product.quantity == 0){
			productRequestList.remove(productRequest);
		}
		else{
			int requestIndex = productRequestList.indexOf(productRequest);
			productRequestList.set(requestIndex, productRequest);
		}

		listAdapter.notifyDataSetChanged();

		if(!productRequestFragment.isDetailShowing()){
			productRequestFragment.refresh(productRequestList);
		}
		else{
			productRequestFragment.changeProduct(productRequest);
		}

		FragmentManager fManager = getActivity().getSupportFragmentManager();
		SendRequestFragment requestFragment = (SendRequestFragment) fManager.findFragmentByTag(SendRequestFragment.class.getName());
		if(requestFragment != null){
			if(!requestFragment.isResumed()){
				requestFragment.reflesh(productRequestList);
			}
			else{
				requestFragment.changeProduct(productRequest);
			}
		}

		if(productRequestList.size() <= 0){
			request = null;
			hideInfoFragment();
			menu.findItem(R.id.action_finish).setVisible(false);
			menu.findItem(R.id.action_cancel).setVisible(false);
		}
	}

	public void onAddComplementListener(View v){
		ProductRequest productRequest = (ProductRequest) v.getTag();
		complementFragment = ComplementDialogFragment.newInstance(productRequest);
		complementFragment.show(getChildFragmentManager(), ComplementDialogFragment.class.getName());
	}

	private void clearRequest(){
		for (ProductRequest productRequest : productRequestList) {
			if(productRequest.product.quantity != 0){
				productRequest.product.quantity = 0;

				ProductListAdapter listAdapter = pagerAdapter.getProductListAdapter(productRequest.product);
				int index = listAdapter.productList.indexOf(productRequest.product);
				listAdapter.productList.set(index, productRequest.product);

				listAdapter.notifyDataSetChanged();

				index = productList.indexOf(productRequest.product);
				if(index >= 0) productList.set(index, productRequest.product);
			}
		}

		productRequestList.clear();
		request = null;
		hideInfoFragment();
		menu.findItem(R.id.action_finish).setVisible(false);
		menu.findItem(R.id.action_cancel).setVisible(false);
	}

	private void buildTabsCategory(){
		Collections.sort(typeList, new Comparator<ProductType>() {

			@Override
			public int compare(ProductType actualType, ProductType otherType) {
				if (actualType.priority < otherType.priority)
					return -1;
				else if (actualType.priority == otherType.priority)
					return 0;
				else
					return 1;
			}
		});

		tabsCategory.removeAllViews();
		int i = 0;
		for (ProductType type : typeList) {
			ImageButton btn = new ImageButton(getActivity());

			LinearLayout.LayoutParams params =
					new LinearLayout.LayoutParams(AndroidUtil.dpToPx(80, getActivity()),
							AndroidUtil.dpToPx(50, getActivity()));

			btn.setLayoutParams(params);
			btn.setId(i++);
			btn.setImageResource(type.imageInfoId);
			btn.setBackgroundColor(Color.parseColor(type.colorId));
			btn.setOnClickListener(this);

			tabsCategory.addView(btn);

//			if(type.priority == positionSelected)
//				changeTabSelected(type);
		}

		changeTabSelected(positionSelected, true);
	}

	private void changeTabSelected(int position, boolean changePage){
		if(typeList != null && !typeList.isEmpty()) {
			ProductType type = typeList.get(position);
			tabsCategory.setBackgroundColor(Color.parseColor(type.colorId));
			if (changePage) {
				changePageSelected = false;
				pagerProduct.setCurrentItem(position);
			}
			positionSelected = position;

			if (position <= 2)
				scrollTop.scrollTo(0, 0);
			else if (typeList.size() - position <= 3) {
				int scrollDimen = (int) getResources().getDimension(R.dimen.scroll_max);
				scrollTop.scrollTo(scrollDimen * position, 0);
			} else {
				int scrollDimen = (int) getResources().getDimension(R.dimen.scroll);
				scrollTop.scrollTo(scrollDimen * position, 0);
			}

			changePageSelected = true;
		}
	}

	public void changeTabSelected(final int position){
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				if(typeList != null){
					positionSelected = position;
					changeTabSelected(position, true);
				}
			}
		}, 100);
	}

	private void showInfoFragment(){
		ProductRequestFragment f = (ProductRequestFragment) getChildFragmentManager().findFragmentByTag(ProductRequestFragment.class.getName());
		getChildFragmentManager().beginTransaction().show(f).commit();
	}

	private void hideInfoFragment() {
		if (refreshBuildMenu) {
			buildMenu();
		}

		ProductRequestFragment f = (ProductRequestFragment) getChildFragmentManager().findFragmentByTag(ProductRequestFragment.class.getName());
		boolean isTablet = getResources().getBoolean(R.bool.is_tablet);
		if (!isTablet) {
			f.dismissDetailWithoutAnim();
			try {
				getChildFragmentManager().beginTransaction().hide(f).commit();
			}catch (IllegalStateException e){}
		}
		else{
			f.refresh(productRequestList);
		}
	}

	private void showSendRequestFragment() {
		if (productRequestFragment.isDetailShowing()) {
			ProductRequestFragment f = (ProductRequestFragment) getChildFragmentManager().findFragmentByTag(ProductRequestFragment.class.getName());
			f.dismissDetail();
		}

		FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
		SendRequestFragment fragment = SendRequestFragment.newInstance(productRequestList, SendRequestFragment.NEW_REQUEST_MODE);

		boolean isTablet = getResources().getBoolean(R.bool.is_tablet);
		if(isTablet) {
			fragment.show(fragmentManager, SendRequestFragment.class.getName());
		}
		else{
			FragmentTransaction transaction = fragmentManager.beginTransaction();
			transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			transaction.add(android.R.id.content, fragment, SendRequestFragment.class.getName()).addToBackStack(null).commit();
		}
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(KeyEvent.KEYCODE_BACK == keyCode &&
				productRequestFragment.isDetailShowing()){

			ProductRequestFragment f = (ProductRequestFragment) getChildFragmentManager().findFragmentByTag(ProductRequestFragment.class.getName());
			f.dismissDetail();

			return false;
		}
		else if(KeyEvent.KEYCODE_BACK == keyCode && toolbarSearch.getVisibility() == View.VISIBLE){
			searchToolbarClose();
			return false;
		}

		if(KeyEvent.KEYCODE_ENTER == keyCode &&
				complementFragment != null &&
				complementFragment.isResumed()){

			AndroidUtil.hideKeyBoard(getActivity(), complementFragment.edtComplement);
			return false;
		}

		return true;
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	public void onConfirmComplement(ProductRequest oldProductRequest, ProductRequest newProductRequest) {
		FragmentManager fManager = getChildFragmentManager();
		ProductRequestFragment infoFragment = (ProductRequestFragment) fManager.findFragmentByTag(ProductRequestFragment.class.getName());

		ProductRequestListAdapter listAdapter = infoFragment.productRequestListAdapter;

		if(oldProductRequest.product.quantity != 0){
			--oldProductRequest.product.quantity;

			int requestIndex = productRequestList.indexOf(oldProductRequest);

			if(productRequestList.contains(newProductRequest)){
				int newProductIndex = productRequestList.indexOf(newProductRequest);
				++productRequestList.get(newProductIndex).product.quantity;
			}
			else{
				productRequestList.add(newProductRequest);
			}

			if(oldProductRequest.product.quantity == 0){
				productRequestList.remove(oldProductRequest);
			}
			else{
				productRequestList.set(requestIndex, oldProductRequest);
			}

			listAdapter.notifyDataSetChanged();

			FragmentManager fParentManager = getActivity().getSupportFragmentManager();
			SendRequestFragment requestFragment = (SendRequestFragment) fParentManager.findFragmentByTag(SendRequestFragment.class.getName());
			if(requestFragment != null){
				if(requestFragment.isResumed()){
					ProductRequestListAdapter adapter =
							requestFragment.productRequestListAdapter;

//					index = adapter.productRequestList.indexOf(oldProductRequest);
//					
//					adapter.productRequestList.add(newProductRequest);
//					
//					if(oldProductRequest.product.quantity == 0){
//						adapter.productRequestList.remove(oldProductRequest);
//					}
//					else
//						adapter.productRequestList.set(index, oldProductRequest);

					adapter.notifyDataSetChanged();
				}
			}
		}

	}

	public void onCancelComplement() {

	}

	public void onConfirmRequest() {
		clearRequest();
	}

	public void onSaveRequest() {
		// TODO Auto-generated method stub
	}

	private class AutoCompleteProductReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle data = intent.getExtras();
			Product product = data.getParcelable(KeysContract.PRODUCT_KEY);
			View view = new View(getActivity());
			view.setTag(product);

			int result = data.getInt(KeysContract.RESULT_KEY);
			if(result == 1)
				onAddProductListener(view);
			else if(result == 2)
				onRemoveProductListener(view);
		}

	}

}
