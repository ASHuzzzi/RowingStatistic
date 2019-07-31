package ru.lizzzi.rowingstatistic.view;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import ru.lizzzi.rowingstatistic.dialogs.CommentDialog;
import ru.lizzzi.rowingstatistic.dialogs.FileNameDialog;
import ru.lizzzi.rowingstatistic.model.ViewModelChart;

public class ChartActivity extends DemoBase implements OnChartValueSelectedListener {

    //графики и массивы для них
    private LineChart chartUp;
    private LineChart chartDown;
    private ArrayList<ILineDataSet> dataSetsUp = new ArrayList<>();
    private ArrayList<ILineDataSet> dataSetsDown = new ArrayList<>();

    private int beginSampleUp = 0; //ссылка в массиве на начало отрезка выборки
    private int endSampleUp = 0; // ссылка в массиве на конец отрезка выборки
    private int beginSampleDown = 0; //ссылка в массиве на начало отрезка выборки
    private int endSampleDown = 0; // ссылка в массиве на конец отрезка выборки
    private double tapPoint = 0;
    private int sampleCounter = 0;

    private String dataForSaveInFile = "";

    private float ramBegin = -1;
    private float ramEnd = 0;

    private RadioButton radioButtonDistance;
    private RadioButton radioButtonTime;
    private ViewModelChart viewModel;

    private int[] colorsForSample = new int[] {
            ColorTemplate.COLORFUL_COLORS[0],
            ColorTemplate.COLORFUL_COLORS[1]
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.chart_main);

