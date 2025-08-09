package com.example.attendant_project.MemoBuddy.model;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "event_table")
public class EventItem {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String text;
    private boolean visible;
    private String category;
    private String imageUri;

//     建構子（Room 需要預設建構子）
    public EventItem() {
        this.text = "";
        visible = true;
        category = "common";
        imageUri = null;
    }

    public EventItem(String text, String category) {
        this.text = text;
        visible = true;
        if(TextUtils.isEmpty(category)){
            this.category = "common";
        }else{
            this.category = category;
        }
        imageUri = null;
    }

    public EventItem(String text, String category,Uri image_uri){
        this.text = text;
        visible = true;
        if(TextUtils.isEmpty(category)){
            this.category = "common";
        }else{
            this.category = category;
        }
        imageUri = image_uri.toString();
    }

    // 🔹 Getter / Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {  // Room 會自動設定主鍵值
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(Boolean visible){
        this.visible = visible;
    }

    public void checkVisible(String categorySelected){
        if(!TextUtils.isEmpty(category) && !TextUtils.isEmpty(categorySelected)){
            if (categorySelected.equals("All")) {
                setVisible(true);
            }else if(!category.equals(categorySelected)){
                setVisible(false);
            }
            else{
                setVisible(true);
            }

            Log.d("EventItem","checkVisible " + visible + " " + category + " " + categorySelected + "，content:" + getText());
        }
    }

    public void setCategory(String category){this.category = category;}

    public String getCategory(){return category;}

    public String getImageUri(){
        return imageUri;
        //需要外部進行解析
    }

    public void setImageUri(String image_uri){
        imageUri = image_uri;
        //需要外部進行解析
    }
}

