package com.brauer.android.yofyz.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.brauer.android.yofyz.DownloadFinishedCallback;
import com.brauer.android.yofyz.R;
import com.brauer.android.yofyz.YogaClass;
import com.brauer.android.yofyz.YogaTable;
import com.brauer.android.yofyz.db.AppDatabase;
import com.brauer.android.yofyz.db.Database;
import com.brauer.android.yofyz.db.LogDbItem;
import com.brauer.android.yofyz.db.YogaDbItem;
import com.brauer.android.yofyz.options.Options;
import com.brauer.android.yofyz.options.OptionsKey;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BackgroundService extends Service {

    public static final String URL = "https://yofyz.diva2.cz/";

    public static final String UPDATES_NOTIFICATION_CHANNEL_ID = "yofyz_updates_channel";
    public static final String FOREGROUND_INFO_NOTIFICATION_CHANNEL_ID = "yofyz_foreground_channel";

    private ScheduledExecutorService executorService;


    public BackgroundService() {
        executorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent(this, ServiceRestartBroadcastReceiver.class);

        sendBroadcast(broadcastIntent);
//        Toast.makeText(getApplicationContext(), "Service destroyed", Toast.LENGTH_SHORT).show();

        executorService.shutdown();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Toast.makeText(getApplicationContext(), "Service started", Toast.LENGTH_SHORT).show();

        createForegroundServiceIsRunningNotificationChannel();
        createUpdateNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, FOREGROUND_INFO_NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Yofyz runs in the background")
                .setContentText("Just lets you know that yofyz is still running in the background, feel free to disable this notification channel.").build();

        // TODO: move url, period to settings
        executorService.scheduleAtFixedRate(() -> new DownloadTask(getApplicationContext()).execute(URL), 30, 60, TimeUnit.SECONDS);

        startForeground(1, notification);
        return super.onStartCommand(intent, flags, startId);
    }


    public static class DownloadTask extends AsyncTask<String, Integer, Result> {

        AppDatabase db;
        Context context;  // TODO: fix leak (?)
        DownloadFinishedCallback callback;


        public DownloadTask(Context context) {
            super();
            this.context = context;
        }

        public DownloadTask(Context context, DownloadFinishedCallback callback) {
            super();
            this.context = context;
            this.callback = callback;
        }

        /**
         * We want all DB R/W operations to happen here...
         * ...but that's ultimately not the case.. sadly ...*TODO
         */
        @Override
        protected Result doInBackground(String... urls) {
            if (Looper.myLooper() == null) {
                Looper.prepare();
            }

            if (db == null) {
                db = Database.getInstance(context).getDatabase();
            }

//            Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show();
            Result result = null;
            if (!isCancelled() && urls != null && urls.length > 0) {
                String urlString = urls[0];
                try {
                    URL url = new URL(urlString);
                    List<YogaClass> resultList = downloadUrl(url);
                    if (resultList != null && !resultList.isEmpty()) {
                        result = new Result(resultList);
                        if (result.mResultValue != null) {
                            // FIXME: very, very ugly...
                            // this is it... everything went smoothly and we have parsed YogaClasses list
                            List<YogaDbItem> diffs = diffNewOnes(result.mResultValue);
                            // write to DB and pass on
                            // delete non existing items
                            db.yogaClassDao().delete(diffOldOnes(result.mResultValue));
                            // update ALL fetched items
                            db.yogaClassDao().insertAll(result.mResultValue.stream().map(YogaClass::toDbItem).collect(Collectors.toList()));

                            // pass-on only NEW items (--> to trigger notifications)
                            result.mResultValue = diffs.stream().map(YogaClass::fromDbItem).collect(Collectors.toList());
                            // add 'updated' item to log
                            db.serviceLogDao().insertAll(LogItem.updated().toDbItem());
                            return result;
                        }
                    } else {
                        throw new IOException("No response received.");
                    }
                } catch (Exception e) {
                    result = new Result(e);
                }
            }
            return result;
        }

        /**
         * Given a URL, sets up a connection and gets the HTTP response body from the server.
         * If the network request is successful, it returns the response body in String form. Otherwise,
         * it will throw an IOException.
         */
        private List<YogaClass> downloadUrl(URL url) throws IOException {

            Document document = Jsoup.connect(url.toString()).get();
            return document.getElementsByTag(YogaTable.TABLE_TAG).stream()
//                    .filter(e -> !e.getElementsByClass(YogaTable.YOGA_TABLE_CSS_CLASS).isEmpty())
                    .filter(e -> e.getElementById("main-rozvrh") != null)
                    .map(YogaTable::fromTable)
                    .findFirst()
                    .map(YogaTable::getYogaItems)
                    .orElse(new ArrayList<>());
        }

        /**  Spawns notifications  */
        @Override
        protected void onPostExecute(Result result) {
            // result is ALREADY a diffNewOnes!
            if (result.mResultValue != null) {
                if (!result.mResultValue.isEmpty()) {
                    handleNotificationsIncaseOfSuccess(result.mResultValue);
                }

            } else if (result.mException != null) {
                    Toast.makeText(
                            context,
                            "Async task failed: " + result.mException.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
            } else {
                Toast.makeText(
                        context,
                        "Async failed without any exceptions.. hmm.. weird..",
                        Toast.LENGTH_LONG
                ).show();

            }
            if (callback != null) {
                callback.finished();
            }
        }


        // results is already a diff (i.e.: NEW only)
        private void handleNotificationsIncaseOfSuccess(List<YogaClass> results) {

            // Handle filter
            String instructor = Optional.ofNullable(Options.fromDb(context)
                    .getOption(OptionsKey.INSTRUCTOR))
                    .map(Options.Option::getValue)
                    .orElse(Options.NO_FILTER);

            List<YogaClass> filtered = results.stream()
                    .filter(yoga -> yoga.getInstructor().equals(instructor))
                    .collect(Collectors.toList());

            if (instructor.equals(Options.NO_FILTER)) {
                filtered =  results;
            }

            if(filtered.isEmpty()) {
                return;
            }


            // Handle notifications
            if (filtered.size() > 2) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, UPDATES_NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentTitle(filtered.size() + " new yoga classes!");
                NotificationManagerCompat.from(context)
                        .notify(new Random().nextInt(), builder.build());

            } else { //make individual notifications if less than 2
                for(YogaClass yogaClass : filtered) {
                    String shortenedName;
                    if (yogaClass.getName().length() >= 22) {
                        shortenedName = yogaClass.getName().substring(0, 22);
                    } else {
                        shortenedName = yogaClass.getName();
                    }
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, UPDATES_NOTIFICATION_CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setBadgeIconType(5)
                            .setContentTitle(yogaClass.formatDate() + " - " + yogaClass.getInstructor())
                            .setContentText(
                                    shortenedName + "... [" + yogaClass.formatAttendance() + "]"
                            );
                    NotificationManagerCompat.from(context)
                            .notify(new Random().nextInt(), builder.build());
                }
            }
        }

        /**
         * Override to add special behavior for cancelled AsyncTask.
         */
        @Override
        protected void onCancelled(Result result) {
            // We'll see outdated 'last updated' field to know sth is wrong
//            Toast.makeText(context, "Async task cancelled", Toast.LENGTH_SHORT).show();
        }

        /**
         * To be appended and marked 'new'
         * Run outside of the main thread!
         */
        private List<YogaDbItem> diffNewOnes(List<YogaClass> downloaded) {
            Set<YogaClass> currentDb = db.yogaClassDao().getAll().stream()
                    .map(YogaClass::fromDbItem)
                    .collect(Collectors.toSet());

            Set<YogaClass> temp = new HashSet<>(downloaded);
            temp.removeAll(currentDb);
            //TODO: 'new' label not implemented
            temp.forEach(item -> item.setN3w(true));
            return temp.stream()
                    .map(YogaClass::toDbItem)
                    .collect(Collectors.toList());
        }


        /**
         * To be removed
         * Run outside of the main thread!
         */
        private List<YogaDbItem> diffOldOnes(List<YogaClass> downloaded) {
            Set<YogaClass> currentDb = db.yogaClassDao().getAll().stream()
                    .map(YogaClass::fromDbItem)
                    .collect(Collectors.toSet());

            currentDb.removeAll(downloaded);
            return currentDb.stream()
                    .map(YogaClass::toDbItem)
                    .collect(Collectors.toList());
        }
    }

    static class Result {

        List<YogaClass> mResultValue;
        Exception mException;

        Result(List<YogaClass> resultValue) {
            mResultValue = resultValue;
        }

        Result(Exception exception) {
            mException = exception;
        }
    }

    //==================== CREATE CHANNELS  ====================//

    private void createForegroundServiceIsRunningNotificationChannel() {
        String channelName = getString(R.string.foreground_info_channel_name);
        String channelDescription = getString(R.string.foreground_info_channel_description);
        NotificationChannel channel = new NotificationChannel(
                FOREGROUND_INFO_NOTIFICATION_CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(channelDescription);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
    }

    private void createUpdateNotificationChannel() {

        String name = getString(R.string.updates_channel_name);
        String description = getString(R.string.updates_channel_description);

        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(UPDATES_NOTIFICATION_CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

}
