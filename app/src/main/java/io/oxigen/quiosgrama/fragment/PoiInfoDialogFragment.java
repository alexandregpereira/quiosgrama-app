package io.oxigen.quiosgrama.fragment;

import io.oxigen.quiosgrama.Poi;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.util.AndroidUtil;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;

public class PoiInfoDialogFragment extends DialogFragment {
	
	private Poi poi;

	public static PoiInfoDialogFragment newInstance(Poi poi){
		PoiInfoDialogFragment f = new PoiInfoDialogFragment();
		Bundle data = new Bundle();
		data.putParcelable(KeysContract.POI_KEY, poi);
		f.setArguments(data);
		return f;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog poiDialog = AndroidUtil.createCustomDialog(
				getActivity(), 
				R.style.Dialog, 
				R.layout.dialog_poi_info);
		
		Bundle data = getArguments();
		poi = data.getParcelable(KeysContract.POI_KEY);
		
		TextView txtPoiInfo = (TextView) poiDialog.findViewById(R.id.txtPoiInfo);
		txtPoiInfo.setCompoundDrawablesWithIntrinsicBounds(AndroidUtil.buildImagesValue(poi.image), 0, 0, 0);
		txtPoiInfo.setText(poi.name);
		
		TextView txtLastMod = (TextView) poiDialog.findViewById(R.id.txtLastMod);
		if(poi.waiterAlterPoi != null){
			txtLastMod.setText(poi.waiterAlterPoi.name);
		}
		else{
			TextView txtLastModLabel = (TextView) poiDialog.findViewById(R.id.txtLastModLabel);
			txtLastModLabel.setVisibility(View.GONE);
			txtLastMod.setVisibility(View.GONE);
		}
		
		return poiDialog;
	}
	
}
