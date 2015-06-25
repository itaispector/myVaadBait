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
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.melnykov.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import adapters.NoticesAdapter;

public class NoticeBoardScreen extends Fragment {

    ImageView noticeBoardUserImage, trashBtn, refreshBtn, ok, cancel;
    FloatingActionButton addNoticeBtn;
    ListView noticeBoardList;
    NoticesAdapter adapter;
    TextView content, noticeBoardFamilyName,noNoticesText;
    EditText contentEdit;
    RelativeLayout nameAndPicHolder;
    ParseDB db;
    Intent i;
    ProgressBarCircularIndeterminate bar;
    View dialogLayout;
    Dialog noticesDialog;
    DrawerLayout mDrawerLayout;
    protected ListView mDrawerList;
    String[] mPagesTitles;
    Button edit, update, delete, cancelBtn;
    String msg = "";
    List NoticeBoardList = new ArrayList();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Initialize with keys
        Parse.initialize(getActivity());
        db = ParseDB.getInstance(getActivity());
        View rootView = inflater.inflate(R.layout.notice_board_screen, container, false);
        nameAndPicHolder = (RelativeLayout) rootView.findViewById(R.id.noticeBoardNamePicHolder);

        bar = (ProgressBarCircularIndeterminate) rootView.findViewById(R.id.progressBarCircularIndeterminate);
        bar.setVisibility(View.VISIBLE);

