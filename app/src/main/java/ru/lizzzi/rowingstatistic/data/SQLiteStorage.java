package ru.lizzzi.rowingstatistic.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.lizzzi.rowingstatistic.charts.data.Entry;

public class SQLiteStorage extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "rower.db";
    private static final int DATABASE_VERSION = 1;
    private static SQLiteDatabase database;
    private Context context;


    public SQLiteStorage(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_ROWER_TABLE = "CREATE TABLE " + RowerData.TABLE_NAME + " ("
                + RowerData.COLUMN_ROWER + " TEXT NOT NULL, "
                + RowerData.COLUMN_DISTANCE + " REAL NOT NULL, "
                + RowerData.COLUMN_TIME + " INTEGER NOT NULL, "
                + RowerData.COLUMN_SPEED + " REAL NOT NULL, "
                + RowerData.COLUMN_STROKE_RATE + " INTEGER NOT NULL, "
                + RowerData.COLUMN_POWER + " INTEGER NOT NULL, "
                + RowerData.COLUMN_FILE_LOCATION + " TEXT);";
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

    public void saveData(List<Map> dataForSave) {
        database = this.getWritableDatabase();
        for (int i = 0; i < dataForSave.size(); i++) {
            ContentValues values = new ContentValues();
            values.put(
                    RowerData.COLUMN_ROWER,
                    dataForSave.get(i).get("rowerName").toString());
            values.put(
                    RowerData.COLUMN_DISTANCE,
                    Double.valueOf(dataForSave.get(i).get("distance").toString()) );
            values.put(
                    RowerData.COLUMN_TIME,
                    Long.valueOf(dataForSave.get(i).get("time").toString()));
            values.put(
                    RowerData.COLUMN_SPEED,
                    Double.valueOf(dataForSave.get(i).get("speed").toString()));
            values.put(
                    RowerData.COLUMN_STROKE_RATE,
                    Integer.valueOf(dataForSave.get(i).get("strokeRate").toString()));
            values.put(
                    RowerData.COLUMN_POWER,
                    Integer.valueOf(dataForSave.get(i).get("power").toString()));
            values.put(
                    RowerData.COLUMN_FILE_LOCATION,
                    dataForSave.get(i).get("fileLocation").toString());
            database.insert(RowerData.TABLE_NAME, null, values);
        }
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
        return result;
    }

    public double getTime(double distancePoint) {
        database = this.getReadableDatabase();
        String[] columns = {RowerData.COLUMN_TIME};
        String selection =
                RowerData.COLUMN_DISTANCE +
                " BETWEEN " +
                (distancePoint - 0.1) +
                " AND " +
                (distancePoint + 0.1);

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
        return result;
    }

    public double getDistance(double timePoint) {
        database = this.getReadableDatabase();
        String[] columns = {RowerData.COLUMN_DISTANCE};
        String selection =
                RowerData.COLUMN_TIME +
                        " BETWEEN " +
                        (timePoint - 10) +
                        " AND " +
                        (timePoint + 10);

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
        return result;
    }

    public List<List<Entry>> getDataFotDrawing(
            String rowerName,
            int rowerPosition,
            int maxPower,
            int timeOrDistance,
            float maxSpeed,
            float maxStrokeRate) {
        List<List<Entry>> dataForChart = new ArrayList<>();
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
        String[] selectionArgs = {rowerName};

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
            List<Entry> rowerPower = new ArrayList<>();
            List<Entry> rowerSpeed = new ArrayList<>();
            List<Entry> rowerStrokeRate = new ArrayList<>();
            float distance, power, speed, strokeRate;
            long time;
            do { //наполняем массивы для построения
                time = cursor.getLong(cursor.getColumnIndex(RowerData.COLUMN_TIME));
                distance = cursor.getFloat(cursor.getColumnIndex(RowerData.COLUMN_DISTANCE));
                if (rowerPosition == 0) {
                    speed = cursor.getFloat(cursor.getColumnIndex(RowerData.COLUMN_SPEED));
                    speed = speed / maxSpeed;
                    strokeRate = cursor.getFloat(cursor.getColumnIndex(RowerData.COLUMN_STROKE_RATE));
                    strokeRate = strokeRate / maxStrokeRate;
                    if (timeOrDistance == 0) {
                        rowerSpeed.add(new Entry(time, speed));
                        rowerStrokeRate.add(new Entry(time, strokeRate));
                    } else {
                        rowerSpeed.add(new Entry(distance, speed));
                        rowerStrokeRate.add(new Entry(distance, strokeRate));
                    }

                }
                power = cursor.getFloat(cursor.getColumnIndex(RowerData.COLUMN_POWER));
                power = power / (float) maxPower;
                if (timeOrDistance == 0) {
                    rowerPower.add(new Entry(time, power));
                } else {
                    rowerPower.add(new Entry(distance, power));
                }
            } while (cursor.moveToNext());
            cursor.close();
            dataForChart.add(0, rowerPower);
            if (rowerPosition == 0) {
                dataForChart.add(1, rowerSpeed);
                dataForChart.add(2, rowerStrokeRate);
            }
        }
        return dataForChart;
    }

    public float getAverageTime(int rower, float periodStart, float periodEnd) {
        database = this.getReadableDatabase();
        String[] columns = {"AVG(" + RowerData.COLUMN_POWER + ")"};

        //для пловца № в промежутке выборки
        String selection =
                RowerData.COLUMN_ROWER +
                "=? AND " +
                RowerData.COLUMN_TIME +
                " BETWEEN " +
                (periodStart - 10) +
                " AND " +
                (periodEnd + 10);
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

        float result = 0;
        if (cursor != null) {
            cursor.moveToFirst();
            //добавлем значение в строку
            result = cursor.getFloat(0);
            cursor.close();
        }
        return result;
    }

    public float getAverageDistance(int rower, float periodStart, float periodEnd) {
        database = this.getReadableDatabase();
        String[] columns = {"AVG(" + RowerData.COLUMN_POWER + ")"};

        //для пловца № в промежутке выборки
        /*ввиду того, что беру тип переменной float (а она имеет плохую точность)
                                            беру выборку с дипазоном +/-0.01
                                            Прим. у float 4,1 может быть 4,100000001.
                                            */
        String selection =
                RowerData.COLUMN_ROWER +
                "=? AND " +
                RowerData.COLUMN_DISTANCE +
                " BETWEEN " +
                (periodStart - 0.01) +
                " AND " +
                (periodEnd + 0.01);
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

        float result = 0;
        if (cursor != null) {
            cursor.moveToFirst();
            //добавлем значение в строку
            result = cursor.getFloat(0);
            cursor.close();
        }
        return result;
    }

    public ArrayList<String> getRowerName() {
        database = this.getReadableDatabase();
        String[] columns = {RowerData.COLUMN_ROWER};
        Cursor cursor = database.query(
                true,
                RowerData.TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                null,
                null
        );
        ArrayList<String> result = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                result.add(cursor.getString(cursor.getColumnIndex(RowerData.COLUMN_ROWER)));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return result;
    }

    public void setNewRowerName(String oldName, String newName) {
        database = this.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(RowerData.COLUMN_ROWER, newName);
        String whereClause = RowerData.COLUMN_ROWER + "=?";
        String[] selectionArgs = {oldName};
        database.update(RowerData.TABLE_NAME, values, whereClause, selectionArgs);
    }

    public void deleteRower(String rowerName) {
        database = this.getReadableDatabase();
        String whereClause = RowerData.COLUMN_ROWER + "=?";
        String[] selectionArgs = {rowerName};
        database.delete(RowerData.TABLE_NAME, whereClause, selectionArgs);
    }

    public boolean checkFileLocation(String fileLocation) {
        database = this.getReadableDatabase();
        String[] columns = {RowerData.COLUMN_FILE_LOCATION};
        String selection =
                RowerData.COLUMN_FILE_LOCATION +
                "=?";
        String[] selectionArgs = {fileLocation};
        Cursor cursor = database.query(
                RowerData.TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null,
                null
        );
        boolean isFound = false;
        if (cursor != null && cursor.getCount() > 0) {
            isFound = true;
            cursor.close();
        }
        return isFound;

    }

    public void closeStorage() {
        if (database != null) {
            database.close();
        }
    }

    public static final class RowerData implements BaseColumns {
        final static String TABLE_NAME = "training";
        final static String COLUMN_ROWER = "rower";
        final static String COLUMN_DISTANCE = "distance";
        final static String COLUMN_TIME = "time";
        final static String COLUMN_SPEED = "speed";
        final static String COLUMN_STROKE_RATE = "strokeRate";
        final static String COLUMN_POWER = "power";
        final static String COLUMN_FILE_LOCATION = "fileLocation";
    }
}
