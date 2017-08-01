package ru.lizzzi.rowingstatistic.charts.interfaces.dataprovider;

import ru.lizzzi.rowingstatistic.charts.components.YAxis.AxisDependency;
import ru.lizzzi.rowingstatistic.charts.data.BarLineScatterCandleBubbleData;
import ru.lizzzi.rowingstatistic.charts.utils.Transformer;

public interface BarLineScatterCandleBubbleDataProvider extends ChartInterface {

    Transformer getTransformer(AxisDependency axis);
    boolean isInverted(AxisDependency axis);
    
    float getLowestVisibleX();
    float getHighestVisibleX();

    BarLineScatterCandleBubbleData getData();
}
