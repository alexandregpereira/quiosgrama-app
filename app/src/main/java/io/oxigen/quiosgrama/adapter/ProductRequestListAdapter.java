package io.oxigen.quiosgrama.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import io.oxigen.quiosgrama.ProductRequest;
import io.oxigen.quiosgrama.ProductType;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.fragment.SendRequestFragment;

public class ProductRequestListAdapter extends ArrayAdapter<ProductRequest>{
	
	private int layoutItem;
	public int index;
	public ArrayList<ProductRequest> productRequestList;
	public ArrayList<ProductRequest> mProductRequestActionModeList;
	private TextView txtQuantity;
	private boolean isTableInfo;

	public ProductRequestListAdapter(Context context, 
			ArrayList<ProductRequest> productRequestList, boolean isTableInfo) {
		
		super(context, R.layout.item_product_request, productRequestList);
		
		this.layoutItem =  R.layout.item_product_request;
		this.productRequestList = productRequestList;
		this.isTableInfo = isTableInfo;
		mProductRequestActionModeList = new ArrayList<>();
	}
    
    public ProductRequestListAdapter(Context context, 
			ProductRequest[] productRequestList, boolean isTableInfo) {
		
		super(context, R.layout.item_product_request, productRequestList);
		
		this.layoutItem =  R.layout.item_product_request;
		this.isTableInfo = isTableInfo;
		mProductRequestActionModeList = new ArrayList<>();
	}
	
	public ProductRequestListAdapter(Context context, 
			ArrayList<ProductRequest> productRequestList) {
		
		super(context, R.layout.item_product_request, productRequestList);
		
		this.layoutItem =  R.layout.item_product_request;
		this.productRequestList = productRequestList;
		this.isTableInfo = false;
	}
	
	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View productItem = convertView;

		if(productItem == null){
			LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
			productItem = inflater.inflate(layoutItem, parent, false);
		}
		
		ProductRequest productRequest = getItem(position);
		
