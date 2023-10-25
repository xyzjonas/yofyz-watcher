package com.brauer.android.yofyz.service;

import com.brauer.android.yofyz.db.LogDbItem;

import java.util.Date;
import java.util.Optional;

public class LogItem implements Comparable<LogItem> {

    private long id;
    private LogKey key;
    private String data;


    private LogItem(long id, LogKey key, String data) {
        this.id = id;
        this.key = key;
        this.data = data;
    }

    public LogDbItem toDbItem() {
        return new LogDbItem(id, key.getValue(), data);
    }

    public static LogItem updated() {
        long now = new Date().getTime();
        return new LogItem(now, LogKey.UPDATED, String.valueOf(now));
    }

    public static LogItem fromDbItem(LogDbItem dbItem) {
        LogKey key = Optional.ofNullable(LogKey.fromString(dbItem.getKey()))
                .orElse(null);
        if (key == null) {
            return null;
        }
        return new LogItem(dbItem.getId(), key, dbItem.getData());
    }

    public long getId() {
        return id;
    }

    public LogKey getKey() {
        return key;
    }

    public String getData() {
        return data;
    }

    @Override
    public int compareTo(LogItem o) {
        return Long.compare(this.id, o.id);
    }
}
