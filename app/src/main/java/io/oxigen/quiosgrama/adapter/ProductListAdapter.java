package io.oxigen.quiosgrama.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import io.oxigen.quiosgrama.Functionary;
import io.oxigen.quiosgrama.Product;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.fragment.SendRequestFragment;

public class ProductListAdapter extends ArrayAdapter<Product>{

	private final QuiosgramaApp app;
	private int layoutItem;
	public int index;
	public ArrayList<Product> productList;
	private TextView txtQuantity;

	public ProductListAdapter(Context context, ArrayList<Product> productList) {
		
		super(context, R.layout.item_product, productList);
		
		this.layoutItem =  R.layout.item_product;
		this.productList = productList;
		app = (QuiosgramaApp) context.getApplicationContext();
	}
	
	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View productItem = convertView;

		if(productItem == null){
			LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
			productItem = inflater.inflate(layoutItem, parent, false);
		}

		int functionaryType = app.getFunctionarySelectedType();
		
		Product product = getItem(position);
		
		((TextView) productItem.findViewById(R.id.txtProduct)).setText(product.name);
		((TextView) productItem.findViewById(R.id.txtPrice)).setText(SendRequestFragment.convertToPrice(product.price));
		txtQuantity = (TextView) productItem.findViewById(R.id.txtQuantity);
		TextView txtX = (TextView) productItem.findViewById(R.id.txtX);
		TextView txtDescription = (TextView) productItem.findViewById(R.id.txtDescription);

		ImageButton btnAddProduct = (ImageButton) productItem.findViewById(R.id.btnAddProduct);
		ImageButton btnRemoveProduct = (ImageButton) productItem.findViewById(R.id.btnRemoveProduct);
		if(functionaryType == Functionary.ADMIN || functionaryType == Functionary.WAITER
				|| (functionaryType == Functionary.CLIENT_WAITER && app.getBillSelected() != null)) {
			txtQuantity.setVisibility(View.VISIBLE);
			txtX.setVisibility(View.VISIBLE);
			btnAddProduct.setVisibility(View.VISIBLE);
			btnRemoveProduct.setVisibility(View.VISIBLE);

			txtQuantity.setText(String.valueOf(product.quantity));
			btnAddProduct.setTag(product);
			btnRemoveProduct.setTag(product);
		}
		else{
			txtQuantity.setVisibility(View.GONE);
			txtX.setVisibility(View.GONE);
			btnAddProduct.setVisibility(View.GONE);
			btnRemoveProduct.setVisibility(View.GONE);
		}

		if(functionaryType != Functionary.ADMIN && functionaryType != Functionary.WAITER) {
			if(product.description != null && !product.description.trim().isEmpty()){
				txtDescription.setVisibility(View.VISIBLE);
				txtDescription.setText(product.description);
			}
			else{
				txtDescription.setVisibility(View.GONE);
			}
		}
		else{
			txtDescription.setVisibility(View.GONE);
		}
		
		return productItem;
	}
	
	public void orderProductListByPopularity(){
		
	}
	
	@Override
	public int getPosition(Product item) {
		return super.getPosition(item);
	}
	
}
