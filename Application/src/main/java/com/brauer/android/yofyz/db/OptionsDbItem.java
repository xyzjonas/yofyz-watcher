package com.brauer.android.yofyz.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class OptionsDbItem {

    @NonNull
    @PrimaryKey
    private String key;
    @ColumnInfo(name = "value")
    private String value;

    public OptionsDbItem(@NonNull String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @NonNull
    @Override
    public String toString() {
        return key + ": " + value;
    }
}

