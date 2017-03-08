package com.edxavier.childgrowthstandards.libs;

import android.content.Context;

import com.edxavier.childgrowthstandards.R;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.realm.implementation.RealmLineDataSet;

/**
 * Created by Eder Xavier Rojas on 07/03/2017.
 */

public class ChartStyler {
    public static Chart<?> setup(Chart<?> chart, Context context) {
        // no description text
        chart.getDescription().setEnabled(false);
        chart.setNoDataText("Sin datos para mostrar");

        // enable touch gestures
        chart.setTouchEnabled(true);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.TOP);
        chart.getXAxis().setTextColor(context.getResources().getColor(R.color.primary_text));
        chart.getXAxis().setDrawLabels(true);

        chart.getXAxis().setAxisLineColor(context.getResources().getColor(R.color.md_green_800));
        chart.getLegend().setTextColor(context.getResources().getColor(R.color.primary_text));
        chart.getLegend().setWordWrapEnabled(true);
        chart.getXAxis().setDrawLabels(true);
        chart.setExtraOffsets(8f, 16f, 8f, 8f);

        chart.animateXY(500, 0);

        if (chart instanceof LineChart) {

            LineChart mChart = (LineChart) chart;
            mChart.getAxisLeft().setTextColor(context.getResources().getColor(R.color.primary_text));
            mChart.setDrawBorders(false);
            //mChart.setBorderColor(context.getResources().getColor(R.color.md_green_500_25));
            mChart.getAxisLeft().setAxisLineColor(context.getResources().getColor(R.color.md_green_800));
            mChart.getAxisRight().setAxisLineColor(context.getResources().getColor(R.color.md_green_800));
            //mChart.getAxisRight().setDrawLabels(false);
            mChart.setDrawGridBackground(true);
            mChart.setGridBackgroundColor(context.getResources().getColor(R.color.md_white_1000_60));
            // enable scaling and dragging
            mChart.setDragEnabled(true);
            mChart.setScaleEnabled(true);
            // if disabled, scaling can be done on x- and y-axis separately
            mChart.setPinchZoom(true);
            //mChart.getAxisRight().setEnabled(true);

            YAxis leftAxis = mChart.getAxisLeft();
            mChart.getAxisRight().setAxisMinimum(0f);

            //reset all limit lines to avoid overlapping lines
            leftAxis.setGridColor(context.getResources().getColor(R.color.md_light_blue_200));
            leftAxis.setDrawZeroLine(false);
            leftAxis.setAxisMinimum(0f);
            leftAxis.setDrawGridLines(false);

            return mChart;
        }else
            return null;
    }

    public static RealmLineDataSet<?> setupCommonLineDataset(RealmLineDataSet<?> dataSet, int color, String label){
        dataSet.setDrawCircles(false);
        dataSet.setColors(color);
        dataSet.setLineWidth(1);
        dataSet.setLabel(label);
        dataSet.setDrawHighlightIndicators(false);
        dataSet.setHighlightEnabled(false);

        return dataSet;
    }
}
