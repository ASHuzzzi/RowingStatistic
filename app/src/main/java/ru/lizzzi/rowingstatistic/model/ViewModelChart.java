package ru.lizzzi.rowingstatistic.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ru.lizzzi.rowingstatistic.charts.data.Entry;
import ru.lizzzi.rowingstatistic.data.SQLiteStorage;

public class ViewModelChart extends AndroidViewModel {
    private SQLiteStorage sqlStorage;
    //файл с полями для запоминания последней открытой папки
    public static final String APP_PREFERENCES = "lastdir";
    public static final String APP_PREFERENCES_COUNTER = "counter";
    public static final String APP_PREFERENCES_DIR = "dir";
    private SharedPreferences sharedPreferencesForSettings;

    public ViewModelChart(@NonNull Application application) {
        super(application);
        sqlStorage = new SQLiteStorage(getApplication().getApplicationContext());
    }

    public int getMaxPower() {
        return sqlStorage.getMaxPower();
    }

    public float getMaxSpeed() {
        return sqlStorage.getMaxSpeed();
    }

    public float getMaxTime() {
        return  sqlStorage.getMaxTime();
    }

    public float getMinTime() {
        return sqlStorage.getMinTime();
    }

    public float getMaxDistance() {
        return sqlStorage.getMaxDistance();
    }

    public float getMaxStrokeRate() {
        return sqlStorage.getMaxStrokeRate();
    }

    public ArrayList<String> getRowerName() {
        return sqlStorage.getRowerName();
    }

    public float getAverageTime(int rower, float startPeriod, float endPeriod) {
        return sqlStorage.getAverageTime(rower, startPeriod, endPeriod);
    }

    public float getAverageDistance(int rower, float startPeriod, float endPeriod) {
        return sqlStorage.getAverageDistance(rower, startPeriod, endPeriod);
    }

    public List<List<Entry>> getDataFotDrawing(
            String rowerName,
            int rowerPosition,
            int maxPower,
            int timeOrDistance,
            float maxSpeed,
            float maxStrokeRate) {
      return sqlStorage.getDataFotDrawing(
              rowerName,
              rowerPosition,
              maxPower,
              timeOrDistance,
              maxSpeed,
              maxStrokeRate);
    }

    public double getTime(double selectedPoint) {
        return sqlStorage.getTime(selectedPoint);
    }

    public double getDistance(double selectedPoint) {
        return sqlStorage.getDistance(selectedPoint);
    }

    public void clearLastOpenDirectory() {
        //присваеваем 0 для сброса запомненой папки в классе OpenFileDialog
        SharedPreferences sharedPreferences =
                getApplication().getApplicationContext().getSharedPreferences(
                        APP_PREFERENCES,
                        Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt(APP_PREFERENCES_COUNTER, 0).apply();
    }

    @Override
    public void onCleared(){
        super.onCleared();
        sqlStorage.closeStorage();
    }
}
