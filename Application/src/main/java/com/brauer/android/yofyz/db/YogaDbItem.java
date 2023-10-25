package com.brauer.android.yofyz.db;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class YogaDbItem {

    @PrimaryKey
    // Let's make the id = date.getTime() [millis from epoch]
    private long id;

    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "instructor")
    private String instructor;
    @ColumnInfo(name = "free")
    private int free;
    @ColumnInfo(name = "subs")
    private int subs;
    @ColumnInfo(name = "new")
    private boolean n3w = false;

    public YogaDbItem(long id, String instructor, String name, int free, int subs) {
        this.id = id;
        this.instructor = instructor;
        this.name = name;
        this.free = free;
        this.subs = subs;
    }

    public long getId() {
        return id;
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

    public void setId(long id) {
        this.id = id;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFree(int free) {
        this.free = free;
    }

    public void setSubs(int subs) {
        this.subs = subs;
    }

    public boolean isN3w() {
        return n3w;
    }

    public void setN3w(boolean n3w) {
        this.n3w = n3w;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof YogaDbItem) {
            YogaDbItem other = (YogaDbItem) obj;
            return this.id == other.id &&
                    this.name.equals(other.name) &&
                    this.instructor.equals(other.instructor) &&
                    this.free == other.free &&
                    this.subs == other.subs;
        }
        return false;
    }
}
