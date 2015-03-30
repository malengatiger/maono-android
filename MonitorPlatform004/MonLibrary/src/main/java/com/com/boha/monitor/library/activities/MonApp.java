package com.com.boha.monitor.library.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.boha.monitor.library.R;
import com.com.boha.monitor.library.dto.CompanyStaffDTO;
import com.com.boha.monitor.library.services.LocationTrackerReceiver;
import com.com.boha.monitor.library.toolbox.BitmapLruCache;
import com.com.boha.monitor.library.util.SharedUtil;
import com.com.boha.monitor.library.util.Statics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.L;
import com.nostra13.universalimageloader.utils.StorageUtils;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;

import java.io.File;
import java.util.HashMap;

/**
 * Created by aubreyM on 2014/05/17.
 * Copyright (c) 2014 Aubrey Malabie. All rights reserved.
 */


@ReportsCrashes(
        formKey = "",
        formUri = Statics.CRASH_REPORTS_URL,
        customReportContent = {
                ReportField.APP_VERSION_NAME,
                ReportField.APP_VERSION_CODE,
                ReportField.ANDROID_VERSION,
                ReportField.PHONE_MODEL,
                ReportField.BRAND,
                ReportField.STACK_TRACE,
                ReportField.PACKAGE_NAME,
                ReportField.CUSTOM_DATA,
                ReportField.LOGCAT},
        socketTimeout = 10000
)
public class MonApp extends Application implements Application.ActivityLifecycleCallbacks {


    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
     * storing them all in Application object helps ensure that they are created only once per
     * application instance.
     */
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    static final String PROPERTY_ID = "UA-53661372-2";
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private  boolean chatActivityVisible;

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = null;
            if (trackerId == TrackerName.APP_TRACKER) {
                t = analytics.newTracker(PROPERTY_ID);
            }
            if (trackerId == TrackerName.GLOBAL_TRACKER) {
                t = analytics.newTracker(R.xml.global_tracker);
            }
            mTrackers.put(trackerId, t);
        }
        Log.i(LOG, "## analytics tracker ID: " + trackerId.toString());
        return mTrackers.get(trackerId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n\n#######################################\n");
        sb.append("#######################################\n");
        sb.append("###\n");
        sb.append("###  Monitor App has started\n");
        sb.append("###\n");
        sb.append("#######################################\n\n");

        Log.d(LOG, sb.toString());
        registerActivityLifecycleCallbacks(this);
        boolean isDebuggable = 0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE);
        if (!isDebuggable) {
            ACRA.init(this);
            CompanyStaffDTO staff = SharedUtil.getCompanyStaff(getApplicationContext());
            if (staff != null) {
                ACRA.getErrorReporter().putCustomData("companyStaffID", "" + staff.getCompanyStaffID());
            }

            Log.e(LOG, "###### ACRA Crash Reporting has been initiated");
        } else {
            Log.d(LOG, "###### ACRA Crash Reporting has NOT been initiated, in DEBUG mode");
        }
        initializeVolley(getApplicationContext());

        DisplayImageOptions defaultOptions =
                new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .cacheOnDisk(true)
                        .showImageOnFail(getApplicationContext().getResources().getDrawable(R.drawable.under_construction))
                        .showImageOnLoading(getApplicationContext().getResources().getDrawable(R.drawable.under_construction))
                        .build();

        File cacheDir = StorageUtils.getCacheDirectory(this, true);
        Log.d(LOG, "## onCreate, ImageLoader cacheDir, files: " + cacheDir.listFiles().length);
        //
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .denyCacheImageMultipleSizesInMemory()
                .diskCache(new UnlimitedDiscCache(cacheDir))
                .memoryCache(new LruMemoryCache(16 * 1024 * 1024))
                .defaultDisplayImageOptions(defaultOptions)
                .build();

        ImageLoader.getInstance().init(config);
        L.writeDebugLogs(false);
        L.writeLogs(false);

        Log.w(LOG, "###### ImageLoaderConfiguration has been initialised");
        startLocationAlarm();

    }

    public void startLocationAlarm() {
        alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intentx = new Intent(getApplicationContext(), LocationTrackerReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intentx, 0);

        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime(), HOUR, alarmIntent);


        Log.e(LOG, "###### AlarmManager: alarm set: ONE_MINUTE");
    }

    static final int
            ONE_MINUTE = 60 * 1000,
            FIVE_MINUTES = ONE_MINUTE * 5,
            FIFTEEN_MINUTES = FIVE_MINUTES * 3,
            HALF_HOUR = FIFTEEN_MINUTES * 2,
            HOUR = HALF_HOUR * 2;

    /**
     * Set up Volley Networking; create RequestQueue and ImageLoader
     *
     * @param context
     */
    public void initializeVolley(Context context) {
        Log.e(LOG, "initializing Volley Networking ...");
        requestQueue = Volley.newRequestQueue(context);
        int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                .getMemoryClass();

        // Use 1/8th of the available memory for this memory cache.
        int cacheSize = 1024 * 1024 * memClass / 8;
        bitmapLruCache = new BitmapLruCache(cacheSize);
        // imageLoader = new ImageLoader(requestQueue, bitmapLruCache);
        Log.i(LOG, "********** Yebo! Volley Networking has been initialized, cache size: " + (cacheSize / 1024) + " KB");

        // Create global configuration and initialize ImageLoader with this configuration
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .build();

        ImageLoader.getInstance().init(config);
    }


    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public BitmapLruCache getBitmapLruCache() {
        return bitmapLruCache;
    }

    RequestQueue requestQueue;
    BitmapLruCache bitmapLruCache;
    ChatActivity chatActivity;
    static final String LOG = "MonApp";

    public ChatActivity getChatActivity() {
        return chatActivity;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (activity instanceof ChatActivity) {
            chatActivity = (ChatActivity)activity;
            chatActivityVisible = true;
            Log.d(LOG,"ChatActivity onActivityCreated, chatActivityVisible: " + chatActivityVisible);
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (activity instanceof ChatActivity) {
            chatActivityVisible = true;
            chatActivity = (ChatActivity)activity;
            Log.d(LOG,"ChatActivity onActivityStarted, chatActivityVisible: " + chatActivityVisible);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity instanceof ChatActivity) {
            chatActivityVisible = true;
            chatActivity = (ChatActivity)activity;
            Log.d(LOG,"ChatActivity onActivityResumed, chatActivityVisible: " + chatActivityVisible);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (activity instanceof ChatActivity) {
            chatActivityVisible = false;
            Log.d(LOG,"ChatActivity onActivityPaused, chatActivityVisible: " + chatActivityVisible);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (activity instanceof ChatActivity) {
            chatActivityVisible = false;
            Log.d(LOG,"ChatActivity onActivityStopped, chatActivityVisible: " + chatActivityVisible);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (activity instanceof ChatActivity) {
            chatActivityVisible = false;
            Log.d(LOG,"ChatActivity onActivityDestroyed, chatActivityVisible: " + chatActivityVisible);
        }
    }

    public boolean isChatActivityVisible() {
        return chatActivityVisible;
    }
}

