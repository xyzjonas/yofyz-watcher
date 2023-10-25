package com.brauer.android.yofyz.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface YogaDao {

    @Query("SELECT * from yogadbitem")
    List<YogaDbItem> getAll();


    @Query("SELECT * FROM yogadbitem WHERE id IN (:ids)")
    List<YogaDbItem> loadAllByIds(int[] ids);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(YogaDbItem... users);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<YogaDbItem> users);

    @Query("DELETE FROM YogaDbItem")
    void deleteAll();

    @Delete
    void delete(YogaDbItem user);

    @Delete
    void delete(YogaDbItem ... users);

    @Delete
    void delete(List<YogaDbItem> users);

}
