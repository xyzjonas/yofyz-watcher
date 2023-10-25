package com.brauer.android.yofyz.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LogDao {

    @Query("SELECT * from LogDbItem")
    List<LogDbItem> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(LogDbItem... users);

    @Query("DELETE FROM LogDbItem")
    void deleteAll();

    @Delete
    void delete(LogDbItem user);

}
