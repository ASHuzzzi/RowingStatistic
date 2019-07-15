package ru.lizzzi.rowingstatistic;

import android.Manifest;
import android.app.ProgressDialog;
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
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ru.lizzzi.rowingstatistic.db.data.RowerDBHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Boolean> {

    //файл с полями для запоминания последней открытой папки
    public static final String APP_PREFERENCES = "lastdir";
    public static final String APP_PREFERENCES_COUNTER = "counter";

    //файл для сохранения настоек для построения графиков
    public static final String APP_PREFERENCES_Chart = "chart_settings";
    public static final String APP_PREFERENCES_TYPE_CHART = "type_chart";
    public static final String APP_PREFERENCES_BACK = "back";
    public static final String APP_PREFERENCES_CHART_NAME1= "chart1";
    public static final String APP_PREFERENCES_CHART_NAME2= "chart2";
    public static final String APP_PREFERENCES_CHART_NAME3= "chart3";
    public static final String APP_PREFERENCES_CHART_NAME4= "chart4";
    public static final String APP_PREFERENCES_CHART_NAME5= "chart5";
    public static final String APP_PREFERENCES_CHART_NAME6= "chart6";
    public static final String APP_PREFERENCES_CHART_NAME7= "chart7";
    public static final String APP_PREFERENCES_CHART_NAME8= "chart8";
    public static final String APP_PREFERENCES_CHART_STROKE_RATE= "strokerate";
    public static final String APP_PREFERENCES_CHART_SPEED= "speed";
    public static final String APP_PREFERENCES_CHART_POWER = "power";
    public static final String APP_PREFERENCES_CHART_TIME_MAX = "timemax";
    public static final String APP_PREFERENCES_CHART_TIME_MIN = "timemin";
    public static final String APP_PREFERENCES_CHART_DISTATNCE_MAX = "distatncemax";
    public static final String APP_PREFERENCES_CHART_DISTATNCE_MIN = "distatncemin";
    private SharedPreferences sharedPreferencesCharts;

    private int numberOfLoadedFiles = 0; //переменная для подсчета кол-ва открытых файлов

    public static final int NUMBER_OF_REQUEST = 23401;
    private RowerDBHelper mDBHelper;

    private Button buttonDistance;
    private Button buttonTime;
    private String loadedFile;
    private ProgressDialog mProgressDialog;
    private List<String> queueOnLoad = new ArrayList<>();
    private Loader fileLoader;


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
                if (numberOfLoadedFiles < 8) { //проверка на макисмальное количество файлов
                    final OpenFileDialog fileDialog = new OpenFileDialog(v.getContext())
                            .setFilter(".*\\.csv")
                            .setOpenDialogListener(new OpenFileDialog.OpenDialogListener() {
                                @Override
                                public void OnSelectedFile(String fileName, String file) {
                                    if (!fileName.equals(loadedFile)) {
                                        loadedFile = fileName;
                                        queueOnLoad.add(fileName);
                                        if (fileLoader == null || !fileLoader.isStarted()) {
                                            startFileLoaders();
                                        }
                                    }else {
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

        mProgressDialog = new ProgressDialog(MainActivity.this);
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
        mDBHelper = new RowerDBHelper(this);
        mDBHelper.clearDB();
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

    public void ShowChartClick(View view){ //собираем данные, готовим их для построения графиоков
        if (numberOfLoadedFiles > 0) {
            EditText chartName1 = findViewById(R.id.Chart1_New_Name);
            EditText chartName2 = findViewById(R.id.Chart2_New_Name);
            EditText chartName3 = findViewById(R.id.Chart3_New_Name);
            EditText chartName4 = findViewById(R.id.Chart4_New_Name);
            EditText chartName5 = findViewById(R.id.Chart5_New_Name);
            EditText chartName6 = findViewById(R.id.Chart6_New_Name);
            EditText chartName7 = findViewById(R.id.Chart7_New_Name);
            EditText chartName8 = findViewById(R.id.Chart8_New_Name);

            sharedPreferencesCharts.edit().putInt(
                    APP_PREFERENCES_BACK,
                    numberOfLoadedFiles).apply();

            //считываем названия графиков
            boolean chartsHaveName = false;
            if (numberOfLoadedFiles > 0) {
                chartsHaveName = saveChartName(APP_PREFERENCES_CHART_NAME1, chartName1);
            }
            if (numberOfLoadedFiles > 1) {
                chartsHaveName = saveChartName(APP_PREFERENCES_CHART_NAME2, chartName2);
            }
            if (numberOfLoadedFiles > 2) {
                chartsHaveName = saveChartName(APP_PREFERENCES_CHART_NAME3, chartName3);
            }
            if (numberOfLoadedFiles > 3) {
                chartsHaveName = saveChartName(APP_PREFERENCES_CHART_NAME4, chartName4);
            }
            if (numberOfLoadedFiles > 4){
                chartsHaveName = saveChartName(APP_PREFERENCES_CHART_NAME5, chartName5);
            }
            if (numberOfLoadedFiles > 5) {
                chartsHaveName = saveChartName(APP_PREFERENCES_CHART_NAME6, chartName6);
            }
            if (numberOfLoadedFiles > 6) {
                chartsHaveName = saveChartName(APP_PREFERENCES_CHART_NAME7, chartName7);
            }
            if (numberOfLoadedFiles > 7) {
                chartsHaveName = saveChartName(APP_PREFERENCES_CHART_NAME8, chartName8);
            }
            if (chartsHaveName) {
                getBoundaryValues(); //получаем границы для графиков
                Intent intent = new Intent(MainActivity.this, ChartActivity.class);
                startActivity(intent);
            } else {
                toastShow("Не у всех графиков есть название!");
            }
        } else {
            toastShow("Вы не загрузили ни один из файлов!");
        }
    }

    private boolean saveChartName(String preferencesKey, EditText editText) {
        if (editText.getText().length() != 0) {
            sharedPreferencesCharts.edit().putString(
                    preferencesKey,
                    editText.getText().toString()).apply();
            return true;
        }
        return false;
    }

    private void getBoundaryValues() { //получаем границы для графиков, max/min скорости и дистанции
        sharedPreferencesCharts = this.getSharedPreferences(APP_PREFERENCES_Chart, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferencesCharts.edit();
        editor.putFloat(
                APP_PREFERENCES_CHART_SPEED,
                mDBHelper.getMaxSpeed());
        editor.putFloat(
                APP_PREFERENCES_CHART_STROKE_RATE,
                mDBHelper.getMaxStrokeRate());
        editor.putInt(
                APP_PREFERENCES_CHART_POWER,
                mDBHelper.getMaxPower());
        editor.putFloat(
                APP_PREFERENCES_CHART_TIME_MAX,
                mDBHelper.getMaxTime());
        editor.putFloat(
                APP_PREFERENCES_CHART_TIME_MIN,
                mDBHelper.getMinTime());
        editor.putFloat(
                APP_PREFERENCES_CHART_DISTATNCE_MAX,
                mDBHelper.getMaxDistance());
        editor.putFloat(
                APP_PREFERENCES_CHART_DISTATNCE_MIN,
                mDBHelper.getMinDistance());
        editor.apply();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent datas){ //использем этот метод чтобы закрыть активность после возврата с предыдущей
        if (requestCode == 1234){
            finish();
        }
    }

    private void startFileLoaders() {
        /*mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("Загружаю. Подождите...");
        mProgressDialog.show();*/
        if (queueOnLoad.size() > 0) {
            //toastShow("Загружаю " + numberOfLoadedFiles );
            Log.d(
                    "LoadFile",
                    "Старт загрузчика для " + numberOfLoadedFiles + " спортсмена");
            Bundle bundle = new Bundle();
            bundle.putString("fileLocation", queueOnLoad.get(0));
            bundle.putString("fileNumber", String.valueOf(numberOfLoadedFiles));
            fileLoader = getSupportLoaderManager().initLoader(
                    numberOfLoadedFiles,
                    bundle,
                    this);
            fileLoader.forceLoad();
        }
    }

    @NonNull
    @Override
    public Loader<Boolean> onCreateLoader(int loaderId, Bundle bundle) {
        return new FileLoader(this, bundle);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Boolean> loader, Boolean aBoolean) {
        numberOfLoadedFiles++;
        final LinearLayout LL_Chart1 = findViewById(R.id.LL_Chart1);
        final LinearLayout LL_Chart2 = findViewById(R.id.LL_Chart2);
        final LinearLayout LL_Chart3 = findViewById(R.id.LL_Chart3);
        final LinearLayout LL_Chart4 = findViewById(R.id.LL_Chart4);
        final LinearLayout LL_Chart5 = findViewById(R.id.LL_Chart5);
        final LinearLayout LL_Chart6 = findViewById(R.id.LL_Chart6);
        final LinearLayout LL_Chart7 = findViewById(R.id.LL_Chart7);
        final LinearLayout LL_Chart8 = findViewById(R.id.LL_Chart8);

        if (numberOfLoadedFiles >0){
            LL_Chart1.setVisibility(View.VISIBLE);
        }
        if (numberOfLoadedFiles >1){
            LL_Chart2.setVisibility(View.VISIBLE);
        }
        if (numberOfLoadedFiles >2){
            LL_Chart3.setVisibility(View.VISIBLE);
        }
        if (numberOfLoadedFiles >3){
            LL_Chart4.setVisibility(View.VISIBLE);
        }
        if (numberOfLoadedFiles >4){
            LL_Chart5.setVisibility(View.VISIBLE);
        }
        if (numberOfLoadedFiles >5){
            LL_Chart6.setVisibility(View.VISIBLE);
        }
        if (numberOfLoadedFiles >6){
            LL_Chart7.setVisibility(View.VISIBLE);
        }
        if (numberOfLoadedFiles >7){
            LL_Chart8.setVisibility(View.VISIBLE);
        }
        mProgressDialog.hide();
        Log.d("LoadFile", "Стоп загрузчика для " + loader.getId() + " спортсмена");
        queueOnLoad.remove(0);
        startFileLoaders();
        toastShow("Файл загружен " + loader.getId() );
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Boolean> loader) {

    }

    private void toastShow(String textToShow) {
        Toast.makeText(getApplicationContext(), textToShow, Toast.LENGTH_LONG).show();
    }
}