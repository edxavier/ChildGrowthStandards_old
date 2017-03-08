package com.edxavier.childgrowthstandards.helpers;

import android.util.Log;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;

import org.joda.time.Chronology;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.chrono.CopticChronology;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Eder Xavier Rojas on 02/10/2016.

public class DayAxisValueFormatter implements AxisValueFormatter
{

    private LineChart chart;

    public DayAxisValueFormatter(LineChart chart) {
        this.chart = chart;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        float aMonth = 30.4375f;
        float oneYear =  365.25f;

        String label = String.format( "%.0f dias",value);
        if(value>0) {
            if (chart.getVisibleXRange() > oneYear * 2 ) {
                label = getYearsAndMonths(value);
            } else if (chart.getVisibleXRange() < oneYear * 2 && chart.getVisibleXRange() >= aMonth * 4) {
                label = getMonths(value);
            } else if (chart.getVisibleXRange() <  aMonth * 4 && chart.getVisibleXRange() > 30) {
                if(value < oneYear)
                    label = getWeeks(value);
                else
                    label = getMonths(value);
            }
        }
        return label;

    }

    private String getYearsAndMonths(float days) {
        String res = "";
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, (int) days);
        Date now = new Date();
        Interval interval = new Interval(now.getTime(), calendar.getTime().getTime());
        Period period = interval.toPeriod();
        if (period.getYears() > 0) {
            res = String.valueOf(period.getYears()) + " a";
        }
        if (period.getYears() > 0 && period.getMonths() > 0){
            res += ", " + period.getMonths() + " m";
        }
        if (period.getMonths() > 0 && period.getYears() <= 0){
            res += period.getMonths() + " m";
        }
        return res;
    }
    private String getMonths(float days){
        String res = "";
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, (int) days);
        Date now = new Date();
        Interval interval = new Interval(now.getTime(),calendar.getTime().getTime());
        Period period = interval.toPeriod(PeriodType.months());
        res = String.valueOf(period.getMonths())+ " m";
        if(period.getWeeks()>0)
            res += ", " + String.valueOf(period.getWeeks())+ " s";

        return res;
    }

    private String getWeeks(float days){
        String res = "";
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, (int) days);
        Date now = new Date();
        Interval interval = new Interval(now.getTime(),calendar.getTime().getTime());
        Period period = interval.toPeriod(PeriodType.weeks());
        res = String.valueOf(period.getWeeks())+ " w";
        return res;
    }

    @Override
    public int getDecimalDigits() {
        return 0;
    }
}*/