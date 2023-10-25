package com.brauer.android.yofyz;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brauer.android.yofyz.db.AppDatabase;
import com.brauer.android.yofyz.db.Database;
import com.brauer.android.yofyz.service.BackgroundService;
import com.brauer.android.yofyz.service.Log;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Bind the refresh actions of the swipe refresh layout
 */
public class RefreshListener implements SwipeRefreshLayout.OnRefreshListener, DownloadCallback, Database.GetAllCallback, DownloadFinishedCallback {

    private final SwipeRefreshLayout layout;
    private final YogaClassAdapter yogaClassAdapter;
    private TextView debugView;

    public RefreshListener(SwipeRefreshLayout layout, YogaClassAdapter yogaClassAdapter, TextView debugView) {
        this.layout = layout;
        this.yogaClassAdapter = yogaClassAdapter;
        this.debugView = debugView;
    }

    /**
     * On swipe:
     *  - start background download & parse task
     *  - register onFinish()
     *  - when finished: refresh view from DB and stop animation
     */
    @Override
    public void onRefresh() {
        layout.setRefreshing(true);
        new BackgroundService.DownloadTask(layout.getContext(), this)
                .execute(BackgroundService.URL);
    }


    //TODO: redo... YogaClasses should equal if all fields are the same..
    //TODO: mark the new as "NEW"
    @Override
    public void updateFromDownload(List<YogaClass> result, List<YogaClass> newOnly) {
        Collections.sort(result, Comparator.reverseOrder());

        yogaClassAdapter.refreshData(result);
        yogaClassAdapter.addData(newOnly);

        String message = "Last Updated: " + getLatest();
        debugView.setText(message);

        layout.setRefreshing(false);
    }


    private Date getLatest() {
        return Log.fromDb(Database.getInstance(layout.getContext()))
                .getLatestUpdate();
    }

    @Override
    public void getAllDatabaseCallback(List<YogaClass> yogaClasses) {
        updateFromDownload(yogaClasses, Collections.emptyList());
    }

    @Override
    public void finished() {
        new RefreshAllFromDb(Database.getInstance(layout.getContext()).getDatabase(), this)
                .execute();
    }

    private static class RefreshAllFromDb extends AsyncTask<Void, Integer, List<YogaClass>> {

        private final AppDatabase db;
        private final DownloadCallback callback;

        RefreshAllFromDb(AppDatabase db, DownloadCallback callback) {
            this.db = db;
            this.callback = callback;
        }

        @Override
        protected List<YogaClass> doInBackground(Void... voids) {
            return db.yogaClassDao()
                    .getAll()
                    .stream()
                    .map(YogaClass::fromDbItem)
                    .collect(Collectors.toList());
        }

        @Override
        protected void onPostExecute(List<YogaClass> yogaClasses) {
            super.onPostExecute(yogaClasses);



            callback.updateFromDownload(yogaClasses, Collections.emptyList());
        }
    }
}
