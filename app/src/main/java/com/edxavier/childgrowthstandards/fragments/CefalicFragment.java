package com.edxavier.childgrowthstandards.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.edxavier.childgrowthstandards.R;
import com.edxavier.childgrowthstandards.db.ChildHistory;
import com.edxavier.childgrowthstandards.db.percentiles.HeadCircForAge;
import com.edxavier.childgrowthstandards.db.percentiles.HeightForAge;
import com.edxavier.childgrowthstandards.db.percentiles.WeightForAge;
import com.edxavier.childgrowthstandards.helpers.MyTextView;
import com.edxavier.childgrowthstandards.helpers.constans.Gender;
import com.edxavier.childgrowthstandards.helpers.constans.Units;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.realm.implementation.RealmLineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.pixplicity.easyprefs.library.Prefs;

import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;


/**
 * A simple {@link Fragment} subclass.
 */
public class CefalicFragment extends Fragment {


    @BindView(R.id.chart)
    LineChart chart;
    RealmResults<ChildHistory> hist;
    @BindView(R.id.admob_container)
    LinearLayout admobContainer;
    @BindView(R.id.zoom_in)
    MaterialRippleLayout zoomIn;
    @BindView(R.id.zoom_out)
    MaterialRippleLayout zoomOut;
    @BindView(R.id.graph_title)
    MyTextView graphTitle;
    @BindView(R.id.zoom_in_btn)
    ImageButton zoomInBtn;
    @BindView(R.id.zoom_out_btn)
    ImageButton zoomOutBtn;
    @BindView(R.id.perc_desc)
    MyTextView percDesc;

    public CefalicFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_weight, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        percDesc.setText(getContext().getResources().getString(R.string.cefalic_desc));

        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("45D8AEB3B66116F8F24E001927292BD5")
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        //AdRequest adRequest = new AdRequest.Builder().build();

        NativeExpressAdView ads = new NativeExpressAdView(getActivity());
        ads.setAdSize(new AdSize(300, 132));
        ads.setAdUnitId(getContext().getResources().getString(R.string.admob_m001));
        ads.loadAd(adRequest);
        ads.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                admobContainer.setVisibility(View.VISIBLE);
            }
        });

        admobContainer.addView(ads);
        centerGraph();

        graphTitle.setText(getContext().getString(R.string.cefalic_perimeter_for_age));

        int edgesColor = getResources().getColor(R.color.md_red_500);
        int innnerEdgeColor = getResources().getColor(R.color.md_orange_500);
        int mediaColor = getResources().getColor(R.color.md_green_500);
        int measuereColro = getResources().getColor(R.color.md_pink_500);
        Realm realm = Realm.getDefaultInstance();
        Bundle args = getArguments();
        hist = realm.where(ChildHistory.class)
                .greaterThan("head_circ", 40f)
                .equalTo("child.id", args.getString("id")).findAll().sort("living_days", Sort.ASCENDING);

        final RealmResults<HeadCircForAge> results = realm.where(HeadCircForAge.class).findAll();
        RealmLineDataSet<HeadCircForAge> mediansDataSet = new RealmLineDataSet<HeadCircForAge>(results, "day", "median_girls");;
        RealmLineDataSet<HeadCircForAge> ninetySevenDataSet = new RealmLineDataSet<HeadCircForAge>(results, "day", "ninetySeven_girls");
        RealmLineDataSet<HeadCircForAge> thirdLineDataSet = new RealmLineDataSet<HeadCircForAge>(results, "day", "third_girls");
        RealmLineDataSet<HeadCircForAge> eightyFiveDataSet = new RealmLineDataSet<HeadCircForAge>(results, "day", "eightyFive_girls");
        RealmLineDataSet<HeadCircForAge> fifteenDataSet = new RealmLineDataSet<HeadCircForAge>(results, "day", "fifteen_girls");
        chart.setGridBackgroundColor(getResources().getColor(R.color.md_grey_500_25));
        chart.getAxisRight().setGridColor(getResources().getColor(R.color.md_pink_500_50));

        LineData lineData = null;
        if (!hist.isEmpty()) {
            if(hist.first().getChild().getGender()== Gender.MALE){
                mediansDataSet = new RealmLineDataSet<HeadCircForAge>(results, "day", "median_boys");
                ninetySevenDataSet = new RealmLineDataSet<HeadCircForAge>(results, "day", "ninetySeven_boys");
                thirdLineDataSet = new RealmLineDataSet<HeadCircForAge>(results, "day", "third_boys");
                eightyFiveDataSet = new RealmLineDataSet<HeadCircForAge>(results, "day", "eightyFive_boys");
                fifteenDataSet = new RealmLineDataSet<HeadCircForAge>(results, "day", "fifteen_boys");
                measuereColro = getResources().getColor(R.color.md_blue_500);
                chart.getAxisRight().setGridColor(getResources().getColor(R.color.md_blue_500_50));

            }
            RealmLineDataSet<ChildHistory> histLineDataSet = new RealmLineDataSet<ChildHistory>(hist, "living_days", "head_circ");
            histLineDataSet.setColors(new int[]{measuereColro});
            histLineDataSet.setCircleColor(measuereColro);
            histLineDataSet.setCircleColorHole(getResources().getColor(R.color.md_blue_grey_500));

            histLineDataSet.setLabel("Medida");
            histLineDataSet.setHighlightEnabled(true);
            // set this to false to disable the drawing of highlight indicator (lines)
            histLineDataSet.setDrawHighlightIndicators(true);
            histLineDataSet.setHighLightColor(measuereColro);
            histLineDataSet.setHighlightLineWidth(0.99f);
            histLineDataSet.setLineWidth(2);
            lineData = new LineData(histLineDataSet);


        }else {
            zoomIn.setVisibility(View.GONE);
            zoomOut.setVisibility(View.GONE);
        }

