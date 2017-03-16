package com.edxavier.childgrowthstandards.fragments;


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

import com.afollestad.materialdialogs.MaterialDialog;
import com.balysv.materialripple.MaterialRippleLayout;
import com.edxavier.childgrowthstandards.R;
import com.edxavier.childgrowthstandards.db.ChildHistory;
import com.edxavier.childgrowthstandards.db.percentiles.BmiForAge;
import com.edxavier.childgrowthstandards.helpers.MyTextView;
import com.edxavier.childgrowthstandards.helpers.constans.Gender;
import com.edxavier.childgrowthstandards.libs.marker.BmiMarkerView;
import com.edxavier.childgrowthstandards.libs.formatter.BmiValueFormatter;
import com.edxavier.childgrowthstandards.libs.ChartStyler;
import com.edxavier.childgrowthstandards.libs.formatter.XDaysValuesFormatter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.realm.implementation.RealmLineDataSet;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * A simple {@link Fragment} subclass.
 */
public class ImcFragment extends Fragment {


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

    public ImcFragment() {}

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
        percDesc.setText(getContext().getResources().getString(R.string.imc_desc));
        showAds();
        zoomOut.setOnClickListener(view2 -> {
            chart.zoomOut();
            if (!hist.isEmpty()) {
                float x = hist.last().getLiving_days();
                float y = hist.last().getBmi();
                chart.centerViewTo(x, y, chart.getAxisLeft().getAxisDependency());
            }
        });
        zoomIn.setOnClickListener(view3 -> {zoomIn();});
        graphTitle.setText(getContext().getString(R.string.imc_for_age));

        int edgesColor = getResources().getColor(R.color.md_red_500);
        int innnerEdgeColor = getResources().getColor(R.color.md_orange_500);
        int mediaColor = getResources().getColor(R.color.md_green_500);
        int measuereColro = getResources().getColor(R.color.md_pink_500);
        realm = Realm.getDefaultInstance();
        Bundle args = getArguments();
        hist = realm.where(ChildHistory.class)
                .equalTo("child.id", args.getString("id")).findAll().sort("living_days", Sort.ASCENDING);

        RealmResults<BmiForAge> results = realm.where(BmiForAge.class).findAll();
        ArrayList<Float> mods = new ArrayList<Float>();
        //para no sobrecargar el grafico de los primeros 1850 registros mostraremos cada 7 registros
        for (int i = 0; i < results.size(); i++) {
            if(i<1850) {
                if (i % 7 == 0)
                    mods.add(results.get(i).getDay());
            }else
                mods.add(results.get(i).getDay());
        }
        Float [] mods7 = new Float[mods.size()];
        mods7 = mods.toArray(mods7);
        results =  results.where().in("day", mods7).findAll();

        RealmLineDataSet<BmiForAge> mediansDataSet = new RealmLineDataSet<BmiForAge>(results, "day", "median_girls");
        RealmLineDataSet<BmiForAge> ninetySevenDataSet = new RealmLineDataSet<BmiForAge>(results, "day", "ninetySeven_girls");
        RealmLineDataSet<BmiForAge> thirdLineDataSet = new RealmLineDataSet<BmiForAge>(results, "day", "third_girls");
        RealmLineDataSet<BmiForAge> eightyFiveDataSet = new RealmLineDataSet<BmiForAge>(results, "day", "eightyFive_girls");
        RealmLineDataSet<BmiForAge> fifteenDataSet = new RealmLineDataSet<BmiForAge>(results, "day", "fifteen_girls");
        chart = (LineChart) ChartStyler.setup(chart, getActivity());
        LineData lineData = null;
        if (!hist.isEmpty()) {
            if(hist.first().getChild().getGender()== Gender.MALE){
                mediansDataSet = new RealmLineDataSet<BmiForAge>(results, "day", "median_boys");
                ninetySevenDataSet = new RealmLineDataSet<BmiForAge>(results, "day", "ninetySeven_boys");
                thirdLineDataSet = new RealmLineDataSet<BmiForAge>(results, "day", "third_boys");
                eightyFiveDataSet = new RealmLineDataSet<BmiForAge>(results, "day", "eightyFive_boys");
                fifteenDataSet = new RealmLineDataSet<BmiForAge>(results, "day", "fifteen_boys");
                measuereColro = getResources().getColor(R.color.md_blue_500);
                chart.getAxisRight().setGridColor(getResources().getColor(R.color.md_blue_500_50));
            }
            RealmLineDataSet<ChildHistory> histLineDataSet = new RealmLineDataSet<ChildHistory>(hist, "living_days", "bmi");
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
            chart.setMarker(new BmiMarkerView(getActivity()));

        chart.setData(lineData);
        chart.setAutoScaleMinMaxEnabled(true);
        chart.getXAxis().setValueFormatter(new XDaysValuesFormatter(chart, getActivity()));
        chart.getAxisLeft().setValueFormatter(new BmiValueFormatter(getActivity()));
        chart.getAxisRight().setValueFormatter(new BmiValueFormatter(getActivity()));
        chart.getAxisRight().setAxisMinimum(8f);
        chart.getAxisLeft().setAxisMinimum(8f);
        chart.invalidate();
        if (!hist.isEmpty())
            centerGraph();

    }

    private void showAds() {
        AdRequest adRequest = new AdRequest.Builder()
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
    }

    private void zoomIn() {
        if (!hist.isEmpty()) {
            float x = hist.last().getLiving_days();
            float y = hist.last().getBmi();
            chart.zoom(1.2f, 1.2f, x, y);
            chart.centerViewTo(x, y,chart.getAxisLeft().getAxisDependency() );
        }else
            chart.zoomIn();
    }

    public void centerGraph() {
        float x = hist.last().getLiving_days();
        float y = hist.last().getBmi();
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
                        .content(getContext().getResources().getString(R.string.imc_desc))
                        .positiveText(R.string.ok)
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
