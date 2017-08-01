package ru.lizzzi.rowingstatistic.charts.interfaces.dataprovider;

import ru.lizzzi.rowingstatistic.charts.data.BarData;

public interface BarDataProvider extends BarLineScatterCandleBubbleDataProvider {

    BarData getBarData();
    boolean isDrawBarShadowEnabled();
    boolean isDrawValueAboveBarEnabled();
    boolean isHighlightFullBarEnabled();
}
