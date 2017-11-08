package com.edxavier.childgrowthstandards.helpers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.edxavier.childgrowthstandards.R;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.realm.implementation.RealmLineDataSet;
import com.pixplicity.easyprefs.library.Prefs;

public class ChartStyler {

    @NonNull
    public static Chart<?> setup(@NonNull Chart<?> chart, @NonNull Context context, String title) {
        // no description text
        chart.getDescription().setEnabled(true);
        Description desc = new Description();
        desc.setTextSize(16);
        desc.setYOffset(16);
        desc.setXOffset(16);
        desc.setText(title);
        chart.setDescription(desc);
        chart.setNoDataText(context.getString(R.string.graph_empty));
        // enable touch gestures
        chart.setTouchEnabled(true);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.TOP);
        chart.getXAxis().setTextColor(context.getResources().getColor(R.color.primary_text));

        chart.getXAxis().setDrawLabels(true);
        chart.getXAxis().setAxisLineColor(context.getResources().getColor(R.color.md_black_1000_75));
        chart.getLegend().setTextColor(context.getResources().getColor(R.color.primary_text));
        chart.getLegend().setWordWrapEnabled(true);
        chart.getXAxis().setDrawLabels(true);
        chart.setExtraOffsets(5f, 15f, 0f, 0f);
        chart.getXAxis().setAxisLineColor(context.getResources().getColor(R.color.md_black_1000_75));
        chart.animateXY(500, 0);

        if (chart instanceof LineChart) {

            LineChart mChart = (LineChart) chart;
            mChart.setAutoScaleMinMaxEnabled(true);
            mChart.getAxisRight().setAxisMinimum(1f);
            mChart.getAxisLeft().setAxisMinimum(1f);
            mChart.getAxisLeft().setTextColor(context.getResources().getColor(R.color.primary_text));
            mChart.setDrawBorders(true);
            mChart.setBorderColor(context.getResources().getColor(R.color.md_black_1000_75));
            mChart.getAxisLeft().setAxisLineColor(context.getResources().getColor(R.color.md_green_800));
            mChart.getAxisRight().setAxisLineColor(context.getResources().getColor(R.color.md_green_800));
            mChart.getAxisRight().setDrawLabels(true);
            mChart.getAxisRight().setDrawAxisLine(true);
            mChart.setDrawGridBackground(false);
            // enable scaling and dragging
            mChart.setDragEnabled(true);
            mChart.setScaleEnabled(true);
            // if disabled, scaling can be done on x- and y-axis separately
            mChart.setPinchZoom(false);
            mChart.getAxisRight().setEnabled(true);

            YAxis leftAxis = mChart.getAxisLeft();

            //reset all limit lines to avoid overlapping lines
            leftAxis.setGridColor(context.getResources().getColor(R.color.md_grey_200));
            leftAxis.setDrawZeroLine(false);

            leftAxis.setDrawLimitLinesBehindData(true);
            //leftAxis.setValueFormatter(new MyYAxisValueFormatter());
            leftAxis.setAxisMinimum(0f);
            leftAxis.setDrawGridLines(false);

            return mChart;
        }
        else
            return chart;

    }

    @NonNull
    public static RealmLineDataSet lineDataSet(@NonNull RealmLineDataSet lineDataSet, @NonNull Context context){
        String title = context.getResources().getString(R.string.weightForage);
        lineDataSet.setLabel(title);
        //acumuladoDataSet.setValueFormatter(new MyValueFormatter());
        lineDataSet.setDrawFilled(true);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setLineWidth(1.2f);
        //acumuladoDataSet.enableDashedLine(10f, 10f, 0f);
        //acumuladoDataSet.setMode(LineDataSet.Mode.LINEAR);
        //acumuladoDataSet.setCubicIntensity(0.1f);
        lineDataSet.setValueTextSize(8f);
        lineDataSet.setCircleColor(context.getResources().getColor(R.color.accent));
        lineDataSet.setValueTextColor(context.getResources().getColor(R.color.accent));
        lineDataSet.setColor(context.getResources().getColor(R.color.md_grey_100));
        lineDataSet.setFillColor(context.getResources().getColor(R.color.accent));

        return lineDataSet;
    }

}
