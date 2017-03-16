package com.edxavier.childgrowthstandards.libs.formatter;

import com.edxavier.childgrowthstandards.helpers.constans.Units;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.Locale;

/**
 * Created by Eder Xavier Rojas on 20/01/2017.
 */

public class LeftHeightValueFormatter implements IAxisValueFormatter {

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return String.format(Locale.getDefault(), "%.0fin", Units.cm_to_inches(value));
    }
}
