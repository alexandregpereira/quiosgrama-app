package io.oxigen.quiosgrama.adapter;

import io.oxigen.quiosgrama.ProductType;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.util.AndroidUtil;
import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CategoryAdapter extends BaseAdapter {
	
	public ArrayList<ProductType> categoryList;
	private Context context;

	public CategoryAdapter(Context context, ArrayList<ProductType> categoryList){
		this.context = context;
		this.categoryList = categoryList;
	}

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	public View getView(int i, View convertView, ViewGroup parent) {
		View gridView = convertView;
		
		if(gridView == null){
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			gridView = inflater.inflate(R.layout.item_category, parent, false);
		}
		
		ProductType category = getItem(i);
		
		TextView txtComplementImage = (TextView) gridView.findViewById(R.id.txtCategoryImage);
		txtComplementImage.setText(category.name);
		txtComplementImage.setCompoundDrawablesWithIntrinsicBounds(0, AndroidUtil.buildImagesValue(category.buttonImage), 0, 0);
		
		StateListDrawable states = new StateListDrawable();
		states.addState(new int[] {android.R.attr.state_pressed},
				new ColorDrawable(context.getResources().getColor(R.color.colorPrimary)));
		states.addState(new int[] { },
				new ColorDrawable(Color.parseColor(category.colorId)));
		
		if(Build.VERSION.SDK_INT >= 16 ){
			txtComplementImage.setBackground(states);
		}
		else{
			txtComplementImage.setBackgroundDrawable(states);
		}
		
		txtComplementImage.setTag(category);
		
		return gridView;
	}
	
	@Override
	public int getCount() {
		return categoryList.size();
	}
	
	@Override
	public ProductType getItem(int i) {
		return categoryList.get(i);
	}
	
	@Override
	public long getItemId(int arg0) {
		return 0;
	}
}
