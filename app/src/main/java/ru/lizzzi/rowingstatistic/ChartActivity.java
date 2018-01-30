package ru.lizzzi.rowingstatistic;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import ru.lizzzi.rowingstatistic.chart.ChartColor;
import ru.lizzzi.rowingstatistic.chart.HourAxisValueFormatter;
import ru.lizzzi.rowingstatistic.db.data.RowerContract.RowerData;
import ru.lizzzi.rowingstatistic.db.data.RowerDBHelper;



public class ChartActivity extends FragmentActivity implements OnChartValueSelectedListener {

    //файл с полями для запоминания последней открытой папки
    public static final String APP_PREFERENCES = "lastdir";
    public static final String APP_PREFERENCES_COUNTER = "counter";
    public static final String APP_PREFERENCES_DIR = "dir";
    private SharedPreferences mSettings;

    //файл для сохранения настоек для построения графиков
    public static final String APP_PREFERENCES_Chart = "chart_settings";
    public static final String APP_PREFERENCES_TYPE_CHART = "type_chart";
    public static final String APP_PREFERENCES_BACK = "back";
    public static final String APP_PREFERENCES_CHART_NAME1 = "chart1";
    public static final String APP_PREFERENCES_CHART_NAME2 = "chart2";
    public static final String APP_PREFERENCES_CHART_NAME3 = "chart3";
    public static final String APP_PREFERENCES_CHART_NAME4 = "chart4";
    public static final String APP_PREFERENCES_CHART_NAME5 = "chart5";
    public static final String APP_PREFERENCES_CHART_NAME6 = "chart6";
    public static final String APP_PREFERENCES_CHART_NAME7 = "chart7";
    public static final String APP_PREFERENCES_CHART_NAME8 = "chart8";
    public static final String APP_PREFERENCES_CHART_SPEED = "speed";
    public static final String APP_PREFERENCES_CHART_STROKE_RATE = "strokerate";
    public static final String APP_PREFERENCES_CHART_POWER = "power";
    public static final String APP_PREFERENCES_CHART_TIME_MAX = "timemax";
    public static final String APP_PREFERENCES_CHART_TIME_MIN = "timemin";
    public static final String APP_PREFERENCES_CHART_DISTATNCE_MAX = "distatncemax";
    private SharedPreferences mCharts;

    //графики и массивы для них
    private LineChart mChartDown;
    private LineChart mChartUp;
    ArrayList<ILineDataSet> dataSetsUp = new ArrayList<>();
    ArrayList<ILineDataSet> dataSetsDown = new ArrayList<>();

    private int countOpenFiles = 0;//переменная для подсчета кол-ва открытых файлов

    final Context context = this;

    private int timeF_distanceT = 1; //тображение по х времени иди дистаниции. 0 и 1 соотвественно
    int beginsample_up = 0; //ссылка в массиве на начало отрезка выборки
    int endsample_up = 0; // ссылка в массиве на конец отрезка выборки
    int beginsample_down = 0; //ссылка в массиве на начало отрезка выборки
    int endsample_down = 0; // ссылка в массиве на конец отрезка выборки
    float max_absolut = 0; //максимальное значение по у для графика скорость/динамика
    int max_power = 0; //максимальное значение по у для графика для гребцов
    float max_stroke_rate = 0;
    float max_time = 0;
    float min_time = 0;
    float max_distance = 0;
    float min_distance = 0;
    double tap_point = 0;
    int counter_peroid = 0;

    String mass_temp;

    float ram_begin = -1;
    float ram_end = 0;

    String[] chart_name_name = new String[8];

    double correct_distance = 7.1;
    double correct_time = 10.1;

