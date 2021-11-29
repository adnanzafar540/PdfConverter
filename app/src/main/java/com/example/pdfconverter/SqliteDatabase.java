package com.example.pdfconverter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



import java.util.ArrayList;

public class SqliteDatabase extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Database";
    public static final String TABLE_NAME = "image_table";
    public static final String COL_1 = "imagepath";
    public static final String COL_2 = "pdfpath";
    public static final String COL_3 = "image_Text";




    public SqliteDatabase(Context context) {
        super(context, DATABASE_NAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String Table = "CREATE TABLE " + TABLE_NAME +
                " (id integer primary key," + COL_1 + " VARCHAR," + COL_2 + " VARCHAR,"+
                COL_3 + " VARCHAR);";
        db.execSQL(Table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(Images model) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, model.getUrl());
        contentValues.put(COL_2, model.pdfurl);
        contentValues.put(COL_3, model.getImage_txt());

        long result = db.insert(TABLE_NAME, null, contentValues);
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public ArrayList<Images> readAllData() {
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        ArrayList<Images> list = new ArrayList<>();
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        while ((cursor.moveToNext())) {
            Images Model = new Images();
            Model.setUrl(cursor.getString(1));
            Model.setImage_txt(cursor.getString(3));
            Model.setPdfurl(cursor.getString(2));
            list.add(Model);
        }
        return list;
    }

}