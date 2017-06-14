package com.example.jushalo.days;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by asus on 2016/12/9.
 */
public class myDB extends SQLiteOpenHelper {
    private static final String DB_NAME = "myDB";
    private static final String TABLE_NAME = "myTABLE";
    private static final int DB_VERSION = 1;

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    public myDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_TABLE = "CREATE TABLE if not exists "
                + TABLE_NAME
                + " (id INTEGER PRIMARY KEY, title TEXT,"
                + " date TEXT, tag INTEGER, setTop TEXT)";
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void insert2DB(String title, String date, int tag, String setTop) throws ParseException {
        SQLiteDatabase db = getWritableDatabase();
        Cursor results = db.query(TABLE_NAME, null, null, null, null, null, null, null);

        String _setTop = setTop;
        if (results.moveToFirst()) {
            if (Objects.equals(_setTop, "是")) {
                clearSetTop();
                Log.e("insert", "insert a top item");
            }
        } else {
            _setTop = "是";
            Log.e("insert", "emptyDatabase, set first item top");
        }
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("date", date);
        cv.put("tag", tag);
        cv.put("setTop", _setTop);
        db.insert(TABLE_NAME, null, cv);
        db.close();
    }

    public void update(String selectedTitle, String selectedDate, String newTitle, String newDate, int tag, String setTop) {
        if (Objects.equals(setTop, "是")) {
            clearSetTop();
        }
        SQLiteDatabase db = getWritableDatabase();
        Cursor results = db.query(TABLE_NAME, null, "title = ?" + " AND date = ?",
                new String[] { selectedTitle, selectedDate }, null, null, null);
        results.moveToFirst();
        int id = results.getInt(results.getColumnIndex("id"));

        ContentValues cv = new ContentValues();
        cv.put("title",newTitle);
        cv.put("date", newDate);
        cv.put("tag", tag);
        cv.put("setTop", setTop);
        db.update(TABLE_NAME, cv, "id = ?", new String[] { id + "" });
        db.close();
    }

