package ru.lizzzi.rowingstatistic.db.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ru.lizzzi.rowingstatistic.db.data.RowerContract.RowerData;

public class RowerDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "rower.db";
    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase database;
    private Context context;


    public RowerDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
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

    public void clearDB() {
        database = this.getWritableDatabase();
        database.delete(RowerData.TABLE_NAME, null, null);
        database.close();
    }

    public void saveData(
            int id,
            double distance,
            long time,
            double speed,
            int strokeRate,
            int power) {
        database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(RowerData.COLUMN_ROWER, id);
        values.put(RowerData.COLUMN_DISTANCE, distance);
        values.put(RowerData.COLUMN_TIME, time);
        values.put(RowerData.COLUMN_SPEED, speed);
        values.put(RowerData.COLUMN_STROKE_RATE, strokeRate);
        values.put(RowerData.COLUMN_POWER, power);
        database.insert(RowerData.TABLE_NAME, null, values);
        database.close();
    }

    public int getMaxSpeed() {
        database = this.getReadableDatabase();
        String[] selection = {"MAX(" + RowerData.COLUMN_SPEED + ")"};
        Cursor cursor = database.query(
                RowerData.TABLE_NAME,  // таблица
                selection,            // столбцы
                null,             // столбцы для условия WHERE
                null,         // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null                   // порядок сортировки
        );
        int result = 0;
        if (cursor != null) {
            cursor.moveToFirst();
            result = cursor.getInt(0);
            cursor.close();
        }
        return result;
    }

    public int getMaxStrokeRate() {
        database = this.getReadableDatabase();
        String[] selection = {"MAX(" + RowerData.COLUMN_STROKE_RATE + ")"};
        Cursor cursor = database.query(
                RowerData.TABLE_NAME,  // таблица
                selection,            // столбцы
                null,             // столбцы для условия WHERE
                null,         // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null                   // порядок сортировки
        );
        int result = 0;
        if (cursor != null) {
            cursor.moveToFirst();
            result = cursor.getInt(0);
            cursor.close();
        }
        return result;
    }

    public int getMaxPower() {
        database = this.getReadableDatabase();
        String[] selection = {"MAX(" + RowerData.COLUMN_POWER + ")"};
        Cursor cursor = database.query(
                RowerData.TABLE_NAME,  // таблица
                selection,            // столбцы
                null,             // столбцы для условия WHERE
                null,         // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null                   // порядок сортировки
        );
        int result = 0;
        if (cursor != null) {
            cursor.moveToFirst();
            result = cursor.getInt(0);
            cursor.close();
        }
        return result;
    }

    public int getMaxTime() {
        database = this.getReadableDatabase();
        String[] selection = {"MAX(" + RowerData.COLUMN_TIME + ")"};
        Cursor cursor = database.query(
                RowerData.TABLE_NAME,  // таблица
                selection,            // столбцы
                null,             // столбцы для условия WHERE
                null,         // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null                   // порядок сортировки
        );
        int result = 0;
        if (cursor != null) {
            cursor.moveToFirst();
            result = cursor.getInt(0);
            cursor.close();
        }
        return result;
    }

    public int getMinTime() {
        database = this.getReadableDatabase();
        String[] selection = {"MIN(" + RowerData.COLUMN_TIME + ")"};
        Cursor cursor = database.query(
                RowerData.TABLE_NAME,  // таблица
                selection,            // столбцы
                null,             // столбцы для условия WHERE
                null,         // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null                   // порядок сортировки
        );
        int result = 0;
        if (cursor != null) {
            cursor.moveToFirst();
            result = cursor.getInt(0);
            cursor.close();
        }
        return result;
    }

    public int getMaxDistance() {
        database = this.getReadableDatabase();
        String[] selection = {"MAX(" + RowerData.COLUMN_DISTANCE + ")"};
        Cursor cursor = database.query(
                RowerData.TABLE_NAME,  // таблица
                selection,            // столбцы
                null,             // столбцы для условия WHERE
                null,         // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null                   // порядок сортировки
        );
        int result = 0;
        if (cursor != null) {
            cursor.moveToFirst();
            result = cursor.getInt(0);
            cursor.close();
        }
        return result;
    }

    public int getMinDistance() {
        database = this.getReadableDatabase();
        String[] selection = {"MIN(" + RowerData.COLUMN_DISTANCE + ")"};
        Cursor cursor = database.query(
                RowerData.TABLE_NAME,  // таблица
                selection,            // столбцы
                null,             // столбцы для условия WHERE
                null,         // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null                   // порядок сортировки
        );
        int result = 0;
        if (cursor != null) {
            cursor.moveToFirst();
            result = cursor.getInt(0);
            cursor.close();
        }
        return result;
    }
}
