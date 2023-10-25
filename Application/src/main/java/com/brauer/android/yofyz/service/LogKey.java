package com.brauer.android.yofyz.service;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum LogKey {
    UPDATED("updated");


    private static Map<String, LogKey> REVERSE_LOOKUP_MAP = Stream.of(LogKey.values())
            .collect(Collectors.toMap(
                    opt -> opt.value,
                    opt -> opt
            ));

    private String value;

    LogKey(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static LogKey fromString(String str) {
        return Optional.ofNullable(REVERSE_LOOKUP_MAP.get(str))
                .orElse(null);
    }

}
