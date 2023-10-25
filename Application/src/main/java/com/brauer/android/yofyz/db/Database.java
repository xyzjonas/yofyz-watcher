package com.brauer.android.yofyz.db;

import android.content.Context;
import android.os.AsyncTask;

import androidx.room.Room;

import com.brauer.android.yofyz.YogaClass;

import java.util.List;
import java.util.stream.Collectors;

public class Database {

    private static Database instance;

    private AppDatabase database;

    private Database(AppDatabase database) {
        this.database = database;
    }

    public AppDatabase getDatabase() {
        return database;
    }


    //================= ABSTRACTED COMMON METHODS =================//

    public void addOptions(OptionsDbItem... items) {

    }

    public void getOptions(GetOptionsCallback callback) {
        //TODO:
    }

    public void removeAll() {
        new RemoveAll(database).execute();
    }

    public void getAll(GetAllCallback callback) {
        AsyncTask<Void, Integer, List<YogaClass>> task = new GetAll(database, callback)
                .execute();
    }

    public void deleteRandom() {
        new RemoveFirst(database).execute();
    }

    public synchronized static Database getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DATABASE WAS NOT INITIALIZED!");
        }
        return instance;
    }

    public synchronized static Database getInstance(Context context) {
        if (instance == null) {
            instance = new Database(Room.databaseBuilder(context, AppDatabase.class, "yofyz-db")
                    .allowMainThreadQueries() //fixme: NO!
                    .fallbackToDestructiveMigration()
                    .build()
            );
        }
        return instance;
    }

    /**  REMOVE ALL ASYNC  */
    private static class RemoveAll extends AsyncTask<Void, Integer, Void> {

        private final AppDatabase db;

        RemoveAll(AppDatabase db) {
            this.db = db;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            db.yogaClassDao().deleteAll();
            return null;
        }
    }

    /**  REMOVE ONE ASYNC  */
    private static class RemoveFirst extends AsyncTask<Void, Integer, Void> {

        private final AppDatabase db;

        public RemoveFirst(AppDatabase db) {
            this.db = db;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            db.yogaClassDao().getAll().stream()
                    .findFirst()
                    .ifPresent(db.yogaClassDao()::delete);
            return null;
        }
    }

    /**  GET ALL ASYNC  */
    private static class GetAll extends AsyncTask<Void, Integer, List<YogaClass>> {

        private final AppDatabase db;
        private final GetAllCallback callback;

        GetAll(AppDatabase db, GetAllCallback callback) {
            this.db = db;
            this.callback = callback;
        }

        @Override
        protected List<YogaClass> doInBackground(Void... voids) {
            return db.yogaClassDao().getAll().stream()
                    .map(YogaClass::fromDbItem)
                    .collect(Collectors.toList());
        }

        @Override
        protected void onPostExecute(List<YogaClass> yogaClasses) {
            super.onPostExecute(yogaClasses);
            callback.getAllDatabaseCallback(yogaClasses);
        }
    }

    public interface GetAllCallback {
        void getAllDatabaseCallback(List<YogaClass> yogaClasses);
    }

    public interface GetOptionsCallback {
        void getOptionsCallback(List<OptionsDbItem> options);
    }
}
