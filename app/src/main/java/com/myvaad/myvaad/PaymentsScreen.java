package com.myvaad.myvaad;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.parse.Parse;
import adapters.PaymentsPagerAdapter;


public class PaymentsScreen extends Fragment {
    ParseDB db;
    ViewPager pager;
    PaymentsPagerAdapter adapter;
    SlidingTabLayout tabs;
    int Numboftabs = 2;
    FragmentActivity myContext;
    FragmentManager fragManager;


    @Override
    public void onAttach(Activity activity) {
        myContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        CharSequence Titles[] = {getActivity().getString(R.string.general_payments), getActivity().getString(R.string.vaad_bait_payments)};

        Parse.initialize(getActivity());
        db = ParseDB.getInstance(getActivity());
        //View rootView = inflater.inflate(resource, root, attachToRoot)
        View rootView = inflater.inflate(R.layout.payments_main_layout, container, false);

        fragManager = myContext.getSupportFragmentManager();
        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        adapter = new PaymentsPagerAdapter(fragManager, Titles, Numboftabs);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) rootView.findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) rootView.findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);

        return rootView;
    }


}
