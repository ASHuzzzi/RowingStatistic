package ru.lizzzi.rowingstatistic;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import ru.lizzzi.rowingstatistic.db.data.RowerDBHelper;

public class MainActivity extends AppCompatActivity {

    //файл с полями для запоминания последней открытой папки
    public static final String APP_PREFERENCES = "lastdir";
    public static final String APP_PREFERENCES_COUNTER = "counter";
    private SharedPreferences sharedPreferences;

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

    //переменные для записи в БД
    private int id;
    private double distance ;
    private long time;
    private double speed;
    private int stroke_rate;
    private int power;

    private int fileNumber = 0; //переменная для подсчета кол-ва открытых файлов

    public static final int NUMBER_OF_REQUEST = 23401;
    private RowerDBHelper mDBHelper;

    private Button buttonDistance;
    private Button buttonTime;
    private String loadedFile;


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
                if (fileNumber < 8) { //проверка на макисмальное количество файлов
                    final OpenFileDialog fileDialog = new OpenFileDialog(v.getContext())
                            .setFilter(".*\\.csv")
                            .setOpenDialogListener(new OpenFileDialog.OpenDialogListener() {
                                @Override
                                public void OnSelectedFile(String fileName, String file) {
                                    if (!fileName.equals(loadedFile)) {
                                        loadedFile = fileName;
                                        LoadData loadData = new LoadData();
                                        loadData.execute(fileName);
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
        sharedPreferences = this.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(APP_PREFERENCES_COUNTER, "0").apply();
    }

    public void ShowChartClick(View view){ //собираем данные, готовим их для построения графиоков
        if (fileNumber > 0) {
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
                    fileNumber).apply();

            //считываем названия графиков
            boolean chartsHaveName = false;
            if (fileNumber > 0) {
                chartsHaveName = saveChartName(APP_PREFERENCES_CHART_NAME1, chartName1);
            }
            if (fileNumber > 1) {
                chartsHaveName = saveChartName(APP_PREFERENCES_CHART_NAME2, chartName2);
            }
            if (fileNumber > 2) {
                chartsHaveName = saveChartName(APP_PREFERENCES_CHART_NAME3, chartName3);
            }
            if (fileNumber > 3) {
                chartsHaveName = saveChartName(APP_PREFERENCES_CHART_NAME4, chartName4);
            }
            if (fileNumber > 4){
                chartsHaveName = saveChartName(APP_PREFERENCES_CHART_NAME5, chartName5);
            }
            if (fileNumber > 5) {
                chartsHaveName = saveChartName(APP_PREFERENCES_CHART_NAME6, chartName6);
            }
            if (fileNumber > 6) {
                chartsHaveName = saveChartName(APP_PREFERENCES_CHART_NAME7, chartName7);
            }
            if (fileNumber > 7) {
                chartsHaveName = saveChartName(APP_PREFERENCES_CHART_NAME8, chartName8);
            }
            if (chartsHaveName) {
                getBoundaryValues(); //получаем границы для графиков
                Intent intent = new Intent(MainActivity.this, ChartActivity.class);
                startActivityForResult(intent, 1234);
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
        editor.putString(
                APP_PREFERENCES_CHART_SPEED,
                String.valueOf(mDBHelper.getMaxSpeed()));
        editor.putString(
                APP_PREFERENCES_CHART_STROKE_RATE,
                String.valueOf(mDBHelper.getMaxStrokeRate()));
        editor.putString(
                APP_PREFERENCES_CHART_POWER,
                String.valueOf(mDBHelper.getMaxPower()));
        editor.putString(
                APP_PREFERENCES_CHART_TIME_MAX,
                String.valueOf(mDBHelper.getMaxTime()));
        editor.putString(
                APP_PREFERENCES_CHART_TIME_MIN,
                String.valueOf(mDBHelper.getMinTime()));
        editor.putString(
                APP_PREFERENCES_CHART_DISTATNCE_MAX,
                String.valueOf(mDBHelper.getMaxDistance()));
        editor.putString(
                APP_PREFERENCES_CHART_DISTATNCE_MIN,
                String.valueOf(mDBHelper.getMinDistance()));
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

    class LoadData extends AsyncTask<String, Void, Void> {

        ProgressDialog mProgressDialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute(){
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("Загружаю. Подождите...");
            mProgressDialog.show();
        }


        @Override
        protected Void doInBackground(String... urls) {
            Parser parser = new Parser(
                    getApplicationContext(),
                    String.valueOf(urls[0]),
                    String.valueOf(fileNumber));
            parser.parseFile();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            final LinearLayout LL_Chart1 = findViewById(R.id.LL_Chart1);
            final LinearLayout LL_Chart2 = findViewById(R.id.LL_Chart2);
            final LinearLayout LL_Chart3 = findViewById(R.id.LL_Chart3);
            final LinearLayout LL_Chart4 = findViewById(R.id.LL_Chart4);
            final LinearLayout LL_Chart5 = findViewById(R.id.LL_Chart5);
            final LinearLayout LL_Chart6 = findViewById(R.id.LL_Chart6);
            final LinearLayout LL_Chart7 = findViewById(R.id.LL_Chart7);
            final LinearLayout LL_Chart8 = findViewById(R.id.LL_Chart8);

            fileNumber++;

            if (fileNumber >0){
                LL_Chart1.setVisibility(View.VISIBLE);
            }
            if (fileNumber >1){
                LL_Chart2.setVisibility(View.VISIBLE);
            }
            if (fileNumber >2){
                LL_Chart3.setVisibility(View.VISIBLE);
            }
            if (fileNumber >3){
                LL_Chart4.setVisibility(View.VISIBLE);
            }
            if (fileNumber >4){
                LL_Chart5.setVisibility(View.VISIBLE);
            }
            if (fileNumber >5){
                LL_Chart6.setVisibility(View.VISIBLE);
            }
            if (fileNumber >6){
                LL_Chart7.setVisibility(View.VISIBLE);
            }
            if (fileNumber >7){
                LL_Chart8.setVisibility(View.VISIBLE);
            }
            mProgressDialog.hide();
            toastShow("Файл загружен");
        }
    }

    private void toastShow(String textToShow) {
        Toast.makeText(getApplicationContext(), textToShow, Toast.LENGTH_LONG).show();
    }
}