package ru.lizzzi.rowingstatistic.db.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.lizzzi.rowingstatistic.charts.data.Entry;
import ru.lizzzi.rowingstatistic.db.data.RowerContract.RowerData;

public class RowerDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "rower.db";
    private static final int DATABASE_VERSION = 1;
    private static SQLiteDatabase database;
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

    public void saveData(List<Map> data) {
        database = this.getWritableDatabase();
        for (int i = 0; i < data.size(); i++) {
            ContentValues values = new ContentValues();
            values.put(
                    RowerData.COLUMN_ROWER,
                    Integer.valueOf(data.get(i).get("fileNumber").toString()));
            values.put(
                    RowerData.COLUMN_DISTANCE,
                    Double.valueOf(data.get(i).get("distance").toString()) );
            values.put(
                    RowerData.COLUMN_TIME,
                    Long.valueOf(data.get(i).get("time").toString()));
            values.put(
                    RowerData.COLUMN_SPEED,
                    Double.valueOf(data.get(i).get("speed").toString()));
            values.put(
                    RowerData.COLUMN_STROKE_RATE,
                    Integer.valueOf(data.get(i).get("strokeRate").toString()));
            values.put(
                    RowerData.COLUMN_POWER,
                    Integer.valueOf(data.get(i).get("power").toString()));
            database.insert(RowerData.TABLE_NAME, null, values);
        }
        database.close();
    }

    public float getMaxSpeed() {
        return getBoundaryValuesForFloat(new String[]{"MAX(" + RowerData.COLUMN_SPEED + ")"});
    }

    public float getMaxStrokeRate() {
        return getBoundaryValuesForFloat(new String[]{"MAX(" + RowerData.COLUMN_STROKE_RATE + ")"});
    }

    public int getMaxPower() {
        return getBoundaryValuesForInt(new String[]{"MAX(" + RowerData.COLUMN_POWER + ")"});
    }

    public float getMaxTime() {
        return getBoundaryValuesForFloat(new String[]{"MAX(" + RowerData.COLUMN_TIME + ")"});
    }

    public float getMinTime() {
        return getBoundaryValuesForFloat(new String[]{"MIN(" + RowerData.COLUMN_TIME + ")"});
    }

    public float getMaxDistance() {
        return getBoundaryValuesForFloat(new String[]{"MAX(" + RowerData.COLUMN_DISTANCE + ")"});
    }

    public float getMinDistance() {
        return getBoundaryValuesForFloat(new String[]{"MIN(" + RowerData.COLUMN_DISTANCE + ")"});
    }

    private int getBoundaryValuesForInt(String[] columns) {
        database = this.getReadableDatabase();
        Cursor cursor = database.query(
                RowerData.TABLE_NAME,  // таблица
                columns,            // столбцы
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
        database.close();
        return result;
    }

    private float getBoundaryValuesForFloat(String[] columns) {
        database = this.getReadableDatabase();
        Cursor cursor = database.query(
                RowerData.TABLE_NAME,  // таблица
                columns,            // столбцы
                null,             // столбцы для условия WHERE
                null,         // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null                   // порядок сортировки
        );
        float result = 0;
        if (cursor != null) {
            cursor.moveToFirst();
            result = cursor.getInt(0);
            cursor.close();
        }
        database.close();
        return result;
    }

    public double getTime(double pointOnDistance) {
        database = this.getReadableDatabase();
        String[] columns = {RowerData.COLUMN_TIME};
        String selection =
                RowerData.COLUMN_DISTANCE +
                " BETWEEN " +
                (pointOnDistance - 0.1) +
                " AND " +
                (pointOnDistance + 0.1);

        Cursor cursor = database.query(
                RowerData.TABLE_NAME,  // таблица
                columns,            // столбцы
                selection,             // столбцы для условия WHERE
                null,         // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null                   // порядок сортировки
        );
        double result = 0;
        if (cursor != null && cursor.moveToNext()) {
            result = cursor.getDouble(cursor.getColumnIndex(RowerData.COLUMN_TIME));
            cursor.close();
        }
        database.close();
        return result;
    }

    public double getDistance(double pointOnDistance) {
        database = this.getReadableDatabase();
        String[] columns = {RowerData.COLUMN_DISTANCE};
        String selection =
                RowerData.COLUMN_TIME +
                        " BETWEEN " +
                        (pointOnDistance - 10) +
                        " AND " +
                        (pointOnDistance + 10);

        Cursor cursor = database.query(
                RowerData.TABLE_NAME,  // таблица
                columns,            // столбцы
                selection,             // столбцы для условия WHERE
                null,         // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null                   // порядок сортировки
        );
        double result = 0;
        if (cursor != null && cursor.moveToNext()) {
            result = cursor.getDouble(cursor.getColumnIndex(RowerData.COLUMN_DISTANCE));
            cursor.close();
        }
        database.close();
        return result;
    }

    public List<List<Entry>> getDataFotDrawing(int rower, int max_power, int timeF_distanceT, float max_absolut, float max_stroke_rate) {

        List<List<Entry>> dataForChart = new ArrayList<>();
        List<Entry> rowersPower = new ArrayList<>();
        List<Entry> rowersSpeed = new ArrayList<>();
        List<Entry> rowersStrokeRate = new ArrayList<>();

        database = this.getReadableDatabase();
        String[] columns = {
                RowerData.COLUMN_ROWER,
                RowerData.COLUMN_TIME,
                RowerData.COLUMN_POWER,
                RowerData.COLUMN_SPEED,
                RowerData.COLUMN_STROKE_RATE,
                RowerData.COLUMN_DISTANCE
        };
        String selection = RowerData.COLUMN_ROWER + "=?";
        String[] selectionArgs = {String.valueOf(rower)};

        Cursor cursor = database.query(
                RowerData.TABLE_NAME,  // таблица
                columns,            // столбцы
                selection,             // столбцы для условия WHERE
                selectionArgs,         // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null                   // порядок сортировки
        );

        if (cursor != null && cursor.moveToFirst()) {
            float distance;
            long time;
            float speed;
            float strokeRate;
            float power;
            do { //наполняем массивы для построения
                time = cursor.getLong(cursor.getColumnIndex(RowerData.COLUMN_TIME));
                distance = cursor.getFloat(cursor.getColumnIndex(RowerData.COLUMN_DISTANCE));
                power = cursor.getFloat(cursor.getColumnIndex(RowerData.COLUMN_POWER));
                power = power / (float) max_power;
                if (timeF_distanceT == 0) {
                    rowersPower.add(new Entry(time, power));
                } else {
                    rowersPower.add(new Entry(distance, power));
                }
                if (rower == 0) {
                    speed = cursor.getFloat(cursor.getColumnIndex(RowerData.COLUMN_SPEED));
                    speed = speed / max_absolut;
                    strokeRate = cursor.getFloat(cursor.getColumnIndex(RowerData.COLUMN_STROKE_RATE));
                    strokeRate = strokeRate / max_stroke_rate;
                    if (timeF_distanceT == 0) {
                        rowersSpeed.add(new Entry(time, speed));
                        rowersStrokeRate.add(new Entry(time, strokeRate));
                    } else {
                        rowersSpeed.add(new Entry(distance, speed));
                        rowersStrokeRate.add(new Entry(distance, strokeRate));
                    }

                }

            } while (cursor.moveToNext());
            cursor.close();
            dataForChart.add(0, rowersPower);
            if (rower == 0) {
                dataForChart.add(1, rowersSpeed);
                dataForChart.add(2, rowersStrokeRate);
            }
            database.close();
        }
        return dataForChart;
    }

    public float getAverageTime(int rower, float periodStar, float periodEnd) {
        database = this.getReadableDatabase();
        String[] columns = {"AVG(" + RowerContract.RowerData.COLUMN_POWER + ")"};

        //для пловца № в промежутке выборки
        String selection =
                RowerContract.RowerData.COLUMN_ROWER +
                "=? AND " +
                RowerContract.RowerData.COLUMN_TIME +
                " BETWEEN " +
                (periodStar - 10) +
                " AND " +
                (periodEnd + 10);
        String[] selectionArgs = {String.valueOf(rower)};
        Cursor cursor = database.query(
                RowerContract.RowerData.TABLE_NAME,  // таблица
                columns,            // столбцы
                selection,             // столбцы для условия WHERE
                selectionArgs,         // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null                   // порядок сортировки
        );

        float result = 0;
        if (cursor != null) {
            cursor.moveToFirst();
            //добавлем значение в строку
            result = cursor.getFloat(0);
            cursor.close();
        }
        database.close();
        return result;
    }

    public float getAverageDistance(int rower, float periodStar, float periodEnd) {
        database = this.getReadableDatabase();
        String[] columns = {"AVG(" + RowerContract.RowerData.COLUMN_POWER + ")"};

        //для пловца № в промежутке выборки
        /*ввиду того, что беру тип переменной float (а она имеет плохую точность)
                                            беру выборку с дипазоном +/-0.01
                                            Прим. у float 4,1 может быть 4,100000001.
                                            */
        String selection =
                RowerContract.RowerData.COLUMN_ROWER +
                        "=? AND " +
                        RowerData.COLUMN_DISTANCE +
                        " BETWEEN " +
                        (periodStar - 0.01) +
                        " AND " +
                        (periodEnd + 0.01);
        String[] selectionArgs = {String.valueOf(rower)};
        Cursor cursor = database.query(
                RowerContract.RowerData.TABLE_NAME,  // таблица
                columns,            // столбцы
                selection,             // столбцы для условия WHERE
                selectionArgs,         // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null                   // порядок сортировки
        );

        float result = 0;
        if (cursor != null) {
            cursor.moveToFirst();
            //добавлем значение в строку
            result = cursor.getFloat(0);
            cursor.close();
        }
        database.close();
        return result;
    }
}
