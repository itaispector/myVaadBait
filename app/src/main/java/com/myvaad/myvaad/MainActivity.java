package com.myvaad.myvaad;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private Toolbar mToolBar;
    private NavigationView mDrawer;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getWindow().getDecorView().getLayoutDirection() == View.LAYOUT_DIRECTION_LTR){
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        setContentView(R.layout.activity_main);
        mToolBar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolBar);
        mDrawer = (NavigationView) findViewById(R.id.main_drawer);
        mDrawer.setNavigationItemSelectedListener(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolBar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.END);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        Fragment fragment0 = new NoticeBoardScreen();
        Fragment fragment1 = new FailuresScreen();
        Fragment fragment2 = new ExpensesScreen();
        Fragment fragment3 = new PaymentsScreen();
        Fragment fragment4 = new UsersScreen();
        FragmentManager fragmentManager = getFragmentManager();

        switch (menuItem.getItemId()) {
            case R.id.notice_board:
                fragmentManager.beginTransaction().replace(R.id.main_content, fragment0).commit();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                menuItem.setChecked(true);
                break;
            case R.id.failures_screen:
                fragmentManager.beginTransaction().replace(R.id.main_content, fragment1).commit();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                menuItem.setChecked(true);
                break;
            case R.id.expenses_screen:
                fragmentManager.beginTransaction().replace(R.id.main_content, fragment2).commit();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                menuItem.setChecked(true);
                break;
            case R.id.payments_screen:
                fragmentManager.beginTransaction().replace(R.id.main_content, fragment3).commit();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                menuItem.setChecked(true);
                break;
            case R.id.users_screen:
                fragmentManager.beginTransaction().replace(R.id.main_content, fragment4).commit();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                menuItem.setChecked(true);
                break;
        }

        return false;
    }
}
