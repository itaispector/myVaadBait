package com.myvaad.myvaad;

import com.parse.Parse;

import adapters.UsersAdapter;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class UsersScreen extends Fragment {
    ListView usersList;
    UsersAdapter adapter;
    ParseDB db;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String appId = "QdwF666zm76ORQcn4KF6JNwDfsb6cj97QunbpT1s";
        String clientId = "OiJI3KdONEN9jML6Mi6r6iQTpR8mIOBv3YgsUhdv";
        //Initialize with keys
        Parse.initialize(getActivity(), appId, clientId);
        db = ParseDB.getInstance(getActivity());
        View rootView = inflater.inflate(R.layout.users_screen, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        //calls the list view and its adapter
        usersList = (ListView) rootView.findViewById(R.id.UsersListView);
        adapter = new UsersAdapter(getActivity(), db.getUsersList());
        usersList.setAdapter(adapter);

        getActivity().setTitle(R.string.UsersScreenTitle);
        setHasOptionsMenu(true);
        return rootView;

    }


}


