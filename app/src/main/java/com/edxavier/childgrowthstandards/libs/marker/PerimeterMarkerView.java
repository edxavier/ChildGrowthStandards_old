package com.edxavier.childgrowthstandards.libs.marker;

import android.content.Context;
import android.widget.TextView;

import com.edxavier.childgrowthstandards.R;
import com.edxavier.childgrowthstandards.helpers.Res;
import com.edxavier.childgrowthstandards.helpers.constans.Units;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.Locale;

/**
 * Created by Eder Xavier Rojas on 08/03/2017.
 */

public class PerimeterMarkerView extends MarkerView {

    private TextView tvContent;
    private TextView age;
    Context context;

    public PerimeterMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        this.context = context;
        tvContent = (TextView) findViewById(R.id.weight);
        age = (TextView) findViewById(R.id.age);
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        int len_unit = Integer.valueOf(Prefs.getString("height_unit", "2"));
        String und2 = "cm";
        float h = highlight.getY();
        if(len_unit == Units.INCH){
            h = Units.cm_to_inches(highlight.getY());
            und2 = " in";
        }
        tvContent.setText(context.getResources().getString(R.string.perimeter) +
                String.format(Locale.getDefault(), "%.1f", h) + und2); // set the entry-value as the display text
        age.setText(Res.getAgeString(highlight.getX(), context));
    }

    private MPPointF mOffset;

    @Override
    public MPPointF getOffset() {

        if(mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = new MPPointF(-(getWidth() / 2), - getHeight() - 20);
        }

        return mOffset;
    }
}