/*
        AxisValueFormatter formatter = new AxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.format(Locale.getDefault(), "%.0f cm ", value);
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        };


        AxisValueFormatter formatter_right = new AxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                return String.format(Locale.getDefault(), "%.1f in", Units.cm_to_inches(value));
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        };

        chart.getAxisLeft().setValueFormatter(formatter);
        chart.getAxisRight().setValueFormatter(formatter_right);

*/


        mediansDataSet.setColors(new int[]{mediaColor});
        mediansDataSet.setDrawCircles(false);
        mediansDataSet.setLabel("P50");
        mediansDataSet.setHighlightEnabled(false);
        mediansDataSet.setDrawHighlightIndicators(false);

        // set this to false to disable the drawing of highlight indicator (lines)
        mediansDataSet.setLineWidth(2);
        if (lineData != null)
            lineData.addDataSet(mediansDataSet);
        else
            lineData = new LineData(mediansDataSet);

        ninetySevenDataSet.setDrawCircles(false);
        ninetySevenDataSet.setColors(new int[]{edgesColor});
        ninetySevenDataSet.setLineWidth(2);
        ninetySevenDataSet.enableDashedLine(24, 45, 0);
        ninetySevenDataSet.setLabel("P3");
        ninetySevenDataSet.setDrawHighlightIndicators(false);
        lineData.addDataSet(ninetySevenDataSet);

        thirdLineDataSet.setDrawCircles(false);
        thirdLineDataSet.setColors(new int[]{edgesColor});
        thirdLineDataSet.setLabel("P15");
        thirdLineDataSet.setLineWidth(2);
        thirdLineDataSet.enableDashedLine(24, 45, 0);
        lineData.addDataSet(thirdLineDataSet);


        eightyFiveDataSet.setDrawCircles(false);
        eightyFiveDataSet.setColors(new int[]{innnerEdgeColor});
        eightyFiveDataSet.setLineWidth(2);
        eightyFiveDataSet.setLabel("P85");
        eightyFiveDataSet.enableDashedLine(24, 45, 0);
        eightyFiveDataSet.setDrawHighlightIndicators(false);
        lineData.addDataSet(eightyFiveDataSet);

        fifteenDataSet.setColors(new int[]{innnerEdgeColor});
        fifteenDataSet.setDrawCircles(false);
        fifteenDataSet.setColors(new int[]{innnerEdgeColor});
        fifteenDataSet.setLineWidth(2);
        fifteenDataSet.enableDashedLine(24, 45, 0);
        fifteenDataSet.setLabel("P15");
        fifteenDataSet.setDrawHighlightIndicators(false);

        lineData.addDataSet(fifteenDataSet);


        chart.setDrawGridBackground(true);
        chart.getLegend().setEnabled(true);
        chart.animateXY(1900, 0);
        chart.getXAxis().setDrawGridLines(false);
        //CustomMarkerView mv = new CustomMarkerView(this, R.layout.tv_content);

        // set the marker to the chart
        //chart.setMarkerView(mv);
        //chart.setHighlightPerDragEnabled(true);
        // chart.setHighlightPerTapEnabled(true);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.TOP);
        chart.getXAxis().setTextColor(getContext().getResources().getColor(R.color.secondary_text));
        chart.getXAxis().setDrawLabels(true);
        chart.getXAxis().setLabelRotationAngle(20);
        chart.getXAxis().setGranularity(1f);
        //chart.getXAxis().setValueFormatter(new DayAxisValueFormatter(chart));
        //chart.getAxisLeft().setValueFormatter(formatter);
        chart.getAxisRight().setTextColor(getContext().getResources().getColor(R.color.secondary_text));
        chart.getAxisLeft().setTextColor(getContext().getResources().getColor(R.color.secondary_text));

        if(Prefs.getBoolean("show_marker", true)) {
            CefalicFragment.CustomMarkerView mv = new CefalicFragment.CustomMarkerView(getActivity(), R.layout.tv_content);
            chart.setMarkerView(mv);
        }

        chart.setDrawMarkerViews(true);
        chart.setData(lineData);
        chart.setNoDataText("No hay datos graficos disponibles");
        chart.setKeepPositionOnRotation(true);
        //chart.setDescription("");
        chart.zoomOut();
        chart.invalidate();

    }


    public void centerGraph() {
        zoomIn.setOnClickListener(view -> {
            float x = hist.last().getLiving_days();
            float y = hist.last().getHeight_cms();
            //chart.zoomAndCenter(3f, 2f, x, y, null);
        });
        zoomOut.setOnClickListener(view -> {
            chart.zoomOut();
        });

    }


    public class CustomMarkerView extends MarkerView {

        private TextView tvContent;
        private MyTextView age;

        public CustomMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            // this markerview only displays a textview
            tvContent = (TextView) findViewById(R.id.weight);
            age = (MyTextView) findViewById(R.id.age);
        }

        // callbacks everytime the MyMarkerView is redrawn, can be used to update the
        // content (user-interface)
        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            int len_unit = Integer.valueOf(Prefs.getString("height_unit", "2"));
            String und2 = " cm";
            float h = highlight.getY();
            if(len_unit == Units.INCH){
                h = Units.cm_to_inches(highlight.getY());
                und2 = " in";
            }
            tvContent.setText(getString(R.string.perimeter) +
                    String.format(Locale.getDefault(), "%.1f", h) +und2); // set the entry-value as the display text
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, (int) highlight.getX());
            Date now = new Date();
            Interval interval = new Interval(now.getTime(), calendar.getTime().getTime());
            Period p = interval.toPeriod(PeriodType.yearMonthDay());
            age.setText(getString(R.string.age) + p.getYears() + getString(R.string.years)
                    + p.getMonths() + getString(R.string.months) + p.getDays() + getString(R.string.days));
        }
/*
        @Override
        public int getXOffset(float xpos) {
            // this will center the marker-view horizontally
            return -(getWidth() / 2);
        }

        @Override
        public int getYOffset(float ypos) {
            // this will cause the marker-view to be above the selected value
            return -getHeight() - 16;
        }*/

    }

}
