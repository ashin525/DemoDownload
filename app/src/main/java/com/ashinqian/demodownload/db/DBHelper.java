package com.ashinqian.demodownload.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by qiany on 2016/2/23.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "download.db";

    private static final int VERSION = 1;

    private static final String SQL_CREATE = "create table thread_info(_id integer primary key autoincrement, thread_id, url, start, end, finished)";
    private static final String SQL_DROP = "drop table if exists thread_info";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(SQL_DROP);
        db.execSQL(SQL_CREATE);
    }
}
