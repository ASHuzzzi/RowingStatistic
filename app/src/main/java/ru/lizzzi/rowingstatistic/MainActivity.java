package ru.lizzzi.rowingstatistic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;

import ru.lizzzi.rowingstatistic.db.data.RowerContract.RowerData;
import ru.lizzzi.rowingstatistic.db.data.RowerDBHelper;

public class MainActivity extends AppCompatActivity {

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
    private SharedPreferences mCharts;

    //переменные для записи в БД
    private int id;
    private double distance ;
    private long time;
    private double speed;
    private int stroke_rate;
    private int power;

    private int countOpenFiles = 0; //переменная для подсчета кол-ва открытых файлов
    //private Date dateStart;
    String chek_load_file_again; //переменная для проверки повторной загрузки файла
    String chek_load_file_again2;//переменная для проверки повторной загрузки файла
    private int timeF_distanceT =  1; //отображение по х времени иди дистаниции. 0 и 1 соотвественно
    int textsize = 12;

    public static final int NUMBER_OF_REQUEST = 23401;

    LoadData NewTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        //отчищаем БД от записей на случай некорректного закрытия приложения в прошлый раз
        RowerDBHelper mDBHelper = new RowerDBHelper(this);
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.delete(RowerData.TABLE_NAME, null, null);
        db.close();
        mDBHelper.close();

        Button time = (Button) findViewById(R.id.button9);
        Button distance = (Button) findViewById(R.id.button10);
        textsize = (int) (time.getTextSize() / Resources.getSystem().getDisplayMetrics().density);

        //показываем по какому значения будут построены графики
        mCharts = this.getSharedPreferences(APP_PREFERENCES_Chart, Context.MODE_PRIVATE);
        if (mCharts.getString(APP_PREFERENCES_TYPE_CHART, "").length()>0){
            timeF_distanceT = Integer.parseInt(mCharts.getString(APP_PREFERENCES_TYPE_CHART, ""));
        }
        if (timeF_distanceT == 0){
            ButtonSelect(time);
            ButtonNoNSelect(distance);
        }else {
            ButtonSelect(distance);
            ButtonNoNSelect(time);
        }

