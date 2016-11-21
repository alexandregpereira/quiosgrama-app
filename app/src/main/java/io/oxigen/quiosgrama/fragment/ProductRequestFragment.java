package io.oxigen.quiosgrama.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import io.oxigen.quiosgrama.ProductRequest;
import io.oxigen.quiosgrama.ProductType;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.adapter.ProductRequestListAdapter;

public class ProductRequestFragment extends Fragment implements OnClickListener{
	
	private ArrayList<ProductType> typeList;
	private LinearLayout tabsCategory;
	private ArrayList<ProductRequest> productRequestList;

	private HorizontalScrollView scrollIcons;
	private LinearLayout infoFragmentParent;
	private ListView listViewProductRequest;
	
	private Animation infoShowAnim;
	private Animation infoDimissAnim;
	private AnimationListener animationListener;
	public ProductRequestListAdapter productRequestListAdapter;
	private View containerLayout;
	private View btnProductRequestListHidden;

	private boolean isTablet;

	public void refresh(ArrayList<ProductRequest> productRequestList){
		this.productRequestList = productRequestList;
		typeList = new ArrayList<>();

		for (ProductRequest productRequest : productRequestList) {
			if( !typeList.contains(productRequest.product.type) )
				typeList.add(productRequest.product.type);
		}

		tabsCategory.removeAllViews();
		buildTabsCategory();

		productRequestListAdapter = new ProductRequestListAdapter(getActivity(), productRequestList, false);
		listViewProductRequest.setAdapter(productRequestListAdapter);
	}
	
	public void changeProduct(ProductRequest productRequest){
		int index = productRequestListAdapter.productRequestList.indexOf(productRequest);
		
		if(productRequest.product.quantity == 0)
			productRequestListAdapter.productRequestList.remove(productRequest);
		else
			productRequestListAdapter.productRequestList.set(index, productRequest);

		productRequestListAdapter.notifyDataSetChanged();
		
		if( !typeList.contains(productRequest.product.type) )
			typeList.add(productRequest.product.type);
		
		tabsCategory.removeAllViews();
		buildTabsCategory();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = buildWidgets(inflater.inflate(R.layout.fragment_product_request, container, false));

		if(productRequestList != null){
			refresh(productRequestList);
		}

		isTablet = getResources().getBoolean(R.bool.is_tablet);
		
		return v;
	}
	
	private View buildWidgets(View v){
		infoFragmentParent = (LinearLayout) v.findViewById(R.id.infoFragmentParent);
		scrollIcons = (HorizontalScrollView) v.findViewById(R.id.scrollIcons);
		tabsCategory = (LinearLayout) v.findViewById(R.id.tabsCategory);
		listViewProductRequest  = (ListView) v.findViewById(R.id.listViewProductRequest);
		return v;
	}
	
	@SuppressLint("InflateParams")
	private void buildTabsCategory(){
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService
			      (Context.LAYOUT_INFLATER_SERVICE);
		
		for (ProductType type : typeList) {
			View btnProductInfo = inflater.inflate(R.layout.button_product_info, null);
			ImageView imgCategory = (ImageView) btnProductInfo.findViewById(R.id.imgCategory);
			TextView txtQuantity = (TextView) btnProductInfo.findViewById(R.id.txtQuantity);

			btnProductInfo.findViewById(R.id.viewProductTypeColor).setBackgroundColor(Color.parseColor(type.colorId));
			
			imgCategory.setBackgroundResource(type.imageInfoId);
			
			int typeQuantity = 0;
			
			ArrayList<ProductRequest> typeFiltered = filterByProductType(productRequestList, type);
			for (ProductRequest productRequest : typeFiltered) {
				typeQuantity += productRequest.product.quantity; 
			}

			if(typeQuantity > 0) {
				txtQuantity.setText(String.valueOf(typeQuantity));

				if (!isTablet) {
					btnProductInfo.setOnClickListener(this);
				}
				tabsCategory.addView(btnProductInfo);
			}
		}
		
		scrollIcons.scrollTo(0, 0);
	}
	
	public boolean isDetailShowing(){
		if(listViewProductRequest != null) {
			if(!isTablet) {
				return listViewProductRequest.getVisibility() == View.VISIBLE;
			}
			else{
				return productRequestList != null && !productRequestList.isEmpty();
			}
		}

		return false;
	}
	
	private void showDetail() {
		if (!isTablet) {
			if (infoShowAnim == null)
				infoShowAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.info_request_show);

			infoFragmentParent.setBackgroundResource(R.color.colorPrimaryDark);
			infoFragmentParent.startAnimation(infoShowAnim);

			listViewProductRequest.setVisibility(View.VISIBLE);
			if (btnProductRequestListHidden != null)
				btnProductRequestListHidden.setVisibility(View.VISIBLE);

			View parent = (View) containerLayout.getParent();
			if (parent.findViewById(R.id.tool_bar_search).getVisibility() == View.VISIBLE) {
				setShowAllDetail();
			}
		}
	}

	public void setShowAllDetail() {
		try {
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) containerLayout.getLayoutParams();
			params.addRule(RelativeLayout.BELOW, R.id.tool_bar_search);
		}catch (ClassCastException e){}
	}

	public void dismissDetail(){
		if (!isTablet) {
			if (infoDimissAnim == null)
				infoDimissAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.info_request_hide);

			infoFragmentParent.setBackgroundResource(R.color.text_less_focus);

			if (animationListener == null) {
				animationListener = new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						listViewProductRequest.setVisibility(View.GONE);
						RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) containerLayout.getLayoutParams();
						params.addRule(RelativeLayout.BELOW, 0);
					}
				};

				infoDimissAnim.setAnimationListener(animationListener);
			}

			infoFragmentParent.startAnimation(infoDimissAnim);
			if (btnProductRequestListHidden != null)
				btnProductRequestListHidden.setVisibility(View.GONE);
		}
	}
	
	public void dismissDetailWithoutAnim(){
		if(!isTablet) {
			listViewProductRequest.setVisibility(View.GONE);
			if (btnProductRequestListHidden != null)
				btnProductRequestListHidden.setVisibility(View.GONE);
			infoFragmentParent.setBackgroundResource(R.color.text_less_focus);
			try {
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) containerLayout.getLayoutParams();
				params.addRule(RelativeLayout.BELOW, 0);
			} catch (ClassCastException e) {
			}
		}
	}

	@Override
	public void onClick(View v) {
		if(listViewProductRequest.getVisibility() == View.GONE){
			showDetail();
		}
		else{
			dismissDetail();
		}
	}

	public ArrayList<ProductRequest> filterByProductType(ArrayList<ProductRequest> productRequestList, ProductType type){
		if(productRequestList != null){
			ArrayList<ProductRequest> productRequestListTemp = new ArrayList<>();
			for (ProductRequest productRequest : productRequestList) {
				if(productRequest.product.type.equals(type))
					productRequestListTemp.add(productRequest);
			}

			return productRequestListTemp;
		}
		else
			return new ArrayList<>();
	}

	public void setContainerLayout(View containerLayout) {
		this.containerLayout = containerLayout;
		View parent = (View) containerLayout.getParent();
		this.btnProductRequestListHidden = parent.findViewById(R.id.btnProductRequestListHidden);
		if(btnProductRequestListHidden != null) btnProductRequestListHidden.setOnClickListener(this);
	}
}