    public void delete(String title, String date) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor results = db.query(TABLE_NAME, null, "title = ?" + " AND date = ?",
                new String[] { title, date }, null, null, null);
        if (results.moveToFirst()) {
            String tem = results.getString(results.getColumnIndex("setTop"));
            if (Objects.equals(tem, "是")) {
                db.delete(TABLE_NAME, "title = ? AND date = ?", new String[] { title, date });
                setFirstItemTop();
            } else {
                db.delete(TABLE_NAME, "title = ? AND date = ?", new String[] { title, date });
            }
        } else {
            Log.e("delete", "not item found");
        }
        db.close();
    }

    public List<Map<String, Object>> returndata() throws ParseException {
        SQLiteDatabase db = getWritableDatabase();
        List<Map<String, Object>> data = new ArrayList<>();

        data.add(getTopDataForList());

        Cursor cursor = db.query(TABLE_NAME, null,
                "setTop = ?", new String[] { "否" }, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Map<String, Object> temp = new LinkedHashMap<>();
                String title = cursor.getString(cursor.getColumnIndex("title"));
                String date = cursor.getString(cursor.getColumnIndex("date"));
                int tag = cursor.getInt(cursor.getColumnIndex("tag"));
                long howmany_days = getDays(date);
                int before_after = 0;
                if (howmany_days > 0) {
                    before_after = R.mipmap.before;
                } else if (howmany_days <= 0 && howmany_days >= -7) {
                    before_after = R.mipmap.future_red;
                } else if (howmany_days < -7 && howmany_days >= -14) {
                    before_after = R.mipmap.future_orange;
                } else if (howmany_days < -14) {
                    before_after = R.mipmap.future_blue;
                }
                String Str_days = String.valueOf(Math.abs(howmany_days));
                String setTop = cursor.getString(cursor.getColumnIndex("setTop"));
                temp.put("title", title);
                temp.put("date", date);
                temp.put("str_days", Str_days);
                temp.put("tag", tag);
                temp.put("when", before_after);
                temp.put("setTop", setTop);
                data.add(temp);
            } while (cursor.moveToNext());
        }

        return data;
    }

    //返回标签数据
    public List<Map<String, Object>> returnByTag(int tag) throws ParseException {
        SQLiteDatabase db = getWritableDatabase();
        List<Map<String, Object>> data = new ArrayList<>();

        if (getTopDataForList() == null) return null;

        if ((int) getTopDataForList().get("tag") == tag) {
            data.add(getTopDataForList());
        }

        Cursor cursor = db.query(TABLE_NAME, null,
                "tag = ? AND setTop = ?", new String[] {tag + "", "否"}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Map<String, Object> temp = new LinkedHashMap<>();
                String title = cursor.getString(cursor.getColumnIndex("title"));
                String date = cursor.getString(cursor.getColumnIndex("date"));
                long howmany_days = getDays(date);
                int before_after = 0;
                if (howmany_days > 0) {
                    before_after = R.mipmap.before;
                } else if (howmany_days <= 0 && howmany_days >= -7) {
                    before_after = R.mipmap.future_red;
                } else if (howmany_days < -7 && howmany_days >= -14) {
                    before_after = R.mipmap.future_orange;
                } else if (howmany_days < -14) {
                    before_after = R.mipmap.future_blue;
                }
                String Str_days = String.valueOf(Math.abs(howmany_days));
                String setTop = cursor.getString(cursor.getColumnIndex("setTop"));
                temp.put("title", title);
                temp.put("date", date);
                temp.put("str_days", Str_days);
                temp.put("tag", tag);
                temp.put("when", before_after);
                temp.put("setTop", setTop);
                data.add(temp);
            } while (cursor.moveToNext());
        }
        return data;
    }

    public List<Map<String, Object>> returnByDate(String _date) throws ParseException {
        SQLiteDatabase db = getWritableDatabase();
        List<Map<String, Object>> data = new ArrayList<>();

        Cursor cursor = db.query(TABLE_NAME, null,
                "date = ?", new String[] { _date }, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Map<String, Object> temp = new LinkedHashMap<>();
                String title = cursor.getString(cursor.getColumnIndex("title"));
                String date = cursor.getString(cursor.getColumnIndex("date"));
                int tag = cursor.getInt(cursor.getColumnIndex("tag"));
                long howmany_days = getDays(date);
                int before_after = 0;
                if (howmany_days > 0) {
                    before_after = R.mipmap.before;
                } else if (howmany_days <= 0 && howmany_days >= -7) {
                    before_after = R.mipmap.future_red;
                } else if (howmany_days < -7 && howmany_days >= -14) {
                    before_after = R.mipmap.future_orange;
                } else if (howmany_days < -14) {
                    before_after = R.mipmap.future_blue;
                }
                String Str_days = String.valueOf(Math.abs(howmany_days));
                String setTop = cursor.getString(cursor.getColumnIndex("setTop"));
                temp.put("title", title);
                temp.put("date", date);
                temp.put("str_days", Str_days);
                temp.put("tag", tag);
                temp.put("when", before_after);
                temp.put("setTop", setTop);
                data.add(temp);
            } while (cursor.moveToNext());
        }
        return data;
    }

    private void clearSetTop() {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("setTop", "否");
        db.update(TABLE_NAME, values, "setTop=?", new String[] {"是"});
    }

    private void setFirstItemTop() {
        Log.e("setFirstItemTop", "called");
        clearSetTop();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            ContentValues values = new ContentValues();
            values.put("setTop", "是");
            db.update(TABLE_NAME, values, "title = ?", new String[] { cursor.getString(cursor.getColumnIndex("title")) });
            Log.e("setFirstItemTop", "tittle##" + cursor.getString(cursor.getColumnIndex("title")));
        }
    }

    public Map<String, Object> getTopData() throws ParseException {
        Map<String, Object> temp = null;
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, "setTop = ?", new String[] { "是" }, null, null, null);
        if (cursor.moveToFirst()) {
            temp = new LinkedHashMap<>();

            String title = cursor.getString(cursor.getColumnIndex("title"));
            String date = cursor.getString(cursor.getColumnIndex("date"));
            int tag = cursor.getInt(cursor.getColumnIndex("tag"));

            String before_after = null;
            long howmany_days = getDays(date);
            if (howmany_days > 0) {
                before_after = "天前";
            } else {
                before_after = "天后";
            }
            String Str_days = String.valueOf(Math.abs(howmany_days));
            String setTop = cursor.getString(cursor.getColumnIndex("setTop"));
            temp.put("title", title);
            temp.put("date", date);
            temp.put("str_days", Str_days);
            temp.put("tag", tag);
            temp.put("when", before_after);
            temp.put("setTop", setTop);
        }
        return temp;

    }

    public Map<String, Object> getTopDataForList() throws ParseException {
        Map<String, Object> temp = null;
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, "setTop = ?", new String[] { "是" }, null, null, null);
        if (cursor.moveToFirst()) {
            temp = new LinkedHashMap<>();

            String title = cursor.getString(cursor.getColumnIndex("title"));
            String date = cursor.getString(cursor.getColumnIndex("date"));
            int tag = cursor.getInt(cursor.getColumnIndex("tag"));

            long howmany_days = getDays(date);
            int before_after = 0;
            if (howmany_days > 0) {
                before_after = R.mipmap.before;
            } else if (howmany_days <= 0 && howmany_days >= -7) {
                before_after = R.mipmap.future_red;
            } else if (howmany_days < -7 && howmany_days >= -14) {
                before_after = R.mipmap.future_orange;
            } else if (howmany_days < -14) {
                before_after = R.mipmap.future_blue;
            }

            String Str_days = String.valueOf(Math.abs(howmany_days));
            String setTop = cursor.getString(cursor.getColumnIndex("setTop"));
            temp.put("title", title);
            temp.put("date", date);
            temp.put("str_days", Str_days);
            temp.put("tag", tag);
            temp.put("when", before_after);
            temp.put("setTop", setTop);
        }
        return temp;
    }

    private long getDays(String date) throws ParseException {
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String now = df.format(curDate);
        Date today = df.parse(now);
        Date day = df.parse(date);
        long timeMeasuredByMillisecond = today.getTime() - day.getTime();
        long days = timeMeasuredByMillisecond / (24 * 60 * 60 * 1000);
        return days;
    }
}