        //получаем доступ к чтению/записи фалов на андроиде с версии 6.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int canRead = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int canWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (canRead != PackageManager.PERMISSION_GRANTED || canWrite != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, NUMBER_OF_REQUEST);
                /*
                //Нужно ли нам показывать объяснения , зачем нам нужно это разрешение
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //показываем объяснение
                } else {
                    //просим разрешение
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, NUMBER_OF_REQUEST);
                }*/
            }
        }

    }

    /*
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case NUMBER_OF_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
            }
        }
    }*/

    @Override
    protected void onStart(){
        super.onStart();

    }

    @Override
    protected void onResume(){
        super.onResume();

    }


    @Override
    public void onDestroy(){
        super.onDestroy();

        //присваеваем 0 для сброса запомненой папки в классе OpenFileDialog
        SharedPreferences mSettings = this.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_COUNTER, "0");
        editor.apply();
    }

    public void OnOpenFileClick(View view) { //открытие файла

        //mCharts = this.getSharedPreferences(APP_PREFERENCES_Chart, Context.MODE_PRIVATE);

        if (countOpenFiles < 8){ //проверка на макисмальное количество файлов
            final OpenFileDialog fileDialog = new OpenFileDialog(this)
                    .setFilter()
                    .setOpenDialogListener(new OpenFileDialog.OpenDialogListener() {


                        @Override
                        public void OnSelectedFile(String fileName, String file) throws IOException, ParseException {
                            chek_load_file_again = fileName;
                            if (chek_load_file_again.equals(chek_load_file_again2)){
                                Toast.makeText(getApplicationContext(), "Вы выбрали уже загруженный файл", Toast.LENGTH_LONG).show();
                            }else {
                                chek_load_file_again2 = fileName;

                                NewTask = (LoadData) getLastNonConfigurationInstance();
                                if (NewTask == null){
                                    NewTask = new LoadData();
                                    NewTask.link(MainActivity.this);
                                    NewTask.execute(fileName);
                                }
                            }
                        }
                    });
            fileDialog.show();
        }else {
            Toast.makeText(getApplicationContext(), "Загружено максимальное количество файлов!", Toast.LENGTH_LONG).show();
        }
    }
    public void OnClick_Time(View view){ //меняет настройку отображения графика с дистанции на время
        mCharts = this.getSharedPreferences(APP_PREFERENCES_Chart, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mCharts.edit();
        editor.putString(APP_PREFERENCES_TYPE_CHART, "0");
        editor.apply();

        Button time = (Button) findViewById(R.id.button9);
        Button distance = (Button) findViewById(R.id.button10);
        ButtonSelect(time);
        ButtonNoNSelect(distance);
    }

    public void OnClick_Distance(View view){ //меняет настройку отображения графика с времени на дистанцию
        mCharts = this.getSharedPreferences(APP_PREFERENCES_Chart, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mCharts.edit();
        editor.putString(APP_PREFERENCES_TYPE_CHART, "1");
        editor.apply();

        Button time = (Button) findViewById(R.id.button9);
        Button distance = (Button) findViewById(R.id.button10);
        ButtonSelect(distance);
        ButtonNoNSelect(time);

    }

    public void ShowChartClick(View view){ //собираем данные, готовим их для построения графиоков

        if (countOpenFiles == 0){
            Toast.makeText(getApplicationContext(), "Вы не загрузили ни один из файлов!", Toast.LENGTH_LONG).show();
        }else {
            mCharts = this.getSharedPreferences(APP_PREFERENCES_Chart, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mCharts.edit();

            final EditText LL_Chart_new_name1 = (EditText) findViewById(R.id.Chart1_New_Name);
            final EditText LL_Chart_new_name2 = (EditText) findViewById(R.id.Chart2_New_Name);
            final EditText LL_Chart_new_name3 = (EditText) findViewById(R.id.Chart3_New_Name);
            final EditText LL_Chart_new_name4 = (EditText) findViewById(R.id.Chart4_New_Name);
            final EditText LL_Chart_new_name5 = (EditText) findViewById(R.id.Chart5_New_Name);
            final EditText LL_Chart_new_name6 = (EditText) findViewById(R.id.Chart6_New_Name);
            final EditText LL_Chart_new_name7 = (EditText) findViewById(R.id.Chart7_New_Name);
            final EditText LL_Chart_new_name8 = (EditText) findViewById(R.id.Chart8_New_Name);

            editor.putString(APP_PREFERENCES_BACK, String.valueOf(countOpenFiles));

            //считываем названия графиков
            int flag = 0;
            if (countOpenFiles > 0){
                if (LL_Chart_new_name1.getText().length() == 0){
                    flag = 1;
                }else{
                    editor.putString(APP_PREFERENCES_CHART_NAME1, String.valueOf(LL_Chart_new_name1.getText()));
                }
            }
            if (countOpenFiles > 1){
                if (LL_Chart_new_name2.getText().length() == 0){
                    flag = 1;
                }else{
                    editor.putString(APP_PREFERENCES_CHART_NAME2, String.valueOf(LL_Chart_new_name2.getText()));
                }
            }
            if (countOpenFiles > 2){
                if (LL_Chart_new_name3.getText().length() == 0){
                    flag = 1;
                }else{
                    editor.putString(APP_PREFERENCES_CHART_NAME3, String.valueOf(LL_Chart_new_name3.getText()));
                }
            }
            if (countOpenFiles > 3){
                if (LL_Chart_new_name4.getText().length() == 0){
                    flag = 1;
                }else{
                    editor.putString(APP_PREFERENCES_CHART_NAME4, String.valueOf(LL_Chart_new_name4.getText()));
                }
            }
            if (countOpenFiles > 4){
                if (LL_Chart_new_name5.getText().length() == 0){
                    flag = 1;
                }else{
                    editor.putString(APP_PREFERENCES_CHART_NAME5, String.valueOf(LL_Chart_new_name5.getText()));
                }
            }
            if (countOpenFiles > 5){
                if (LL_Chart_new_name6.getText().length() == 0){
                    flag = 1;
                }else{
                    editor.putString(APP_PREFERENCES_CHART_NAME6, String.valueOf(LL_Chart_new_name6.getText()));
                }
            }
            if (countOpenFiles > 6){
                if (LL_Chart_new_name7.getText().length() == 0){
                    flag = 1;
                }else{
                    editor.putString(APP_PREFERENCES_CHART_NAME7, String.valueOf(LL_Chart_new_name7.getText()));
                }
            }
            if (countOpenFiles > 7){
                if (LL_Chart_new_name8.getText().length() == 0){
                    flag = 1;
                }else{
                    editor.putString(APP_PREFERENCES_CHART_NAME8, String.valueOf(LL_Chart_new_name8.getText()));
                }
            }
            if (flag == 0){
                editor.apply();
                showDB(); //получаем границы для графиков


                Intent intent = new Intent(MainActivity.this, ChartActivity.class);
                startActivity(intent);

            }else {
                Toast.makeText(getApplicationContext(), "Не у всех графиков есть название!", Toast.LENGTH_LONG).show();
                editor.clear();
            }
        }
    }

    private void WriteDB(int id, double distatnce, long time, double speed, int strokerate, int power){ //пишем в БД

        RowerDBHelper mDBHelper = new RowerDBHelper(MainActivity.this);
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(RowerData.COLUMN_ROWER, id);
        values.put(RowerData.COLUMN_DISTANCE, distatnce);
        values.put(RowerData.COLUMN_TIME, time);
        values.put(RowerData.COLUMN_SPEED, speed);
        values.put(RowerData.COLUMN_STROKE_RATE, strokerate);
        values. put(RowerData.COLUMN_POWER, power);

        db.insert(RowerData.TABLE_NAME, null, values);
        db.close();

    }

    private void showDB() { //получаем границы для графиков, max/min скорости и дистанции

        mCharts = this.getSharedPreferences(APP_PREFERENCES_Chart, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mCharts.edit();

        RowerDBHelper mDBHelper = new RowerDBHelper(this);
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor;

        String[] max_speed_db = {
                "MAX(" + RowerData.COLUMN_SPEED + ")",
        };

        cursor = db.query(
                RowerData.TABLE_NAME,  // таблица
                max_speed_db,            // столбцы
                null,             // столбцы для условия WHERE
                null,         // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null                   // порядок сортировки
        );

        if (cursor != null) {
            cursor.moveToFirst();
            editor.putString(APP_PREFERENCES_CHART_SPEED, cursor.getString(0));
            cursor.close();
        }


        String[] max_stroke_rate_db = {
                "MAX(" + RowerData.COLUMN_STROKE_RATE + ")",
        };

        cursor = db.query(
                RowerData.TABLE_NAME,  // таблица
                max_stroke_rate_db,            // столбцы
                null,             // столбцы для условия WHERE
                null,         // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null                   // порядок сортировки
        );

        if (cursor != null) {
            cursor.moveToFirst();
            editor.putString(APP_PREFERENCES_CHART_STROKE_RATE, cursor.getString(0));
            cursor.close();
        }


        String[] max_power_db = {
                "MAX(" + RowerData.COLUMN_POWER + ")",
        };

        cursor = db.query(
                RowerData.TABLE_NAME,  // таблица
                max_power_db,            // столбцы
                null,             // столбцы для условия WHERE
                null,         // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null                   // порядок сортировки
        );

        if (cursor != null) {
            cursor.moveToFirst();
            editor.putString(APP_PREFERENCES_CHART_POWER, cursor.getString(0));
            cursor.close();
        }


        String[] max_time_db = {
                "MAX(" + RowerData.COLUMN_TIME + ")",
        };

        cursor = db.query(
                RowerData.TABLE_NAME,  // таблица
                max_time_db,            // столбцы
                null,             // столбцы для условия WHERE
                null,         // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null                   // порядок сортировки
        );

        if (cursor != null) {
            cursor.moveToFirst();
            editor.putString(APP_PREFERENCES_CHART_TIME_MAX, cursor.getString(0));
            cursor.close();
        }


        String[] min_time_db = {
                "MIN(" + RowerData.COLUMN_TIME + ")",
        };

        cursor = db.query(
                RowerData.TABLE_NAME,  // таблица
                min_time_db,            // столбцы
                null,             // столбцы для условия WHERE
                null,         // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null                   // порядок сортировки
        );

        if (cursor != null) {
            cursor.moveToFirst();
            editor.putString(APP_PREFERENCES_CHART_TIME_MIN, cursor.getString(0));
            cursor.close();

        }

        String[] max_distatnce_db = {
                "MAX(" + RowerData.COLUMN_DISTANCE + ")",
        };

        cursor = db.query(
                RowerData.TABLE_NAME,  // таблица
                max_distatnce_db,            // столбцы
                null,             // столбцы для условия WHERE
                null,         // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null                   // порядок сортировки
        );

        if (cursor != null) {
            cursor.moveToFirst();
            editor.putString(APP_PREFERENCES_CHART_DISTATNCE_MAX, cursor.getString(0));
            cursor.close();
        }


        String[] min_distatnce_db = {
                "MIN(" + RowerData.COLUMN_DISTANCE + ")",
        };

        cursor = db.query(
                RowerData.TABLE_NAME,  // таблица
                min_distatnce_db,            // столбцы
                null,             // столбцы для условия WHERE
                null,         // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null                   // порядок сортировки
        );

        if (cursor != null) {
            cursor.moveToFirst();
            editor.putString(APP_PREFERENCES_CHART_DISTATNCE_MIN, cursor.getString(0));
            cursor.close();
        }

        editor.apply();
    }

    private void ButtonSelect(Button button){ //метод выбора кнопки
        button.setTextColor(getResources().getColor(R.color.colorPrimary));
        button.setTypeface(null, Typeface.BOLD);
        button.setTextSize(textsize + 2);
    }

    private void ButtonNoNSelect(Button button){ //метод снятия выбора с кнопки
        button.setTextColor(getResources().getColor(R.color.colorBlack));
        button.setTypeface(null, Typeface.NORMAL);
        button.setTextSize(textsize);
    }

    static class LoadData extends AsyncTask<String, Void, Void> {

        @SuppressLint("StaticFieldLeak")
        MainActivity activity;
        ProgressDialog mProgressDialog;

        // получаем ссылку на MainActivity
        void link(MainActivity act) {
            activity = act;
            mProgressDialog = new ProgressDialog(activity);
        }



        @Override
        protected void onPreExecute(){
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage(activity.getString(R.string.load_data));
            mProgressDialog.show();
        }


        @Override
        protected Void doInBackground(String... urls) {

            BufferedReader reader = null;
            String filName = String.valueOf(urls[0]) ;
            try {
                reader = new BufferedReader(new FileReader(filName));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            // считываем построчно
            String line;
            String timeStart = null;
            Scanner scanner;
            boolean flag = false;
            int index = 0;
            int row = 0;
            try {
                assert reader != null;
                while ((line = reader.readLine()) != null) {
                    scanner = new Scanner(line);
                    scanner.useDelimiter(",");
                    while (scanner.hasNext()) {
                        String data = scanner.next();
                        activity.id = activity.countOpenFiles;
                        if (row == 3){ //в этой строке берем время начала тренировки
                            if (index == 1){
                                activity.distance = 0;
                                             /*
                                            Берем время начала тренировки. Ниже берем именно время, отрезая дату
                                             */
                                timeStart = (data.substring(9));
                                @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                                timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                                Date dateStart = timeFormat.parse(timeStart);
                                activity.time = dateStart.getTime();
                                activity.speed = 0;
                                activity.stroke_rate = 0;
                                activity.power = 0;
                            }
                        }
                        if (row > 29){ //начало массива данных тренировки
                            if (index == 1){
                                if (!"---".equals(data)){
                                    activity.distance = Double.parseDouble(data);
                                }else {
                                    flag = true;
                                }
                            }else if (index == 3){
                                if (!"---".equals(data)){
                                    @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormat1 = new SimpleDateFormat("HH:mm");
                                    @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                                    timeFormat1.setTimeZone(TimeZone.getTimeZone("UTC"));
                                    timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                                    Date date1 = timeFormat1.parse(timeStart);
                                    Date date2 = timeFormat.parse(data);
                                    activity.time = date1.getTime() + date2.getTime();
                                }else {
                                    flag = true;
                                }

                            }else if (index == 5){
                                if (!"---".equals(data)){
                                    activity.speed = Double.parseDouble(data);
                                }else {
                                    flag = true;
                                }
                            }else if (index == 8){
                                if (!"---".equals(data)){
                                    activity.stroke_rate = Integer.parseInt(data);
                                }else {
                                    flag = true;
                                }
                            }else  if (index == 13){
                                if (!"---".equals(data)){
                                    activity.power = Integer.parseInt(data);
                                }else {
                                    flag = true;
                                }
                            }

                                        /*
                                        Загружаем массив данных, на случай чего его можно расширить, дописав необходыме индексы
                                        в этот метод, а так же дополнив БД полями
                                         */
                        }
                        index++;
                    }

                    index = 0;
                    if (row == 3 || (row > 29 & !flag )){
                        activity.WriteDB(activity.id, activity.distance, activity.time, activity.speed, activity.stroke_rate, activity.power); //пишем в БД строку
                    }
                    row++;
                    flag = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }catch (NullPointerException e){
                e.printStackTrace();
                Toast.makeText(activity.getApplicationContext(), R.string.empty_file, Toast.LENGTH_LONG).show();
            }

            //закрываем наш ридер
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            final LinearLayout LL_Chart1 = (LinearLayout)activity.findViewById(R.id.LL_Chart1);
            final LinearLayout LL_Chart2 = (LinearLayout)activity.findViewById(R.id.LL_Chart2);
            final LinearLayout LL_Chart3 = (LinearLayout)activity.findViewById(R.id.LL_Chart3);
            final LinearLayout LL_Chart4 = (LinearLayout)activity.findViewById(R.id.LL_Chart4);
            final LinearLayout LL_Chart5 = (LinearLayout)activity.findViewById(R.id.LL_Chart5);
            final LinearLayout LL_Chart6 = (LinearLayout)activity.findViewById(R.id.LL_Chart6);
            final LinearLayout LL_Chart7 = (LinearLayout)activity.findViewById(R.id.LL_Chart7);
            final LinearLayout LL_Chart8 = (LinearLayout)activity.findViewById(R.id.LL_Chart8);

            activity.countOpenFiles++;

            if (activity.countOpenFiles >0){
                LL_Chart1.setVisibility(View.VISIBLE);
            }
            if (activity.countOpenFiles >1){
                LL_Chart2.setVisibility(View.VISIBLE);
            }
            if (activity.countOpenFiles >2){
                LL_Chart3.setVisibility(View.VISIBLE);
            }
            if (activity.countOpenFiles >3){
                LL_Chart4.setVisibility(View.VISIBLE);
            }
            if (activity.countOpenFiles >4){
                LL_Chart5.setVisibility(View.VISIBLE);
            }
            if (activity.countOpenFiles >5){
                LL_Chart6.setVisibility(View.VISIBLE);
            }
            if (activity.countOpenFiles >6){
                LL_Chart7.setVisibility(View.VISIBLE);
            }
            if (activity.countOpenFiles >7){
                LL_Chart8.setVisibility(View.VISIBLE);
            }
            mProgressDialog.hide();
            Toast.makeText(activity.getApplicationContext(), "Файл загружен", Toast.LENGTH_LONG).show();
            activity = null; // обнуляем ссылку
        }
    }
}