package com.brauer.android.yofyz.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class LogDbItem {

    @PrimaryKey
    private long id;

    @ColumnInfo(name = "key")
    private String key;

    @ColumnInfo(name = "data")
    private String data;

    public LogDbItem(long id, String key, String data) {
        this.id = id;
        this.key = key;
        this.data = data;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getData() {
        return data;
    }

    public void setData(String meta) {
        this.data = meta;
    }
}
