package com.edxavier.childgrowthstandards.helpers;

import android.content.Context;

import com.edxavier.childgrowthstandards.R;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Eder Xavier Rojas on 18/10/2016.
 */

public class Res {
    public static String getStr(int ressourceId, Context context){
        return context.getResources().getString(ressourceId);
    }

    public static String getAgeString(Date age, Context context) {
        String ageString = "";
        LocalDate birthdate =  new LocalDate(age);

        Period p = new Period(birthdate, new LocalDate(), PeriodType.yearMonthDay());
        if (p.getYears() == 1)
            ageString +=  p.getYears() + context.getString(R.string.year);
        else if (p.getYears() > 1)
            ageString +=  p.getYears() + context.getString(R.string.years);
        if (p.getMonths() == 1)
            ageString +=  p.getMonths() + context.getString(R.string.month);
        else if (p.getMonths() > 1)
            ageString +=  p.getMonths() + context.getString(R.string.months);

        if (p.getDays() == 1)
            ageString +=  p.getDays() + context.getString(R.string.day);
        else if (p.getDays() >= 1)
            ageString +=  p.getDays() + context.getString(R.string.days);
        return ageString;
    }

    public static String getAgeString(float days, Context context) {
        String ageString = "";
        LocalDate birthdate = new LocalDate();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, (int) days);

        Period p = new Period(birthdate, new LocalDate(calendar.getTime()), PeriodType.yearMonthDay());
        if (p.getYears() == 1)
            ageString +=  p.getYears() + context.getString(R.string.year);
        else if (p.getYears() > 1)
            ageString +=  p.getYears() + context.getString(R.string.years);
        if (p.getMonths() == 1)
            ageString +=  p.getMonths() + context.getString(R.string.month);
        else if (p.getMonths() > 1)
            ageString +=  p.getMonths() +context.getString(R.string.months);

        if (p.getDays() == 1)
            ageString +=  p.getDays() + context.getString(R.string.day);
        else if (p.getDays() >= 1)
            ageString +=  p.getDays() + context.getString(R.string.days);
        return ageString;
    }
}
