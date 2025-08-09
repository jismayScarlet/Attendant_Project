package com.example.attendant_project.MemoBuddy.ViewModel;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.attendant_project.MemoBuddy.model.AppDatabase;
import com.example.attendant_project.MemoBuddy.model.EventDao;
import com.example.attendant_project.MemoBuddy.model.EventItem;

import java.util.List;

public class EventRepository {
    private final EventDao dao;
    private final LiveData<List<EventItem>> allEvents;

    public EventRepository(Application app) {
        AppDatabase db = AppDatabase.getInstance(app);
        dao = db.eventDao();
        allEvents = dao.getAllEvents();
    }

    public LiveData<List<EventItem>> getAllEvents() {
        return allEvents;
    }

    public void insert(EventItem e) {
        AppDatabase.databaseWriteExecutor.execute(() -> dao.insert(e));
    }

    public void update(EventItem e){
        AppDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                dao.update(e);
            }
        });
    }

    public void delete(EventItem e){
        AppDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                dao.delete(e);
            }
        });
    }
}
