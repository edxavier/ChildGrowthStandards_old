package com.edxavier.childgrowthstandards.helpers;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;

import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Eder Xavier Rojas on 02/10/2016.

public class HeightAxisValueFormatter implements AxisValueFormatter
{

    private LineChart chart;

    public HeightAxisValueFormatter(LineChart chart) {
        this.chart = chart;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return String.format( "%.0f cm",value);
    }
    @Override
    public int getDecimalDigits() {
        return 0;
    }
}*/