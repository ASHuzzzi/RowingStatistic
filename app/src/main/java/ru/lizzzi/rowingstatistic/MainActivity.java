package ru.lizzzi.rowingstatistic;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ru.lizzzi.rowingstatistic.db.data.RowerDBHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    //файл с полями для запоминания последней открытой папки
    public static final String APP_PREFERENCES = "lastdir";
    public static final String APP_PREFERENCES_COUNTER = "counter";

    //файл для сохранения настоек для построения графиков
    public static final String APP_PREFERENCES_Chart = "chart_settings";
    public static final String APP_PREFERENCES_TYPE_CHART = "type_chart";
    private SharedPreferences sharedPreferencesCharts;

    public static final int NUMBER_OF_REQUEST = 23401;

    private Button buttonDistance;
    private Button buttonTime;
    private String loadedFile;
    private List<String> queueOnLoad = new ArrayList<>();
    private RecyclerView recyclerView;
    private Adapter adapter;
    private List<String> rowers = new ArrayList<>();
    private int MAX_OPEN_FILE = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        buttonTime = findViewById(R.id.buttonTime);
        buttonTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferencesCharts.edit().putInt(APP_PREFERENCES_TYPE_CHART, 0).apply();
                ButtonSelect(buttonTime);
                ButtonNoNSelect(buttonDistance);
            }
        });
        buttonDistance = findViewById(R.id.buttonDistance);
        buttonDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferencesCharts.edit().putInt(APP_PREFERENCES_TYPE_CHART, 1).apply();
                ButtonSelect(buttonDistance);
                ButtonNoNSelect(buttonTime);
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
                                    if (!fileName.equals(loadedFile)) {
                                        loadedFile = fileName;
                                        queueOnLoad.add(fileName);
                                        rowers.add("Load");
                                        if (queueOnLoad.size() == 1) {
                                            adapter = new Adapter(getApplicationContext(), rowers);
                                            LinearLayoutManager layoutManager =
                                                    new LinearLayoutManager(getApplicationContext());
                                            recyclerView.setLayoutManager(layoutManager);
                                            recyclerView.setAdapter(adapter);
                                            startFileLoaders();
                                        } else {
                                            adapter.notifyDataSetChanged();
                                        }
                                    } else {
                                        toastShow("Вы выбрали уже загруженный файл");
                                    }
                                }
                            });
                    fileDialog.show();
                } else {
                    toastShow("Загружено максимальное количество файлов!");
                }
            }
        });

        Button buttonShowChart = findViewById(R.id.button11);
        buttonShowChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rowers.size() > 0) {
                    Intent intent = new Intent(MainActivity.this, ChartActivity.class);
                    startActivity(intent);
                } else {
                    toastShow("Вы не загрузили ни один из файлов!");
                }
            }
        });
        recyclerView = findViewById(R.id.recyclerView);
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
    protected void onStart(){
        super.onStart();
        //отчищаем БД от записей на случай некорректного закрытия приложения в прошлый раз
        RowerDBHelper rowerDBHelper = new RowerDBHelper(this);
        rowerDBHelper.clearDB();
        //показываем по какому значения будут построены графики
        sharedPreferencesCharts =
                this.getSharedPreferences(APP_PREFERENCES_Chart, Context.MODE_PRIVATE);
        int typeChart = sharedPreferencesCharts.getInt(APP_PREFERENCES_TYPE_CHART, 1);
        int TIME = 0;
        if (typeChart == TIME) {
            ButtonSelect(buttonTime);
            ButtonNoNSelect(buttonDistance);
        } else {
            ButtonSelect(buttonDistance);
            ButtonNoNSelect(buttonTime);
        }
    }

    @Override
    protected void onResume(){
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
        super.onResume();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //присваеваем 0 для сброса запомненой папки в классе OpenFileDialog
        SharedPreferences sharedPreferences =
                this.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt(APP_PREFERENCES_COUNTER, 0).apply();
    }

    private void ButtonSelect(Button button){ //метод выбора кнопки
        button.setTextColor(getResources().getColor(R.color.colorPrimary));
        button.setTypeface(null, Typeface.BOLD);
    }

    private void ButtonNoNSelect(Button button){ //метод снятия выбора с кнопки
        button.setTextColor(getResources().getColor(R.color.colorBlack));
        button.setTypeface(null, Typeface.NORMAL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){ //использем этот метод чтобы закрыть активность после возврата с предыдущей
        if (requestCode == 1234){
            finish();
        }
    }

    private void startFileLoaders() {
        if (queueOnLoad.size() > 0) {
            Log.d(
                    "LoadFile",
                    "Старт загрузчика для " + rowers.size() + " спортсмена");
            Bundle bundle = new Bundle();
            int loaderID = rowers.size()-1;
            bundle.putString("fileLocation", queueOnLoad.get(0));
            bundle.putString("fileNumber", String.valueOf(queueOnLoad.size()));
            Loader fileLoader = getSupportLoaderManager().initLoader(
                    loaderID,
                    bundle,
                    this);
            fileLoader.forceLoad();
        }
    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int loaderId, Bundle bundle) {
        return new FileLoader(this, bundle);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String chartName) {
        rowers.set(loader.getId(), chartName);
        adapter.notifyDataSetChanged();
        Log.d("LoadFile", "Стоп загрузчика для " + loader.getId() + " спортсмена");
        queueOnLoad.remove(0);
        startFileLoaders();
        toastShow("Файл загружен " + loader.getId());
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }

    private void toastShow(String textToShow) {
        Toast.makeText(getApplicationContext(), textToShow, Toast.LENGTH_LONG).show();
    }
}