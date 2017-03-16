package com.edxavier.childgrowthstandards.libs.marker;

import android.content.Context;
import android.widget.TextView;

import com.edxavier.childgrowthstandards.R;
import com.edxavier.childgrowthstandards.helpers.MyTextView;
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

public class WxHMarkerView extends MarkerView {

    private TextView tvContent;
    private MyTextView age;
    Context context;

    public WxHMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        this.context = context;
        tvContent = (TextView) findViewById(R.id.weight);
        age = (MyTextView) findViewById(R.id.age);
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        int wei_unit = Integer.valueOf(Prefs.getString("weight_unit", "0"));
        int len_unit = Integer.valueOf(Prefs.getString("height_unit", "2"));
        String und = "kg";
        String und2 = "cm";

        float h = highlight.getX();
        float w = highlight.getY();
        if(wei_unit == Units.POUND) {
            w = Units.kg_to_pnds(highlight.getY());
            und = "lb";
        }
        if(len_unit == Units.INCH){
            h = Units.cm_to_inches(highlight.getY());
            und2 = "in";
        }
        tvContent.setText(String.format(Locale.getDefault(), context.getResources().getString(R.string.weight) + " %.1f "+ und, w));
        age.setText(String.format(Locale.getDefault(), context.getResources().getString(R.string.height) + " %.1f "+ und2, h));
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
