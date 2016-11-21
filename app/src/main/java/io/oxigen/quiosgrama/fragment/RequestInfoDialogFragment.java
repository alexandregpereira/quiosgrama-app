package io.oxigen.quiosgrama.fragment;

import io.oxigen.quiosgrama.ProductRequest;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.Request;
import io.oxigen.quiosgrama.adapter.ProductRequestListAdapter;
import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.util.AndroidUtil;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.ListView;
import android.widget.TextView;

public class RequestInfoDialogFragment extends DialogFragment{

    private ListView listViewProduct;

    private ProductRequestListAdapter prodReqAdapter;
    private ProductRequest[] prodRequestList;

	public static RequestInfoDialogFragment newInstance(ProductRequest[] prodRequestList){
		RequestInfoDialogFragment f = new RequestInfoDialogFragment();
		Bundle data = new Bundle();
		data.putParcelableArray(KeysContract.PRODUCT_REQUEST_LIST_KEY, prodRequestList);
		f.setArguments(data);
		return f;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog requestDialog = AndroidUtil.createCustomDialog(
				getActivity(), 
				R.style.Dialog, 
				R.layout.dialog_request_info);
		
        Bundle data = getArguments();
		prodRequestList = (ProductRequest[]) data.getParcelableArray(KeysContract.PRODUCT_REQUEST_LIST_KEY);
        
		if(prodRequestList != null && prodRequestList.length > 0){
			Request request = prodRequestList[0].request;
			((TextView)requestDialog.findViewById(R.id.txtTitle)).setText(
					request.waiter.toString() + " - "  + request.bill.toString());
		}
		
        listViewProduct = (ListView) requestDialog.findViewById(R.id.listViewProduct);
        
        
        prodReqAdapter = new ProductRequestListAdapter(getActivity(), 
					prodRequestList, 
					true);
                    
        listViewProduct.setAdapter(prodReqAdapter);
        
		return requestDialog;
	}
	
}
