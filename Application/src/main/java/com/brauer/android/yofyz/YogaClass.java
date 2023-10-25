package com.brauer.android.yofyz;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.brauer.android.yofyz.db.YogaDbItem;

import java.time.Instant;
import java.util.Date;

/**
 * Base Yoga class object, used throughout the project
 */
public class YogaClass implements Comparable<YogaClass> {

    public static final YogaClass NULL_CLASS = new YogaClass(Date.from(Instant.EPOCH), "Nobody", "Non existing class", 0, 0, false);

    // !!! DATE is the unique identifier -> i.e. assumption is that two classes can't overlap
    private final Date date;
    private final String instructor;
    private final String name;
    private final int free;
    private final int subs;
    private boolean n3w;

    private YogaClass(Date date, String instructor, String name, int free, int subs, boolean n3w) {
        this.date = date;
        this.instructor = instructor;
        this.name = name;
        this.free = free;
        this.subs = subs;
        this.n3w = n3w;
    }

    public boolean isNew() {
        return n3w;
    }

    public long getId() {
        return date.getTime();
    }

    public Date getDate() {
        return date;
    }

    public String getInstructor() {
        return instructor;
    }

    public String getName() {
        return name;
    }

    public int getFree() {
        return free;
    }

    public int getSubs() {
        return subs;
    }

    public void setN3w(boolean n3w) {
        this.n3w = n3w;
    }

    /**
     * @param date the time!
     * @param item Row item from the directly parsed table object
     * @return the sweet sweet yoga class object
     */
    public static YogaClass fromRowItem(Date date, YogaTable.Row.Item item) {
        return new YogaClass(
                date,
                item.getInstructor(),
                item.getName(),
                item.getFreeSlots(),
                item.getSubstitutes(),
                false);
    }

    /**
     * @param dbItem DB object fetched from the ROOM DB
     * @return the sweet sweet yoga class object
     */
    public static YogaClass fromDbItem(YogaDbItem dbItem) {
        return new YogaClass(
                Date.from(Instant.ofEpochMilli(dbItem.getId())), // in DB: id = time
                dbItem.getInstructor(),
                dbItem.getName(),
                dbItem.getFree(),
                dbItem.getSubs(),
                false);
    }

    public YogaDbItem toDbItem() {
        return new YogaDbItem(
                this.getDate().getTime(), // in DB: time = id
                this.getInstructor(),
                this.getName(),
                this.getFree(),
                this.getSubs());
    }

    public boolean hasFreeSlots() {
        return free > 0;
    }



    @Override
    public int compareTo(YogaClass yogaClass) {
        if (this.getDate().equals(yogaClass.getDate())) {
            return 0;
        } else if (this.getDate().after(yogaClass.getDate())) {
            return 1;
        }
        return -1;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * (hash + this.date.toInstant().getNano());
        hash = 31 * (hash * getStringValue(this.name));
        hash = 31 * (hash * getStringValue(this.name));
        return hash;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof YogaClass)) {
            return false;
        }
        YogaClass other = (YogaClass) obj;
        return this.date.equals(other.date)
                && this.instructor.equals(other.instructor)
                && this.name.equals(other.name);
    }

    //======================== FORMATTING ========================//

    private static int getStringValue(String str) {
        int value = 0;
        for (int i = 0; i < str.length(); i++) {
            value = value + str.codePointAt(i);
        }
        return value;
    }

    @NonNull
    @Override
    public String toString() {
        return YogaClassAdapter.DATE_FORMAT.format(date) + ": " + name + "(" + instructor + ") [free:" + free + ", subs:" + subs + "]";
    }

    public String formatDate() {
        return YogaClassAdapter.DATE_FORMAT.format(date);
    }

    public String formatAttendance() {
        if (hasFreeSlots()) {
            return free + "+ free";
        } else if(subs > 0) {
            return subs + " substitutes";
        } else {
            return "full";
        }
    }
}
