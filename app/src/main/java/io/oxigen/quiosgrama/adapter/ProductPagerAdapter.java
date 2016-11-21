package io.oxigen.quiosgrama.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import io.oxigen.quiosgrama.Product;
import io.oxigen.quiosgrama.ProductType;
import io.oxigen.quiosgrama.R;

public class ProductPagerAdapter extends PagerAdapter{

	public ArrayList<ProductType> typeList;
	private ArrayList<ProductListAdapter> listAdapterArray;
	private Context context;
	private int pageLayout;
	
	public ProductPagerAdapter(Context context, ArrayList<Product> productList, ArrayList<ProductType> typeList){
		this.context = context;
		this.pageLayout = R.layout.page_product;
		listAdapterArray = new ArrayList<>();
		this.typeList = typeList;
		
		for (ProductType type : typeList) {
			ArrayList<Product> productListFiltered = filterByProductType(productList, type);
			listAdapterArray.add(new ProductListAdapter(context, productListFiltered));
		}
	}
	
	@Override
	public int getCount() {
		return typeList.size();
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		LinearLayout tabItem = new LinearLayout(context);
		String inflater = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater vi = (LayoutInflater)context.getSystemService(inflater);
		vi.inflate(pageLayout, tabItem, true);
		
		ProductType type = typeList.get(position);
		TextView txtProductType = (TextView) tabItem.findViewById(R.id.txtProductType);
		txtProductType.setText(type.toString());
		
		LinearLayout layoutTitle = (LinearLayout) tabItem.findViewById(R.id.layoutTitle);
		GradientDrawable shapeDrawable = (GradientDrawable) layoutTitle.getBackground();
		if(shapeDrawable != null){
			shapeDrawable.setColor(Color.parseColor(type.colorId));
		}
		else{
			layoutTitle.setBackgroundColor(Color.parseColor(type.colorId));
		}
		
		ListView listViewProduct = (ListView) tabItem.findViewById(R.id.listViewProduct);
		
		ProductListAdapter listAdapter = listAdapterArray.get(position);
		listViewProduct.setAdapter( listAdapter );
		
		container.addView(tabItem);
		
		return tabItem;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((LinearLayout) object);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return (view==object);
	}
	
	public static ArrayList<Product> filterByProductType(ArrayList<Product> productList, ProductType type){
		if(productList != null){
			ArrayList<Product> productListTemp = new ArrayList<>();
			for (Product product : productList) {
				if(product.type.equals(type))
					productListTemp.add(product);
			}
			
			return productListTemp;
		}
		else
			return new ArrayList<>();
	}
	
	public ProductListAdapter getProductListAdapter(Product product){
		for (ProductListAdapter productListAdapter : listAdapterArray) {
			if(productListAdapter.productList.contains(product))
				return productListAdapter;
		}
		
		return null;
	}
}
