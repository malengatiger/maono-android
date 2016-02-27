package com.boha.monitor.library.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.boha.monitor.library.activities.MonApp;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.util.OKHttpException;
import com.boha.monitor.library.util.OKUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.library.util.WebCheck;

import java.util.Date;

/**
 * This IntentService manages the data refresh
 */
public class DataRefreshService extends IntentService {

    public DataRefreshService() {
        super("DataRefreshService");
    }
    static final String LOG = DataRefreshService.class.getSimpleName();
    public static final String DATA_REFRESHED = "dataRefreshed";
    public static final String BROADCAST_ACTION =
            "com.boha.monitor.DATA.REFRESHED";
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.w(LOG,"$$$$$$$$$$$$$ onHandleIntent, DataRefreshService starting: " + new Date().toString());
        if (WebCheck.checkNetworkAvailability(getApplicationContext()).isNetworkUnavailable()) {
            Log.e(LOG,"No network, quitting the DataRefreshService");
            return;
        }
        refreshData();
    }

    private void refreshData() {
        Log.d(LOG,"....starting data refresh from server ...........");
        final long start = System.currentTimeMillis();
        OKUtil okUtil = new OKUtil();
        RequestDTO w = new RequestDTO();

        StaffDTO staff = SharedUtil.getCompanyStaff(getApplicationContext());
        MonitorDTO monitor = SharedUtil.getMonitor(getApplicationContext());

        if (staff != null) {
            w.setRequestType(RequestDTO.GET_STAFF_DATA);
            w.setStaffID(staff.getStaffID());
        }
        if (monitor != null) {
            w.setRequestType(RequestDTO.GET_MONITOR_PROJECTS);
            w.setMonitorID(monitor.getMonitorID());
        }
        try {
            Log.d(LOG,".....sending refresh request: " + w.getRequestType());
            okUtil.sendGETRequest(getApplicationContext(), w, new OKUtil.OKListener() {
                @Override
                public void onResponse(ResponseDTO response) {
                    if (response.getStatusCode() == 0) {
                        Util.cacheOnSnappy((MonApp) getApplication(),
                                response, new Util.SnappyListener() {
                            @Override
                            public void onCachingComplete() {

                                long end = System.currentTimeMillis();
                                Log.w(LOG, "@@@@ DataRefreshService complete. Database duly refreshed: " +
                                        " elapsed ms: " + (end - start));
                                Intent m = new Intent(BROADCAST_ACTION);
                                m.putExtra(DATA_REFRESHED,true);
                                LocalBroadcastManager.getInstance(getApplicationContext())
                                        .sendBroadcast(m);
                            }

                            @Override
                            public void onError(String message) {
                                Log.e(LOG, message);
                            }
                        });
                    } else {
                        onError(response.getMessage());
                    }
                }

                @Override
                public void onError(String message) {
                    Log.e(LOG,"Data refresh failed: " + message);
                }
            });

        } catch (OKHttpException e) {
            e.printStackTrace();
        }
    }
}