        final SwipeRefreshLayout swipeView = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe);
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

                        adapter.refresh(NoticeBoardList);

                        Log.d("Swipe", "Refreshing Number*******************");
                    }
                }, 500);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }


        //calls the list view and its adapter
        noticeBoardList = (ListView) rootView.findViewById(R.id.NoticeBoardListView);
        noticeBoardFamilyName = (TextView) rootView.findViewById(R.id.noticeBoardFamilyName);
        noticeBoardUserImage = (ImageView) rootView.findViewById(R.id.noticeBoardUserImage);

        noNoticesText = (TextView) rootView.findViewById(R.id.no_notices_text);

        trashBtn = (ImageView) rootView.findViewById(R.id.noticeBoardDeleteAllNoticesBtn);
        refreshBtn = (ImageView) rootView.findViewById(R.id.noticeBoardRefreshBtn);


        //adapter =  new NoticesAdapter(getActivity(),db.getCurrentUserNoticeBoard());
        //noticeBoardList.setAdapter(adapter);
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
                    NoticeBoardList.clear();
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
                        NoticeBoardList.add(rowNoticeList);
                        adapter = new NoticesAdapter(getActivity(), NoticeBoardList);
                        noticeBoardList.setAdapter(adapter);
                        bar.setVisibility(View.GONE);
                    }
                    if(NoticeBoardList.isEmpty()){
                        bar.setVisibility(View.GONE);
                        noNoticesText.setVisibility(View.VISIBLE);
                    }else{
                        noNoticesText.setVisibility(View.GONE);
                    }
                } else {
                    Log.e("**PARSE ERROR**", "Error: " + e.getMessage());
                }
            }
        });

        getActivity().setTitle(R.string.NoticeBoardScreenTitle);


        noticeBoardFamilyName.setText(db.getcurrentUserFamilyName());
        noticeBoardUserImage.setImageBitmap(db.getcurrentUserPicture());
        if (!db.isCurrentUserAdmin())
            trashBtn.setVisibility(View.GONE);
        setHasOptionsMenu(true);

        //listview item click listener
        noticeBoardList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int idx,
                                    long arg3) {
                showNotice(adapter.getItem(idx));

            }
        });

        //scroll listener for listview
        noticeBoardList.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                /*
                nameAndPicHolder.setVisibility(View.GONE);
					if (scrollState==SCROLL_STATE_IDLE && noticeBoardList.getChildAt(0).getTop()>=15){						
						nameAndPicHolder.setVisibility(View.VISIBLE);						
					}
					*/
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
                if (firstVisibleItem == 0)
                    swipeView.setEnabled(true);
                else
                    swipeView.setEnabled(false);
            }
        });
        //--------      

        //refresh button listener
        refreshBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_animation);
                refreshBtn.startAnimation(animation);

                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation arg0) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onAnimationRepeat(Animation arg0) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onAnimationEnd(Animation arg0) {
                        refreshNotices();
                    }
                });
            }
        });


        //floating add Button
        addNoticeBtn = (FloatingActionButton) rootView.findViewById(R.id.add_notice_btn);
        addNoticeBtn.attachToListView(noticeBoardList);
        addNoticeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNotice();
            }
        });


        //delete all messages listener
        trashBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                deleteAllDialog();

            }
        });
        return rootView;

    }

    public void addNotice() {
        dialogLayout = View.inflate(getActivity(), R.layout.notice_board_add_message_dialog, null);
        noticesDialog = new Dialog(getActivity());
        noticesDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        noticesDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        noticesDialog.setContentView(dialogLayout);
        noticesDialog.show();

        ok = (ImageView) dialogLayout.findViewById(R.id.noticesDialogNoticeOkBtn);
        cancel = (ImageView) dialogLayout.findViewById(R.id.noticesDialogNoticeCancelBtn);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogButtons(v);
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogButtons(v);
            }
        });
    }

    //action for ok or cancel button - add notice or close dialog
    public void dialogButtons(View v) {
        contentEdit = (EditText) dialogLayout.findViewById(R.id.noticesDialogNoticeData);
        msg = contentEdit.getText().toString();
        if (v.getId() == R.id.noticesDialogNoticeCancelBtn) {
            noticesDialog.dismiss();
        } else {
            if (msg.matches("")) {
                Toast toast = Toast.makeText(getActivity(), R.string.empty_notice, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            } else {
                db.updateNoticeBoard(msg);
                refreshNotices();
                noticesDialog.dismiss();
            }
        }
    }


    public void showNotice(final List notice) {
        dialogLayout = View.inflate(getActivity(), R.layout.notice_board_show_message_dialog, null);
        noticesDialog = new Dialog(getActivity());
        noticesDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        noticesDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        noticesDialog.setContentView(dialogLayout);
        content = (TextView) dialogLayout.findViewById(R.id.noticesShowDialogNoticeDataTV);
        content.setText("" + notice.get(1));
        edit = (Button) dialogLayout.findViewById(R.id.noticesShowDialogNoticeEditBtn);
        update = (Button) dialogLayout.findViewById(R.id.noticesShowDialogNoticeUpdateBtn);
        delete = (Button) dialogLayout.findViewById(R.id.noticesShowDialogNoticeDeleteBtn);
        cancelBtn = (Button) dialogLayout.findViewById(R.id.noticesShowDialogNoticeCancelBtn);

        //checks who is the user to see what buttons to show in the notice show dialog
        if (!("" + notice.get(3)).matches(db.getcurrentUserFamilyName()) && !db.isCurrentUserAdmin()) {
            delete.setVisibility(View.GONE);
            edit.setVisibility(View.GONE);
        } else if (("" + notice.get(3)).matches(db.getcurrentUserFamilyName()) || db.isCurrentUserAdmin()) {
            //allowing edit only of admin or notice owner
            content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editContent();
                }
            });
        }


        noticesDialog.show();

        //cancel button listener
        cancelBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                noticesDialog.dismiss();
            }
        });

        //edit button listener
        edit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                editContent();

            }
        });

        //update button listener
        update.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                msg = contentEdit.getText().toString();
                db.editNoticeBoard(msg, "" + notice.get(0));
                refreshNotices();
                noticesDialog.dismiss();
                edit.setVisibility(View.VISIBLE);
                delete.setVisibility(View.VISIBLE);
                update.setVisibility(View.GONE);

            }
        });

        //delete button listener
        delete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                db.deleteNotice("" + notice.get(0));
                refreshNotices();
                noticesDialog.dismiss();
            }
        });
    }

    //sets the notice to be editabled
    public void editContent() {
        edit.setVisibility(View.GONE);
        update.setVisibility(View.VISIBLE);
        contentEdit = (EditText) dialogLayout.findViewById(R.id.noticesShowDialogNoticeData);
        contentEdit.setText(content.getText().toString());
        contentEdit.setVisibility(View.VISIBLE);
        contentEdit.requestFocus();
        content.setVisibility(View.GONE);
        delete.setVisibility(View.GONE);
    }

    public void refreshNotices() {
        Fragment fragment1 = new NoticeBoardScreen();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment1).commit();
    }

    public void deleteAllDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setMessage(R.string.title);

        dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.deleteAllNotices(db.getCurrentUserBuildingCode());
                refreshNotices();
            }
        });

        dialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        dialog.show();
    }

    public void myToast(String s) {
        Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();
    }

}


