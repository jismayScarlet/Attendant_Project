package com.example.attendant_project.MemoBuddy.ViewModel;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.widget.EditText;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.attendant_project.MemoBuddy.model.EventItem;

import java.util.List;

//public class EventViewModel extends ViewModel {
//    private final MutableLiveData<List<EventItemModel>> eventList = new MutableLiveData<>(new ArrayList<>());
//
//    public LiveData<List<EventItemModel>> getEventList() {
//        return eventList;
//    }
//
//    public void addEvent() {
//        List<EventItemModel> list = new ArrayList<>(eventList.getValue());
//        list.add(new EventItemModel());
//        eventList.setValue(list);
//    }
//
//    public void updateEvent(int index, String text) {
//        List<EventItemModel> list = new ArrayList<>(eventList.getValue());
//        if (index >= 0 && index < list.size()) {
//            list.get(index).setText(text);
//            eventList.setValue(list);
//        }
//    }
//
//    public void toggleEditable(int index) {
//        List<EventItemModel> list = new ArrayList<>(eventList.getValue());
//        if (index >= 0 && index < list.size()) {
//            EventItemModel item = list.get(index);
//            item.setEditable(!item.isEditable());
//            eventList.setValue(list);
//        }
//    }
//
//    public void removeEvent(int index) {
//        List<EventItemModel> list = new ArrayList<>(eventList.getValue());
//        if (index >= 0 && index < list.size()) {
//            list.remove(index);
//            eventList.setValue(list);
//        }
//    }
//
//
//}
public class EventViewModel extends AndroidViewModel {
    private final EventRepository repo;
    private final LiveData<List<EventItem>> allEvents;
    public EventViewModel(Application app) {
        super(app);
        repo = new EventRepository(app);
        allEvents = repo.getAllEvents();
    }

    public LiveData<List<EventItem>> getAllEvents() {
        return allEvents;
    }



    public void itemConfig(EventItem item,Context context) {
        EditText inputBox = new EditText(context);
        inputBox.setHint("修改內容");

        inputBox.setText(item.getText());
        new AlertDialog.Builder(context)
                .setMessage("修改內容(注意：時間格式請務必依照 yyyy-MM-dd hh:mm 填寫,否則將會無法維持排序正常)")
                .setView(inputBox)
                .setPositiveButton("確定修改內容",(dialog,witch)->{
                    item.setText(inputBox.getText().toString());
                    repo.update(item);
                })
                .setNegativeButton("取消",((dialog, which) -> {
                    dialog.cancel();
                }))
                .setNeutralButton("刪除",((dialog, which) -> {
                    repo.delete(item);
                }))
                .show();
    }

    public void addEvent(EventItem e) {
        repo.insert(e);
    }

    public void deleteEventItem(EventItem e){
        repo.delete(e);
    }
}