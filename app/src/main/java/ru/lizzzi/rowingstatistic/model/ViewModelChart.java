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
    private static final String APP_PREFERENCES = "lastdir";
    private static final String APP_PREFERENCES_COUNTER = "counter";

    //файл для сохранения настоек для построения графиков
    private static final String APP_PREFERENCES_Chart = "chart_settings";
    private static final String APP_PREFERENCES_TYPE_CHART = "type_chart";
    private SharedPreferences sharedPreferencesCharts;

    private int typeOfMeasurement;
    private int TIME = 0;
    private int DISTANCE = 1;
    private float maxSpeed;
    private int maxPower;
    private float maxStrokeRate;
    private float minValueForCharts;
    private float maxValueForCharts;
    private ArrayList<String> chartName;

    public ViewModelChart(@NonNull Application application) {
        super(application);
        sqlStorage = new SQLiteStorage(getApplication().getApplicationContext());
        sharedPreferencesCharts = getApplication().getApplicationContext().getSharedPreferences(
                APP_PREFERENCES_Chart,
                Context.MODE_PRIVATE);
        typeOfMeasurement = sharedPreferencesCharts.getInt(APP_PREFERENCES_TYPE_CHART, 1);
        getBoundaryValues(showByTime());
        maxPower = getMaxPower();
        maxSpeed = getMaxSpeed();
        maxStrokeRate = getMaxStrokeRate();
        chartName = getRowerName();
    }

    private int getMaxPower() {
        return sqlStorage.getMaxPower();
    }

    private float getMaxSpeed() {
        return sqlStorage.getMaxSpeed();
    }

    private float getMaxTime() {
        return  sqlStorage.getMaxTime();
    }

    private float getMinTime() {
        return sqlStorage.getMinTime();
    }

    private float getMaxDistance() {
        return sqlStorage.getMaxDistance();
    }

    private float getMaxStrokeRate() {
        return sqlStorage.getMaxStrokeRate();
    }

    public ArrayList<String> getRowerName() {
        return sqlStorage.getRowerName();
    }

    public float getMinValueForCharts() {
        return minValueForCharts;
    }

    public float getMaxValueForCharts() {
        return  maxValueForCharts;
    }

    public float getAverageTime(int rower, float startPeriod, float endPeriod) {
        return sqlStorage.getAverageTime(rower, startPeriod, endPeriod);
    }

    public float getAverageDistance(int rower, float startPeriod, float endPeriod) {
        return sqlStorage.getAverageDistance(rower, startPeriod, endPeriod);
    }

    public List<List<Entry>> getDataFotDrawing(String rowerName, int rowerPosition) {
      return sqlStorage.getDataFotDrawing(
              rowerName,
              rowerPosition,
              maxPower,
              typeOfMeasurement,
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

    public boolean showByTime() {
        return typeOfMeasurement == TIME;
    }

    public void setShowByTime(Boolean showByTime) {
        typeOfMeasurement = (showByTime) ? TIME : DISTANCE;
        sharedPreferencesCharts.edit().putInt(
                APP_PREFERENCES_TYPE_CHART,
                typeOfMeasurement
        ).apply();
        getBoundaryValues(showByTime);
    }

    private void getBoundaryValues(Boolean showByTime) {
        minValueForCharts = (showByTime) ? getMinTime() : 0;
        maxValueForCharts = (showByTime) ? getMaxTime() : getMaxDistance();
    }

    public ArrayList<String> getChartName() {
        return chartName;
    }

    public int getChartNameSize() {
        return chartName.size();
    }

    @Override
    public void onCleared(){
        super.onCleared();
        sqlStorage.closeStorage();
    }
}
