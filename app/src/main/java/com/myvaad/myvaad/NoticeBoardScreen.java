package com.myvaad.myvaad;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.rey.material.widget.ProgressView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import adapters.NoticesAdapter;
import dialogs.RingProgressDialog;

public class NoticeBoardScreen extends Fragment {

    FloatingActionButton addNoticeBtn;
    ListView noticeBoardListView;
    NoticesAdapter adapter;
    TextView content, noNoticesText;
    EditText input;
    ParseDB db;
    Intent i;
    View dialogLayout;
    Dialog customDialog;
    ProgressView bar;
    Button edit, button, delete, cancel;
    String msg = "";
    List noticeBoardList = new ArrayList();
    InputMethodManager imm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        db = ParseDB.getInstance(getActivity());
        db.saveUserInstallationInBackground();
        View rootView = inflater.inflate(R.layout.notice_board_screen, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setElevation(0);
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
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
                        String userObjectId = noticeRow.getString("user");

                        rowNoticeList.add(ObjectId);        // 0
                        rowNoticeList.add(content);         // 1
                        rowNoticeList.add(noticeTimeStr);   // 2
                        rowNoticeList.add(familyName);      // 3
                        rowNoticeList.add(userPic);         // 4
                        rowNoticeList.add(apartmentNumber); // 5
                        rowNoticeList.add(userObjectId);    // 6
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
                addNoticeDialog();
            }
        });

        return rootView;

    }


    private void addNoticeDialog() {
        mDialog(R.layout.send_message_dialog);

        TextView title = (TextView) dialogLayout.findViewById(R.id.title);
        title.setText(getString(R.string.noticesShowDialogTitle));
        input = (EditText) dialogLayout.findViewById(R.id.input);
        input.setHint(getString(R.string.noticesShowDialogData));
        input.addTextChangedListener(textWatcherListener);
        button = (Button) dialogLayout.findViewById(R.id.button);
        button.setText(getString(R.string.add));
        input.requestFocus();
        openKeyboard();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = input.getText().toString();
                customDialog.dismiss();
                addNotice(msg);
            }
        });

        customDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                closeKeyboard();
            }
        });
    }

    private void addNotice(String msg) {
        final RingProgressDialog loadDialog = new RingProgressDialog(getActivity());
        ParseObject notice = new ParseObject("noticeBoard");
        ParseUser currentUser = db.getcurrentUser();
        //Get current user from the method getcurrentUser() and put him in a new field
        notice.put("user", currentUser);
        notice.put("userFamilyName", db.getcurrentUserFamilyName());
        notice.put("userPic", currentUser.getParseFile("picture"));
        notice.put("content", msg);
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

    public void showNotice(final List notice) {
        String userObjectId = "" + notice.get(6);
        String currentMsg = "" + notice.get(1);
        // if not admin or not user who posted notice
        if (db.isCurrentUserAdmin() || userObjectId.matches(db.getCurrentUserObjectId())) {
            // show dialog with edit options
            mDialog(R.layout.notice_dialog);

            customDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    closeKeyboard();
                }
            });

            button = (Button) dialogLayout.findViewById(R.id.update);
            delete = (Button) dialogLayout.findViewById(R.id.delete);
            cancel = (Button) dialogLayout.findViewById(R.id.cancel);
            input = (EditText) dialogLayout.findViewById(R.id.input);
            input.setText(currentMsg);
            input.requestFocus();
            openKeyboard();
            input.addTextChangedListener(textWatcherListener);


            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String editedNotice = input.getText().toString();
                    db.editNoticeBoard(editedNotice.toString(), "" + notice.get(0));
                    customDialog.dismiss();
                    refreshNotices();
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    db.deleteNotice("" + notice.get(0));
                    customDialog.dismiss();
                    refreshNotices();
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    customDialog.dismiss();
                }
            });
        } else {
            // show dialog only with msg and close btn
            MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                    .customView(R.layout.custom_layout_content, false)
                    .positiveText(getString(R.string.close))
                    .positiveColorRes(R.color.colorPrimary)
                    .btnStackedGravity(GravityEnum.END)
                    .forceStacking(true)
                    .show();
            View v = dialog.getCustomView();
            TextView tv = (TextView) v.findViewById(R.id.text);
            tv.setText(currentMsg);
        }

    }

    // listener to watch if fields are empty or not, if empty add button is disabled
    private TextWatcher textWatcherListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            boolean check = false;
            check = (input.getText().toString().isEmpty());
            button.setEnabled(!check);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };


    public void refreshNotices() {
        Fragment fragment1 = new NoticeBoardScreen();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_content, fragment1).commit();
    }

    public void deleteAllDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.custom_layout_content, false)
                .buttonsGravity(GravityEnum.END)
                .positiveColorRes(R.color.colorPrimary)
                .negativeColorRes(R.color.colorPrimary)
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
        View v = dialog.getCustomView();
        TextView tv = (TextView) v.findViewById(R.id.text);
        tv.setText(R.string.delete_all_notice_dialog_text);
    }

    public void mDialog(int layout) {
        dialogLayout = View.inflate(getActivity(), layout, null);
        customDialog = new Dialog(getActivity());
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        customDialog.setContentView(dialogLayout);
        customDialog.show();
    }

    public void myToast(String s) {
        Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();
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

    private void openKeyboard() {
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void closeKeyboard(){
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
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


}


