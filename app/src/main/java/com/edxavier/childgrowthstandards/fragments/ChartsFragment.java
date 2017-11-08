package com.edxavier.childgrowthstandards.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.edxavier.childgrowthstandards.R;
import com.edxavier.childgrowthstandards.db.Child;
import com.edxavier.childgrowthstandards.db.ChildHistory;
import com.edxavier.childgrowthstandards.db.percentiles.BmiForAge;
import com.edxavier.childgrowthstandards.db.percentiles.HeadCircForAge;
import com.edxavier.childgrowthstandards.db.percentiles.HeightForAge;
import com.edxavier.childgrowthstandards.db.percentiles.WeightForAge;
import com.edxavier.childgrowthstandards.db.percentiles.WeightForHeight;
import com.edxavier.childgrowthstandards.helpers.ChartStyler;
import com.edxavier.childgrowthstandards.helpers.constans.Gender;
import com.edxavier.childgrowthstandards.libs.QuickChart;
import com.edxavier.childgrowthstandards.libs.formatter.LeftHeightValueFormatter;
import com.edxavier.childgrowthstandards.libs.formatter.LeftWeightValueFormatter;
import com.edxavier.childgrowthstandards.libs.formatter.RightHeightValueFormatter;
import com.edxavier.childgrowthstandards.libs.formatter.RightWeightValueFormatter;
import com.edxavier.childgrowthstandards.libs.formatter.TopHeightValueFormatter;
import com.edxavier.childgrowthstandards.libs.formatter.XDaysValuesFormatter;
import com.edxavier.childgrowthstandards.libs.marker.BmiMarkerView;
import com.edxavier.childgrowthstandards.libs.marker.HeightMarkerView;
import com.edxavier.childgrowthstandards.libs.marker.MyMarkerView;
import com.edxavier.childgrowthstandards.libs.marker.PerimeterMarkerView;
import com.edxavier.childgrowthstandards.libs.marker.WxHMarkerView;
import com.github.mikephil.charting.charts.LineChart;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.pixplicity.easyprefs.library.Prefs;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChartsFragment extends Fragment {


    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.chart)
    LineChart chart;
    Unbinder unbinder;
    @BindView(R.id.zoom_in)
    FloatingActionButton zoomIn;
    @BindView(R.id.chart_type)
    FloatingActionButton chartType;
    @BindView(R.id.zoom_out)
    FloatingActionButton zoomOut;
    @BindView(R.id.cardView)
    CardView cardView;
    private Bundle args;
    private Realm realm;
    private Child children;
    private RealmResults<ChildHistory> recordHist;
    private int selectedGraph;
    private float vX;
    private float vY;
    String help ="";
    public ChartsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_charts, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        args = getArguments();
        realm = Realm.getDefaultInstance();
        children = realm.where(Child.class).equalTo("id", args.getString("id")).findFirst();
        recordHist = realm.where(ChildHistory.class)
                //.greaterThan("weight_pnds", 0f)
                //.lessThan("weight_pnds", 70f)
                .equalTo("child.id", args.getString("id"))
                .findAll().sort("living_days", Sort.ASCENDING);
        setupAds();
        setWeightChart();
    }

    private void setupAds() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                try {
                    adView.setVisibility(View.VISIBLE);
                }catch (Exception ignored){}
            }
        });
        adView.loadAd(adRequest);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        realm.close();
    }

    void setWeightChart(){
        help = getContext().getResources().getString(R.string.weight_desc);
        if(!recordHist.isEmpty()) {
            vX = recordHist.last().getLiving_days();
            vY = recordHist.last().getWeight_pnds();
        }
        LocalDate birthdate = new LocalDate(children.getBirthday());
        Period p = new Period(birthdate, new LocalDate(), PeriodType.days());
        String days = String.valueOf(p.getDays()+200);
        chart = (LineChart) ChartStyler.setup(chart, getActivity(), getString(R.string.weightForage));
        RealmResults<WeightForAge> results = realm.where(WeightForAge.class).lessThan("day", Float.valueOf(days)).findAll();
        chart.setData(QuickChart.getWeigtPercentiles(results, children.getGender(), getContext()));

        if(!recordHist.isEmpty()) {
            chart.getData().addDataSet(QuickChart.getHistoryLineDataset(
                    recordHist.where().greaterThan("weight_pnds", 0f).findAll(),
                    getContext(), "living_days", "weight_pnds"));
        }
        chart.getXAxis().setValueFormatter(new XDaysValuesFormatter(chart, getContext()));
        chart.getAxisLeft().setValueFormatter(new LeftWeightValueFormatter());
        chart.getAxisRight().setValueFormatter(new RightWeightValueFormatter());
        if(Prefs.getBoolean("show_marker", true))
            chart.setMarker(new MyMarkerView(getActivity(), R.layout.tv_content));

        chart.invalidate();
    }

    @OnClick({R.id.zoom_in, R.id.chart_type, R.id.zoom_out})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.zoom_in:
                try {
                    if (!recordHist.isEmpty()) {
                        //chart.zoom(1.2f, 1.2f, x, y);
                        chart.centerViewTo(vX, vY,chart.getAxisLeft().getAxisDependency() );
                    }
                    chart.zoomIn();
                }catch (Exception ignored){}
                break;
            case R.id.chart_type:
                new MaterialDialog.Builder(getContext())
                        .title("Seleccione el grafico")
                        .items(R.array.chart_options)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                LocalDate birthdate;
                                String days;
                                Period p;
                                selectedGraph = which;
                                switch (which) {
                                    //Peso para la edad
                                    case 0:
                                        setWeightChart();
                                        break;
                                    //Altura para la edad
                                    case 1:
                                        help = getContext().getResources().getString(R.string.height_desc);
                                        if(!recordHist.isEmpty()) {
                                            vX = recordHist.last().getLiving_days();
                                            vY = recordHist.last().getHeight_cms();
                                        }
                                        birthdate = new LocalDate(children.getBirthday());
                                        p = new Period(birthdate, new LocalDate(), PeriodType.days());
                                        days = String.valueOf(p.getDays()+200);
                                        chart = (LineChart) ChartStyler.setup(chart, getActivity(), getString(R.string.len_for_age));
                                        RealmResults<HeightForAge> results = realm.where(HeightForAge.class).lessThan("day", Float.valueOf(days)).findAll();
                                        chart.setData(QuickChart.getHeigtPercentiles(results, children.getGender(), getContext()));
                                        if(!recordHist.isEmpty()) {
                                            chart.getData().addDataSet(QuickChart.getHistoryLineDataset(
                                                    recordHist.where().greaterThan("height_cms", 10f).findAll(),
                                                    getContext(), "living_days", "height_cms"));
                                        }
                                        chart.getXAxis().setValueFormatter(new XDaysValuesFormatter(chart, getContext()));
                                        chart.getAxisLeft().setValueFormatter(new LeftHeightValueFormatter());
                                        chart.getAxisRight().setValueFormatter(new RightHeightValueFormatter());
                                        chart.getAxisRight().setAxisMinimum(40f);
                                        chart.getAxisLeft().setAxisMinimum(40f);
                                        if(Prefs.getBoolean("show_marker", true))
                                            chart.setMarker(new HeightMarkerView(getActivity(), R.layout.tv_content));
                                        chart.invalidate();
                                        break;
                                    //Peso para la altura
                                    case 2:
                                        help = getContext().getResources().getString(R.string.weight_for_height_desc);
                                        if(!recordHist.isEmpty()) {
                                            vX = recordHist.last().getHeight_cms();
                                            vY = recordHist.last().getWeight_pnds();
                                        }
                                        birthdate = new LocalDate(children.getBirthday());
                                        p = new Period(birthdate, new LocalDate(), PeriodType.days());
                                        days = String.valueOf(p.getDays());
                                        RealmResults<HeightForAge> hfa = realm.where(HeightForAge.class).lessThan("day", Float.valueOf(days))
                                                .findAllSorted("day", Sort.DESCENDING);
                                        String h = "170";
                                        if(hfa.size()>0) {
                                            h = String.valueOf(hfa.first().getNinetySeven_boys() + 6);
                                        }

                                        chart = (LineChart) ChartStyler.setup(chart, getActivity(), getString(R.string.weightForlen));
                                        RealmResults<WeightForHeight> results2 = realm.where(WeightForHeight.class)
                                                .lessThan("height", Float.valueOf(h)).findAll();
                                        chart.setData(QuickChart.getWeigt_x_HeightPercentiles(results2, children.getGender(), getContext()));
                                        if(!recordHist.isEmpty()) {
                                            chart.getData().addDataSet(QuickChart.getHistoryLineDataset(
                                                    recordHist.where().greaterThan("weight_pnds", 0f)
                                                            .greaterThan("height_cms", 10f).findAll(),
                                                    getContext(), "height_cms", "weight_pnds"));
                                        }
                                        chart.getXAxis().setValueFormatter(new TopHeightValueFormatter());

                                        chart.getAxisLeft().setValueFormatter(new LeftWeightValueFormatter());
                                        chart.getAxisRight().setValueFormatter(new RightWeightValueFormatter());
                                        chart.getAxisRight().setAxisMinimum(2f);
                                        chart.getAxisLeft().setAxisMinimum(2f);
                                        if(Prefs.getBoolean("show_marker", true))
                                            chart.setMarker(new WxHMarkerView(getActivity(), R.layout.tv_content));

                                        chart.invalidate();
                                        break;
                                    case 3:
                                        help = getContext().getResources().getString(R.string.imc_desc);
                                        if(!recordHist.isEmpty()) {
                                            vX = recordHist.last().getLiving_days();
                                            vY = recordHist.last().getBmi();
                                        }
                                        birthdate = new LocalDate(children.getBirthday());
                                        p = new Period(birthdate, new LocalDate(), PeriodType.days());
                                        days = String.valueOf(p.getDays()+200);
                                        chart = (LineChart) ChartStyler.setup(chart, getActivity(), getString(R.string.imc_for_age));
                                        RealmResults<BmiForAge> results3 = realm.where(BmiForAge.class).lessThan("day", Float.valueOf(days)).findAll();
                                        chart.setData(QuickChart.getIMCPercentiles(results3, children.getGender(), getContext()));
                                        if(!recordHist.isEmpty()) {
                                            chart.getData().addDataSet(QuickChart.getHistoryLineDataset(
                                                    recordHist.where().greaterThan("bmi", 0f).findAll(),
                                                    getContext(), "living_days", "bmi"));
                                        }
                                        chart.getXAxis().setValueFormatter(new XDaysValuesFormatter(chart, getContext()));
                                        chart.getAxisLeft().setValueFormatter(new LeftHeightValueFormatter());
                                        chart.getAxisRight().setValueFormatter(new RightHeightValueFormatter());
                                        chart.getAxisRight().setAxisMinimum(9f);
                                        chart.getAxisLeft().setAxisMinimum(9f);
                                        if(Prefs.getBoolean("show_marker", true))
                                            chart.setMarker(new BmiMarkerView(getActivity()));
                                        chart.invalidate();
                                        break;
                                    case 4:
                                        help = getContext().getResources().getString(R.string.cefalic_desc);
                                        if(!recordHist.isEmpty()) {
                                            vX = recordHist.last().getLiving_days();
                                            vY = recordHist.last().getHead_circ();
                                        }
                                        birthdate = new LocalDate(children.getBirthday());
                                        p = new Period(birthdate, new LocalDate(), PeriodType.days());
                                        days = String.valueOf(p.getDays()+200);
                                        chart = (LineChart) ChartStyler.setup(chart, getActivity(), getString(R.string.cefalic_perimeter_for_age));
                                        RealmResults<HeadCircForAge> results4 = realm.where(HeadCircForAge.class)
                                                .lessThan("day", Float.valueOf(days))
                                                .findAll();
                                        chart.setData(QuickChart.getPerimeterPercentiles(results4, children.getGender(), getContext()));
                                        if(!recordHist.isEmpty()) {
                                            chart.getData().addDataSet(QuickChart.getHistoryLineDataset(
                                                    recordHist.where().greaterThan("head_circ", 10f).findAll(),
                                                    getContext(), "living_days", "head_circ"));
                                        }
                                        chart.getXAxis().setValueFormatter(new XDaysValuesFormatter(chart, getContext()));
                                        chart.getAxisLeft().setValueFormatter(new LeftHeightValueFormatter());
                                        chart.getAxisRight().setValueFormatter(new RightHeightValueFormatter());
                                        chart.getAxisRight().setAxisMinimum(30f);
                                        chart.getAxisLeft().setAxisMinimum(30f);
                                        if(Prefs.getBoolean("show_marker", true))
                                            chart.setMarker(new PerimeterMarkerView(getActivity(), R.layout.tv_content));
                                        chart.invalidate();
                                        break;
                                }
                            }
                        })
                        .show();
                break;
            case R.id.zoom_out:
                try {
                    if (!recordHist.isEmpty()) {
                        chart.centerViewTo(vX, vY,chart.getAxisLeft().getAxisDependency() );
                    }
                    chart.zoomOut();
                }catch (Exception ignored){}
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ac_info:
                new MaterialDialog.Builder(getActivity())
                        .title(getContext().getResources().getString(R.string.info))
                        .content(help)
                        .positiveText(R.string.ok)
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