        Button buttonSampleStart = findViewById(R.id.buttonSampleStart);
        buttonSampleStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beginSampleClick();
            }
        });
        Button buttonSampleEnd = findViewById(R.id.buttonSampleEnd);
        buttonSampleEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endSampleClick();
            }
        });
        Button buttonProcessSample = findViewById(R.id.buttonProcessSample);
        buttonProcessSample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editPeriod(view);
            }
        });
        Button buttonWriteInFile = findViewById(R.id.buttonWriteInFile);
        buttonWriteInFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeInFile(view);
            }
        });

        //показываем, выделением кнопок, по каким величинам построена ось абсцисс
        radioButtonTime = findViewById(R.id.radioTime);
        radioButtonTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setShowByTime(true);
            }
        });
        radioButtonDistance = findViewById(R.id.radioDistance);
        radioButtonDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setShowByTime(false);
            }
        });

        chartUp =  findViewById(R.id.chartUp);
        chartDown = findViewById(R.id.chartDown);
        viewModel = ViewModelProviders.of(this).get(ViewModelChart.class);
    }

    @Override
    public void onStart() {
        super.onStart();
        showCharts();  //строим графики
        if (viewModel.showByTime()) {
            radioButtonTime.setChecked(true);
        } else {
            radioButtonDistance.setChecked(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.clearLastOpenDirectory();
    }

    public void beginSampleClick() { //метод выбора начало выборки
        /*
        алгоритм:
        проверяем что у нас по оси абсцисс - время или дистанция
        после проверка на превышение граничных условий
        если проверка прошла успешно, то строится график

        для времени и дистанции все кроме проверки граничных условий совпадает по алгоритму
         */
        if (tapPoint >= 0) {
            if (tapPoint < viewModel.getMinValueForCharts() || tapPoint > viewModel.getMaxValueForCharts()) {
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
                    ArrayList<Entry> values_up = new ArrayList<>();
                    ArrayList<Entry> values_down = new ArrayList<>();
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
                    chartShow(chartUp, dataSetsUp);
                    chartShow(chartDown, dataSetsDown);
                    if (ramEnd != 0){
                        sampleShow(); //метод будет выполнен если есть начало и конец периода.
                    }
                }
            }
        }
    }

    public void endSampleClick() { //метод выбора конца выборки
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
            if (tapPoint < viewModel.getMinValueForCharts() || tapPoint > viewModel.getMaxValueForCharts()) {
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
                    ArrayList<Entry> values_up = new ArrayList<>();
                    ArrayList<Entry> values_down = new ArrayList<>();
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
                    chartShow(chartUp, dataSetsUp);
                    chartShow(chartDown, dataSetsDown);
                    if (ramBegin != -1){
                        sampleShow();
                    }
                }
            }
        }
    }

    public void editPeriod(View view) { //метод обработки выборки
        if (ramBegin > -1 && ramEnd > 0) {
            CommentDialog commentDialog = new CommentDialog();
            commentDialog.show(getSupportFragmentManager(), null);
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    "Выберите границы периода",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    public void gerAverageValue(String inputComment) {
        dataForSaveInFile = dataForSaveInFile + (sampleCounter++) + ";";
        //цикл для взятия среднего значения
        for (int rower = 0; rower < viewModel.getChartNameSize(); rower++) { //для каждого пловца
            float average = (viewModel.showByTime())
                    ? viewModel.getAverageTime(rower, ramBegin, ramEnd)
                    : viewModel.getAverageDistance(rower, ramBegin, ramEnd);
            dataForSaveInFile = dataForSaveInFile + average + ";";
        }
        dataForSaveInFile =
                dataForSaveInFile + inputComment + "\n"; //добавляем коммент к строке
        ramBegin = -1;
        ramEnd = 0;
        ;
        Toast.makeText(
                getApplicationContext(),
                "Выборка добавлена",
                Toast.LENGTH_SHORT
        ).show();
    }


    @Override
    public void onValueSelected(Entry e, Highlight h) {
        //служебный метод сгенерированный автоматически
    }

    @Override
    public void onNothingSelected() {
        //служебный метод сгенерированный автоматически
    }

    public void writeInFile(View v) { //запись в файл
        if (dataForSaveInFile.length() > 1) {
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("chartName", viewModel.getChartName());
            bundle.putString("dataForSaveInFile", dataForSaveInFile);
            FileNameDialog fileNameDialog = new FileNameDialog();
            fileNameDialog.setArguments(bundle);
            fileNameDialog.show(getSupportFragmentManager(), null);
        } else {
            Toast.makeText(getApplicationContext(), "Нет данных для сохранения!", Toast.LENGTH_LONG).show();
        }
    }

    private void setShowByTime(Boolean showByTime) {
        viewModel.setShowByTime(showByTime);
        dataSetsDown.clear();
        dataSetsUp.clear();
        beginSampleUp = 0;
        beginSampleDown = 0;
        endSampleUp = 0;
        endSampleDown = 0;
        showCharts();
        if (ramBegin != -1) {
            tapPoint = (float) findDistance(ramBegin);
            ramEnd = (float) findDistance(ramEnd);
            beginSampleClick();
        }
        if (ramEnd != 0) {
            tapPoint = ramEnd;
            endSampleClick();
        }
        tapPoint = 0;
    }


    public void syncCharts(LineChart chart1, LineChart chart2) { //синхронизируем графики
        float[] srcVals = new float[9];
        float[] dstVals = new float[9];

        Matrix  srcMatrix = chart1.getViewPortHandler().getMatrixTouch();
        srcMatrix.getValues(srcVals);
        Matrix dstMatrix = chart2.getViewPortHandler().getMatrixTouch();
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

        for (int rower = 0; rower < viewModel.getChartNameSize(); rower++) {
            List<List<Entry>> dataForChart = viewModel.getDataFotDrawing(
                    viewModel.getChartName().get(rower),
                    rower);
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
            LineDataSet lineDataSetDown = new LineDataSet(dataForChart.get(0), viewModel.getChartName().get(rower));

            lineDataSetDown.setLineWidth(LINE_WIDTH);
            lineDataSetDown.setCircleRadius(CIRCLE_RADIUS);

            int color = chartColors[rower % chartColors.length];
            lineDataSetDown.setColor(color);
            lineDataSetDown.setCircleColor(color);
            dataSetsDown.add(lineDataSetDown);
        }

        //строим графики
        chartShow(chartUp, dataSetsUp);
        chartShow(chartDown, dataSetsDown);
    }

    private void chartShow(LineChart view, final ArrayList<ILineDataSet> dataSetsUp) { //задаем настройки отображения и строим график
        view.setDrawGridBackground(false);
        view.getDescription().setEnabled(false);
        view.setDrawBorders(true);
        view.getAxisLeft().setEnabled(true);
        view.getAxisRight().setDrawAxisLine(false);
        view.getAxisRight().setDrawGridLines(false);
        view.getXAxis().setDrawAxisLine(false);
        view.getXAxis().setDrawGridLines(false);
        if (viewModel.showByTime()) {
            view.getXAxis().setValueFormatter(new HourAxisValueFormatter(0));
        } else {
            view.getXAxis().setValueFormatter(new DefaultAxisValueFormatter(0));
        }

        view.setOnChartValueSelectedListener(new OnChartValueSelectedListener() { //слушатель клика по графку
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                tapPoint = e.getX();
                sampleShow();
            }

            @Override
            public void onNothingSelected() {

            }
        });

        view.setOnChartGestureListener(new OnChartGestureListener() {
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

        view.setPinchZoom(false);
        view.setScaleYEnabled(false);

        Legend legend = view.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(true);

        view.resetTracking();

        LineData lineData = new LineData(dataSetsUp);
        lineData.setDrawValues(false);
        view.setData(lineData);
        view.invalidate();
    }

    private void sampleShow() { //метод для отображения времени и дистанции по клику или при фиксировании выборки
        TextView timeSample = findViewById(R.id.timesample);
        TextView distanceSample = findViewById(R.id.distancesample);
        double correctDistance = new Double(7.1);
        double correctTime = new Double(10.1);

        if (ramBegin != -1 && ramEnd != 0) { //если зафиксирована выборка
            double start;
            double end;
            if (viewModel.showByTime()) {
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
                if (viewModel.showByTime()) {
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
        return viewModel.getTime(point);
    }

    private double findDistance(double point) { //метод поиска дистанции по времени
        return viewModel.getDistance(point);
    }
}