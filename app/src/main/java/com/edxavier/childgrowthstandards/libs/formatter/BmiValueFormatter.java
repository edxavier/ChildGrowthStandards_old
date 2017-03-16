package com.edxavier.childgrowthstandards.libs.formatter;

import android.content.Context;

import com.edxavier.childgrowthstandards.R;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.Locale;

/**
 * Created by Eder Xavier Rojas on 20/01/2017.
 */

public class BmiValueFormatter implements IAxisValueFormatter {
    public BmiValueFormatter(Context context) {
        Context context1 = context;
    }
    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return String.format(Locale.getDefault(), "%.1f", value);
    }
}
