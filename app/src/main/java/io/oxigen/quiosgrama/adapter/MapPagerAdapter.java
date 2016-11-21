package io.oxigen.quiosgrama.adapter;

import io.oxigen.quiosgrama.fragment.MapFragment;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;

public class MapPagerAdapter extends FragmentPagerAdapter {

	private List<MapFragment> fragments;

	public MapPagerAdapter(FragmentManager fm, ArrayList<MapFragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}

	@Override
	public int getCount() {
		return fragments.size();
	}

	@Override
	public Fragment getItem(int i) {
		return fragments.get(i);
	}
	
	@Override
    public int getItemPosition(Object object){
        return PagerAdapter.POSITION_NONE;
    }
	
}
