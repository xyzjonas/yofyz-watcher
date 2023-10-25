package com.brauer.android.yofyz.options;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum OptionsKey {

    ENABLED("enabled"),
    URL("url"),
    INSTRUCTOR("instructor");


    private static Map<String, OptionsKey> REVERSE_LOOKUP_MAP = Stream.of(OptionsKey.values())
            .collect(Collectors.toMap(
                    opt -> opt.value,
                    opt -> opt
            ));

    private String value;

    OptionsKey(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static OptionsKey fromString(String str) {
        return Optional.ofNullable(REVERSE_LOOKUP_MAP.get(str))
                .orElse(null);
    }
}
