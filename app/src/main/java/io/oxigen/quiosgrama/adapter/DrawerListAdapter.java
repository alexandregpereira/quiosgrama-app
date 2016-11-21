package io.oxigen.quiosgrama.adapter;

import org.xmlpull.v1.XmlPullParser;

import io.oxigen.quiosgrama.Product;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.fragment.SendRequestFragment;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DrawerListAdapter  extends ArrayAdapter<String>{
	
	private int layoutItem;

	public DrawerListAdapter(Context context, String[] menuArray) {
		super(context, R.layout.item_drawer_list, menuArray);
		layoutItem = R.layout.item_drawer_list;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View item = convertView;
		
		if(item == null){
			LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
			item = inflater.inflate(layoutItem, parent, false);
			
			String itemMenu = getItem(position);
			
			((TextView) item.findViewById(R.id.idDrawerItem)).setText(itemMenu);
			
			switch (position) {
				case 0:
					((TextView) item.findViewById(R.id.idDrawerItem)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_category, 0, 0, 0);
				break;
				
				case 1:
					((TextView) item.findViewById(R.id.idDrawerItem)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu, 0, 0, 0);
					break;
					
				case 2:
					((TextView) item.findViewById(R.id.idDrawerItem)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_map, 0, 0, 0);
					break;
					
				case 3:
					((TextView) item.findViewById(R.id.idDrawerItem)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_requests, 0, 0, 0);
					break;
					
				case 4:
					((TextView) item.findViewById(R.id.idDrawerItem)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_map, 0, 0, 0);
					break;
					
			}
		}
		
		return item;
	}

}
