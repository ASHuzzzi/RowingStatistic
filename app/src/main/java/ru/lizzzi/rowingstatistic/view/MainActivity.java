package ru.lizzzi.rowingstatistic.view;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ru.lizzzi.rowingstatistic.Adapter;
import ru.lizzzi.rowingstatistic.OpenFileDialog;
import ru.lizzzi.rowingstatistic.R;
import ru.lizzzi.rowingstatistic.model.ViewModelMain;

public class MainActivity extends AppCompatActivity {

    public static final int NUMBER_OF_REQUEST = 23401;
    public final String FILE_IS_LOADING = "Load";

    private RadioButton radioButtonDistance;
    private RadioButton radioButtonTime;
    private String loadedFile;
    private RecyclerView recyclerView;
    private Adapter adapter;
    private ArrayList<String> rowers = new ArrayList<>();
    private int MAX_OPEN_FILE = 8;
    private ViewModelMain model;
    private LiveData<List<String>> liveData;

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if (rowers.size() > 0) {
            bundle.putStringArrayList("rowers", rowers);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        rowers = savedInstanceState.getStringArrayList("rowers");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        radioButtonTime = findViewById(R.id.radioTime);
        radioButtonTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.setShowByTime(true);
            }
        });
        radioButtonDistance = findViewById(R.id.radioDistance);
        radioButtonDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.setShowByTime(false);
            }
        });

        Button buttonFileExplorer = findViewById(R.id.buttonFileExplorer);
        buttonFileExplorer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (rowers.size() < MAX_OPEN_FILE) { //проверка на макисмальное количество файлов
                    final OpenFileDialog fileDialog = new OpenFileDialog(v.getContext())
                            .setFilter(".*\\.csv")
                            .setOpenDialogListener(new OpenFileDialog.OpenDialogListener() {
                                @Override
                                public void OnSelectedFile(String fileName, String file) {
                                    fileCheckForReload(fileName);
                                }
                            });
                    fileDialog.show();
                } else {
                    toastShow("Загружено максимальное количество файлов!");
                }
            }
        });

        Button buttonShowChart = findViewById(R.id.buttonShowChart);
        buttonShowChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rowers.size() > 0) {
                    if (rowers.get(rowers.size() - 1).contains(FILE_IS_LOADING)) {
                        toastShow("Дождитесь окончания загрузки файлов");
                    } else {
                        Intent intent = new Intent(
                                MainActivity.this,
                                ChartActivity.class);
                        startActivity(intent);
                    }
                } else {
                    toastShow("Вы не загрузили ни один из файлов!");
                }
            }
        });
        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        model = ViewModelProviders.of(this).get(ViewModelMain.class);
        model.clearStorage();
    }

    private void fileCheckForReload(String fileName) {
        if (!fileName.equals(loadedFile)) {
            getDataRowers(fileName);
        } else {
            toastShow("Вы выбрали уже загруженный файл");
        }
    }

    private void getDataRowers(String fileName) {
        loadedFile = fileName;
        rowers.add(FILE_IS_LOADING);
        liveData = model.addFileToDownload(fileName);
        if (rowers.size() == 1) {
            adapter = new Adapter(MainActivity.this, rowers);
            recyclerView.setAdapter(adapter);
            liveData.observe(MainActivity.this, new Observer<List<String>>() {
                @Override
                public void onChanged(@Nullable List<String> loadedData) {
                    if (loadedData != null) {
                        for (int i = 0; i < loadedData.size(); i++) {
                            rowers.set(i, loadedData.get(i));
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case NUMBER_OF_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
                return;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (model.showByTime()) {
            radioButtonTime.setChecked(true);
        } else {
            radioButtonDistance.setChecked(true);
        }
    }

    @Override
    protected void onResume() {
        //получаем доступ к чтению/записи фалов на андроиде с версии 6.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int canRead = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            int canWrite = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (canRead != PackageManager.PERMISSION_GRANTED
                    || canWrite != PackageManager.PERMISSION_GRANTED) {
                //Нужно ли нам показывать объяснения , зачем нам нужно это разрешение
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //просим разрешение
                    requestPermissions(new String[]{
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE},
                            NUMBER_OF_REQUEST);
                }
            }
        }
        rowers = model.getRowers();
        if (rowers.size() > 0 ) {
            adapter = new Adapter(MainActivity.this, rowers);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        model.clearLastOpenDirectory();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        //использем этот метод чтобы закрыть активность после возврата с предыдущей
        if (requestCode == 1234){
            finish();
        }
    }

    private void toastShow(String textToShow) {
        Toast.makeText(getApplicationContext(), textToShow, Toast.LENGTH_LONG).show();
    }

    public void renameChart(String oldName, String newName) {
        model.renameRowers(oldName, newName);
    }
}