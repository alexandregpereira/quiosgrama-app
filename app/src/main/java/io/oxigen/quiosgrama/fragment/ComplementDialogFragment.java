package io.oxigen.quiosgrama.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;

import io.oxigen.quiosgrama.Complement;
import io.oxigen.quiosgrama.ProductRequest;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.adapter.AutoCompleteAdapter;
import io.oxigen.quiosgrama.adapter.ComplementAdapter;
import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.util.AndroidUtil;

public class ComplementDialogFragment extends DialogFragment implements OnClickListener, OnItemClickListener {
	
	private static final String IS_EDITABLED_KEY = "isEditabledKey";
	
	public AutoCompleteTextView edtComplement;
	private TextView txtComplement;
	private TextView txtValue;
	private ImageButton btnAddComplement;
	private HashSet<Complement> complementAddedSet;

	QuiosgramaApp app;
	private double mTotal = 0;

	public static ComplementDialogFragment newInstance(ProductRequest productRequest){
		ComplementDialogFragment frag = new ComplementDialogFragment();
		Bundle args = new Bundle();
		args.putParcelable(KeysContract.PRODUCT_REQUEST_KEY, productRequest);
		args.putBoolean(IS_EDITABLED_KEY, true);
		frag.setArguments(args);
		return frag;
	}
	
	public static ComplementDialogFragment newInstance(ProductRequest productRequest, 
			boolean isEditabled){
		ComplementDialogFragment frag = new ComplementDialogFragment();
		Bundle args = new Bundle();
		args.putParcelable(KeysContract.PRODUCT_REQUEST_KEY, productRequest);
		args.putBoolean(IS_EDITABLED_KEY, isEditabled);
		frag.setArguments(args);
		return frag;
	}
	
	public interface ComplementDialogListener{
		public void onConfirmComplement(ProductRequest oldProductRequest, ProductRequest newProductRequest);
		public void onCancelComplement();
	}
	
	ComplementDialogListener mListener;

	private Button btnConfirm;
	private GridView gridComplement;

	private ComplementAdapter complementAdapter;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog complementDialog = AndroidUtil.createCustomDialog(getActivity(), 
				R.style.Dialog, 
				R.layout.dialog_complement);
		
		app = (QuiosgramaApp) getActivity().getApplication();
		
		complementAddedSet = new HashSet<>();
		buildWidgets(complementDialog);
		
		Bundle data = getArguments();
		ProductRequest productRequest = data.getParcelable(KeysContract.PRODUCT_REQUEST_KEY);
		if(productRequest.complement != null){
			buildComplementAddedSet(productRequest.complement.toString());
		}

		boolean isEditabled = data.getBoolean(IS_EDITABLED_KEY);
		if(!isEditabled){
			btnConfirm.setVisibility(View.GONE);
			edtComplement.setVisibility(View.GONE);
			btnAddComplement.setVisibility(View.GONE);
		}
		else{
			buildComplementGrid(complementDialog, productRequest);
		}
		
