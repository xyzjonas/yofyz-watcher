package com.brauer.android.yofyz;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.brauer.android.yofyz.db.Database;
import com.brauer.android.yofyz.options.Options;
import com.brauer.android.yofyz.options.OptionsActivity;
import com.brauer.android.yofyz.service.BackgroundService;

import java.util.Collections;

/**
 * Main Activity for displaying and refreshing the list of yoga classes
 *
 * Network fragment is used to fetch the data in the background
 */
public class MainActivity extends FragmentActivity {




    private RecyclerView recyclerView;
    private YogaClassAdapter mAdapter;
    private LinearLayoutManager layoutManager;
    private SwipeRefreshLayout refreshLayout;

    private BackgroundService mBackgroundService;
    private Intent mServiceIntent;

    private RefreshListener refreshListener;


    // Reference to the TextView showing fetched data, so we can clear it with a button
    // as necessary.
    // private TextView mDataText;

    // Boolean telling us whether a download is in progress, so we don't trigger overlapping
    // downloads with consecutive button clicks.
    public static boolean mDownloading = false;

    private TextView mDataText;

//    public static synchronized void setDebugText(Object obj) {
//        mDataText.setText(obj.toString());
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sample_main);

        mDataText = (TextView) findViewById(R.id.data_text);

        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new YogaClassAdapter();
        recyclerView.setAdapter(mAdapter);

        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation()));

        this.refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        this.refreshListener = new RefreshListener(refreshLayout, mAdapter, mDataText);
        refreshLayout.setOnRefreshListener(refreshListener);

        Database.getInstance(getApplicationContext());

//        mDataText.setText("Items in DB: " + db.yogaClassDao().getAll().size());

        mBackgroundService = new BackgroundService();
        mServiceIntent = new Intent(getApplicationContext(), mBackgroundService.getClass());

        boolean isServiceConfiguredRunning = Options.fromDb(getApplicationContext())
                .isServiceRunning();
        // should be running but isn't
        if (isServiceConfiguredRunning
            && !isMyServiceRunning(mBackgroundService.getClass(), (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE))) {
            Toast.makeText(getApplicationContext(), "Should have been running, damn...", Toast.LENGTH_SHORT).show();
                startForegroundService(mServiceIntent);
        }

        // refresh view
        refreshListener.onRefresh();
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass, ActivityManager manager) {
//        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        stopService(mServiceIntent);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Clear the text and cancel download.
            case R.id.clear_action:
                Database.getInstance(getApplicationContext())
                        .removeAll();
                mAdapter.refreshData(Collections.emptyList());
                Toast.makeText(getApplicationContext(), "Data cleared", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.delete_random:
                Database.getInstance().deleteRandom();
                Database.getInstance().getAll(refreshListener);
                return true;
            case R.id.settings:
                Intent myIntent = new Intent(MainActivity.this, OptionsActivity.class);
//                myIntent.putExtra("key", value); //Optional parameters
                MainActivity.this.startActivity(myIntent);
                return true;
        }
        return false;
    }
}