package com.example.attendant_project.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Stack;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "GPTUserAbout.db";
    private static final int DATABASE_VERSION = 1;
    private static String memoryTitle,memoryDetail;
    private Context context;
    private String temporaryMessage = null;
    private int onCreateResultCode;

    public DatabaseHelper(Context context,String memoryTitle,String memoryDetail){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        this.memoryTitle = memoryTitle;
        this.memoryDetail = memoryDetail;
    }

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        onCreateResultCode = creatNewMemery(memoryTitle,memoryDetail);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 升級資料表時使用，例如加入新欄位
        db.execSQL("DROP TABLE IF EXISTS GPTmemery");
        onCreate(db);
    }

    
    public int creatNewMemery(String memoryTitle,String memoryDetail){//created = 1;un-create = 2;inputProblem = 0;
        SQLiteDatabase db = getWritableDatabase();
        if(memoryTitle != null && !doesTableExist(memoryTitle)) {
            String createTableSQL =
                    "CREATE TABLE " + memoryTitle + " (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "memoryDetail TEXT default " + memoryDetail + ")";
            db.execSQL(createTableSQL);
            db.close();
            return 1;
        } else if (memoryTitle != null && doesTableExist(memoryTitle) && memoryDetail != null) {
            String memoryDetailSearch =
                    "SELECT 1 FROM " + memoryTitle + " WHERE memoryDetail = ? LIMIT 1";
            Cursor cursor = db.rawQuery(memoryDetailSearch,new String[]{memoryDetail});
            if(!cursor.moveToFirst()){
                insertDetail(memoryTitle,memoryDetail);
            }
            db.close();
            return 2;
        }else{
            db.close();
            return  0;
        }

    }

    private void insertDetail(String memoryTitle, String memoryDetail) {
        SQLiteDatabase db = getWritableDatabase();
        if(doesTableExist(memoryTitle)){
            db.insert(memoryTitle, memoryDetail, null);
        }
        db.close();
    }

    private void inerDetail(String memoryTitle, String memoryDetail,String dateTime) {//附帶記憶時間
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        if(doesTableExist(memoryTitle)){
            values.put(memoryDetail, dateTime);
            db.insert(memoryTitle, null, values);
        }
        db.close();
    }

    private boolean doesTableExist(String memoryTitle) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?",
                new String[]{memoryTitle}
        );//統計
        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        cursor.close();
        db.close();
        return exists;
    }

    public Stack<String> getTableName(String memoryTitle){//模糊搜尋
        Stack<String> tableNames = new Stack<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name LIKE ?",
                new String[]{"%" + memoryTitle + "%"});
        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            tableNames.push(cursor.getString(0));
        }
        db.close();
        return  tableNames;
    }

    public Stack<String> checkMemoryDetailDate(String memoryTitle,String memoryDetail){
        SQLiteDatabase db = getReadableDatabase();
        Stack<String> memoryDetails = new Stack<>();
        Cursor cursor = db.rawQuery(
                "SELECT memoryDetail FROM " + memoryTitle + " AND memoryDetail LIKE ?",
                new String[]{"%" + memoryDetail + "%"}
        );
        while (cursor.moveToNext()){
            memoryDetails.push(cursor.getString(0));
        }
        cursor.close();
        db.close();
        return memoryDetails;
    }

    public Stack<String> loadMemoryDetail(String memoryTitle){
        SQLiteDatabase db = getReadableDatabase();
        Stack<String> memoryDetails = new Stack<>();
        Cursor cursor = db.rawQuery("SELECT memoryDetail FROM " + memoryTitle, null);
        while (cursor.moveToNext()){
            memoryDetails.push(cursor.getString(0));
        }
        cursor.close();
        db.close();
        return memoryDetails;
    }


    public boolean deleteMemery(String memoryTitle) {
        try {
        SQLiteDatabase db = getWritableDatabase();
        String deletTable =
                "DROP TABLE " + memoryTitle ;
            db.execSQL(deletTable);
            db.close();
        }catch (SQLException e){
            return false;
        }
        return true;
    }

    public boolean destroyDataBase(){
        boolean deleted = context.deleteDatabase("GPTUserAbout.db");
        return deleted;
    }

    public int getOnCreateResultCode() {
        return onCreateResultCode;
    }


    //    public void updateUser(String memoryTitle , String memoryDetail) {
//        SQLiteDatabase db = getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put("memoryDetail", memoryDetail);
//        db.update(memoryTitle, values);
//        db.close();
//    }

//    public void changeColumn(){//修改欄位
//        SQLiteDatabase db = getWritableDatabase();
//        // 1. 建立新的表（不含職稱欄位）
//        db.execSQL("""
//            CREATE TABLE GPTmemeryNew (
//                id INTEGER PRIMARY KEY,
//                memery TEXT,
//                datetime TEXT
//            );
//        """);
//
//        // 2. 搬資料
//        db.execSQL("""
//            INSERT INTO GPTmemeryNew (id,memery,datetime)
//            SELECT id,memery,datetime FROM GPTmemery;
//        """);
//
//        // 3. 刪除舊表
//        db.execSQL("DROP TABLE GPTmemery;");
//
//        // 4. 重新命名
//        db.execSQL("ALTER TABLE GPTmemeryNew RENAME TO GPTmemery;");
//    }


}