		if(productRequest != null){
			if(mProductRequestActionModeList != null && mProductRequestActionModeList.contains(productRequest)){
				productItem.setBackgroundResource(R.color.colorPrimary);
			}
			else{
				productItem.setBackgroundResource(android.R.color.transparent);
			}

//			int colorType = Color.parseColor(productRequest.product.type.colorId);
//			if(position == 0){
//				GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{colorType, colorType});
//				float radius = getContext().getResources().getDimension(R.dimen.shape_default_radius);
//				float[] corners = new float[]{radius, radius, 0, 0, 0, 0, 0, 0};
//				gd.setCornerRadii(corners);
//				viewTypeColor.setBackground(gd);
//			}
//			else if(position == productRequestList.size() -1){
//				GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{colorType, colorType});
//				float radius = getContext().getResources().getDimension(R.dimen.shape_default_radius);
//				float[] corners = new float[]{0, 0, 0, 0, 0, 0, radius, radius};
//				gd.setCornerRadii(corners);
//				viewTypeColor.setBackground(gd);
//			}
			View viewTypeColor = productItem.findViewById(R.id.viewTypeColor);
			ImageView imgProductType = (ImageView) productItem.findViewById(R.id.imgProductType);
			productRequest.product.type = searchProductType(productRequest.product.type.id);
			if(isTableInfo){
				imgProductType.setVisibility(View.VISIBLE);
				viewTypeColor.setVisibility(View.GONE);

				GradientDrawable shapeDrawable = (GradientDrawable) imgProductType.getBackground();
				shapeDrawable.setColor(Color.parseColor(productRequest.product.type.colorId));
				imgProductType.setImageResource(productRequest.product.type.imageInfoId);
			}
			else{
				imgProductType.setVisibility(View.GONE);
				viewTypeColor.setVisibility(View.VISIBLE);

				viewTypeColor.setBackgroundColor(Color.parseColor(productRequest.product.type.colorId));
			}

			TextView txtProduct = (TextView) productItem.findViewById(R.id.txtProduct);
			txtProduct.setText(productRequest.product.name);

			TextView txtPrice = (TextView) productItem.findViewById(R.id.txtPrice);
			TextView txtComplementPrice = (TextView) productItem.findViewById(R.id.txtComplementPrice);
			TextView txtPriceTotal = (TextView) productItem.findViewById(R.id.txtPriceTotal);
			if(productRequest.product.quantity == 1){
				txtPrice.setVisibility(View.GONE);
			}
			else{
				txtPrice.setVisibility(View.VISIBLE);
				txtPrice.setText(SendRequestFragment.convertToPrice(productRequest.product.price));
			}

			if(productRequest.complement != null) {
				txtPriceTotal.setText(SendRequestFragment.convertToPrice(productRequest.product.price * productRequest.product.quantity + productRequest.complement.price * productRequest.product.quantity));
			}
			else{
				txtPriceTotal.setText(SendRequestFragment.convertToPrice(productRequest.product.price * productRequest.product.quantity));
			}

			if(productRequest.complement != null && productRequest.complement.price != 0){
				txtComplementPrice.setVisibility(View.VISIBLE);
				if(productRequest.complement.price > 0){
					txtComplementPrice.setText(String.format("+%.2f", productRequest.complement.price * productRequest.product.quantity));
				}
				else{
					txtComplementPrice.setText(String.format("-%.2f", productRequest.complement.price * productRequest.product.quantity));
				}
			}
			else{
				txtComplementPrice.setVisibility(View.GONE);
			}

			txtQuantity = (TextView) productItem.findViewById(R.id.txtQuantity);
			txtQuantity.setText(String.valueOf(productRequest.product.quantity));

			ImageButton btnAddProductRequest = (ImageButton) productItem.findViewById(R.id.btnAddProductRequest);
			ImageButton btnRemoveProductRequest = (ImageButton) productItem.findViewById(R.id.btnRemoveProductRequest);
			Button btnAddComplement = (Button) productItem.findViewById(R.id.btnAddComplement);

			if(isTableInfo){
				btnAddProductRequest.setVisibility(View.GONE);
				btnRemoveProductRequest.setVisibility(View.GONE);
				btnAddComplement.setVisibility(View.GONE);
			}
			else{
				btnAddProductRequest.setTag(productRequest);
				btnRemoveProductRequest.setTag(productRequest);
			}

			TextView txtCheckComplement = (TextView) productItem.findViewById(R.id.txtCheckComplement);
			if(productRequest.complement == null 
					|| productRequest.complement.description == null
					|| productRequest.complement.description.trim().isEmpty()){
				if(isTableInfo){
					txtCheckComplement.setVisibility(View.GONE);
				}
				else
					btnAddComplement.setBackgroundResource(R.drawable.selector_btn_save);
			}
			else{
				if(isTableInfo){
					txtCheckComplement.setVisibility(View.VISIBLE);
					txtCheckComplement.setText(productRequest.complement.description);
				}
				else
					btnAddComplement.setBackgroundResource(R.drawable.selector_btn_complement_on);
			}

			TextView txtTransferRoute = (TextView) productItem.findViewById(R.id.txtTransferRoute);
			if(isTableInfo) {
				if (productRequest.transferRoute == null) {
					txtTransferRoute.setVisibility(View.GONE);
				} else {
					txtTransferRoute.setVisibility(View.VISIBLE);
					txtCheckComplement.setVisibility(View.VISIBLE);
					txtTransferRoute.setText(productRequest.transferRoute);
				}
			}
			else{
				txtTransferRoute.setVisibility(View.GONE);
			}

			TextView txtX = (TextView) productItem.findViewById(R.id.txtX);
			int color;
			if(!productRequest.valid){
				color = getContext().getResources().getColor(R.color.text_disable);
				txtQuantity.setTextColor(color);
				txtCheckComplement.setTextColor(color);
				txtPrice.setTextColor(color);
				txtComplementPrice.setTextColor(color);

				txtProduct.setPaintFlags(txtProduct.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				txtPrice.setPaintFlags(txtPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				txtComplementPrice.setPaintFlags(txtComplementPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				txtQuantity.setPaintFlags(txtQuantity.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				txtCheckComplement.setPaintFlags(txtCheckComplement.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				txtPriceTotal.setPaintFlags(txtPriceTotal.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			}
			else{
				color = getContext().getResources().getColor(android.R.color.black);
				txtQuantity.setTextColor(getContext().getResources().getColor(R.color.red));
				txtCheckComplement.setTextColor(getContext().getResources().getColor(R.color.text_less_focus));
				txtPrice.setTextColor(getContext().getResources().getColor(R.color.text_less_focus));
				txtComplementPrice.setTextColor(getContext().getResources().getColor(R.color.text_less_focus));

				txtProduct.setPaintFlags(txtProduct.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
				txtPrice.setPaintFlags(txtPrice.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
				txtComplementPrice.setPaintFlags(txtComplementPrice.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
				txtQuantity.setPaintFlags(txtQuantity.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
				txtCheckComplement.setPaintFlags(txtCheckComplement.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
				txtPriceTotal.setPaintFlags(txtPriceTotal.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
			}

			txtProduct.setTextColor(color);
			txtX.setTextColor(color);
			txtPriceTotal.setTextColor(color);

			btnAddComplement.setTag(productRequest);
		}
		
		return productItem;
	}

	private ProductType searchProductType(long id) {
		QuiosgramaApp app = (QuiosgramaApp) getContext().getApplicationContext();
		ArrayList<ProductType> typeList = app.getProductTypeList();
		for(ProductType type : typeList){
			if(id == type.id) return type;
		}
		return null;
	}

	@Override
	public int getPosition(ProductRequest productRequest) {
		return super.getPosition(productRequest);
	}
}
