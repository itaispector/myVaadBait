package com.myvaad.myvaad;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import com.rey.material.widget.ProgressView;

import adapters.NoticesAdapter;

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
        //Initialize with keys
        Parse.initialize(getActivity());
        db = ParseDB.getInstance(getActivity());
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


        //adapter =  new NoticesAdapter(getActivity(),db.getCurrentUserNoticeBoard());
        //noticeBoardListView.setAdapter(adapter);
        String CurrentUserBuildingCode = db.getCurrentUserBuildingCode();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("noticeBoard");
        //Query Constraints-->all the notices for current user building
        query.whereContains("buildingCode", CurrentUserBuildingCode);
        query.orderByDescending("updatedAt");

        //finding all the notices for current user building
        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> notices, ParseException e) {
                if (e == null) {
                    noticeBoardList.clear();
                    for (ParseObject noticeRow : notices) {
                        List rowNoticeList = new ArrayList();
                        //get specific data from each row
                        String content = noticeRow.getString("content");

                        Date updatedAt = noticeRow.getUpdatedAt();
                        String noticeTime = updatedAt.toLocaleString();
                        String ObjectId = noticeRow.getObjectId();

                        String familyName = noticeRow.getString("userFamilyName");
                        ParseFile userPicture = noticeRow.getParseFile("userPic");
                        Bitmap userPic = db.parseFileToBitmap(userPicture);

                        rowNoticeList.add(ObjectId);
                        rowNoticeList.add(content);
                        rowNoticeList.add(noticeTime);
                        rowNoticeList.add(familyName);
                        rowNoticeList.add(userPic);
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
                            public void onPositive(MaterialDialog dialog) {
                                db.updateNoticeBoard(dialog.getInputEditText().getText().toString());
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        refreshNotices();
                                    }

                                },350);
                                dialog.dismiss();
                            }
                        }).show();
            }
        });

        return rootView;

    }

    /*
    @Override
    public void onResume() {
        super.onResume();
        refreshNotices();
    }
    */

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
                .positiveText("ביטול")
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
        }else{
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
}


