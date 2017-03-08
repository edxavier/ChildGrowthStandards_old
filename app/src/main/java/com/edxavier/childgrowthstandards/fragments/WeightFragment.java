package com.edxavier.childgrowthstandards.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.edxavier.childgrowthstandards.R;
import com.edxavier.childgrowthstandards.db.ChildHistory;
import com.edxavier.childgrowthstandards.db.percentiles.WeightForAge;
import com.edxavier.childgrowthstandards.helpers.MyTextView;
import com.edxavier.childgrowthstandards.helpers.constans.Gender;
import com.edxavier.childgrowthstandards.helpers.constans.Units;
import com.edxavier.childgrowthstandards.libs.ChartStyler;
import com.edxavier.childgrowthstandards.libs.MyMarkerView;
import com.edxavier.childgrowthstandards.libs.RightWeightValueFormatter;
import com.edxavier.childgrowthstandards.libs.XDaysValuesFormatter;
import com.edxavier.childgrowthstandards.libs.LeftWeightValueFormatter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.MarkerView;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;


/**
 * A simple {@link Fragment} subclass.
 */
public class WeightFragment extends Fragment {


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
    Realm realm;

    public WeightFragment() {
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
        percDesc.setText(getContext().getResources().getString(R.string.weight_desc));

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
        zoomOut.setOnClickListener(view2 -> {
            chart.zoomOut();
        });
        zoomIn.setOnClickListener(view3 -> {
            zoomIn();
        });

        graphTitle.setText(getString(R.string.weightForage));
        admobContainer.addView(ads);

        int edgesColor = getResources().getColor(R.color.md_red_500);
        int innnerEdgeColor = getResources().getColor(R.color.md_orange_500);
        int mediaColor = getResources().getColor(R.color.md_green_500);
        int measuereColro = getResources().getColor(R.color.md_pink_500);
        realm = Realm.getDefaultInstance();
        Bundle args = getArguments();
        hist = realm.where(ChildHistory.class)
                .equalTo("child.id", args.getString("id"))
                .findAll().sort("living_days", Sort.ASCENDING);

        RealmResults<WeightForAge> results = realm.where(WeightForAge.class).findAll();
        ArrayList<Float> mods = new ArrayList<Float>();
        //para no sobrecargar el grafico de los primeros 1850 registros mostraremos cada 7 registros
        for (int i = 0; i < results.size(); i++) {
            if(i<1850) {
                if (i % 7 == 0) {
                    mods.add(results.get(i).getDay());
                }
            }else
                mods.add(results.get(i).getDay());
        }
        Float [] mods7 = new Float[mods.size()];
        mods7 = mods.toArray(mods7);
        results =  results.where().in("day", mods7).findAll();



        RealmLineDataSet<WeightForAge> mediansDataSet = new RealmLineDataSet<WeightForAge>(results, "day", "median_girls");;
        RealmLineDataSet<WeightForAge> ninetySevenDataSet = new RealmLineDataSet<WeightForAge>(results, "day", "ninetySeven_girls");
        RealmLineDataSet<WeightForAge> thirdLineDataSet = new RealmLineDataSet<WeightForAge>(results, "day", "third_girls");
        RealmLineDataSet<WeightForAge> eightyFiveDataSet = new RealmLineDataSet<WeightForAge>(results, "day", "eightyFive_girls");
        RealmLineDataSet<WeightForAge> fifteenDataSet = new RealmLineDataSet<WeightForAge>(results, "day", "fifteen_girls");
        chart = (LineChart) ChartStyler.setup(chart, getActivity());

        LineData lineData = null;
        if (!hist.isEmpty()) {
            if(hist.first().getChild().getGender()== Gender.MALE){
                mediansDataSet = new RealmLineDataSet<WeightForAge>(results, "day", "median_boys");
                ninetySevenDataSet = new RealmLineDataSet<WeightForAge>(results, "day", "ninetySeven_boys");
                thirdLineDataSet = new RealmLineDataSet<WeightForAge>(results, "day", "third_boys");
                eightyFiveDataSet = new RealmLineDataSet<WeightForAge>(results, "day", "eightyFive_boys");
                fifteenDataSet = new RealmLineDataSet<WeightForAge>(results, "day", "fifteen_boys");
                measuereColro = getResources().getColor(R.color.md_blue_500);
                chart.getAxisRight().setGridColor(getResources().getColor(R.color.md_blue_500_50));
            }
            RealmLineDataSet<ChildHistory> histLineDataSet = new RealmLineDataSet<ChildHistory>(hist, "living_days", "weight_pnds");
            histLineDataSet.setColors(measuereColro);
            histLineDataSet.setCircleColor(measuereColro);
            histLineDataSet.setCircleColorHole(getResources().getColor(R.color.md_blue_grey_500));
            histLineDataSet.setLabel(getResources().getString(R.string.measure));
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

        mediansDataSet.setColors(mediaColor);
        mediansDataSet.setDrawCircles(false);
        mediansDataSet.setLabel(getResources().getString(R.string.p50));
        mediansDataSet.setHighlightEnabled(false);
        mediansDataSet.setDrawHighlightIndicators(false);

        // set this to false to disable the drawing of highlight indicator (lines)
        mediansDataSet.setLineWidth(1);
        if (lineData != null)
            lineData.addDataSet(mediansDataSet);
        else
            lineData = new LineData(mediansDataSet);

        lineData.addDataSet(ChartStyler.setupCommonLineDataset(ninetySevenDataSet, edgesColor, getResources().getString(R.string.p97)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(eightyFiveDataSet, innnerEdgeColor, getResources().getString(R.string.p85)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(fifteenDataSet, innnerEdgeColor, getResources().getString(R.string.p15)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(thirdLineDataSet, edgesColor, getResources().getString(R.string.p3)));

        chart.setData(lineData);
        chart.setAutoScaleMinMaxEnabled(true);
        chart.getXAxis().setValueFormatter(new XDaysValuesFormatter(chart, getActivity()));
        chart.getAxisLeft().setValueFormatter(new LeftWeightValueFormatter());
        chart.getAxisRight().setValueFormatter(new RightWeightValueFormatter());
        chart.setMarker(new MyMarkerView(getActivity(), R.layout.tv_content));
//chart.setVisibleXRangeMaximum(1800);
        chart.invalidate();

        if (!hist.isEmpty())
            centerGraph();

    }


    public void centerGraph() {
        float x = hist.last().getLiving_days();
        float y = hist.last().getWeight_pnds();
        chart.zoom(3f, 2f, x, y);
    }

    public void zoomIn() {
        if (!hist.isEmpty()) {
            float x = hist.last().getLiving_days();
            float y = hist.last().getWeight_pnds();
            chart.zoom(2f, 1f, x, y);
        }else
            chart.zoomIn();
    }


    @Override
    public void onDestroy() {
        realm.close();
        super.onDestroy();
    }
}