		return complementDialog;
	}

	private void buildWidgets(Dialog dialog) {
		edtComplement = (AutoCompleteTextView) dialog.findViewById(R.id.edtComplement);
		btnConfirm = (Button) dialog.findViewById(R.id.btnConfirm);
		txtComplement = (TextView) dialog.findViewById(R.id.txtComplement);
		txtValue = (TextView) dialog.findViewById(R.id.txtValue);
		btnAddComplement = (ImageButton) dialog.findViewById(R.id.btnAddComplement);
		
		ProductRequest productRequest = getArguments().getParcelable(KeysContract.PRODUCT_REQUEST_KEY);
		btnConfirm.setTag(productRequest);
		
		btnConfirm.setOnClickListener(this);
		btnAddComplement.setOnClickListener(this);
		
		if(app.getComplementList() != null){
			ArrayList<String> complementList = new ArrayList<>(app.getComplementList().size());
			for (Complement complement : app.getComplementList()) {
				complementList.add(complement.toString());
			}
			AutoCompleteAdapter completeAdapter = new AutoCompleteAdapter(
					getActivity(), 
					R.layout.item_autocomplete, 
					complementList);

			edtComplement.setAdapter(completeAdapter);
		}
	}
	
	private void buildComplementAddedSet(String complementText) {
		String[] complements = complementText.split(";");
		for (String compTemp : complements) {
			addComplementFromText(compTemp);
		}
	}
	
	private void buildComplementGrid(Dialog complementDialog, ProductRequest productRequest) {
		if(app.getComplementList() != null && productRequest.product != null){
			ArrayList<Complement> complementList = new ArrayList<>(app.getComplementList().size());
			for (Complement complement : app.getComplementList()) {
				if(complement.containsProductType(productRequest.product.type)){
					complementList.add(complement);
				}
			}
			
			if(!complementList.isEmpty()){
				gridComplement = (GridView) complementDialog.findViewById(R.id.gridComplement);
				complementAdapter = new ComplementAdapter(getActivity(), complementList, complementAddedSet);
				gridComplement.setAdapter(complementAdapter);
				gridComplement.setOnItemClickListener(this);
			}
		}
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (ComplementDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement ComplementDialogListener");
        }
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnConfirm:
				addComplementButtonAction();

				String complementString = txtComplement.getText().toString();
				ProductRequest oldProductRequest = (ProductRequest) v.getTag();
				Complement complement = new Complement(complementString);
				if(!complementString.equals("") &&
						!(oldProductRequest.complement != null &&
						oldProductRequest.complement.equals(complement))){

					complement.price = mTotal;
					ProductRequest newProductRequest = new ProductRequest(
							oldProductRequest.request, oldProductRequest.product, complement);
					newProductRequest.product.quantity = 1;
					mListener.onConfirmComplement(oldProductRequest, newProductRequest);
				}
				
				dismiss();
			break;
			
			case R.id.btnAddComplement:
				addComplementButtonAction();
			break;
			
			default:
			break;
		}
		 
	}

	private void addComplementButtonAction(){
		String complementText = edtComplement.getText().toString();

		if(complementText.contains(";")){
			String[] complements = complementText.split(";");
			for (String compTemp : complements) {
				addComplementFromText(compTemp);
			}
		}
		else{
			addComplementFromText(complementText);
		}
		edtComplement.setText("");
		AndroidUtil.hideKeyBoard(getActivity(), edtComplement);
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
		Complement complement = complementAdapter.getItem(position);
		complementAdapter.notifyDataSetChanged();
		
		if(!complementAddedSet.contains(complement))
			addComplement(complement);
		else
			removeComplement(complement);
	}
	
	private void addComplementFromText(String complementString) {
		complementString = complementString.trim();
		if(!complementString.isEmpty()){
			Complement comp = new Complement(complementString);
			if(!complementAddedSet.contains(comp)){
				if(app.getComplementList() != null && app.getComplementList().contains(comp)){
					int i = app.getComplementList().indexOf(comp);
					comp = app.getComplementList().get(i);
					if(complementAdapter != null) complementAdapter.notifyDataSetChanged();
				}
				else{
					app.addComplement(comp);
				}

				addComplement(comp);
			}
		}
	}

	private void removeComplement(Complement complement) {
		String complementText = txtComplement.getText().toString();
		String newComplementText = "";
		String[] complements = complementText.split(";");
		for (String compTemp : complements) {
			compTemp = compTemp.trim();
			if(!compTemp.equals(complement.description) && !compTemp.isEmpty()){
				if(newComplementText.isEmpty()){
					newComplementText = compTemp;
				}
				else {
					newComplementText += "; " + compTemp;
				}
			}
			else{
				complementAddedSet.remove(complement);
			}
		}
		
		txtComplement.setText(newComplementText);
		calculateValue();
	}

	private void addComplement(Complement complement) {
		String complementText = txtComplement.getText().toString();
		
		if(complementText == null)
			complementText = "";

		if(complementAddedSet.isEmpty()){
			complementText = complement.description;
		}
		else {
			complementText +=  "; " + complement.description;
		}
		complementAddedSet.add(complement);
		txtComplement.setText(complementText);

		calculateValue();
	}

	private void calculateValue() {
		if(complementAddedSet != null && !complementAddedSet.isEmpty()){
			mTotal = 0;
			for(Complement complement : complementAddedSet){
				mTotal += complement.price;
			}

			txtValue.setText(String.format("R$ %.2f", mTotal));
		}
		else{
			txtValue.setText(String.format("R$ %.2f", 0f));
		}
	}
}
