package com.com.boha.monitor.library.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.com.boha.monitor.library.adapters.PictureAdapter;
import com.com.boha.monitor.library.dto.CompanyStaffDTO;
import com.com.boha.monitor.library.dto.ProjectSiteDTO;
import com.com.boha.monitor.library.dto.transfer.PhotoUploadDTO;
import com.com.boha.monitor.library.dto.transfer.RequestDTO;
import com.com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.com.boha.monitor.library.util.DividerItemDecoration;
import com.com.boha.monitor.library.util.ErrorUtil;
import com.com.boha.monitor.library.util.SharedUtil;
import com.com.boha.monitor.library.util.Statics;
import com.com.boha.monitor.library.util.Util;
import com.com.boha.monitor.library.util.WebSocketUtil;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;

public class SitePictureGridActivity extends ActionBarActivity {

    RecyclerView list;
    TextView title;
    Context ctx;
    Button btnStatusRpt;
    ProjectSiteDTO projectSite;
    Activity activity;
    int lastIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getApplicationContext();
        activity = this;
        setContentView(R.layout.activity_picture_recycler_grid);

        title = (TextView) findViewById(R.id.RCV_title);
        btnStatusRpt = (Button) findViewById(R.id.RCV_btnStatusReport);


        projectSite = (ProjectSiteDTO) getIntent().getSerializableExtra("projectSite");
        if (projectSite != null) {
            if (projectSite.getPhotoUploadList() == null || projectSite.getPhotoUploadList().isEmpty()) {
                Util.showErrorToast(ctx, getString(R.string.no_photos));
                finish();
            }
            photoList = projectSite.getPhotoUploadList();
            if (projectSite.getBeneficiary() != null) {
                title.setText(projectSite.getProjectSiteName() + ": " + projectSite.getBeneficiary().getFullName());
            } else {
                title.setText(projectSite.getProjectSiteName());
            }
            setGrid();
        }
        btnStatusRpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i;
                if (projectSite != null) {
                    i = new Intent(ctx, SiteStatusReportActivity.class);
                    i.putExtra("projectSite", projectSite);
                    startActivity(i);
                }


            }
        });
        setTitle(SharedUtil.getCompany(ctx).getCompanyName());
        CompanyStaffDTO staff = SharedUtil.getCompanyStaff(ctx);
        getSupportActionBar().setSubtitle(staff.getFullName());
        Statics.setRobotoFontLight(ctx, title);
        MonApp app = (MonApp) getApplication();
        Tracker t = app.getTracker(MonApp.TrackerName.APP_TRACKER);

        t.setScreenName("PictureRecyclerGridActivity");
        t.send(new HitBuilders.AppViewBuilder().build());

        //TODO - remove after deleting duplicate photos on server
//        title.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (photosForDeletion.isEmpty()) return;
//                Util.flashOnce(title, 200, new Util.UtilAnimationListener() {
//                    @Override
//                    public void onAnimationEnded() {
//                        AlertDialog.Builder d = new AlertDialog.Builder(activity);
//                        d.setIcon(ctx.getResources().getDrawable(R.drawable.delete_48))
//                                .setTitle("Delete Photos")
//                                .setMessage("Do you want to delete " + photosForDeletion.size() + " photos?")
//                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//
//                                        deletePhotos();
//                                    }
//                                })
//                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//
//                                    }
//                                })
//                                .show();
//                    }
//                });
//            }
//        });
    }

    List<PhotoUploadDTO> photoList;

    private void setGrid() {
        list = (RecyclerView) findViewById(R.id.FI_recyclerView);
        list.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        list.setItemAnimator(new DefaultItemAnimator());
        list.addItemDecoration(new DividerItemDecoration(ctx, RecyclerView.VERTICAL));

        adapter = new PictureAdapter(photoList,
                ctx, new PictureAdapter.PictureListener() {
            @Override
            public void onPictureClicked(int position) {
                Log.e(LOG, "Picture clicked..., position = " + position);
                lastIndex = position;
                photosForDeletion.add(photoList.get(position));
                    Intent i = new Intent(getApplicationContext(),FullPhotoActivity.class);
                    i.putExtra("projectSite", projectSite);
                    startActivity(i);
            }
        });

        list.setAdapter(adapter);
        if (lastIndex < photoList.size()) {
            list.getLayoutManager().scrollToPosition(lastIndex);
        }
    }

    private void deletePhotos() {
        RequestDTO w = new RequestDTO(RequestDTO.DELETE_SITE_IMAGES);
        w.setPhotoUploadList(photosForDeletion);

        //remove photos from main list
        List<PhotoUploadDTO> tempList = new ArrayList<>();
        for (PhotoUploadDTO p : photoList) {
            if (p.isSelected() == null || p.isSelected() == false) {
                tempList.add(p);
            }
        }
        photoList = tempList;
        setGrid();

        WebSocketUtil.sendRequest(ctx, Statics.COMPANY_ENDPOINT, w, new WebSocketUtil.WebSocketListener() {
            @Override
            public void onMessage(final ResponseDTO response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!ErrorUtil.checkServerError(ctx, response)) {
                            return;
                        }
                        photosForDeletion.clear();
                        Util.showToast(ctx, response.getMessage());
                    }
                });

            }

            @Override
            public void onClose() {

            }

            @Override
            public void onError(String message) {

            }
        });
    }


    private List<PhotoUploadDTO> photosForDeletion = new ArrayList<>();


    private class TouchListener implements RecyclerView.OnItemTouchListener, View.OnTouchListener {

        /**
         * Silently observe and/or take over touch events sent to the RecyclerView
         * before they are handled by either the RecyclerView itself or its child views.
         * <p/>
         * <p>The onInterceptTouchEvent methods of each attached OnItemTouchListener will be run
         * in the order in which each listener was added, before any other touch processing
         * by the RecyclerView itself or child views occurs.</p>
         *
         * @param rv
         * @param e  MotionEvent describing the touch event. All coordinates are in
         *           the RecyclerView's coordinate system.
         * @return true if this OnItemTouchListener wishes to begin intercepting touch events, false
         * to continue with the current behavior and continue observing future events in
         * the gesture.
         */
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            Log.w(LOG, "$$ onInterceptTouchEvent" + e.toString());
            return false;
        }

        /**
         * Process a touch event as part of a gesture that was claimed by returning true from
         * a previous call to {@link #onInterceptTouchEvent}.
         *
         * @param rv
         * @param e  MotionEvent describing the touch event. All coordinates are in
         */
        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            Log.w(LOG, "$$ onTouchEvent, e:  " + e.toString());
        }

        /**
         * Called when a touch event is dispatched to a view. This allows listeners to
         * get a chance to respond before the target view.
         *
         * @param v     The view the touch event has been dispatched to.
         * @param event The MotionEvent object containing full information about
         *              the event.
         * @return True if the listener has consumed the event, false otherwise.
         */
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.w(LOG, "$$ onTouch, e:  " + event.toString());
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_picture_recycler_grid, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_status) {
            Intent i = new Intent(ctx, SiteStatusReportActivity.class);
            i.putExtra("projectSite", projectSite);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    PictureAdapter adapter;

    @Override
    public void onPause() {
        overridePendingTransition(com.boha.monitor.library.R.anim.slide_in_left, com.boha.monitor.library.R.anim.slide_out_right);
        super.onPause();
    }

    static final String LOG = SitePictureGridActivity.class.getSimpleName();
}