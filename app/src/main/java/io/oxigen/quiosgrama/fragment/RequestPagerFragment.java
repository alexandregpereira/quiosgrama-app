package io.oxigen.quiosgrama.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;
import java.util.ArrayList;

import io.oxigen.quiosgrama.Functionary;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.adapter.RequestPagerAdapter;
import io.oxigen.quiosgrama.listener.BackFragmentListener;

/**
 * Created by Alexandre on 14/04/2016.
 *
 */
public class RequestPagerFragment extends Fragment {

    private ArrayList<Fragment> fragments;
    private BackFragmentListener backListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_request_pager, container, false);
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        QuiosgramaApp app = (QuiosgramaApp) getActivity().getApplication();

        ViewPager pagerRequest = (ViewPager) v.findViewById(R.id.pagerRequest);
        TabLayout tabLayout = (TabLayout) v.findViewById(R.id.tablayout);

        int functionaryType = app.getFunctionarySelectedType();

        if(functionaryType == Functionary.CLIENT_WAITER){
            tabLayout.setVisibility(View.GONE);
        }

        if(fragments == null){
            fragments = new ArrayList<>();

            fragments.add(new WaiterRequestFragment());
            if(functionaryType == Functionary.ADMIN || functionaryType == Functionary.WAITER) {
                fragments.add(new TableRequestFragment());
            }
        }

        RequestPagerAdapter requestAdapter = new RequestPagerAdapter(getChildFragmentManager(), fragments);
        pagerRequest.setAdapter(requestAdapter);
        tabLayout.setupWithViewPager(pagerRequest);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            backListener = (BackFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement BackListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class
                    .getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        backListener.backFragmentPressed();
    }
}
