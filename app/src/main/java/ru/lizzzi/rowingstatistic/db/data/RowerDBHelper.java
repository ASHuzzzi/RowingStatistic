package ru.lizzzi.rowingstatistic.db.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ru.lizzzi.rowingstatistic.db.data.RowerContract.RowerData;

public class RowerDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "rower.db";
    private static final int DATABASE_VERSION = 1;


    public RowerDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_ROWER_TABLE = "CREATE TABLE " + RowerData.TABLE_NAME + " ("
                + RowerData._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + RowerData.COLUMN_ROWER + " INTEGER NOT NULL, "
                + RowerData.COLUMN_DISTANCE + " REAL NOT NULL, "
                + RowerData.COLUMN_TIME + " INTEGER NOT NULL, "
                + RowerData.COLUMN_SPEED + " REAL NOT NULL, "
                + RowerData.COLUMN_STROKE_RATE + " INTEGER NOT NULL, "
                + RowerData.COLUMN_POWER + " INTEGER NOT NULL);";

        // Запускаем создание таблицы
        db.execSQL(SQL_CREATE_ROWER_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
