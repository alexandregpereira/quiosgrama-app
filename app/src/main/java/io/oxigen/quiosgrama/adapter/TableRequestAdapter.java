package io.oxigen.quiosgrama.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import io.oxigen.quiosgrama.ProductRequest;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.Request;
import io.oxigen.quiosgrama.util.AndroidUtil;
import io.oxigen.quiosgrama.util.ImageUtil;


public class TableRequestAdapter extends ArrayAdapter<Request>{

    HashMap<Request, HashSet<ProductRequest>> mapProdRequest;

	public TableRequestAdapter(Context context,
			Request[] objects, HashMap<Request, HashSet<ProductRequest>> mapProdRequest) {
		super(context, R.layout.item_table_request, objects);
		
        this.mapProdRequest = mapProdRequest;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View productRequestItem = convertView;

		LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
		productRequestItem = inflater.inflate(R.layout.item_table_request, parent, false);

		Request request = getItem(position);

		TextView txtWaiter = (TextView) productRequestItem.findViewById(R.id.txtWaiter);
		txtWaiter.setText(request.waiter.name);
		((TextView) productRequestItem.findViewById(R.id.txtDate)).setText(AndroidUtil.calculateDateDays(getContext(), request.requestTime, new Date()));

        TextView txtRequestResume = (TextView) productRequestItem.findViewById(R.id.txtRequestResume);
        String requestResume = "";

		boolean valid = false;
        for(ProductRequest prodRequest : mapProdRequest.get(request) ){
        	int quantity = 0;
        	if(prodRequest.quantity > 0){
        		quantity = prodRequest.quantity;
        	}
        	else if(prodRequest.product.quantity > 0){
        		quantity = prodRequest.product.quantity;
        	}

			String resume = quantity + " x " + prodRequest.product.name;

			if(!prodRequest.valid){
				String color = "#" + getContext().getResources().getString(R.color.text_disable).substring(3);
				resume ="<font color=" + color + ">" + resume + "</font>, ";
			}
			else{
				valid = true;
				txtWaiter.setTextColor(getContext().getResources().getColor(android.R.color.black));
				resume += ", ";
			}

			requestResume += resume;
        }

		if(requestResume.length() > 0){
			requestResume = requestResume.substring(0, requestResume.length()-2);
		}

        txtRequestResume.setText(Html.fromHtml(requestResume));

		if(valid){
			txtWaiter.setTextColor(getContext().getResources().getColor(android.R.color.black));
			txtWaiter.setPaintFlags(txtWaiter.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
		}
		else{
			txtWaiter.setTextColor(getContext().getResources().getColor(R.color.text_disable));
			txtWaiter.setPaintFlags(txtWaiter.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		}

        if(request.syncStatus == 1){
        	((ImageView)productRequestItem.findViewById(R.id.imgStatus))
        			.setImageBitmap(ImageUtil.changeColorIconBitmap(getContext(), R.drawable.ic_check, R.color.red));
        }
        else{
        	((ImageView)productRequestItem.findViewById(R.id.imgStatus))
					.setImageBitmap(ImageUtil.changeColorIconBitmap(getContext(), R.drawable.ic_check, R.color.green));
        }
        
        productRequestItem.setTag(request);
        
		return productRequestItem;
	}
}
