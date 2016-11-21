package io.oxigen.quiosgrama.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.adapter.CategoryAdapter;
import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.service.SyncServerService;

public class CategoryFragment extends Fragment implements OnItemClickListener{
	
//	Container container;
	GridView gridCategory;
	View txtFirstTime;

	CategoryFragmentListener mListener;

	QuiosgramaApp app;
	
	public interface CategoryFragmentListener{
		public void onCategoryClicked(int prioritySelected);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.fragment_category, container, false);
		app = (QuiosgramaApp) getActivity().getApplication();
		
		gridCategory = (GridView) v.findViewById(R.id.gridCategory);
		txtFirstTime = v.findViewById(R.id.txtFirstTime);

		if(!QuiosgramaApp.firstTime) {
			if (app.getProductTypeList() == null) {
				Intent intent = new Intent(getActivity(), SyncServerService.class);
				intent.putExtra(KeysContract.METHOD_KEY, SyncServerService.GET_OBJECT_CONTAINER);
				getActivity().startService(intent);
			} else {
				buildCategoriesButtons();
			}
		}
		else{
			txtFirstTime.setVisibility(View.VISIBLE);
			gridCategory.setVisibility(View.GONE);
		}
		
		return v;
	}

	public void onSyncCompleted() {
		buildCategoriesButtons();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		mListener.onCategoryClicked(position);
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (CategoryFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement CategoryFragmentListener");
        }
    }
	
	private void buildCategoriesButtons() {
		if(!QuiosgramaApp.firstTime) {
			txtFirstTime.setVisibility(View.GONE);
			gridCategory.setVisibility(View.VISIBLE);

			gridCategory.setAdapter(new CategoryAdapter(getActivity(), app.getProductTypeList()));
			gridCategory.setOnItemClickListener(this);
		}
	}
}
