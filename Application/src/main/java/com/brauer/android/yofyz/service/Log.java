package com.brauer.android.yofyz.service;

import com.brauer.android.yofyz.db.Database;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Log {

    private List<LogItem> log;

    private Log(List<LogItem> log) {
        this.log = log;
    }

    public static Log fromDb(Database database) {
        return new Log(database.getDatabase()
                .serviceLogDao()
                .getAll()
                .stream()
                .map(LogItem::fromDbItem)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList()));
    }

    public Date getLatestUpdate() {
        String data = log.stream()
                .filter(item -> item.getKey().equals(LogKey.UPDATED))
                .sorted()
                .reduce((a,b) -> b)
                .map(LogItem::getData)
                .orElse(null);
        Date date = Date.from(Instant.EPOCH);
        if (data != null) {
            try {
                date = Date.from(Instant.ofEpochMilli(Long.parseLong(data)));
            } catch (NumberFormatException e) {
                // not needed
            }
        }
        return date;
    }
}
