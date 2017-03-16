package com.edxavier.childgrowthstandards.libs.formatter;

import android.content.Context;

import com.edxavier.childgrowthstandards.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Eder Xavier Rojas on 20/01/2017.
 */

public class XDaysValuesFormatter implements IAxisValueFormatter {


    private LineChart chart;
    private Context context;

    public XDaysValuesFormatter(LineChart chart, Context context) {
        this.chart = chart;
        this.context = context;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        float aMonth = 30.4375f;
        float oneYear =  365.25f;

        String label = String.format( Locale.getDefault(), "%.0fd",value);
        if(value>0) {
            if(chart.getVisibleXRange() < aMonth * 6){
                label = getWeeks(value);
            }else if(chart.getVisibleXRange() > aMonth * 6 && chart.getVisibleXRange() < oneYear * 3 && value < oneYear * 3){
                label = getMonths(value);
            }else {
                label = getYearsAndMonths(value);
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
            res = String.valueOf(period.getYears()) + context.getResources().getString(R.string.year_av);
        }
        if (period.getYears() > 0 && period.getMonths() > 0){
            res += period.getMonths() + "m";
        }
        if (period.getMonths() > 0 && period.getYears() <= 0){
            res += period.getMonths() + "m";
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
        res = String.valueOf(period.getMonths())+ "m";
        if(period.getWeeks()>0)
            res += String.valueOf(period.getWeeks())+ context.getResources().getString(R.string.week_av);

        return res;
    }

    private String getWeeks(float days){
        String res = "";
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, (int) days);
        Date now = new Date();
        Interval interval = new Interval(now.getTime(),calendar.getTime().getTime());
        Period period = interval.toPeriod(PeriodType.weeks());
        res = String.valueOf(period.getWeeks())+context.getResources().getString(R.string.week_av);
        return res;
    }
}
