package com.brauer.android.yofyz.options;

import android.content.Context;
import android.widget.Toast;

import com.brauer.android.yofyz.db.AppDatabase;
import com.brauer.android.yofyz.db.Database;
import com.brauer.android.yofyz.db.OptionsDbItem;
import com.brauer.android.yofyz.db.YogaDbItem;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Options {

    public static final String NO_FILTER = "NO FILTER";

    private final Map<OptionsKey, Option> options;
    private final AppDatabase databaseRef;

    private Options(Map<OptionsKey, Option> options, AppDatabase databaseRef) {
        this.options = options;
        this.databaseRef = databaseRef;
    }

    public boolean isServiceRunning() {
        return Optional.ofNullable(options.get(OptionsKey.ENABLED))
                .map(Option::getValue)
                .map(Boolean::valueOf)
                .orElse(false);
    }

    public Option getOption(OptionsKey key) {
        return Optional.ofNullable(options.get(key))
                .orElse(null);
    }

    public Map<OptionsKey, Option> getOptions() {
        return options;
    }

    public void putOption(OptionsKey key, Option value) {
        if (value.getValue().equals(NO_FILTER)) {
            options.remove(key);
        } else {
            options.put(key, value);
        }
    }

    public void writeDown() {
        databaseRef.optionsItemDao().deleteAll();
        databaseRef.optionsItemDao().insertAll(
                options.entrySet().stream()
                        .map(entry -> new OptionsDbItem(entry.getKey().getValue(), entry.getValue().getValue()))
                        .collect(Collectors.toList())
        );
    }

    public static Options fromDb(Context context) {

        AppDatabase databaseInstance = Database.getInstance(context).getDatabase();

        List<OptionsDbItem> dbItems = databaseInstance
                .optionsItemDao()
                .getAll();

        Map<OptionsKey, Option> options = new EnumMap<>(OptionsKey.class);

        for(OptionsDbItem item : dbItems) {
            OptionsKey key = OptionsKey.fromString(item.getKey());

            if (key == OptionsKey.INSTRUCTOR) {
                    Set<String> allInstructorsInDb = databaseInstance.yogaClassDao()
                            .getAll()
                            .stream()
                            .map(YogaDbItem::getInstructor)
                            .collect(Collectors.toSet());
                    options.put(OptionsKey.INSTRUCTOR, new Instructor(item.getValue(), allInstructorsInDb));
            } else if (key == OptionsKey.ENABLED) {
                options.put(OptionsKey.ENABLED, new ServiceRunning(Boolean.parseBoolean(item.getValue())));
            }
        }
        return new Options(options, databaseInstance);
    }

    public static class ServiceRunning implements Option {

        private boolean isRunning;

        public ServiceRunning(boolean isRunning) {
            this.isRunning = isRunning;
        }

        @Override
        public String getValue() {
            return String.valueOf(isRunning);
        }

        @Override
        public Set<String> getPossibilities() {
            return new HashSet<>(Arrays.asList("true", "false"));
        }
    }

    public static class Instructor implements Option {

        private String name;
        private Set<String> possibleInstructors;

        public Instructor(String name, Collection<? extends String> possibleInstructors) {
            this.name = name;
            this.possibleInstructors = new HashSet<>(possibleInstructors);
        }

        @Override
        public String getValue() {
            return name;
        }

        @Override
        public Set<String> getPossibilities() {
            return possibleInstructors;
        }
    }



    public interface Option {

        String getValue();

        Set<String> getPossibilities();

    }
}
