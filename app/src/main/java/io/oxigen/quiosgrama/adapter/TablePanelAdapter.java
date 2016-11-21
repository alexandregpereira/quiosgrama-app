package io.oxigen.quiosgrama.adapter;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import io.oxigen.quiosgrama.Bill;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.fragment.MapPagerFragment;

public class TablePanelAdapter extends BaseAdapter {

	private final MapPagerFragment mFragment;
	public ArrayList<Bill> billList;
	private Context context;

	public interface OnItemClickListener {
		void onItemClicked(Bill bill);
		void onItemLongClicked(Bill bill);
	}

	public TablePanelAdapter(Context context, MapPagerFragment fragment, ArrayList<Bill> billList){
		this.context = context;
		this.billList = billList;
		this.mFragment = fragment;
	}

	@Override
	public View getView(int i, View convertView, ViewGroup parent) {
		View btnTableMap = convertView;

		boolean isTablet = context.getResources().getBoolean(R.bool.is_tablet);
		if(btnTableMap == null){
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if(isTablet){
				btnTableMap = inflater.inflate(R.layout.button_table_panel, parent, false);
			}
			else{
				btnTableMap = inflater.inflate(R.layout.button_table_map, parent, false);
			}
		}
		
		Bill bill = getItem(i);

		TextView txtTableNumber = (TextView) btnTableMap.findViewById(R.id.txtTableNumber);
		txtTableNumber.setText(String.valueOf(bill.table.number));
		if(!isTablet) {
			ImageView imgCategory = (ImageView) btnTableMap.findViewById(R.id.imgCategory);

			if (bill.paidTime != null)
				imgCategory.setBackgroundResource(R.drawable.selector_ic_table_closed);
			else if (bill.openTime != null && bill.closeTime != null)
				imgCategory.setBackgroundResource(R.drawable.selector_ic_table_semi_closed);
			else if (bill.openTime != null)
				imgCategory.setBackgroundResource(R.drawable.selector_ic_table_opened);
			else
				imgCategory.setBackgroundResource(R.drawable.selector_ic_table_closed);
		}
		else{
			StateListDrawable states = new StateListDrawable();
			states.addState(new int[] {android.R.attr.state_pressed},
					new ColorDrawable(context.getResources().getColor(R.color.colorPrimary)));

			int color;
			if (bill.paidTime != null) {
				color = context.getResources().getColor(R.color.red);
			}
			else if (bill.openTime != null && bill.closeTime != null) {
				color = context.getResources().getColor(R.color.yellow_orange);
			}
			else if (bill.openTime != null){
				color = context.getResources().getColor(R.color.green);
			}
			else {
				color = context.getResources().getColor(R.color.red);
			}

			states.addState(new int[]{},
					new ColorDrawable(color));

			if(Build.VERSION.SDK_INT >= 16 ){
				btnTableMap.setBackground(states);
			}
			else{
				btnTableMap.setBackgroundDrawable(states);
			}
		}

		btnTableMap.setTag(bill);
		btnTableMap.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mFragment.onItemClicked((Bill) view.getTag());
			}
		});
		btnTableMap.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				mFragment.onItemLongClicked((Bill) view.getTag());
				return true;
			}
		});
		
		return btnTableMap;
	}
	
	@Override
	public int getCount() {
		return billList.size();
	}
	
	@Override
	public Bill getItem(int i) {
		return billList.get(i);
	}
	
	@Override
	public long getItemId(int arg0) {
		return 0;
	}
}