    int textsize = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.chart_main);

        //получаем значения из файла chart_settings
        mCharts = this.getSharedPreferences(APP_PREFERENCES_Chart, Context.MODE_PRIVATE);
        countOpenFiles = Integer.parseInt(mCharts.getString(APP_PREFERENCES_BACK, ""));
        if (mCharts.getString(APP_PREFERENCES_TYPE_CHART, "").length() > 0) {
            timeF_distanceT = Integer.parseInt(mCharts.getString(APP_PREFERENCES_TYPE_CHART, ""));
        }
        max_power = Integer.parseInt(mCharts.getString(APP_PREFERENCES_CHART_POWER, ""));
        max_absolut = Float.parseFloat(mCharts.getString(APP_PREFERENCES_CHART_SPEED, ""));
        max_time = Float.parseFloat(mCharts.getString(APP_PREFERENCES_CHART_TIME_MAX, ""));
        min_time = Float.parseFloat(mCharts.getString(APP_PREFERENCES_CHART_TIME_MIN, ""));
        max_distance = Float.parseFloat(mCharts.getString(APP_PREFERENCES_CHART_DISTATNCE_MAX, ""));
        min_distance = 0;
        max_stroke_rate = Float.parseFloat(mCharts.getString(APP_PREFERENCES_CHART_STROKE_RATE, ""));

        //получаем названия графиков
        if (countOpenFiles > 0) {
            chart_name_name[0] = mCharts.getString(APP_PREFERENCES_CHART_NAME1, "");
        }
        if (countOpenFiles > 1) {
            chart_name_name[1] = mCharts.getString(APP_PREFERENCES_CHART_NAME2, "");
        }
        if (countOpenFiles > 2) {
            chart_name_name[2] = mCharts.getString(APP_PREFERENCES_CHART_NAME3, "");
        }
        if (countOpenFiles > 3) {
            chart_name_name[3] = mCharts.getString(APP_PREFERENCES_CHART_NAME4, "");
        }
        if (countOpenFiles > 4) {
            chart_name_name[4] = mCharts.getString(APP_PREFERENCES_CHART_NAME5, "");
        }
        if (countOpenFiles > 5) {
            chart_name_name[5] = mCharts.getString(APP_PREFERENCES_CHART_NAME6, "");
        }
        if (countOpenFiles > 6) {
            chart_name_name[6] = mCharts.getString(APP_PREFERENCES_CHART_NAME7, "");
        }
        if (countOpenFiles > 7) {
            chart_name_name[7] = mCharts.getString(APP_PREFERENCES_CHART_NAME8, "");
        }
        ChartShowALL();  //строим графики

        //показываем, выделением кнопок, по каким величинам построена ось абсцисс
        Button time = (Button) findViewById(R.id.button17);
        Button distance = (Button) findViewById(R.id.button16);
        textsize = (int) (time.getTextSize() / Resources.getSystem().getDisplayMetrics().density);
        if (timeF_distanceT == 0) {
            ButtonSelect(time);
            ButtonNoNSelect(distance);
        } else {
            ButtonSelect(distance);
            ButtonNoNSelect(time);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //дублируем удаление запомненной папки в File Dialog
        mSettings = this.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_COUNTER, "0");
        editor.apply();

        //отчищаем базу
        RowerDBHelper mDBHelper = new RowerDBHelper(this);
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.delete(RowerData.TABLE_NAME, null, null);
        db.close();
        mDBHelper.close();
    }


    public void BeginSampleClick(View view) { //метод выбора начало выборки
        /*
        алгоритм:
        проверяем что у нас по оси абсцисс - время или дистанция
        после проверка на превышение граничных условий
        если проверка прошла успешно, то строится график

        для времени и дистанции все кроме проверки граничных условий совпадает по алгоритму
         */

        if (tap_point >= 0){
            if (timeF_distanceT == 0) {
                if (tap_point < min_time || tap_point > max_time) {
                    Toast.makeText(getApplicationContext(), "Значение вне доступного периода", Toast.LENGTH_LONG).show();
                } else {
                    if (ram_end > 0 && ram_end < tap_point) {
                        Toast.makeText(getApplicationContext(), "Начало периода не может быть позже конца", Toast.LENGTH_LONG).show();
                    } else {
                        // граница строится как график
                        // заполняем массив для еще одного графика
                        ram_begin = (float) tap_point;
                        ArrayList<Entry> values_up = new ArrayList<>();
                        ArrayList<Entry> values_down = new ArrayList<>();
                        values_up.add(new Entry(ram_begin, 0));
                        values_up.add(new Entry(ram_begin, 1));

                        values_down.add(new Entry(ram_begin, 0));
                        values_down.add(new Entry(ram_begin, 1));
                        LineDataSet d_up = new LineDataSet(values_up, "Начало выборки");
                        LineDataSet d_down = new LineDataSet(values_down, "Начало выборки");


                        d_up.setLineWidth(3.5f);
                        d_up.setCircleRadius(1f);

                        int color = mColors2[0];
                        d_up.setColor(color);
                        d_up.setCircleColor(color);

                        d_down.setLineWidth(3.5f);
                        d_down.setCircleRadius(1f);

                        d_down.setColor(color);
                        d_down.setCircleColor(color);
                        if (beginsample_up == 0) {
                            dataSetsUp.add(d_up);
                            beginsample_up = dataSetsUp.size();
                            dataSetsDown.add(d_down);
                            beginsample_down = dataSetsDown.size();
                        } else {
                            dataSetsUp.set(beginsample_up - 1, d_up);
                            dataSetsDown.set(beginsample_down - 1, d_down);
                        }
                        ChartShowUp(dataSetsUp); //строим границе на верхнем графике
                        ChartShowDown(dataSetsDown); //строим границу на нижнем графике
                        if (ram_end != 0){
                            sample_show(); //метод будет выполнен если есть начало и конец периода.
                        }
                    }
                }

            } else {
                if (tap_point < min_distance || tap_point > max_distance) {
                    Toast.makeText(getApplicationContext(), "Значение вне доступного периода", Toast.LENGTH_LONG).show();
                } else {
                    if (ram_end > 0 && ram_end < tap_point) {
                        Toast.makeText(getApplicationContext(), "Начало периода не может быть позже конца", Toast.LENGTH_LONG).show();
                    } else {
                        //здесь все аналогично выше
                        ram_begin = (float) tap_point;

                        ArrayList<Entry> values_up = new ArrayList<>();
                        ArrayList<Entry> values_down = new ArrayList<>();
                        values_up.add(new Entry(ram_begin, 0));
                        values_up.add(new Entry(ram_begin, 1));

                        values_down.add(new Entry(ram_begin, 0));
                        values_down.add(new Entry(ram_begin, 1));
                        LineDataSet d_up = new LineDataSet(values_up, "Начало выборки");
                        LineDataSet d_down = new LineDataSet(values_down, "Начало выборки");

                        d_up.setLineWidth(3.5f);
                        d_up.setCircleRadius(1f);

                        int color = mColors2[0];
                        d_up.setColor(color);
                        d_up.setCircleColor(color);

                        d_down.setLineWidth(3.5f);
                        d_down.setCircleRadius(1f);

                        d_down.setColor(color);
                        d_down.setCircleColor(color);
                        if (beginsample_up == 0) {
                            dataSetsUp.add(d_up);
                            beginsample_up = dataSetsUp.size();
                            dataSetsDown.add(d_down);
                            beginsample_down = dataSetsDown.size();
                        } else {
                            dataSetsUp.set(beginsample_up - 1, d_up);
                            dataSetsDown.set(beginsample_down - 1, d_down);
                        }
                        ChartShowUp(dataSetsUp);
                        ChartShowDown(dataSetsDown);
                        if (ram_end != 0){
                            sample_show();
                        }
                    }
                }
            }
        }

        //tap_point = 0;

    }

    public void EndSampleClick(View view) { //метод выбора конца выборки
        /*
        алгоритм:
        проверяем что у нас по оси абсцисс - время или дистанция
        после проверка на превышение граничных условий
        если проверка прошла успешно, то строится график

        для времени и дистанции все кроме проверки граничных условий совпадает по алгоритму поэтому
        чтобы не плодить одинаковые строки комментов, здесь я их опущу.
        общие моменты описаны в строках 206-249
         */


        if (tap_point > 0){
            if (timeF_distanceT == 0) {
                if (tap_point < min_time || tap_point > (max_time + 10)) {
                    Toast.makeText(getApplicationContext(), "Значение вне доступного периода", Toast.LENGTH_LONG).show();
                } else {
                    if (ram_begin > 0 && tap_point < ram_begin) {
                        Toast.makeText(getApplicationContext(), "Конец периода не может быть меньше начала", Toast.LENGTH_LONG).show();
                    } else {
                        ram_end = (float) tap_point;

                        ArrayList<Entry> values_up = new ArrayList<>();
                        ArrayList<Entry> values_down = new ArrayList<>();
                        values_up.add(new Entry(ram_end, 0));
                        values_up.add(new Entry(ram_end, 1));

                        values_down.add(new Entry(ram_end, 0));
                        values_down.add(new Entry(ram_end, 1));
                        LineDataSet d_up = new LineDataSet(values_up, "Конец выборки");
                        LineDataSet d_down = new LineDataSet(values_down, "Конец выборки");

                        d_up.setLineWidth(3.5f);
                        d_up.setCircleRadius(1f);

                        int color = mColors2[1];
                        d_up.setColor(color);
                        d_up.setCircleColor(color);

                        d_down.setLineWidth(3.5f);
                        d_down.setCircleRadius(1f);

                        d_down.setColor(color);
                        d_down.setCircleColor(color);
                        if (endsample_up == 0) {
                            dataSetsUp.add(d_up);
                            endsample_up = dataSetsUp.size();
                            dataSetsDown.add(d_down);
                            endsample_down = dataSetsDown.size();
                        } else {
                            dataSetsUp.set(endsample_up - 1, d_up);
                            dataSetsDown.set(endsample_down - 1, d_down);
                        }
                        ChartShowUp(dataSetsUp);
                        ChartShowDown(dataSetsDown);
                        if (ram_begin != -1){
                            sample_show();
                        }
                    }
                }
            } else {
                if (tap_point < min_distance || tap_point > max_distance) {
                    Toast.makeText(getApplicationContext(), "Значение вне доступного периода", Toast.LENGTH_LONG).show();
                } else {
                    if (ram_begin > 0 && tap_point < ram_begin) {
                        Toast.makeText(getApplicationContext(), "Конец периода не может быть меньше начала", Toast.LENGTH_LONG).show();
                    } else {
                        ram_end = (float) tap_point;

                        ArrayList<Entry> values_up = new ArrayList<>();
                        ArrayList<Entry> values_down = new ArrayList<>();
                        values_up.add(new Entry(ram_end, 0));
                        values_up.add(new Entry(ram_end, 1));

                        values_down.add(new Entry(ram_end, 0));
                        values_down.add(new Entry(ram_end, 1));
                        LineDataSet d_up = new LineDataSet(values_up, "Конец выборки");
                        LineDataSet d_down = new LineDataSet(values_down, "Конец выборки");

                        d_up.setLineWidth(3.5f);
                        d_up.setCircleRadius(1f);

                        int color = mColors2[1];
                        d_up.setColor(color);
                        d_up.setCircleColor(color);

                        d_down.setLineWidth(3.5f);
                        d_down.setCircleRadius(1f);

                        d_down.setColor(color);
                        d_down.setCircleColor(color);
                        if (endsample_up == 0) {
                            dataSetsUp.add(d_up);
                            endsample_up = dataSetsUp.size();
                            dataSetsDown.add(d_down);
                            endsample_down = dataSetsDown.size();
                        } else {
                            dataSetsUp.set(endsample_up - 1, d_up);
                            dataSetsDown.set(endsample_down - 1, d_down);
                        }
                        ChartShowUp(dataSetsUp);
                        ChartShowDown(dataSetsDown);
                        if (ram_begin != -1){
                            sample_show();
                        }
                    }
                }
            }
        }
    }


    public void EditPeriodClick(View view) { //метод обработки выборки

        if (ram_begin > -1 && ram_end > 0) { //проверка на наличие начала и конца выборки
            final RowerDBHelper mDBHelper = new RowerDBHelper(this);

            //сначала создаем поле для ввода комментария
            LayoutInflater li = LayoutInflater.from(context);
            @SuppressLint("InflateParams") final View promptsView = li.inflate(R.layout.comment_for_result, null);

            //Создаем AlertDialog
            AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context);

            //Настраиваем prompt.xml для нашего AlertDialog:
            mDialogBuilder.setView(promptsView);

            //Настраиваем отображение поля для ввода текста в открытом диалоге:
            final EditText userInput = (EditText) promptsView.findViewById(R.id.editText);

            //Настраиваем сообщение в диалоговом окне:
            mDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("Обработать",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {



                                    SQLiteDatabase db = mDBHelper.getReadableDatabase();
                                    Cursor cursor;
                                    if (TextUtils.isEmpty(mass_temp)){
                                        mass_temp = (counter_peroid + 1) + ";";
                                    }else{
                                        mass_temp = mass_temp.concat((counter_peroid + 1) + ";");
                                    }


                                    //цикл для взятия среднего значения
                                    for (int z = 0; z < countOpenFiles; z++) { //для каждого пловца

                                        if (timeF_distanceT == 0) { //проверка по времени или дистанции делать запрос в БД
                                            //берем среднее значение по мощности...
                                            String[] projection = {
                                                    "AVG(" + RowerData.COLUMN_POWER + ")"
                                            };

                                            //для пловца № в промежутке выборки
                                            String selection = RowerData.COLUMN_ROWER + "=? AND " + RowerData.COLUMN_TIME
                                                    + " BETWEEN " + (ram_begin - 10) + " AND " + (ram_end + 10);
                                            String[] selectionArgs = {String.valueOf(z)};

                                            cursor = db.query(
                                                    RowerData.TABLE_NAME,  // таблица
                                                    projection,            // столбцы
                                                    selection,             // столбцы для условия WHERE
                                                    selectionArgs,         // значения для условия WHERE
                                                    null,                  // Don't group the rows
                                                    null,                  // Don't filter by row groups
                                                    null                   // порядок сортировки
                                            );

                                            if (cursor != null) {
                                                cursor.moveToFirst();
                                                //добавлем значение в строку
                                                mass_temp = mass_temp.concat(String.valueOf(cursor.getFloat(0)) + ";");
                                                cursor.close();
                                            }

                                        } else {
                                            //все аналогично выше, только выборка берется по дистанции
                                            String[] projection = {
                                                    "AVG(" + RowerData.COLUMN_POWER + ")"
                                            };

                                            /*ввиду того, что беру тип переменной float (а она имеет плохую точность)
                                            беру выборку с дипазоном +/-0.01
                                            Прим. у float 4,1 может быть 4,100000001.
                                            */
                                            String selection = RowerData.COLUMN_ROWER + "=? AND " + RowerData.COLUMN_DISTANCE
                                                    + " BETWEEN " + (ram_begin - 0.01) + " AND " + (ram_end + 0.01);
                                            String[] selectionArgs = {String.valueOf((z))};

                                            cursor = db.query(
                                                    RowerData.TABLE_NAME,  // таблица
                                                    projection,            // столбцы
                                                    selection,             // столбцы для условия WHERE
                                                    selectionArgs,         // значения для условия WHERE
                                                    null,                  // Don't group the rows
                                                    null,                  // Don't filter by row groups
                                                    null                   // порядок сортировки
                                            );

                                            if (cursor != null) {
                                                cursor.moveToFirst();
                                                mass_temp = mass_temp.concat(String.valueOf(cursor.getFloat(0)) + ";");
                                                cursor.close();
                                            }

                                        }
                                    }

                                    mass_temp = mass_temp + String.valueOf(userInput.getText()) + "\n"; //добавляем коммент к строке
                                    ram_begin = -1;
                                    ram_end = 0;
                                    counter_peroid++;
                                    Toast.makeText(getApplicationContext(), "Выборка добавлена", Toast.LENGTH_SHORT).show();
                                }
                            })
                    .setNegativeButton("Отмена",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    Toast.makeText(getApplicationContext(), "Выборка не добавлена!", Toast.LENGTH_SHORT).show();
                                }
                            });

            //Создаем AlertDialog:
            AlertDialog alertDialog = mDialogBuilder.create();

            //и отображаем его:
            alertDialog.show();
            Button nbutton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            nbutton.setTextColor(getResources().getColor(R.color.colorRed));
            //nbutton.setGravity(Gravity.LEFT);
            Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            pbutton.setTextColor(getResources().getColor(R.color.colorGreen));
        } else {
            Toast.makeText(getApplicationContext(), "Выберите границы периода", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onValueSelected(Entry e, Highlight h) {
        //служебный метод сгенерированный автоматически
    }

    @Override
    public void onNothingSelected() {
        //служебный метод сгенерированный автоматически
    }

    @Override
    public void onBackPressed() {
        openQuitDialog(); //запускает окно Quit Dialog когда нажата кнопка назад
    }

    private void openQuitDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                ChartActivity.this);
        quitDialog.setTitle("Закрыть приложение?");

        quitDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        quitDialog.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        quitDialog.show();
    }

    public void Whrite_file(View v) { //запись в файл

        if (mass_temp.length() > 1) { //проверяем чтобы строка была не пустая
            LayoutInflater li = LayoutInflater.from(context);
            @SuppressLint("InflateParams") final View promptsView = li.inflate(R.layout.result_filename, null);

            //Создаем AlertDialog
            AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context);

            //Настраиваем prompt.xml для нашего AlertDialog:
            mDialogBuilder.setView(promptsView);

            //Настраиваем отображение поля для ввода текста в открытом диалоге:
            final EditText userInput = (EditText) promptsView.findViewById(R.id.input_text);

            //Настраиваем сообщение в диалоговом окне:
            mDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("Записать",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    String firts_row = "Период;";

                                    if (countOpenFiles > 0) {
                                        firts_row = firts_row + mCharts.getString(APP_PREFERENCES_CHART_NAME1, "") + ";";
                                    }
                                    if (countOpenFiles > 1) {
                                        firts_row = firts_row + mCharts.getString(APP_PREFERENCES_CHART_NAME2, "") + ";";
                                    }
                                    if (countOpenFiles > 2) {
                                        firts_row = firts_row + mCharts.getString(APP_PREFERENCES_CHART_NAME3, "") + ";";
                                    }
                                    if (countOpenFiles > 3) {
                                        firts_row = firts_row + mCharts.getString(APP_PREFERENCES_CHART_NAME4, "") + ";";
                                    }
                                    if (countOpenFiles > 4) {
                                        firts_row = firts_row + mCharts.getString(APP_PREFERENCES_CHART_NAME5, "") + ";";
                                    }
                                    if (countOpenFiles > 5) {
                                        firts_row = firts_row + mCharts.getString(APP_PREFERENCES_CHART_NAME6, "") + ";";
                                    }
                                    if (countOpenFiles > 6) {
                                        firts_row = firts_row + mCharts.getString(APP_PREFERENCES_CHART_NAME7, "") + ";";
                                    }
                                    if (countOpenFiles > 7) {
                                        firts_row = firts_row + mCharts.getString(APP_PREFERENCES_CHART_NAME8, "") + ";";
                                    }
                                    firts_row = firts_row + "Комментарий" + "\n";

                                    mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
                                    String sdFile;
                                    if (userInput.length() > 0) {
                                        sdFile = String.valueOf(userInput.getText());
                                        sdFile = sdFile + ".csv";
                                    } else {
                                        Calendar c = Calendar.getInstance();
                                        int hour = c.get(Calendar.HOUR_OF_DAY);
                                        int minute = c.get(Calendar.MINUTE);
                                        int second = c.get(Calendar.SECOND);
                                        sdFile = "result_" + hour + "-" + minute + "-" + second + ".csv";
                                    }



                                    try{
                                        String currentPath = Environment.getExternalStorageDirectory().getPath();
                                        if (currentPath.equals(mSettings.getString(APP_PREFERENCES_DIR, ""))){
                                            throw new Exception();
                                        }

                                        //пытаемся сохраниться в папку с исходниками
                                        File myFile = new File(mSettings.getString(APP_PREFERENCES_DIR, "") + "/" + sdFile);
                                        myFile.createNewFile();                                         // Создается файл, если он не был создан
                                        FileOutputStream outputStream = new FileOutputStream(myFile);   // После чего создаем поток для записи                 // и производим непосредственно запись
                                        outputStream.write(firts_row.getBytes("Cp1251"));
                                        outputStream.write(mass_temp.getBytes("Cp1251"));
                                        outputStream.close();
                                        firts_row = "";
                                        mass_temp = "";
                                        Toast.makeText(getApplicationContext(), "Файл сохранен в папку с иcходными файлами!", Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        String state = Environment.getExternalStorageState();
                                        if (Environment.MEDIA_MOUNTED.equals(state)) //проверям доступность внешней памяти
                                        {
                                            try { //сохраняем во внешнюю память
                                                File sdDir = Environment.getExternalStorageDirectory();
                                                File myFile = new File(sdDir + "/" + sdFile);
                                                myFile.createNewFile();                                         // Создается файл, если он не был создан
                                                FileOutputStream outputStream = new FileOutputStream(myFile);   // После чего создаем поток для записи                 // и производим непосредственно запись
                                                outputStream.write(firts_row.getBytes("Cp1251"));
                                                outputStream.write(mass_temp.getBytes("Cp1251"));
                                                outputStream.close();
                                                mass_temp = "";
                                                Toast.makeText(getApplicationContext(), "Файл сохранен на внешнюю память!", Toast.LENGTH_SHORT).show();
                                            } catch (Exception e1) {
                                                e1.printStackTrace();
                                                Toast.makeText(getApplicationContext(), "Файл не сохранен!", Toast.LENGTH_LONG).show();
                                            }
                                        } else {
                                            try { //сохраняем во внутренню память
                                                File sdDir = context.getFilesDir();
                                                File myFile = new File(sdDir + "/" + sdFile);
                                                myFile.createNewFile();                                         // Создается файл, если он не был создан
                                                FileOutputStream outputStream = new FileOutputStream(myFile);   // После чего создаем поток для записи                 // и производим непосредственно запись
                                                outputStream.write(firts_row.getBytes("Cp1251"));
                                                outputStream.write(mass_temp.getBytes("Cp1251"));
                                                outputStream.close();
                                                mass_temp = "";
                                                Toast.makeText(getApplicationContext(), "Файл сохранен в папку приложения!", Toast.LENGTH_SHORT).show();
                                            } catch (Exception e2) {
                                                e2.printStackTrace();
                                                Toast.makeText(getApplicationContext(), "Файл не сохранен!", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }
                                }
                            })
                    .setNegativeButton("Отмена",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

            //Создаем AlertDialog:
            AlertDialog alertDialog = mDialogBuilder.create();

            //и отображаем его:
            alertDialog.show();
            Button nbutton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            nbutton.setTextColor(getResources().getColor(R.color.colorRed));
            Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            pbutton.setTextColor(getResources().getColor(R.color.colorGreen));
        } else {
            Toast.makeText(getApplicationContext(), "Нет данных для сохранения!", Toast.LENGTH_LONG).show();
        }
    }

    public void Сhange_time(View view) { //метод смены графика с дистанции на время

        mCharts = this.getSharedPreferences(APP_PREFERENCES_Chart, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mCharts.edit();
        editor.putString(APP_PREFERENCES_TYPE_CHART, "0");
        editor.apply();

        Button time = (Button) findViewById(R.id.button17);
        Button distance = (Button) findViewById(R.id.button16);
        ButtonNoNSelect(distance);
        ButtonSelect(time);


        dataSetsDown.clear();
        dataSetsUp.clear();
        beginsample_up = 0;
        beginsample_down = 0;
        endsample_up = 0;
        endsample_down = 0;
        timeF_distanceT = 0;
        ChartShowALL();
        if (ram_begin != -1) {
            tap_point = (float) Find_Time(ram_begin);
            ram_end = (float) Find_Time(ram_end);
            BeginSampleClick(view);
        }
        if (ram_end != 0) {
            tap_point = ram_end;
            EndSampleClick(view);
        }
        tap_point = 0;
    }

    public void Сhange_distance(View view) { //метод смены графика с времени на дистанцию

        mCharts = this.getSharedPreferences(APP_PREFERENCES_Chart, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mCharts.edit();
        editor.putString(APP_PREFERENCES_TYPE_CHART, "1");
        editor.apply();

        Button time = (Button) findViewById(R.id.button17);
        Button distance = (Button) findViewById(R.id.button16);
        ButtonNoNSelect(time);
        ButtonSelect(distance);

        dataSetsDown.clear();
        dataSetsUp.clear();
        beginsample_up = 0;
        beginsample_down = 0;
        endsample_up = 0;
        endsample_down = 0;
        timeF_distanceT = 1;
        ChartShowALL();
        if (ram_begin != -1) {
            tap_point = (float) Find_Distance(ram_begin);
            ram_end = (float) Find_Distance(ram_end);
            BeginSampleClick(view);
        }
        if (ram_end != 0) {
            tap_point = ram_end;
            EndSampleClick(view);
        }
        tap_point = 0;
    }


    public void syncCharts(LineChart chart1, LineChart chart2) { //синхронизируем графики
        Matrix srcMatrix;
        float[] srcVals = new float[9];
        Matrix dstMatrix;
        float[] dstVals = new float[9];

        srcMatrix = chart1.getViewPortHandler().getMatrixTouch();
        srcMatrix.getValues(srcVals);
        dstMatrix = chart2.getViewPortHandler().getMatrixTouch();
        dstMatrix.getValues(dstVals);
        dstVals[Matrix.MSCALE_X] = srcVals[Matrix.MSCALE_X];
        dstVals[Matrix.MTRANS_X] = srcVals[Matrix.MTRANS_X];
        dstMatrix.setValues(dstVals);
        chart2.getViewPortHandler().refresh(dstMatrix, chart2, true);
    }


    private void ChartShowALL() { //метод построения графиков
        for (int z = 0; z < countOpenFiles; z++) {

            ArrayList<Entry> rower_AL = new ArrayList<>();
            ArrayList<Entry> speed_AL = new ArrayList<>();
            ArrayList<Entry> stroke_rate_AL = new ArrayList<>();

            RowerDBHelper mDBHelper = new RowerDBHelper(this);
            SQLiteDatabase db = mDBHelper.getReadableDatabase();
            Cursor cursor;

            //Делаем запрос в БД для полуения данных
            String[] projection = {
                    RowerData.COLUMN_ROWER, RowerData.COLUMN_TIME, RowerData.COLUMN_POWER,
                    RowerData.COLUMN_SPEED, RowerData.COLUMN_STROKE_RATE, RowerData.COLUMN_DISTANCE
            };
            String selection = RowerData.COLUMN_ROWER + "=?";
            String[] selectionArgs = {String.valueOf(z)};

            cursor = db.query(
                    RowerData.TABLE_NAME,  // таблица
                    projection,            // столбцы
                    selection,             // столбцы для условия WHERE
                    selectionArgs,         // значения для условия WHERE
                    null,                  // Don't group the rows
                    null,                  // Don't filter by row groups
                    null                   // порядок сортировки
            );

            if (cursor != null && cursor.moveToFirst()) {
                do { //наполняем массивы для построения
                    long time = cursor.getLong(cursor.getColumnIndex(RowerData.COLUMN_TIME));
                    float distance = cursor.getFloat(cursor.getColumnIndex(RowerData.COLUMN_DISTANCE));
                    int power = cursor.getInt(cursor.getColumnIndex(RowerData.COLUMN_POWER));
                    float ppower = ((float) power / (float) max_power);
                    if (timeF_distanceT == 0) {
                        rower_AL.add(new com.github.mikephil.charting.data.Entry(time, ppower));
                    } else {
                        rower_AL.add(new com.github.mikephil.charting.data.Entry(distance, ppower));
                    }

                    if (z == 0) {
                        float speed = cursor.getFloat(cursor.getColumnIndex(RowerData.COLUMN_SPEED));
                        float sspeed = (speed / max_absolut);
                        int stroke_rate = cursor.getInt(cursor.getColumnIndex(RowerData.COLUMN_STROKE_RATE));
                        float sstroke_rate = ((float) stroke_rate / max_stroke_rate);
                        if (timeF_distanceT == 0) {
                            speed_AL.add(new com.github.mikephil.charting.data.Entry(time, sspeed));
                            stroke_rate_AL.add(new com.github.mikephil.charting.data.Entry(time, sstroke_rate));
                        } else {
                            speed_AL.add(new com.github.mikephil.charting.data.Entry(distance, sspeed));
                            stroke_rate_AL.add(new com.github.mikephil.charting.data.Entry(distance, sstroke_rate));
                        }

                    }

                } while (cursor.moveToNext());
                cursor.close();
            }

            db.close();

            LineDataSet d = new LineDataSet(rower_AL, chart_name_name[z]);

            d.setLineWidth(2.5f);
            d.setCircleRadius(6f);

            int color = mColors1[z % mColors1.length];
            d.setColor(color);
            d.setCircleColor(color);
            dataSetsDown.add(d);

            if (z == 0) { //цикл для построения верхних графиков
                LineDataSet d2 = new LineDataSet(speed_AL, "Скорость");
                d2.setLineWidth(2.5f);
                d2.setCircleRadius(6f);

                int color2 = mColors1[8];
                d2.setColor(color2);
                d2.setCircleColor(color2);
                dataSetsUp.add(d2);

                d2 = new LineDataSet(stroke_rate_AL, "Темп гребли");
                d2.setLineWidth(2.5f);
                d2.setCircleRadius(6f);

                color2 = mColors1[9];
                d2.setColor(color2);
                d2.setCircleColor(color2);
                dataSetsUp.add(d2);
            }
        }

        //строим графики
        ChartShowUp(dataSetsUp);
        ChartShowDown(dataSetsDown);
    }

    private void ChartShowUp(ArrayList<ILineDataSet> dataSetsDown) { //задаем настройки отображения и строим график
        mChartUp = (LineChart) findViewById(R.id.chartUp);

        mChartUp.setDrawGridBackground(false);
        mChartUp.getDescription().setEnabled(false);
        mChartUp.setDrawBorders(true);

        mChartUp.getAxisLeft().setEnabled(true);
        mChartUp.getAxisRight().setDrawAxisLine(false);
        mChartUp.getAxisRight().setDrawGridLines(false);
        //mChartUp.getXAxis().setEnabled(false);
        //mChartUp.getXAxis().setDrawAxisLine(false);
        //mChartUp.getXAxis().setDrawGridLines(false);
        if (timeF_distanceT == 0) {
            mChartUp.getXAxis().setValueFormatter( new HourAxisValueFormatter(0));
        } else {
            mChartUp.getXAxis().setValueFormatter( new DefaultAxisValueFormatter(0));
        }

        mChartUp.setOnChartValueSelectedListener(new OnChartValueSelectedListener() { //слушатель клика по графку
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                tap_point = e.getX();
                sample_show();
            }

            @Override
            public void onNothingSelected() {

            }
        });


        mChartUp.setOnChartGestureListener(new OnChartGestureListener() {

            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                syncCharts(mChartUp, mChartDown);
            }

            @Override
            public void onChartLongPressed(MotionEvent me) {

            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {

            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {

            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
                syncCharts(mChartUp, mChartDown);

            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {
                syncCharts(mChartUp, mChartDown);
            }
        });

        mChartUp.setPinchZoom(false);
        mChartUp.setScaleYEnabled(false);


        Legend l1 = mChartUp.getLegend();
        l1.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l1.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l1.setOrientation(Legend.LegendOrientation.VERTICAL);
        l1.setDrawInside(true);

        mChartUp.resetTracking();

        LineData data = new LineData(dataSetsDown);
        data.setDrawValues(false);
        mChartUp.setData(data);
        mChartUp.invalidate();
    }

    private void ChartShowDown(final ArrayList<ILineDataSet> dataSetsUp) { //задаем настройки отображения и строим график
        mChartDown = (LineChart) findViewById(R.id.chartDown);

        mChartDown.setDrawGridBackground(false);
        mChartDown.getDescription().setEnabled(false);
        mChartDown.setDrawBorders(true);

        mChartDown.getAxisLeft().setEnabled(true);
        mChartDown.getAxisRight().setDrawAxisLine(false);
        mChartDown.getAxisRight().setDrawGridLines(false);
        //mChartDown.getXAxis().setDrawAxisLine(false);
        //mChartDown.getXAxis().setDrawGridLines(false);
        if (timeF_distanceT == 0) {
            mChartDown.getXAxis().setValueFormatter(new HourAxisValueFormatter(0));
        } else {
            mChartDown.getXAxis().setValueFormatter(new DefaultAxisValueFormatter(0));
        }

        mChartDown.setOnChartValueSelectedListener(new OnChartValueSelectedListener() { //слушатель клика по графку

            @Override
            public void onValueSelected(Entry e, Highlight h) {
                tap_point = e.getX();
                sample_show();
            }

            @Override
            public void onNothingSelected() {

            }
        });

        mChartDown.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartLongPressed(MotionEvent me) {

            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {

            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {

            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
                syncCharts(mChartDown, mChartUp);

            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {
                syncCharts(mChartDown, mChartUp);
            }
        });

        mChartDown.setPinchZoom(false);
        mChartDown.setScaleYEnabled(false);


        Legend l2 = mChartDown.getLegend();
        l2.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l2.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l2.setOrientation(Legend.LegendOrientation.VERTICAL);
        l2.setDrawInside(true);

        mChartDown.resetTracking();


        LineData data2 = new LineData(dataSetsUp);
        data2.setDrawValues(false);
        mChartDown.setData(data2);
        mChartDown.invalidate();
    }

    private int[] mColors1 = new int[]{ //цвета для графиков
            ChartColor.CHARTLINE_COLORS[0],
            ChartColor.CHARTLINE_COLORS[1],
            ChartColor.CHARTLINE_COLORS[2],
            ChartColor.CHARTLINE_COLORS[3],
            ChartColor.CHARTLINE_COLORS[4],
            ChartColor.CHARTLINE_COLORS[5],
            ChartColor.CHARTLINE_COLORS[6],
            ChartColor.CHARTLINE_COLORS[7],
            ChartColor.CHARTLINE_COLORS[8],
            ChartColor.CHARTLINE_COLORS[9]
    };

    private int[] mColors2 = new int[]{
            ChartColor.CHARTBORDER_COLORS[0],
            ChartColor.CHARTBORDER_COLORS[1]
    };

    private void sample_show() { //метод для отображения времени и дистанции по клику или при фиксировании выборки

        TextView timesample = (TextView) findViewById(R.id.timesample);
        TextView distancesample = (TextView) findViewById(R.id.distancesample);

        if (ram_begin != -1 && ram_end != 0) { //если зафиксирована выборка
            double start;
            double end;
            if (timeF_distanceT == 0) {
                start = Find_Distance(ram_begin);
                end = Find_Distance(ram_end);
                correct_distance = end - start;

                start = Find_Time(start);
                end = Find_Time(end);
                correct_time = end - start;

            } else {
                start = Find_Time(ram_begin);
                end = Find_Time(ram_end);
                correct_time = end - start;
                correct_distance = ram_end - ram_begin;
            }

        } else { //если выборки нет, показываем значения по тапу на графике
            if (tap_point > 0) {
                if (timeF_distanceT == 0) {
                    correct_distance = Find_Distance(tap_point);
                    correct_time = Find_Time(correct_distance);
                } else {
                    correct_time = Find_Time(tap_point);
                    correct_distance = Find_Distance(correct_time);
                }
            }
        }
        String pattern = "##0.0";
        DecimalFormat decimalFormat = new DecimalFormat(pattern);
        String format = decimalFormat.format(correct_distance);
        distancesample.setText(format);

        long itemLong = (long) (correct_time);
        Date itemDate = new Date(itemLong);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss.S");
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String itemDateStr = timeFormat.format(itemDate);
        timesample.setText(itemDateStr);
    }

    private double Find_Time(double point) { //метод поиска времени по дистанции
        RowerDBHelper mDBHelper = new RowerDBHelper(this);
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor;
        String[] projection1 = {
                RowerData.COLUMN_TIME
        };
        String selection_begin1 = RowerData.COLUMN_DISTANCE + " BETWEEN " + (point - 0.1) + " AND " + (point + 0.1);

        cursor = db.query(
                RowerData.TABLE_NAME,  // таблица
                projection1,            // столбцы
                selection_begin1,             // столбцы для условия WHERE
                null,         // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null                   // порядок сортировки
        );

        if (cursor != null && cursor.moveToNext()) {
            correct_time = cursor.getDouble(cursor.getColumnIndex(RowerData.COLUMN_TIME));
            cursor.close();
        }

        db.close();
        mDBHelper.close();
        return correct_time;
    }


    private double Find_Distance(double point) { //метод поиска дистанции по времени
        RowerDBHelper mDBHelper = new RowerDBHelper(this);
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor;
        String[] projection = {
                RowerData.COLUMN_DISTANCE
        };

        String selection_begin = RowerData.COLUMN_TIME + " BETWEEN " + (point - 10) + " AND " + (point + 10);

        cursor = db.query(
                RowerData.TABLE_NAME,  // таблица
                projection,            // столбцы
                selection_begin,             // столбцы для условия WHERE
                null,         // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null                   // порядок сортировки
        );

        if (cursor != null && cursor.moveToNext()) {
            correct_distance = cursor.getDouble(cursor.getColumnIndex(RowerData.COLUMN_DISTANCE));
            cursor.close();
        }

        db.close();
        mDBHelper.close();
        return correct_distance;
    }

    private void ButtonSelect(Button button){ //метод выбора кнопки
        button.setTextColor(getResources().getColor(R.color.colorPrimary));
        button.setTypeface(null, Typeface.BOLD);
        if (textsize == 10){
            button.setTextSize(textsize);
        }else {
            button.setTextSize(textsize + 2);
        }

    }

    private void ButtonNoNSelect(Button button){ //метод снятия выбора с кнопки
        button.setTextColor(getResources().getColor(R.color.colorBlack));
        button.setTypeface(null, Typeface.NORMAL);
        if (textsize == 10){
            button.setTextSize(textsize - 2);
        }else {
            button.setTextSize(textsize);
        }
    }
}