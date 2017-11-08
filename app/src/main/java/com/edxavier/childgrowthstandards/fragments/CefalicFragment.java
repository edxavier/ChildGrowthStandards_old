package com.edxavier.childgrowthstandards.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import com.edxavier.childgrowthstandards.R;
import com.edxavier.childgrowthstandards.db.ChildHistory;
import com.edxavier.childgrowthstandards.db.percentiles.HeadCircForAge;
import com.edxavier.childgrowthstandards.helpers.constans.Gender;
import com.edxavier.childgrowthstandards.libs.ChartStyler;
import com.edxavier.childgrowthstandards.libs.formatter.LeftHeightValueFormatter;
import com.edxavier.childgrowthstandards.libs.formatter.RightHeightValueFormatter;
import com.edxavier.childgrowthstandards.libs.formatter.XDaysValuesFormatter;
import com.edxavier.childgrowthstandards.libs.marker.PerimeterMarkerView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.realm.implementation.RealmLineDataSet;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.pixplicity.easyprefs.library.Prefs;

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
    @BindView(R.id.zoom_in_btn)
    ImageButton zoomIn;
    @BindView(R.id.zoom_out_btn)
    ImageButton zoomOut;
    @BindView(R.id.graph_title)
    TextView graphTitle;
    @BindView(R.id.perc_desc)
    TextView percDesc;
    Realm realm;

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
        setHasOptionsMenu(true);
        percDesc.setText(getContext().getResources().getString(R.string.cefalic_desc));
        showAds();
        graphTitle.setText(getContext().getString(R.string.cefalic_perimeter_for_age));
        zoomOut.setOnClickListener(view2 -> {
            chart.zoomOut();
            if (!hist.isEmpty()) {
                float x = hist.last().getLiving_days();
                float y = hist.last().getHead_circ();
                chart.centerViewTo(x, y, chart.getAxisLeft().getAxisDependency());
            }
        });
        zoomIn.setOnClickListener(view3 -> {zoomIn();});

        int edgesColor = getResources().getColor(R.color.md_red_500);
        int innnerEdgeColor = getResources().getColor(R.color.md_orange_500);
        int mediaColor = getResources().getColor(R.color.md_green_500);
        int measuereColro = getResources().getColor(R.color.md_pink_500);
        realm = Realm.getDefaultInstance();
        Bundle args = getArguments();
        hist = realm.where(ChildHistory.class)
                .greaterThan("head_circ", 40f)
                .equalTo("child.id", args.getString("id")).findAll().sort("living_days", Sort.ASCENDING);

        RealmResults<HeadCircForAge> results = realm.where(HeadCircForAge.class).findAll();
        RealmLineDataSet<HeadCircForAge> mediansDataSet = new RealmLineDataSet<HeadCircForAge>(results, "day", "median_girls");
        RealmLineDataSet<HeadCircForAge> ninetySevenDataSet = new RealmLineDataSet<HeadCircForAge>(results, "day", "ninetySeven_girls");
        RealmLineDataSet<HeadCircForAge> thirdLineDataSet = new RealmLineDataSet<HeadCircForAge>(results, "day", "third_girls");
        RealmLineDataSet<HeadCircForAge> eightyFiveDataSet = new RealmLineDataSet<HeadCircForAge>(results, "day", "eightyFive_girls");
        RealmLineDataSet<HeadCircForAge> fifteenDataSet = new RealmLineDataSet<HeadCircForAge>(results, "day", "fifteen_girls");
        chart = (LineChart) ChartStyler.setup(chart, getActivity());

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

        if (lineData != null)
            lineData.addDataSet(ChartStyler.setupCommonLineDataset(mediansDataSet, mediaColor, getResources().getString(R.string.p50)));
        else
            lineData = new LineData(ChartStyler.setupCommonLineDataset(mediansDataSet, mediaColor, getResources().getString(R.string.p50)));

        lineData.addDataSet(ChartStyler.setupCommonLineDataset(ninetySevenDataSet, edgesColor, getResources().getString(R.string.p97)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(eightyFiveDataSet, innnerEdgeColor, getResources().getString(R.string.p85)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(fifteenDataSet, innnerEdgeColor, getResources().getString(R.string.p15)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(thirdLineDataSet, edgesColor, getResources().getString(R.string.p3)));

        if(Prefs.getBoolean("show_marker", true))
            chart.setMarker(new PerimeterMarkerView(getActivity(), R.layout.tv_content));

        chart.setData(lineData);
        chart.setAutoScaleMinMaxEnabled(true);
        chart.getXAxis().setValueFormatter(new XDaysValuesFormatter(chart, getActivity()));
        chart.getAxisLeft().setValueFormatter(new LeftHeightValueFormatter());
        chart.getAxisRight().setValueFormatter(new RightHeightValueFormatter());
        chart.getAxisRight().setAxisMinimum(30f);
        chart.getAxisLeft().setAxisMinimum(30f);
        chart.invalidate();

        if (!hist.isEmpty())
            centerGraph();

    }
    private void showAds() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();
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

    public void centerGraph() {
        float x = hist.last().getLiving_days();
        float y = hist.last().getHead_circ();
        chart.zoom(1.5f, 1.5f, x, y);
        chart.centerViewTo(x, y,chart.getAxisLeft().getAxisDependency()) ;
    }
    private void zoomIn() {
        if (!hist.isEmpty()) {
            float x = hist.last().getLiving_days();
            float y = hist.last().getHead_circ();
            chart.zoom(1.2f, 1.2f, x, y);
            chart.centerViewTo(x, y,chart.getAxisLeft().getAxisDependency() );
        }else
            chart.zoomIn();
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
                        .content(getContext().getResources().getString(R.string.cefalic_desc))
                        .positiveText(R.string.ok)
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
