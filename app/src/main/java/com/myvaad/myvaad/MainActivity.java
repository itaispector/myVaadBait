package com.myvaad.myvaad;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private Toolbar mToolBar;
    private NavigationView mDrawer;
    private DrawerLayout mDrawerLayout;
    TextView familyName, userEmail;
    ImageView userImage, navHeaderBackground;
    ParseDB db;
    Fragment fragment0;
    FragmentManager fragmentManager;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = ParseDB.getInstance(this);

        if (getWindow().getDecorView().getLayoutDirection() == View.LAYOUT_DIRECTION_LTR) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        fragmentManager = getFragmentManager();
        fragment0 = new NoticeBoardScreen();
        fragmentManager.beginTransaction().replace(R.id.main_content, fragment0).commit();


        mToolBar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolBar);
        mDrawer = (NavigationView) findViewById(R.id.main_drawer);
        mDrawer.setNavigationItemSelectedListener(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolBar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                printStandardDate();
                super.onDrawerOpened(drawerView);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        familyName = (TextView) findViewById(R.id.nav_header_family_name);
        userEmail = (TextView) findViewById(R.id.nav_header_email);
        userImage = (ImageView) findViewById(R.id.user_image);
        navHeaderBackground = (ImageView) findViewById(R.id.nav_header_background);

        familyName.setText("משפחת " + db.getcurrentUserFamilyName());
        userEmail.setText(db.getcurrentUserEmail());
        userImage.setImageBitmap(db.getcurrentUserPicture());

        printStandardDate();

        if (db.isCurrentUserAdmin()) {
            mDrawer.getMenu().findItem(R.id.users_screen).setVisible(true);
        }
    }

    private void printStandardDate() {
        String currentDateTimeString = new SimpleDateFormat("HH").format(new Date()).toString();
        int currentTime = Integer.valueOf(currentDateTimeString);
        if (currentTime >= 20 || currentTime < 6) {
            navHeaderBackground.setImageDrawable(getResources().getDrawable(R.drawable.img_drawer_header));
        } else {
            navHeaderBackground.setImageDrawable(getResources().getDrawable(R.drawable.city_day_1));
        }
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
            case R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.about:
                new MaterialDialog.Builder(this)
                        .iconRes(R.mipmap.ic_launcher)
                        .titleGravity(GravityEnum.END)
                        .contentGravity(GravityEnum.END)
                        .positiveColorRes(R.color.colorPrimary)
                        .neutralColorRes(R.color.colorPrimary)
                        .negativeColorRes(R.color.colorPrimary)
                        .widgetColorRes(R.color.colorPrimary)
                        .customView(R.layout.about_dialog, true)
                        .title("אודות הועד שלי")
                        .positiveText("אישור")
                        .btnStackedGravity(GravityEnum.CENTER)
                        .forceStacking(true)
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        Fragment fragment1 = new FailuresScreen();
        Fragment fragment2 = new ExpensesScreen();
        Fragment fragment3 = new PaymentsScreen();
        Fragment fragment4 = new UsersScreen();

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
            case R.id.navigation_logout:
                db.logOutUser(this);
                mDrawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.navigation_userPrifile:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                Intent i = new Intent(this, UserProfile.class);
                startActivity(i);
                break;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getFragmentManager().findFragmentById(R.id.main_content);
        if (mDrawerLayout.isDrawerOpen(mDrawer)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (!(currentFragment instanceof NoticeBoardScreen)) {
            fragmentManager.beginTransaction().replace(R.id.main_content, fragment0).commit();
            mDrawer.getMenu().findItem(R.id.notice_board).setChecked(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Fragment currentFragment = getFragmentManager().findFragmentById(R.id.main_content);
        if (!(currentFragment instanceof PaymentsScreen) && !(currentFragment instanceof ExpensesScreen)) {
            FragmentTransaction fragTransaction = (this).getFragmentManager().beginTransaction();
            fragTransaction.detach(currentFragment);
            fragTransaction.attach(currentFragment);
            fragTransaction.commit();
        }
        familyName.setText("משפחת " + db.getcurrentUserFamilyName());
        userEmail.setText(db.getcurrentUserEmail());
        userImage.setImageBitmap(db.getcurrentUserPicture());
    }
}
