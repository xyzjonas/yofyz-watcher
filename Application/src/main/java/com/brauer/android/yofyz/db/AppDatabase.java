package com.brauer.android.yofyz.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {YogaDbItem.class, LogDbItem.class, OptionsDbItem.class}, version = 7, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract YogaDao yogaClassDao();

    public abstract LogDao serviceLogDao();

    public abstract OptionsDao optionsItemDao();

}
