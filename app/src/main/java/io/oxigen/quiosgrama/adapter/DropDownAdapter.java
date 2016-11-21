package io.oxigen.quiosgrama.adapter;

import io.oxigen.quiosgrama.Bill;

import java.util.ArrayList;

import android.content.Context;
import android.widget.ArrayAdapter;

public class DropDownAdapter extends ArrayAdapter<Bill>{

	public DropDownAdapter(Context context, int resource,
			int textViewResourceId, ArrayList<Bill> objects) {
		
		super(context, resource, textViewResourceId, objects);
	}

}
