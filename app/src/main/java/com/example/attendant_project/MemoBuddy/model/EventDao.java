package com.example.attendant_project.MemoBuddy.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(EventItem event);

    @Update
    void update(EventItem event);

    @Delete
    void delete(EventItem event);

    @Query("SELECT * FROM event_table")
    LiveData<List<EventItem>> getAllEvents();
}