package com.example.attendant_project.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
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
        onCreateResultCode = creatNewMemory(db,memoryTitle,memoryDetail);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 升級資料表時使用，例如加入新欄位
        db.execSQL("DROP TABLE IF EXISTS GPTmemery");
        onCreate(db);
    }

    
//    public int creatNewMemery(SQLiteDatabase db,String memoryTitle,String memoryDetail){//created = 1;un-create = 2;inputProblem = 0;
//        if(memoryTitle != null && !doesTableExist(memoryTitle)) {
//            String createTableSQL =
//                    "CREATE TABLE " + memoryTitle + " (" +
//                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
//                            "memoryDetail TEXT default " + memoryDetail + ")";
//            db.execSQL(createTableSQL);
//            return 1;
//        } else if (memoryTitle != null && doesTableExist(memoryTitle)) {
//            String memoryDetailSearch =
//                    "SELECT 1 FROM " + memoryTitle + " WHERE memoryDetail = ? LIMIT 1";
//            Cursor cursor = db.rawQuery(memoryDetailSearch,new String[]{memoryDetail});
//            if(!cursor.moveToFirst()){
//                insertDetail(memoryTitle,memoryDetail);
//            }
//            return 2;
//        }else{
//            return  0;
//        }
//
//    }

    public int creatNewMemory(SQLiteDatabase db, String memoryTitle, String memoryDetail) {//created = 1;un-create = 2;inputProblem = 0;
        String safeTitle = "\"" + memoryTitle.replace("\"", "") + "\"";
        String safeDetail = "\"" + memoryDetail.replace("\"", "") + "\"";
        Log.i("database function","createNewmemory");
        if (memoryTitle != null && !doesTableExist(memoryTitle)) {
            String createTableSQL =
                    "CREATE TABLE " + safeTitle + " (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "memoryDetail TEXT DEFAULT '" + safeDetail + "')";
            db.execSQL(createTableSQL);
            onCreateResultCode = 1;
            return 1;
        } else if (memoryTitle != null && doesTableExist(memoryTitle)) {
            String memoryDetailSearch =
                    "SELECT 1 FROM " + safeTitle + " WHERE memoryDetail = ? LIMIT 1";
            Cursor cursor = db.rawQuery(memoryDetailSearch, new String[]{memoryDetail});
            if (!cursor.moveToFirst()) {
                insertDetail(memoryTitle, memoryDetail);
            }
            onCreateResultCode = 2;
            return 2;
        } else {
            onCreateResultCode = 0;
            return 0;
        }
    }

    private void insertDetail(String memoryTitle, String memoryDetail) {
        Log.i("database function","insertDetail");
        String safeDetail = "\"" + memoryDetail.replace("\"", "") + "\"";
        String safeTitle = "\"" + memoryTitle.replace("\"", "") + "\"";
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(safeDetail, "");
        if(doesTableExist(memoryTitle)){
            db.insert(safeTitle, null,values);
        }
    }

    private void inerDetail(String memoryTitle, String memoryDetail,String dateTime) {//附帶記憶時間
        String safeDetail = "\"" + memoryDetail.replace("\"", "") + "\"";
        String safeTitle = "\"" + memoryTitle.replace("\"", "") + "\"";
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        if(doesTableExist(memoryTitle)){
            values.put(safeDetail, dateTime);
            db.insert(safeTitle, null, values);
        }
        
    }

    private boolean doesTableExist(String memoryTitle) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?",
                new String[]{memoryTitle}
        );//統計
        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        cursor.close();
        return exists;
    }

    public Stack<String> getTableName(String memoryTitle){//模糊搜尋
        Log.i("database function","getTableName：" + memoryTitle);
        SQLiteDatabase db = getWritableDatabase();
        Stack<String> tableNames = new Stack<>();
        Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name LIKE ? COLLATE NOCASE",
                new String[]{"%" + memoryTitle + "%"});
        if (cursor.moveToFirst()) {
            do {
                tableNames.push(cursor.getString(0));
                Log.i("database function", "table name = " + cursor.getString(0));
            } while (cursor.moveToNext());
        }

        cursor = db.rawQuery(//搜索全表單 測試
                "SELECT name FROM sqlite_master WHERE type='table'",
                null);
        if(cursor.moveToFirst()) {
            do {
                Log.i("database function", "QueryTest table name = " + cursor.getString(0));
            }while (cursor.moveToNext());
        }
        cursor.close();
        return  tableNames;
    }

    public Stack<String> checkMemoryDetailDate(String memoryTitle,String memoryDetail){
        String safeDetail = "\"" + memoryDetail.replace("\"", "") + "\"";
        String safeTitle = "\"" + memoryTitle.replace("\"", "") + "\"";
        SQLiteDatabase db = getWritableDatabase();
        Stack<String> memoryDetails = new Stack<>();
        Cursor cursor = db.rawQuery(
                "SELECT " + safeDetail + " FROM " + safeTitle + " AND memoryDetail LIKE ?",
                new String[]{"%" + memoryDetail + "%"}
        );
        while (cursor.moveToNext()){
            memoryDetails.push(cursor.getString(0));
        }
        cursor.close();
        return memoryDetails;
    }

    public Stack<String> loadMemoryDetail(String memoryTitle){//可能會異常
        Log.i("database function","loadMemoryDetail");
        String safeDetail = "\"" + memoryDetail.replace("\"", "") + "\"";
        String safeTitle = "\"" + memoryTitle.replace("\"", "") + "\"";
        SQLiteDatabase db = getWritableDatabase();
        Stack<String> memoryDetails = new Stack<>();
        Cursor cursor = db.rawQuery("SELECT " + safeDetail + " FROM " + safeTitle, null);
        while (cursor.moveToNext()){
            memoryDetails.push(cursor.getString(0));
        }
        cursor.close();
        return memoryDetails;
    }


    public boolean deleteMemory(String memoryTitle) {
        Log.i("database function","deleteMemory");
        String safeTitle = "\"" + memoryTitle.replace("\"", "") + "\"";
        SQLiteDatabase db = getWritableDatabase();
        try {
        String deletTable =
                "DROP TABLE " + safeTitle ;
            db.execSQL(deletTable);
            
        }catch (SQLException e){
            return false;
        }
        return true;
    }

    public boolean destroyDataBase(Context context){
        Log.i("database function","destroyDatabase");
        boolean deleted = context.deleteDatabase("GPTUserAbout.db");//包裝過的database.deleteDatabase方法，直接幫你抓好資料庫路徑以及並執行刪除
        return deleted;
    }

    public boolean doesDatabaseExist(Context context, String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.exists();
    }

    public boolean doesDatabaseExist(Context context) {
        Log.i("database function","doesDatabaseExist");
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        return dbFile.exists();
    }

    public int getOnCreateResultCode() {
        return onCreateResultCode;
    }

    /*
    safeQuery 工具方法，它支援：
    指定 table 名稱與欄位名稱（都會自動轉義）
    傳入比對值（自動安全綁定）
    回傳 Cursor，你可以自己決定怎麼處理結果
    */
    private Cursor safeQuery(SQLiteDatabase db, String table, String column, String value) {
        String safeTable = quoteIdentifier(table);
        String safeColumn = quoteIdentifier(column);

        String sql = "SELECT * FROM " + safeTable + " WHERE " + safeColumn + " = ?";

        return db.rawQuery(sql, new String[]{value});
    }
    private String quoteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\""; // ANSI SQL 標準跳脫
    }

    //    public void updateUser(String memoryTitle , String memoryDetail) {
//        SQLiteDatabase db = getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put("memoryDetail", memoryDetail);
//        db.update(memoryTitle, values);
//        
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
