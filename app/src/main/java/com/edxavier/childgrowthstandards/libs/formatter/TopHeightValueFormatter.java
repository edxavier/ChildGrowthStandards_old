package com.edxavier.childgrowthstandards.libs.formatter;

import com.edxavier.childgrowthstandards.helpers.constans.Units;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.Locale;

/**
 * Created by Eder Xavier Rojas on 20/01/2017.
 */

public class TopHeightValueFormatter implements IAxisValueFormatter {

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        int len_unit = Integer.valueOf(Prefs.getString("height_unit", "2"));
        String und2 = "cm";
        if(len_unit == Units.INCH){
            value = Units.cm_to_inches(value);
            und2 = "in";
        }
        return String.format(Locale.getDefault(), "%.0f"+ und2, value);
    }
}
