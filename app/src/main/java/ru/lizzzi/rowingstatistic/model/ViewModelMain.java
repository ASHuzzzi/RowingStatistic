package ru.lizzzi.rowingstatistic.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ru.lizzzi.rowingstatistic.data.FileParser;
import ru.lizzzi.rowingstatistic.data.SQLiteStorage;

public class ViewModelMain extends AndroidViewModel {
    private List<String> queueOnLoad = new ArrayList<>();
    private ArrayList<String> rowers = new ArrayList<>();
    private MutableLiveData<List<String>> liveData;
    private static final Executor executor = new ThreadPoolExecutor(0, 1, 60,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    private int numberOfLoadedFiles;
    private SQLiteStorage sqlStorage;

    //файл для сохранения настоек для построения графиков
    private static final String APP_PREFERENCES_Chart = "chart_settings";
    private static final String APP_PREFERENCES_TYPE_CHART = "type_chart";
    private SharedPreferences sharedPreferencesCharts;

    //файл с полями для запоминания последней открытой папки
    private static final String APP_PREFERENCES = "lastdir";
    private static final String APP_PREFERENCES_COUNTER = "counter";

    public ViewModelMain(@NonNull Application application) {
        super(application);
        sqlStorage = new SQLiteStorage(getApplication().getBaseContext());
        sharedPreferencesCharts = getApplication().getApplicationContext().getSharedPreferences(
                APP_PREFERENCES_Chart,
                Context.MODE_PRIVATE);
    }

    public LiveData<List<String>> addFileToDownload(String fileLocation) {
        if (liveData == null) {
            liveData = new MutableLiveData<>();
            numberOfLoadedFiles = 0;
        }
        queueOnLoad.add(fileLocation);
        startLoadFromQueue();
        return liveData;
    }

    private void startLoadFromQueue() {
        if (queueOnLoad.size() > 0) {
            parseDataIntoDB(queueOnLoad.get(queueOnLoad.size() - 1));
        }
    }

    private void parseDataIntoDB(final String fileLocation) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                FileParser fileParser = new FileParser(
                        getApplication().getBaseContext(),
                        fileLocation,
                        numberOfLoadedFiles);
                String rowerName = fileParser.parseFile();
                rowers.set(numberOfLoadedFiles, rowerName);
                liveData.postValue(rowers);
                numberOfLoadedFiles++;
                if (queueOnLoad.size() > 0) {
                    queueOnLoad.remove(0);
                }
            }
        });
    }

    public ArrayList<String> getRowers() {
        return rowers = sqlStorage.getRowerName();
    }

    public void clearStorage() {
        sqlStorage.clearDB();
    }

    public void renameRowers(String oldName, String newName) {
        sqlStorage.setNewRowerName(oldName, newName);
    }

    public boolean showByTime() {
        int typeOfMeasurement = sharedPreferencesCharts.getInt(APP_PREFERENCES_TYPE_CHART, 1);
        int TIME = 0;
        return typeOfMeasurement == TIME;
    }

    public void setShowByTime(Boolean showByTime) {
        if (showByTime) {
            sharedPreferencesCharts.edit().putInt(APP_PREFERENCES_TYPE_CHART, 0).apply();
        } else {
            sharedPreferencesCharts.edit().putInt(APP_PREFERENCES_TYPE_CHART, 1).apply();
        }
    }

    public void clearLastOpenDirectory() {
        //присваеваем 0 для сброса запомненой папки в классе OpenFileDialog
        SharedPreferences sharedPreferences =
                getApplication().getApplicationContext().getSharedPreferences(
                        APP_PREFERENCES,
                        Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt(APP_PREFERENCES_COUNTER, 0).apply();
    }

    public ArrayList<String> removeRowerFromStorage(String rowerName) {
        sqlStorage.deleteRower(rowerName);
        rowers.remove(rowerName);
        numberOfLoadedFiles--;
        return rowers;
    }

    public boolean checkFileLocation(String fileLocation) {
        return sqlStorage.checkFileLocation(fileLocation);
    }

    @Override
    public void onCleared(){
        super.onCleared();
        sqlStorage.closeStorage();
    }
}
