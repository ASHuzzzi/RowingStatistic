package ru.lizzzi.rowingstatistic.charts.interfaces.dataprovider;

import ru.lizzzi.rowingstatistic.charts.components.YAxis;
import ru.lizzzi.rowingstatistic.charts.data.LineData;

public interface LineDataProvider extends BarLineScatterCandleBubbleDataProvider {

    LineData getLineData();

    YAxis getAxis(YAxis.AxisDependency dependency);
}
