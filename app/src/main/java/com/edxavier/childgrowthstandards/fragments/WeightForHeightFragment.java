package com.edxavier.childgrowthstandards.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.balysv.materialripple.MaterialRippleLayout;
import com.edxavier.childgrowthstandards.R;
import com.edxavier.childgrowthstandards.db.ChildHistory;
import com.edxavier.childgrowthstandards.db.percentiles.WeightForHeight;
import com.edxavier.childgrowthstandards.helpers.MyTextView;
import com.edxavier.childgrowthstandards.helpers.constans.Gender;
import com.edxavier.childgrowthstandards.helpers.constans.Units;
import com.edxavier.childgrowthstandards.libs.formatter.LeftWeightValueFormatter;
import com.edxavier.childgrowthstandards.libs.formatter.RightWeightValueFormatter;
import com.edxavier.childgrowthstandards.libs.formatter.TopHeightValueFormatter;
import com.edxavier.childgrowthstandards.libs.marker.BmiMarkerView;
import com.edxavier.childgrowthstandards.libs.formatter.BmiValueFormatter;
import com.edxavier.childgrowthstandards.libs.ChartStyler;
import com.edxavier.childgrowthstandards.libs.formatter.XDaysValuesFormatter;
import com.edxavier.childgrowthstandards.libs.marker.WxHMarkerView;
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

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;


/**
 * A simple {@link Fragment} subclass.
 */
public class WeightForHeightFragment extends Fragment {
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

    public WeightForHeightFragment() {
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
        setHasOptionsMenu(true);
        percDesc.setText(getContext().getResources().getString(R.string.weight_for_height_desc));
        realm = Realm.getDefaultInstance();
        Bundle args = getArguments();
        showAds();
        zoomOut.setOnClickListener(view2 -> {
            chart.zoomOut();
            if (!hist.isEmpty()) {
                float x = hist.last().getHeight_cms();
                float y = hist.last().getWeight_pnds();
                chart.centerViewTo(x, y, chart.getAxisLeft().getAxisDependency());
            }
        });
        zoomIn.setOnClickListener(view3 -> {zoomIn();});


        hist = realm.where(ChildHistory.class)
                .greaterThan("height_cms", 40f)
                .equalTo("child.id", args.getString("id")).findAll().sort("living_days", Sort.ASCENDING);

        graphTitle.setText(getContext().getString(R.string.weightForlen));

        int edgesColor = getResources().getColor(R.color.md_red_500);
        int innnerEdgeColor = getResources().getColor(R.color.md_orange_500);
        int mediaColor = getResources().getColor(R.color.md_green_500);
        int measuereColro = getResources().getColor(R.color.md_pink_500);

        RealmResults<WeightForHeight> results = realm.where(WeightForHeight.class).findAll();

        RealmLineDataSet<WeightForHeight> mediansDataSet = new RealmLineDataSet<WeightForHeight>(results, "height", "median_girls");
        RealmLineDataSet<WeightForHeight> ninetySevenDataSet = new RealmLineDataSet<WeightForHeight>(results, "height", "ninetySeven_girls");
        RealmLineDataSet<WeightForHeight> thirdLineDataSet = new RealmLineDataSet<WeightForHeight>(results, "height", "third_girls");
        RealmLineDataSet<WeightForHeight> eightyFiveDataSet = new RealmLineDataSet<WeightForHeight>(results, "height", "eightyFive_girls");
        RealmLineDataSet<WeightForHeight> fifteenDataSet = new RealmLineDataSet<WeightForHeight>(results, "height", "fifteen_girlsv");
        chart = (LineChart) ChartStyler.setup(chart, getActivity());

        LineData lineData = null;
        if (!hist.isEmpty()) {
            if (hist.first().getChild().getGender() == Gender.MALE) {
                mediansDataSet = new RealmLineDataSet<WeightForHeight>(results, "height", "median_boys");
                ninetySevenDataSet = new RealmLineDataSet<WeightForHeight>(results, "height", "ninetySeven_boys");
                thirdLineDataSet = new RealmLineDataSet<WeightForHeight>(results, "height", "third_boys");
                eightyFiveDataSet = new RealmLineDataSet<WeightForHeight>(results, "height", "eightyFive_boys");
                fifteenDataSet = new RealmLineDataSet<WeightForHeight>(results, "height", "fifteen_boys");
                measuereColro = getResources().getColor(R.color.md_blue_500);
                chart.getAxisRight().setGridColor(getResources().getColor(R.color.md_blue_500_50));

            }
            RealmLineDataSet<ChildHistory> histLineDataSet = new RealmLineDataSet<ChildHistory>(hist, "height_cms", "weight_pnds");
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
        } else {
            zoomIn.setVisibility(View.GONE);
            zoomOut.setVisibility(View.GONE);
        }
        if (lineData != null)
            lineData.addDataSet(ChartStyler.setupCommonLineDataset(mediansDataSet, mediaColor, getResources().getString(R.string.p50)));
        else
            lineData = new LineData(ChartStyler.setupCommonLineDataset(mediansDataSet, mediaColor, getResources().getString(R.string.p50)));

        lineData.addDataSet(ChartStyler.setupCommonLineDataset(ninetySevenDataSet, edgesColor, getResources().getString(R.string.p97)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(eightyFiveDataSet, innnerEdgeColor, getResources().getString(R.string.p85)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(fifteenDataSet, innnerEdgeColor, getResources().getString(R.string.p15)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(thirdLineDataSet, edgesColor, getResources().getString(R.string.p3)));

        if(Prefs.getBoolean("show_marker", true))
            chart.setMarker(new WxHMarkerView(getActivity(), R.layout.tv_content));

        chart.setData(lineData);
        chart.setAutoScaleMinMaxEnabled(true);
        chart.getXAxis().setValueFormatter(new TopHeightValueFormatter());
        chart.getAxisLeft().setValueFormatter(new LeftWeightValueFormatter());
        chart.getAxisRight().setValueFormatter(new RightWeightValueFormatter());
        chart.getAxisRight().setAxisMinimum(1f);
        chart.getAxisLeft().setAxisMinimum(1f);
        chart.invalidate();
        if (!hist.isEmpty())
            centerGraph();

    }

    private void showAds() {
        AdRequest adRequest = new AdRequest.Builder().build();
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
    }

    private void zoomIn() {
        if (!hist.isEmpty()) {
            float x = hist.last().getHeight_cms();
            float y = hist.last().getWeight_pnds();
            chart.zoom(1.2f, 1.2f, x, y);
            chart.centerViewTo(x, y,chart.getAxisLeft().getAxisDependency() );
        }else
            chart.zoomIn();
    }

    public void centerGraph() {
        float x = hist.last().getHeight_cms();
        float y = hist.last().getWeight_pnds();
        chart.zoom(1.5f, 1.5f, x, y);
        chart.centerViewTo(x, y,chart.getAxisLeft().getAxisDependency()) ;
    }

    @Override
    public void onDestroy() {
        realm.close();
        super.onDestroy();
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ac_info:
                new MaterialDialog.Builder(getActivity())
                        .title(getContext().getResources().getString(R.string.info))
                        .content(getContext().getResources().getString(R.string.weight_for_height_desc))
                        .positiveText(R.string.ok)
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

