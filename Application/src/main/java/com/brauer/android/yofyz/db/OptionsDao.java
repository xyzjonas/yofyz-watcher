package com.brauer.android.yofyz.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.Collection;
import java.util.List;

@Dao
public interface OptionsDao {

    @Query("SELECT * from OptionsDbItem")
    List<OptionsDbItem> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(OptionsDbItem... opts);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Collection<OptionsDbItem> opts);

    @Query("DELETE FROM OptionsDbItem")
    void deleteAll();

    @Delete
    void delete(OptionsDbItem optionsDbItem);

}
