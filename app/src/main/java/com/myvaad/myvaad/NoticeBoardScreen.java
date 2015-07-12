package com.myvaad.myvaad;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.LocalServerSocket;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.rey.material.widget.ProgressView;

import adapters.NoticesAdapter;
import dialogs.RingProgressDialog;

public class NoticeBoardScreen extends Fragment {

    ImageView ok, cancel;
    FloatingActionButton addNoticeBtn;
    ListView noticeBoardListView;
    NoticesAdapter adapter;
    TextView content, noNoticesText;
    EditText contentEdit;
    ParseDB db;
    Intent i;
    View dialogLayout;
    ProgressView bar;
    Dialog noticesDialog;
    Button edit, update, delete, cancelBtn;
    String msg = "";
    List noticeBoardList = new ArrayList();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        db = ParseDB.getInstance(getActivity());
        db.saveUserInstallationInBackground();
        View rootView = inflater.inflate(R.layout.notice_board_screen, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setElevation(0);
        final SwipeRefreshLayout swipeView = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe);
        bar = (ProgressView) rootView.findViewById(R.id.progress_loader);
        bar.setVisibility(View.VISIBLE);
        swipeView.setColorSchemeColors(Color.parseColor("#007ca2"), Color.parseColor("#007ca2"), Color.parseColor("#007ca2"), Color.parseColor("#007ca2"));
        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeView.setRefreshing(true);
                Log.d("Swipe", "Refreshing Number");
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeView.setRefreshing(false);
                        refreshNotices();
                        Log.d("Swipe", "Refreshing Number*******************");
                    }
                }, 500);
            }
        });

        setHasOptionsMenu(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        //calls the list view and its adapter
        noticeBoardListView = (ListView) rootView.findViewById(R.id.NoticeBoardListView);

        noNoticesText = (TextView) rootView.findViewById(R.id.no_notices_text);

        String CurrentUserBuildingCode = db.getCurrentUserBuildingCode();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("noticeBoard");
        //Query Constraints-->all the notices for current user building
        query.whereContains("buildingCode", CurrentUserBuildingCode);
        query.orderByDescending("createdAt");

        //finding all the notices for current user building
        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> notices, ParseException e) {
                if (e == null) {
                    //Get current time
                    Calendar rightNow = Calendar.getInstance();
                    //Creating instance of SimpleDateFormat and init with new Date format

                    SimpleDateFormat postFormatter2 = new SimpleDateFormat("HH:mm", new Locale("he"));
                    SimpleDateFormat postFormatter3 = new SimpleDateFormat("אתמול" + "  " + "HH:mm", new Locale("he"));
                    SimpleDateFormat postFormatter4 = new SimpleDateFormat("EEEE  " + "HH:mm", new Locale("he"));
                    SimpleDateFormat postFormatter5 = new SimpleDateFormat("dd " + "ב" + "MMMM", new Locale("he"));
                    SimpleDateFormat toDayFormatter = new SimpleDateFormat("d");

                    String currentDayStr = toDayFormatter.format(rightNow.getTime());
                    int currentDay = Integer.parseInt(currentDayStr);

                    String noticeTimeStr = "";
                    noticeBoardList.clear();

                    for (ParseObject noticeRow : notices) {
                        List rowNoticeList = new ArrayList();
                        //get specific data from each row
                        String content = noticeRow.getString("content");
                        Date createdAt = noticeRow.getCreatedAt();

                        String updateAtDayStr = toDayFormatter.format(createdAt);
                        int updateAtDay = Integer.parseInt(updateAtDayStr);
                        int dayDiff = currentDay - updateAtDay;

                        if (dayDiff < 0) {//The message is in last month!!!
                            dayDiff += getLastMonthTotalDays();
                        }

                        if (dayDiff == 0) {//today
                            noticeTimeStr = postFormatter2.format(createdAt);
                        } else if (dayDiff == 1) {
                            noticeTimeStr = postFormatter3.format(createdAt);
                        } else if (dayDiff > 1 && dayDiff < 7) {
                            noticeTimeStr = postFormatter4.format(createdAt);
                        } else {
                            noticeTimeStr = postFormatter5.format(createdAt);
                        }

                        String ObjectId = noticeRow.getObjectId();
                        String familyName = noticeRow.getString("userFamilyName");
                        ParseFile userPicture = noticeRow.getParseFile("userPic");
                        String apartmentNumber = noticeRow.getString("apartmentNumber");
                        Bitmap userPic = db.parseFileToBitmap(userPicture);

                        rowNoticeList.add(ObjectId);
                        rowNoticeList.add(content);
                        rowNoticeList.add(noticeTimeStr);
                        rowNoticeList.add(familyName);
                        rowNoticeList.add(userPic);
                        rowNoticeList.add(apartmentNumber);
                        noticeBoardList.add(rowNoticeList);
                        adapter = new NoticesAdapter(getActivity(), noticeBoardList);
                        noticeBoardListView.setAdapter(adapter);
                        bar.setVisibility(View.GONE);
                    }
                    if (noticeBoardList.isEmpty()) {
                        bar.setVisibility(View.GONE);
                        noNoticesText.setVisibility(View.VISIBLE);
                    } else {
                        noNoticesText.setVisibility(View.GONE);
                    }
                } else {
                    Log.e("**PARSE ERROR**", "Error: " + e.getMessage());
                }
            }
        });

        getActivity().setTitle(R.string.NoticeBoardScreenTitle);

        if (!db.isCurrentUserAdmin())
            // trashBtn.setVisibility(View.GONE);
            setHasOptionsMenu(true);

        //listview item click listener
        noticeBoardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int idx,
                                    long arg3) {
                showNotice(adapter.getItem(idx));

            }
        });

        //scroll listener for listview
        noticeBoardListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
                if (firstVisibleItem == 0)
                    swipeView.setEnabled(true);
                else
                    swipeView.setEnabled(false);
            }
        });

        //floating add Button
        addNoticeBtn = (FloatingActionButton) rootView.findViewById(R.id.add_notice_btn);
        addNoticeBtn.attachToListView(noticeBoardListView);
        addNoticeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(getActivity())
                        .titleGravity(GravityEnum.END)
                        .contentGravity(GravityEnum.END)
                        .positiveColorRes(R.color.colorPrimary)
                        .neutralColorRes(R.color.colorPrimary)
                        .negativeColorRes(R.color.colorPrimary)
                        .widgetColorRes(R.color.colorPrimary)
                        .title(R.string.noticesShowDialogTitle)
                        .inputType(InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_PERSON_NAME |
                                InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                        .positiveText(R.string.add)
                        .btnStackedGravity(GravityEnum.START)
                        .forceStacking(true)
                        .alwaysCallInputCallback() // this forces the callback to be invoked with every input change
                        .input(R.string.noticesShowDialogData, 0, false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                            }
                        })
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(final MaterialDialog dialog) {
                                final RingProgressDialog loadDialog = new RingProgressDialog(getActivity());
                                dialog.dismiss();
                                ParseObject notice = new ParseObject("noticeBoard");
                                ParseUser currentUser = db.getcurrentUser();
                                //Get current user from the method getcurrentUser() and put him in a new field
                                notice.put("user", currentUser);
                                notice.put("userFamilyName", db.getcurrentUserFamilyName());
                                notice.put("userPic", currentUser.getParseFile("picture"));
                                notice.put("content", dialog.getInputEditText().getText().toString());
                                notice.put("apartmentNumber", currentUser.getString("apartmentNumber"));
                                //get current user buildingCode and put it in new field
                                notice.put("buildingCode", currentUser.getString("buildingCode"));
                                notice.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        refreshNotices();
                                        loadDialog.dismiss();
                                    }
                                });
                            }
                        }).show();
            }
        });

        return rootView;

    }

    public void showNotice(final List notice) {
        String hint = getResources().getString(R.string.noticesShowDialogData);
        MaterialDialog.Builder dialogM = new MaterialDialog.Builder(getActivity())
                .titleGravity(GravityEnum.END)
                .contentGravity(GravityEnum.END)
                .positiveColorRes(R.color.colorPrimary)
                .neutralColorRes(R.color.colorPrimary)
                .negativeColorRes(R.color.colorPrimary)
                .widgetColorRes(R.color.colorPrimary)
                .inputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PERSON_NAME |
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                .positiveText("סגור")
                .forceStacking(true)
                .btnStackedGravity(GravityEnum.START)
                .alwaysCallInputCallback() // this forces the callback to be invoked with every input change
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        if (("" + notice.get(3)).matches(db.getcurrentUserFamilyName()) || db.isCurrentUserAdmin()) {
                            db.editNoticeBoard(dialog.getInputEditText().getText().toString(), "" + notice.get(0));
                            refreshNotices();
                            dialog.dismiss();
                        } else {
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        db.deleteNotice("" + notice.get(0));
                        refreshNotices();
                        dialog.dismiss();
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        dialog.dismiss();
                    }
                });
        if (("" + notice.get(3)).matches(db.getcurrentUserFamilyName()) || db.isCurrentUserAdmin()) {
            dialogM.input(hint, "" + notice.get(1), false, new MaterialDialog.InputCallback() {
                @Override
                public void onInput(MaterialDialog dialog, CharSequence input) {
                    dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                }
            });
            dialogM.positiveText("עדכן");
            dialogM.negativeText("מחק");
            dialogM.neutralText("ביטול");
            dialogM.forceStacking(false);
        } else {
            dialogM.content("" + notice.get(1));
        }
        dialogM.show();
    }

    public void refreshNotices() {
        Fragment fragment1 = new NoticeBoardScreen();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_content, fragment1).commit();
    }

    public void deleteAllDialog() {
        new MaterialDialog.Builder(getActivity())
                .titleGravity(GravityEnum.END)
                .contentGravity(GravityEnum.END)
                .positiveColorRes(R.color.colorPrimary)
                .neutralColorRes(R.color.colorPrimary)
                .negativeColorRes(R.color.colorPrimary)
                .widgetColorRes(R.color.colorPrimary)
                .content(R.string.delete_all_notice_dialog_text)
                .inputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PERSON_NAME |
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        db.deleteAllNotices(db.getCurrentUserBuildingCode());
                        refreshNotices();
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.dismiss();
                    }
                }).show();
    }

    public void myToast(String s) {
        Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (db.isCurrentUserAdmin()) {
            inflater.inflate(R.menu.notice_board_menu, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.trash_btn:
                deleteAllDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public int getLastMonthTotalDays() {
        //get current month and year
        Calendar c = Calendar.getInstance();
        int thisMonth = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);

        if (thisMonth == 0) year -= 1; // thisMonth == 0 -->JANUARY
        // Create a calendar object and set year and month to last month --> thisMonth-1 == last month
        Calendar mycal = new GregorianCalendar(year, thisMonth - 1, 1);

        // Get the number of days in that month
        int daysInMonth = mycal.getActualMaximum(Calendar.DAY_OF_MONTH);

        return daysInMonth;
    }
}


