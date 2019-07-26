package ru.lizzzi.rowingstatistic.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import ru.lizzzi.rowingstatistic.R;
import ru.lizzzi.rowingstatistic.charts.charts.LineChart;
import ru.lizzzi.rowingstatistic.charts.components.HourAxisValueFormatter;
import ru.lizzzi.rowingstatistic.charts.components.Legend;
import ru.lizzzi.rowingstatistic.charts.data.Entry;
import ru.lizzzi.rowingstatistic.charts.data.LineData;
import ru.lizzzi.rowingstatistic.charts.data.LineDataSet;
import ru.lizzzi.rowingstatistic.charts.formatter.DefaultAxisValueFormatter;
import ru.lizzzi.rowingstatistic.charts.highlight.Highlight;
import ru.lizzzi.rowingstatistic.charts.interfaces.datasets.ILineDataSet;
import ru.lizzzi.rowingstatistic.charts.listener.ChartTouchListener;
import ru.lizzzi.rowingstatistic.charts.listener.OnChartGestureListener;
import ru.lizzzi.rowingstatistic.charts.listener.OnChartValueSelectedListener;
import ru.lizzzi.rowingstatistic.charts.notimportant.DemoBase;
import ru.lizzzi.rowingstatistic.charts.utils.ColorTemplate;
import ru.lizzzi.rowingstatistic.data.SQLiteStorage;

public class ChartActivity extends DemoBase implements OnChartValueSelectedListener {

    //файл с полями для запоминания последней открытой папки
    public static final String APP_PREFERENCES = "lastdir";
    public static final String APP_PREFERENCES_COUNTER = "counter";
    public static final String APP_PREFERENCES_DIR = "dir";
    private SharedPreferences sharedPreferencesForSettings;

    //файл для сохранения настоек для построения графиков
    public static final String APP_PREFERENCES_Chart = "chart_settings";
    public static final String APP_PREFERENCES_TYPE_CHART = "type_chart";
    private SharedPreferences sharedPreferencesForCharts;

    //графики и массивы для них
    private LineChart chartUp;
    private LineChart chartDown;
    private ArrayList<ILineDataSet> dataSetsUp = new ArrayList<>();
    private ArrayList<ILineDataSet> dataSetsDown = new ArrayList<>();

    private int timeOrDistance = 1; //тображение по х времени иди дистаниции. 0 и 1 соотвественно
    private int beginSampleUp = 0; //ссылка в массиве на начало отрезка выборки
    private int endSampleUp = 0; // ссылка в массиве на конец отрезка выборки
    private int beginSampleDown = 0; //ссылка в массиве на начало отрезка выборки
    private int endSampleDown = 0; // ссылка в массиве на конец отрезка выборки
    private float maxSpeed = 0; //максимальное значение по у для графика скорость/динамика
    private int maxPower = 0; //максимальное значение по у для графика для гребцов
    private float maxStrokeRate = 0;
    private float maxTime = 0;
    private float minTime = 0;
    private float maxDistance = 0;
    private float minDistance = 0;
    private double tapPoint = 0;
    private int counterPeriod = 0;

    private String dataForSaveInFile = "";

    private float ramBegin = -1;
    private float ramEnd = 0;

    private Button buttonTime;
    private Button buttonDistance;
    private SQLiteStorage sqlStorage;
    private List<String> chartName = new ArrayList<>();

    private int[] colorsForSample = new int[]{
            ColorTemplate.COLORFUL_COLORS[0],
            ColorTemplate.COLORFUL_COLORS[1]
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.chart_main);

