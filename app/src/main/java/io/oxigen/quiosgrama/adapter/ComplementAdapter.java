package io.oxigen.quiosgrama.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;

import io.oxigen.quiosgrama.Complement;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.util.AndroidUtil;

public class ComplementAdapter extends BaseAdapter {
	
	public ArrayList<Complement> complementList;
	private HashSet<Complement> complementAddedSet;
	private Context context;

	public ComplementAdapter(Context context, ArrayList<Complement> complementList, HashSet<Complement> complementAddedSet){
		this.context = context;
		this.complementList = complementList;
		this.complementAddedSet = complementAddedSet;
	}

	@Override
	public View getView(int i, View convertView, ViewGroup parent) {
		View gridView = convertView;
		
		if(gridView == null){
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			gridView = inflater.inflate(R.layout.item_complement, parent, false);
		}
		
		Complement complement = getItem(i);
		
		TextView txtComplementImage = (TextView) gridView.findViewById(R.id.txtComplementImage);
		if(complement.price != 0){
			txtComplementImage.setText(String.format("%s R$ %.2f", complement.description, complement.price));
		}
		else {
			txtComplementImage.setText(complement.description);
		}
		txtComplementImage.setCompoundDrawablesWithIntrinsicBounds(0, AndroidUtil.buildImagesValue(complement.drawable), 0, 0);
		if(complementAddedSet.contains(complement))
			txtComplementImage.setBackgroundResource(R.drawable.shape_color_primary);
		else
			txtComplementImage.setBackgroundResource(R.drawable.selector_gridview_complement);
		
		return gridView;
	}
	
	@Override
	public int getCount() {
		return complementList.size();
	}
	
	@Override
	public Complement getItem(int i) {
		return complementList.get(i);
	}
	
	@Override
	public long getItemId(int arg0) {
		return 0;
	}
}
