package ru.lizzzi.rowingstatistic;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MyViewModel extends AndroidViewModel {
    private List<String> queueOnLoad = new ArrayList<>();
    private List<String> rowers = new ArrayList<>();
    private MutableLiveData<List<String>> data;
    private static final Executor executor = new ThreadPoolExecutor(0, 1, 60,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    private int countLoadedFiles;

    public MyViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<String>> getData(String fileLocation) {
        if (data == null) {
            data = new MutableLiveData<>();
            countLoadedFiles = 0;
        }
        queueOnLoad.add(fileLocation);
        startLoadFromQueue();
        return data;
    }

    private void startLoadFromQueue() {
        if (queueOnLoad.size() > 0) {
            loadData(queueOnLoad.get(queueOnLoad.size() - 1));
        }
    }

    private void loadData(final String fileLocation) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                FileParser fileParser = new FileParser(
                        getApplication(),
                        fileLocation,
                        countLoadedFiles);
                rowers.add(countLoadedFiles, fileParser.parseFile());
                data.postValue(rowers);
                countLoadedFiles++;
                if (queueOnLoad.size() > 0) {
                    queueOnLoad.remove(0);
                }
            }
        });
    }
}