        //показываем, выделением кнопок, по каким величинам построена ось абсцисс
        buttonTime = findViewById(R.id.buttonTime);
        buttonTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferencesForCharts.edit().putInt(APP_PREFERENCES_TYPE_CHART, 0).apply();
                ButtonSelect(buttonTime);
                ButtonNoNSelect(buttonDistance);
            }
        });
        buttonDistance = findViewById(R.id.buttonDistance);
        buttonDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferencesForCharts.edit().putInt(APP_PREFERENCES_TYPE_CHART, 1).apply();
                ButtonSelect(buttonDistance);
                ButtonNoNSelect(buttonTime);
            }
        });

        sqlStorage = new SQLiteStorage(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        //получаем значения из файла chart_settings
        sharedPreferencesForCharts =
                this.getSharedPreferences(APP_PREFERENCES_Chart, Context.MODE_PRIVATE);
        timeOrDistance = (sharedPreferencesForCharts.getInt(APP_PREFERENCES_TYPE_CHART, 0));
        maxPower = sqlStorage.getMaxPower();
        maxSpeed = sqlStorage.getMaxSpeed();
        maxTime = sqlStorage.getMaxTime();
        minTime = sqlStorage.getMinTime();
        maxDistance = sqlStorage.getMaxDistance();
        minDistance = 0;
        maxStrokeRate = sqlStorage.getMaxStrokeRate();

        chartName = sqlStorage.getRowerName();
        showCharts();  //строим графики
        int typeChart = sharedPreferencesForCharts.getInt(APP_PREFERENCES_TYPE_CHART, 1);
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
    public void onDestroy() {
        super.onDestroy();
        //дублируем удаление запомненной папки в File Dialog
        sharedPreferencesForSettings =
                this.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        sharedPreferencesForSettings.edit().putInt(APP_PREFERENCES_COUNTER, 0).apply();
        //отчищаем базу
        sqlStorage.clearDB();
    }


    public void beginSampleClick(View view) { //метод выбора начало выборки
        /*
        алгоритм:
        проверяем что у нас по оси абсцисс - время или дистанция
        после проверка на превышение граничных условий
        если проверка прошла успешно, то строится график

        для времени и дистанции все кроме проверки граничных условий совпадает по алгоритму
         */

        if (tapPoint >= 0) {
            if (timeOrDistance == 0) {
                if (tapPoint < minTime || tapPoint > maxTime) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Значение вне доступного периода",
                            Toast.LENGTH_LONG
                    ).show();
                } else {
                    if (ramEnd > 0 && ramEnd < tapPoint) {
                        Toast.makeText(
                                getApplicationContext(),
                                "Начало периода не может быть позже конца",
                                Toast.LENGTH_LONG
                        ).show();
                    } else {
                        // граница строится как график
                        // заполняем массив для еще одного графика
                        ramBegin = (float) tapPoint;
                        ArrayList<Entry> values_up = new ArrayList<Entry>();
                        ArrayList<Entry> values_down = new ArrayList<Entry>();
                        values_up.add(new Entry(ramBegin, 0));
                        values_up.add(new Entry(ramBegin, 1));

                        values_down.add(new Entry(ramBegin, 0));
                        values_down.add(new Entry(ramBegin, 1));
                        LineDataSet d_up = new LineDataSet(values_up, "Начало выборки");
                        LineDataSet d_down = new LineDataSet(values_down, "Начало выборки");

                        d_up.setLineWidth(3.5f);
                        d_up.setCircleRadius(1f);

                        int color = colorsForSample[0];
                        d_up.setColor(color);
                        d_up.setCircleColor(color);

                        d_down.setLineWidth(3.5f);
                        d_down.setCircleRadius(1f);

                        d_down.setColor(color);
                        d_down.setCircleColor(color);
                        if (beginSampleUp == 0) {
                            dataSetsUp.add(d_up);
                            beginSampleUp = dataSetsUp.size();
                            dataSetsDown.add(d_down);
                            beginSampleDown = dataSetsDown.size();
                        } else {
                            dataSetsUp.set(beginSampleUp - 1, d_up);
                            dataSetsDown.set(beginSampleDown - 1, d_down);
                        }
                        chartShowUp(dataSetsUp); //строим границе на верхнем графике
                        chartShowDown(dataSetsDown); //строим границу на нижнем графике
                        if (ramEnd != 0){
                            sampleShow(); //метод будет выполнен если есть начало и конец периода.
                        }
                    }
                }
            } else {
                if (tapPoint < minDistance || tapPoint > maxDistance) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Значение вне доступного периода",
                            Toast.LENGTH_LONG
                    ).show();
                } else {
                    if (ramEnd > 0 && ramEnd < tapPoint) {
                        Toast.makeText(
                                getApplicationContext(),
                                "Начало периода не может быть позже конца",
                                Toast.LENGTH_LONG
                        ).show();
                    } else {
                        //здесь все аналогично выше
                        ramBegin = (float) tapPoint;
                        ArrayList<Entry> values_up = new ArrayList<Entry>();
                        ArrayList<Entry> values_down = new ArrayList<Entry>();
                        values_up.add(new Entry(ramBegin, 0));
                        values_up.add(new Entry(ramBegin, 1));

                        values_down.add(new Entry(ramBegin, 0));
                        values_down.add(new Entry(ramBegin, 1));
                        LineDataSet d_up = new LineDataSet(values_up, "Начало выборки");
                        LineDataSet d_down = new LineDataSet(values_down, "Начало выборки");

                        d_up.setLineWidth(3.5f);
                        d_up.setCircleRadius(1f);

                        int color = colorsForSample[0];
                        d_up.setColor(color);
                        d_up.setCircleColor(color);

                        d_down.setLineWidth(3.5f);
                        d_down.setCircleRadius(1f);

                        d_down.setColor(color);
                        d_down.setCircleColor(color);
                        if (beginSampleUp == 0) {
                            dataSetsUp.add(d_up);
                            beginSampleUp = dataSetsUp.size();
                            dataSetsDown.add(d_down);
                            beginSampleDown = dataSetsDown.size();
                        } else {
                            dataSetsUp.set(beginSampleUp - 1, d_up);
                            dataSetsDown.set(beginSampleDown - 1, d_down);
                        }
                        chartShowUp(dataSetsUp);
                        chartShowDown(dataSetsDown);
                        if (ramEnd != 0){
                            sampleShow();
                        }
                    }
                }
            }
        }
    }

    public void endSampleClick(View view) { //метод выбора конца выборки
        /*
        алгоритм:
        проверяем что у нас по оси абсцисс - время или дистанция
        после проверка на превышение граничных условий
        если проверка прошла успешно, то строится график

        для времени и дистанции все кроме проверки граничных условий совпадает по алгоритму поэтому
        чтобы не плодить одинаковые строки комментов, здесь я их опущу.
        общие моменты описаны в строках 206-249
         */

        if (tapPoint > 0) {
            if (timeOrDistance == 0) {
                if (tapPoint < minTime || tapPoint > (maxTime + 10)) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Значение вне доступного периода",
                            Toast.LENGTH_LONG
                    ).show();
                } else {
                    if (ramBegin > 0 && tapPoint < ramBegin) {
                        Toast.makeText(
                                getApplicationContext(),
                                "Начало периода не может быть позже конца",
                                Toast.LENGTH_LONG
                        ).show();
                    } else {
                        ramEnd = (float) tapPoint;
                        ArrayList<Entry> values_up = new ArrayList<Entry>();
                        ArrayList<Entry> values_down = new ArrayList<Entry>();
                        values_up.add(new Entry(ramEnd, 0));
                        values_up.add(new Entry(ramEnd, 1));

                        values_down.add(new Entry(ramEnd, 0));
                        values_down.add(new Entry(ramEnd, 1));
                        LineDataSet d_up = new LineDataSet(values_up, "Конец выборки");
                        LineDataSet d_down = new LineDataSet(values_down, "Конец выборки");

                        d_up.setLineWidth(3.5f);
                        d_up.setCircleRadius(1f);

                        int color = colorsForSample[1];
                        d_up.setColor(color);
                        d_up.setCircleColor(color);

                        d_down.setLineWidth(3.5f);
                        d_down.setCircleRadius(1f);

                        d_down.setColor(color);
                        d_down.setCircleColor(color);
                        if (endSampleUp == 0) {
                            dataSetsUp.add(d_up);
                            endSampleUp = dataSetsUp.size();
                            dataSetsDown.add(d_down);
                            endSampleDown = dataSetsDown.size();
                        } else {
                            dataSetsUp.set(endSampleUp - 1, d_up);
                            dataSetsDown.set(endSampleDown - 1, d_down);
                        }
                        chartShowUp(dataSetsUp);
                        chartShowDown(dataSetsDown);
                        if (ramBegin != -1){
                            sampleShow();
                        }
                    }
                }
            } else {
                if (tapPoint < minDistance || tapPoint > maxDistance) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Значение вне доступного периода",
                            Toast.LENGTH_LONG
                    ).show();
                } else {
                    if (ramBegin > 0 && tapPoint < ramBegin) {
                        Toast.makeText(
                                getApplicationContext(),
                                "Начало периода не может быть позже конца",
                                Toast.LENGTH_LONG
                        ).show();
                    } else {
                        ramEnd = (float) tapPoint;
                        ArrayList<Entry> values_up = new ArrayList<Entry>();
                        ArrayList<Entry> values_down = new ArrayList<Entry>();
                        values_up.add(new Entry(ramEnd, 0));
                        values_up.add(new Entry(ramEnd, 1));

                        values_down.add(new Entry(ramEnd, 0));
                        values_down.add(new Entry(ramEnd, 1));
                        LineDataSet d_up = new LineDataSet(values_up, "Конец выборки");
                        LineDataSet d_down = new LineDataSet(values_down, "Конец выборки");

                        d_up.setLineWidth(3.5f);
                        d_up.setCircleRadius(1f);

                        int color = colorsForSample[1];
                        d_up.setColor(color);
                        d_up.setCircleColor(color);

                        d_down.setLineWidth(3.5f);
                        d_down.setCircleRadius(1f);

                        d_down.setColor(color);
                        d_down.setCircleColor(color);
                        if (endSampleUp == 0) {
                            dataSetsUp.add(d_up);
                            endSampleUp = dataSetsUp.size();
                            dataSetsDown.add(d_down);
                            endSampleDown = dataSetsDown.size();
                        } else {
                            dataSetsUp.set(endSampleUp - 1, d_up);
                            dataSetsDown.set(endSampleDown - 1, d_down);
                        }
                        chartShowUp(dataSetsUp);
                        chartShowDown(dataSetsDown);
                        if (ramBegin != -1){
                            sampleShow();
                        }
                    }
                }
            }
        }
    }

    public void editPeriod(View view) { //метод обработки выборки

        if (ramBegin > -1 && ramEnd > 0) { //проверка на наличие начала и конца выборки
            final SQLiteStorage mDBHelper = new SQLiteStorage(this);

            //сначала создаем поле для ввода комментария
            LayoutInflater li = LayoutInflater.from(this);
            final View promptsView = li.inflate(R.layout.comment_for_result, null);

            //Создаем AlertDialog
            AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(this);

            //Настраиваем prompt.xml для нашего AlertDialog:
            mDialogBuilder.setView(promptsView);

            //Настраиваем отображение поля для ввода текста в открытом диалоге:
            final EditText userInput = promptsView.findViewById(R.id.editText);

            //Настраиваем сообщение в диалоговом окне:
            mDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("Обработать",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dataForSaveInFile = dataForSaveInFile + (counterPeriod + 1) + ";";
                                    //цикл для взятия среднего значения
                                    for (int rower = 0; rower < chartName.size(); rower++) { //для каждого пловца
                                        float average = (timeOrDistance == 0)
                                                ? sqlStorage.getAverageTime(
                                                        rower,
                                                        ramBegin,
                                                        ramEnd)
                                                : sqlStorage.getAverageDistance(
                                                        rower,
                                                        ramBegin,
                                                        ramEnd);
                                        dataForSaveInFile = dataForSaveInFile + average + ";";
                                    }
                                    dataForSaveInFile =
                                            dataForSaveInFile + userInput.getText() + "\n"; //добавляем коммент к строке
                                    ramBegin = -1;
                                    ramEnd = 0;
                                    counterPeriod++;
                                    Toast.makeText(
                                            getApplicationContext(),
                                            "Выборка добавлена",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            })
                    .setNegativeButton("Отмена",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    Toast.makeText(
                                            getApplicationContext(),
                                            "Выборка не добавлена!",
                                            Toast.LENGTH_SHORT
                                    ).show();
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
            Toast.makeText(
                    getApplicationContext(),
                    "Выберите границы периода",
                    Toast.LENGTH_LONG
            ).show();
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

    public void writeInFile(View v) { //запись в файл

        if (dataForSaveInFile.length() > 1) { //проверяем чтобы строка была не пустая
            LayoutInflater li = LayoutInflater.from(this);
            final View promptsView = li.inflate(R.layout.result_filename, null);

            //Создаем AlertDialog
            AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(this);

            //Настраиваем prompt.xml для нашего AlertDialog:
            mDialogBuilder.setView(promptsView);

            //Настраиваем отображение поля для ввода текста в открытом диалоге:
            final EditText userInput = promptsView.findViewById(R.id.input_text);

            //Настраиваем сообщение в диалоговом окне:
            mDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("Записать",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    String firstRow = "Период;";
                                    if (chartName.size() > 0) {
                                        firstRow = firstRow + chartName.get(0) + ";";
                                    }
                                    if (chartName.size() > 1) {
                                        firstRow = firstRow + chartName.get(1) + ";";
                                    }
                                    if (chartName.size() > 2) {
                                        firstRow = firstRow + chartName.get(2) + ";";
                                    }
                                    if (chartName.size() > 3) {
                                        firstRow = firstRow + chartName.get(3) + ";";
                                    }
                                    if (chartName.size() > 4) {
                                        firstRow = firstRow + chartName.get(4) + ";";
                                    }
                                    if (chartName.size() > 5) {
                                        firstRow = firstRow + chartName.get(5) + ";";
                                    }
                                    if (chartName.size() > 6) {
                                        firstRow = firstRow + chartName.get(6) + ";";
                                    }
                                    if (chartName.size() > 7) {
                                        firstRow = firstRow + chartName.get(7) + ";";
                                    }
                                    firstRow = firstRow + "Комментарий" + "\n";

                                    sharedPreferencesForSettings = getApplicationContext().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
                                    String nameSavedFile;
                                    if (userInput.length() > 0) {
                                        nameSavedFile = userInput.getText().toString() + ".csv";
                                    } else {
                                        Calendar currentTime = Calendar.getInstance();
                                        nameSavedFile =
                                                "Result_" +
                                                currentTime.get(Calendar.HOUR_OF_DAY) + "-" +
                                                currentTime.get(Calendar.MINUTE) + "-" +
                                                currentTime.get(Calendar.SECOND) + ".csv";
                                    }

                                    String checkDirectory;
                                    File pathForSave;
                                    String textForToast;
                                    //пытаемся сохраниться в папку с исходниками
                                    try{
                                        checkDirectory = Environment.getExternalStorageDirectory().getPath();
                                        if (checkDirectory.equals(sharedPreferencesForSettings.getString(APP_PREFERENCES_DIR, ""))){
                                            throw new Exception();
                                        }
                                        pathForSave = new File(
                                                sharedPreferencesForSettings.getString(APP_PREFERENCES_DIR, "") +
                                                "/" +
                                                nameSavedFile);
                                        saveFile(pathForSave, firstRow, dataForSaveInFile);
                                        //пытаемся сохраниться в папку с исходниками
                                        pathForSave = new File(sharedPreferencesForSettings.getString(APP_PREFERENCES_DIR, "") + "/" + nameSavedFile);
                                        saveFile(pathForSave, firstRow, dataForSaveInFile);
                                        textForToast = "Файл сохранен в папку с иcходными файлами!";
                                    } catch (Exception e) {
                                        checkDirectory = Environment.getExternalStorageState();
                                        if (Environment.MEDIA_MOUNTED.equals(checkDirectory)) //проверям доступность внешней памяти
                                        {
                                            try { //сохраняем во внешнюю память
                                                pathForSave = new File(
                                                        Environment.getExternalStorageDirectory() +
                                                        "/" +
                                                        nameSavedFile);
                                                saveFile(pathForSave, firstRow, dataForSaveInFile);
                                                textForToast = "Файл сохранен на внешнюю память!";
                                            } catch (Exception e1) {
                                                e1.printStackTrace();
                                                textForToast = "Файл не сохранен!";
                                            }
                                        } else {
                                            try { //сохраняем во внутренню память
                                                pathForSave = new File(
                                                        getApplicationContext().getFilesDir() +
                                                        "/" +
                                                        nameSavedFile);
                                                saveFile(pathForSave, firstRow, dataForSaveInFile);
                                                textForToast = "Файл сохранен в папку приложения!";
                                            } catch (Exception e2) {
                                                e2.printStackTrace();
                                                textForToast = "Файл не сохранен!";
                                            }
                                        }
                                    }
                                    Toast.makeText(getApplicationContext(), textForToast, Toast.LENGTH_SHORT).show();
                                    dataForSaveInFile = "";
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

    private boolean saveFile(File myFile, String firts_row, String mass_temp) throws IOException {
        if (myFile.createNewFile()) {
            FileOutputStream outputStream = new FileOutputStream(myFile);   // После чего создаем поток для записи                 // и производим непосредственно запись
            outputStream.write(firts_row.getBytes("Cp1251"));
            outputStream.write(mass_temp.getBytes("Cp1251"));
            outputStream.close();
            return true;
        } else {
            return false;
        }
    }

    public void changeOnTime(View view) { //метод смены графика с дистанции на время

        sharedPreferencesForCharts = this.getSharedPreferences(APP_PREFERENCES_Chart, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferencesForCharts.edit();
        editor.putString(APP_PREFERENCES_TYPE_CHART, "0");
        editor.apply();

        Button time = findViewById(R.id.buttonTime);
        Button distance = findViewById(R.id.buttonDistance);
        ButtonNoNSelect(distance);
        ButtonSelect(time);


        dataSetsDown.clear();
        dataSetsUp.clear();
        beginSampleUp = 0;
        beginSampleDown = 0;
        endSampleUp = 0;
        endSampleDown = 0;
        timeOrDistance = 0;
        showCharts();
        if (ramBegin != -1) {
            tapPoint = (float) findTime(ramBegin);
            ramEnd = (float) findTime(ramEnd);
            beginSampleClick(view);
        }
        if (ramEnd != 0) {
            tapPoint = ramEnd;
            endSampleClick(view);
        }
        tapPoint = 0;
    }

    public void changeOnDistance(View view) { //метод смены графика с времени на дистанцию

        sharedPreferencesForCharts = this.getSharedPreferences(APP_PREFERENCES_Chart, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferencesForCharts.edit();
        editor.putString(APP_PREFERENCES_TYPE_CHART, "1");
        editor.apply();

        Button time = findViewById(R.id.buttonTime);
        Button distance = findViewById(R.id.buttonDistance);
        ButtonNoNSelect(time);
        ButtonSelect(distance);

        dataSetsDown.clear();
        dataSetsUp.clear();
        beginSampleUp = 0;
        beginSampleDown = 0;
        endSampleUp = 0;
        endSampleDown = 0;
        timeOrDistance = 1;
        showCharts();
        if (ramBegin != -1) {
            tapPoint = (float) findDistance(ramBegin);
            ramEnd = (float) findDistance(ramEnd);
            beginSampleClick(view);
        }
        if (ramEnd != 0) {
            tapPoint = ramEnd;
            endSampleClick(view);
        }
        tapPoint = 0;
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


    private void showCharts() { //метод построения графиков
        int[] chartColors = new int[]{ //цвета для графиков
                ColorTemplate.VORDIPLOM_COLORS[0],
                ColorTemplate.VORDIPLOM_COLORS[1],
                ColorTemplate.VORDIPLOM_COLORS[2],
                ColorTemplate.VORDIPLOM_COLORS[3],
                ColorTemplate.VORDIPLOM_COLORS[4],
                ColorTemplate.VORDIPLOM_COLORS[5],
                ColorTemplate.VORDIPLOM_COLORS[6],
                ColorTemplate.VORDIPLOM_COLORS[7],
                ColorTemplate.VORDIPLOM_COLORS[8],
                ColorTemplate.VORDIPLOM_COLORS[9]
        };

        float LINE_WIDTH = 2.5f;
        float CIRCLE_RADIUS = 6f;

        for (int rower = 0; rower < chartName.size(); rower++) {
            List<List<Entry>> dataForChart = sqlStorage.getDataFotDrawing(
                    chartName.get(rower),
                    rower,
                    maxPower,
                    timeOrDistance,
                    maxSpeed,
                    maxStrokeRate);
            if (rower == 0) { //цикл для построения верхних графиков
                LineDataSet lineDataSetUp = new LineDataSet(dataForChart.get(1), "Скорость");
                lineDataSetUp.setLineWidth(LINE_WIDTH);
                lineDataSetUp.setCircleRadius(CIRCLE_RADIUS);

                int color2 = chartColors[8];
                lineDataSetUp.setColor(color2);
                lineDataSetUp.setCircleColor(color2);
                dataSetsUp.add(lineDataSetUp);

                lineDataSetUp = new LineDataSet(dataForChart.get(2), "Темп гребли");
                lineDataSetUp.setLineWidth(LINE_WIDTH);
                lineDataSetUp.setCircleRadius(CIRCLE_RADIUS);

                color2 = chartColors[9];
                lineDataSetUp.setColor(color2);
                lineDataSetUp.setCircleColor(color2);
                dataSetsUp.add(lineDataSetUp);
            }
            LineDataSet lineDataSetDown = new LineDataSet(dataForChart.get(0), chartName.get(rower));

            lineDataSetDown.setLineWidth(LINE_WIDTH);
            lineDataSetDown.setCircleRadius(CIRCLE_RADIUS);

            int color = chartColors[rower % chartColors.length];
            lineDataSetDown.setColor(color);
            lineDataSetDown.setCircleColor(color);
            dataSetsDown.add(lineDataSetDown);
        }

        //строим графики
        chartShowUp(dataSetsUp);
        chartShowDown(dataSetsDown);
    }

    private void chartShowUp(ArrayList<ILineDataSet> dataSetsDown) { //задаем настройки отображения и строим график
        chartUp = findViewById(R.id.chartUp);
        chartUp.setDrawGridBackground(false);
        chartUp.getDescription().setEnabled(false);
        chartUp.setDrawBorders(true);
        chartUp.getAxisLeft().setEnabled(true);
        chartUp.getAxisRight().setDrawAxisLine(false);
        chartUp.getAxisRight().setDrawGridLines(false);
        chartUp.getXAxis().setDrawAxisLine(false);
        chartUp.getXAxis().setDrawGridLines(false);
        if (timeOrDistance == 0) {
            chartUp.getXAxis().setValueFormatter(new HourAxisValueFormatter(0));
        } else {
            chartUp.getXAxis().setValueFormatter(new DefaultAxisValueFormatter(0));
        }

        chartUp.setOnChartValueSelectedListener(new OnChartValueSelectedListener() { //слушатель клика по графку
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                tapPoint = e.getX();
                sampleShow();
            }

            @Override
            public void onNothingSelected() {

            }
        });
        chartUp.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                syncCharts(chartUp, chartDown);
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
                syncCharts(chartUp, chartDown);

            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {
                syncCharts(chartUp, chartDown);
            }
        });

        chartUp.setPinchZoom(false);
        chartUp.setScaleYEnabled(false);

        Legend legend = chartUp.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(true);

        chartUp.resetTracking();

        LineData lineData = new LineData(dataSetsDown);
        lineData.setDrawValues(false);
        chartUp.setData(lineData);
        chartUp.invalidate();
    }

    private void chartShowDown(final ArrayList<ILineDataSet> dataSetsUp) { //задаем настройки отображения и строим график
        chartDown = findViewById(R.id.chartDown);
        chartDown.setDrawGridBackground(false);
        chartDown.getDescription().setEnabled(false);
        chartDown.setDrawBorders(true);
        chartDown.getAxisLeft().setEnabled(true);
        chartDown.getAxisRight().setDrawAxisLine(false);
        chartDown.getAxisRight().setDrawGridLines(false);
        chartDown.getXAxis().setDrawAxisLine(false);
        chartDown.getXAxis().setDrawGridLines(false);
        if (timeOrDistance == 0) {
            chartDown.getXAxis().setValueFormatter(new HourAxisValueFormatter(0));
        } else {
            chartDown.getXAxis().setValueFormatter(new DefaultAxisValueFormatter(0));
        }

        chartDown.setOnChartValueSelectedListener(new OnChartValueSelectedListener() { //слушатель клика по графку
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                tapPoint = e.getX();
                sampleShow();
            }

            @Override
            public void onNothingSelected() {

            }
        });

        chartDown.setOnChartGestureListener(new OnChartGestureListener() {
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
                syncCharts(chartDown, chartUp);

            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {
                syncCharts(chartDown, chartUp);
            }
        });

        chartDown.setPinchZoom(false);
        chartDown.setScaleYEnabled(false);


        Legend legend = chartDown.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(true);

        chartDown.resetTracking();

        LineData lineData = new LineData(dataSetsUp);
        lineData.setDrawValues(false);
        chartDown.setData(lineData);
        chartDown.invalidate();
    }

    private void sampleShow() { //метод для отображения времени и дистанции по клику или при фиксировании выборки
        TextView timeSample = findViewById(R.id.timesample);
        TextView distanceSample = findViewById(R.id.distancesample);
        double correctDistance = new Double(7.1);
        double correctTime = new Double(10.1);

        if (ramBegin != -1 && ramEnd != 0) { //если зафиксирована выборка
            double start;
            double end;
            if (timeOrDistance == 0) {
                start = findDistance(ramBegin);
                end = findDistance(ramEnd);
                correctDistance = end - start;

                start = findTime(start);
                end = findTime(end);
                correctTime = end - start;
            } else {
                start = findTime(ramBegin);
                end = findTime(ramEnd);
                correctTime = end - start;
                correctDistance = ramEnd - ramBegin;
            }
        } else { //если выборки нет, показываем значения по тапу на графике
            if (tapPoint > 0) {
                if (timeOrDistance == 0) {
                    correctDistance = findDistance(tapPoint);
                    correctTime = findTime(correctDistance);
                } else {
                    correctTime = findTime(tapPoint);
                    correctDistance = findDistance(correctTime);
                }
            }
        }
        String pattern = "##0.0";
        DecimalFormat decimalFormat = new DecimalFormat(pattern);
        String format = decimalFormat.format(correctDistance);
        distanceSample.setText(format);

        long itemLong = (long) (correctTime);
        Date itemDate = new Date(itemLong);
        SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss.S", Locale.getDefault());
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String itemDateStr = timeFormat.format(itemDate);
        timeSample.setText(itemDateStr);
    }

    private double findTime(double point) { //метод поиска времени по дистанции
        return sqlStorage.getTime(point);
    }

    private double findDistance(double point) { //метод поиска дистанции по времени
        return sqlStorage.getDistance(point);
    }

    private void ButtonSelect(Button button){ //метод выбора кнопки
        button.setTextColor(getResources().getColor(R.color.colorPrimary));
        button.setTypeface(null, Typeface.BOLD);
    }

    private void ButtonNoNSelect(Button button){ //метод снятия выбора с кнопки
        button.setTextColor(getResources().getColor(R.color.colorBlack));
        button.setTypeface(null, Typeface.NORMAL);
    }
}