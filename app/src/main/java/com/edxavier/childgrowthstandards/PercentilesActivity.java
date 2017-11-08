package com.edxavier.childgrowthstandards;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.edxavier.childgrowthstandards.db.percentiles.BmiForAge;
import com.edxavier.childgrowthstandards.db.percentiles.HeadCircForAge;
import com.edxavier.childgrowthstandards.db.percentiles.HeightForAge;
import com.edxavier.childgrowthstandards.db.percentiles.WeightForAge;
import com.edxavier.childgrowthstandards.db.percentiles.WeightForHeight;
import com.edxavier.childgrowthstandards.helpers.ChartStyler;
import com.edxavier.childgrowthstandards.helpers.constans.Units;
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
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class PercentilesActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.chart)
    LineChart chart;
    @BindView(R.id.zoom_in)
    FloatingActionButton zoomIn;
    @BindView(R.id.chart_type)
    FloatingActionButton chartType;
    @BindView(R.id.zoom_out)
    FloatingActionButton zoomOut;
    private Realm realm;
    private String help;
    private float peso = 0f;
    private int genero;
    private float days = 0f;
    private float altura = 0f;
    private float imc = 0f;
    private float pc = 0f;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_percentiles);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        FirebaseAnalytics mFirebaseAnalytics;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        //mFirebaseAnalytics.logEvent("show_child_percentiles", bundle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        realm = Realm.getDefaultInstance();
        Bundle args = getIntent().getExtras();
        if (args != null) {
            peso = args.getFloat("peso");
            altura = args.getFloat("altura");
            imc = args.getFloat("imc");
            pc = args.getFloat("pc");
            genero = args.getInt("genero");
            days = args.getFloat("dias") + 200;

        }
        setWeightChart();
        if(Units.isTimeToAds()){
            requestInterstical();
            showInterstical();
        }
    }

    LineDataSet addPoint(float x, float y) {
        List<Entry> valsComp1 = new ArrayList<Entry>();
        Entry c1e2 = new Entry(x, y); // 1 == quarter 2 ...
        valsComp1.add(c1e2);
        LineDataSet setComp1 = new LineDataSet(valsComp1, getResources().getString(R.string.measure));
        setComp1.setColors(getResources().getColor(R.color.md_blue_grey_500));
        setComp1.setCircleColor(getResources().getColor(R.color.md_blue_grey_500));
        setComp1.setCircleColorHole(getResources().getColor(R.color.md_blue_grey_500));

        setComp1.setHighlightLineWidth(1.5f);
        setComp1.setHighLightColor(getResources().getColor(R.color.md_blue_grey_500));
        setComp1.setHighlightEnabled(true);
        return setComp1;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void setWeightChart() {
        help = getResources().getString(R.string.weight_desc);
        chart = (LineChart) ChartStyler.setup(chart, this, getString(R.string.weightForage));
        RealmResults<WeightForAge> results = realm.where(WeightForAge.class).lessThan("day", days).findAll();
        chart.setData(QuickChart.getWeigtPercentiles(results, genero, this));
        chart.getData().addDataSet(addPoint(days - 200, peso));

        chart.getXAxis().setValueFormatter(new XDaysValuesFormatter(chart, this));

        chart.getAxisLeft().setValueFormatter(new LeftWeightValueFormatter());
        chart.getAxisRight().setValueFormatter(new RightWeightValueFormatter());
        if (Prefs.getBoolean("show_marker", true))
            chart.setMarker(new MyMarkerView(this, R.layout.tv_content));

        chart.invalidate();
    }


    @OnClick({R.id.zoom_in, R.id.chart_type, R.id.zoom_out})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.zoom_in:
                try {
                    chart.zoomIn();
                }catch (Exception ignored){}
                break;
            case R.id.chart_type:
                new MaterialDialog.Builder(this)
                        .title("Seleccione el grafico")
                        .items(R.array.chart_options)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                switch (which) {
                                    //Peso para la edad
                                    case 0:
                                        setWeightChart();
                                        break;
                                    //Altura para la edad
                                    case 1:
                                        help = getResources().getString(R.string.height_desc);

                                        chart = (LineChart) ChartStyler.setup(chart, PercentilesActivity.this, getString(R.string.len_for_age));
                                        RealmResults<HeightForAge> results = realm.where(HeightForAge.class).lessThan("day", days).findAll();
                                        chart.setData(QuickChart.getHeigtPercentiles(results, genero, getBaseContext()));
                                        chart.getData().addDataSet(addPoint(days - 200, altura));

                                        chart.getXAxis().setValueFormatter(new XDaysValuesFormatter(chart, getBaseContext()));
                                        chart.getAxisLeft().setValueFormatter(new LeftHeightValueFormatter());
                                        chart.getAxisRight().setValueFormatter(new RightHeightValueFormatter());
                                        chart.getAxisRight().setAxisMinimum(40f);
                                        chart.getAxisLeft().setAxisMinimum(40f);
                                        if(Prefs.getBoolean("show_marker", true))
                                            chart.setMarker(new HeightMarkerView(getBaseContext(), R.layout.tv_content));
                                        chart.invalidate();
                                        break;
                                    //Peso para la altura
                                    case 2:
                                        help = getResources().getString(R.string.weight_for_height_desc);
                                        RealmResults<HeightForAge> hfa = realm.where(HeightForAge.class).lessThan("day", days)
                                                .findAllSorted("day", Sort.DESCENDING);
                                        String h = "170";
                                        if(hfa.size()>0) {
                                            h = String.valueOf(hfa.first().getNinetySeven_boys() + 6);
                                        }

                                        chart = (LineChart) ChartStyler.setup(chart, getBaseContext(), getString(R.string.weightForlen));
                                        RealmResults<WeightForHeight> results2 = realm.where(WeightForHeight.class)
                                                .lessThan("height", Float.valueOf(h)).findAll();
                                        chart.setData(QuickChart.getWeigt_x_HeightPercentiles(results2, genero, getBaseContext()));
                                        chart.getData().addDataSet(addPoint(altura , peso));

                                        chart.getXAxis().setValueFormatter(new TopHeightValueFormatter());

                                        chart.getAxisLeft().setValueFormatter(new LeftWeightValueFormatter());
                                        chart.getAxisRight().setValueFormatter(new RightWeightValueFormatter());
                                        chart.getAxisRight().setAxisMinimum(2f);
                                        chart.getAxisLeft().setAxisMinimum(2f);
                                        if(Prefs.getBoolean("show_marker", true))
                                            chart.setMarker(new WxHMarkerView(getBaseContext(), R.layout.tv_content));
                                        chart.invalidate();
                                        break;
                                    case 3:
                                        help = getResources().getString(R.string.imc_desc);
                                        chart = (LineChart) ChartStyler.setup(chart, getBaseContext(), getString(R.string.imc_for_age));
                                        RealmResults<BmiForAge> results3 = realm.where(BmiForAge.class).lessThan("day", days).findAll();
                                        chart.setData(QuickChart.getIMCPercentiles(results3, genero, getBaseContext()));
                                        chart.getData().addDataSet(addPoint(days - 200, imc));

                                        chart.getXAxis().setValueFormatter(new XDaysValuesFormatter(chart, getBaseContext()));
                                        chart.getAxisLeft().setValueFormatter(new LeftHeightValueFormatter());
                                        chart.getAxisRight().setValueFormatter(new RightHeightValueFormatter());
                                        chart.getAxisRight().setAxisMinimum(9f);
                                        chart.getAxisLeft().setAxisMinimum(9f);
                                        if(Prefs.getBoolean("show_marker", true))
                                            chart.setMarker(new BmiMarkerView(getBaseContext()));
                                        chart.invalidate();
                                        break;
                                    case 4:
                                        help = getResources().getString(R.string.cefalic_desc);

                                        chart = (LineChart) ChartStyler.setup(chart, getBaseContext(), getString(R.string.cefalic_perimeter_for_age));
                                        RealmResults<HeadCircForAge> results4 = realm.where(HeadCircForAge.class)
                                                .lessThan("day", Float.valueOf(days))
                                                .findAll();
                                        chart.setData(QuickChart.getPerimeterPercentiles(results4, genero, getBaseContext()));
                                        chart.getData().addDataSet(addPoint(days - 200, pc));

                                        chart.getXAxis().setValueFormatter(new XDaysValuesFormatter(chart, getBaseContext()));
                                        chart.getAxisLeft().setValueFormatter(new LeftHeightValueFormatter());
                                        chart.getAxisRight().setValueFormatter(new RightHeightValueFormatter());
                                        chart.getAxisRight().setAxisMinimum(30f);
                                        chart.getAxisLeft().setAxisMinimum(30f);
                                        if(Prefs.getBoolean("show_marker", true))
                                            chart.setMarker(new PerimeterMarkerView(getBaseContext(), R.layout.tv_content));
                                        chart.invalidate();
                                        break;
                                }
                            }
                        })
                        .show();
                break;
            case R.id.zoom_out:
                try {
                    chart.zoomOut();
                }catch (Exception ignored){}
                break;
        }
    }


    public void showInterstical() {
        if(mInterstitialAd.isLoaded()) {
            try {
                MaterialDialog dlg = new MaterialDialog.Builder(this)
                        .title(R.string.ads_notice)
                        .cancelable(false)
                        .progress(true, 0)
                        .progressIndeterminateStyle(true)
                        .build();
                dlg.show();
                Observable.interval(1, TimeUnit.MILLISECONDS).take(2500)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aLong -> {},
                                throwable -> {}, () -> {
                                    dlg.dismiss();
                                    mInterstitialAd.show();
                                });
            }catch (Exception ignored){}
        }else {
            Observable.interval(1, TimeUnit.SECONDS).take(4)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> {},
                            throwable -> {}, this::showInterstical);
        }
    }

    public  void requestInterstical(){
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mInterstitialAd = new InterstitialAd(getApplicationContext());
        mInterstitialAd.setAdUnitId(getApplicationContext().getResources().getString(R.string.admob_interstical));
        mInterstitialAd.setAdListener(new AdListener() {

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                //SystemClock.sleep(5000);
                Observable.interval(1, TimeUnit.SECONDS).take(4)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aLong -> {},
                                throwable -> {}, () -> {
                                    requestInterstical();
                                });
            }
        });
        if(!mInterstitialAd.isLoaded())
            mInterstitialAd.loadAd(adRequest);
    }

}
