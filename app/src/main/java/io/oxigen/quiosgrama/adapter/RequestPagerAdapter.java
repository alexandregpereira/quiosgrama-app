package io.oxigen.quiosgrama.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class RequestPagerAdapter extends FragmentPagerAdapter {

	private List<Fragment> fragments;

	public RequestPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
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
	public CharSequence getPageTitle(int i) {
		return fragments.get(i).toString();
	}

}
