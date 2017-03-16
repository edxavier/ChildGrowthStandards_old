package com.edxavier.childgrowthstandards;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.edxavier.childgrowthstandards.db.percentiles.BmiForAge;
import com.edxavier.childgrowthstandards.db.percentiles.HeadCircForAge;
import com.edxavier.childgrowthstandards.db.percentiles.HeightForAge;
import com.edxavier.childgrowthstandards.db.percentiles.WeightForAge;
import com.edxavier.childgrowthstandards.db.percentiles.WeightForHeight;
import com.edxavier.childgrowthstandards.helpers.MyTextView;
import com.edxavier.childgrowthstandards.helpers.constans.Gender;
import com.edxavier.childgrowthstandards.helpers.constans.Units;
import com.edxavier.childgrowthstandards.libs.ChartStyler;
import com.edxavier.childgrowthstandards.libs.QuickChart;
import com.edxavier.childgrowthstandards.libs.formatter.BmiValueFormatter;
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
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.pixplicity.easyprefs.library.Prefs;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.ceryle.radiorealbutton.library.RadioRealButtonGroup;
import io.realm.Realm;
import io.realm.RealmResults;
import rx.android.schedulers.AndroidSchedulers;

public class LaunchPercentil extends RxAppCompatActivity implements DatePickerDialog.OnDateSetListener, CompoundButton.OnCheckedChangeListener {

    @BindView(R.id.toolbar_title)
    MyTextView toolbarTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.calendar)
    FloatingActionButton calendar;
    @BindView(R.id.txtChildBirthday)
    EditText txtChildBirthday;
    @BindView(R.id.childGender)
    RadioRealButtonGroup childGender;
    @BindView(R.id.txtPeso)
    EditText txtPeso;
    @BindView(R.id.pesoContainer)
    TextInputLayout pesoContainer;
    @BindView(R.id.chk_lb_peso)
    RadioButton chkLbPeso;
    @BindView(R.id.chk_kg_peso)
    RadioButton chkKgPeso;
    @BindView(R.id.rdGroup_peso)
    RadioGroup rdGroupPeso;
    @BindView(R.id.txtAltura)
    EditText txtAltura;
    @BindView(R.id.alturaContainer)
    TextInputLayout alturaContainer;
    @BindView(R.id.chk_cm_altura)
    RadioButton chkCmAltura;
    @BindView(R.id.chk_in_altura)
    RadioButton chkInAltura;
    @BindView(R.id.rdGroup_altura)
    RadioGroup rdGroupAltura;
    @BindView(R.id.txtPerimetro)
    EditText txtPerimetro;
    @BindView(R.id.perimetroContainer)
    TextInputLayout perimetroContainer;
    @BindView(R.id.chk_cm_perimetro)
    RadioButton chkCmPerimetro;
    @BindView(R.id.chk_in_perimetro)
    RadioButton chkInPerimetro;
    @BindView(R.id.rdGroup_perimetro)
    RadioGroup rdGroupPerimetro;
    @BindView(R.id.admob_container)
    LinearLayout admobContainer;
    @BindView(R.id.spinner)
    MaterialSpinner spinner;
    @BindView(R.id.chart)
    LineChart chart;
    FirebaseAnalytics analytics;

    int gender = Gender.MALE;
    int edgesColor;
    int innnerEdgeColor;
    int mediaColor;
    int measuereColro;
    int totalCalls = 0;
    Realm realm = Realm.getDefaultInstance();
    LineData lineData = null;
    Period p;
    LocalDate birthdate;
    float x = 0;
    float y = 0;
    int len_unit = Integer.valueOf(Prefs.getString("height_unit", "2"));
    int wei_unit = Integer.valueOf(Prefs.getString("weight_unit", "0"));
    @BindView(R.id.zoom_in)
    FloatingActionButton zoomIn;
    @BindView(R.id.zoom_out)
    FloatingActionButton zoomOut;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_percentil);
        ButterKnife.bind(this);
        analytics = FirebaseAnalytics.getInstance(this);
        analytics.logEvent("quick_percentil_activity", null);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        p = new Period(new LocalDate(), new LocalDate(), PeriodType.days());
        setupWidgets();
        setAds();
        setUnits();
        SimpleDateFormat time_format = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        txtChildBirthday.setText(time_format.format(new Date()));
        edgesColor = getResources().getColor(R.color.md_red_500);
        innnerEdgeColor = getResources().getColor(R.color.md_orange_500);
        mediaColor = getResources().getColor(R.color.md_green_500);
        measuereColro = getResources().getColor(R.color.md_blue_grey_500);
        chart = (LineChart) ChartStyler.setup(chart, this);
        chart.getXAxis().setValueFormatter(new XDaysValuesFormatter(chart, this));
        chart.getAxisLeft().setValueFormatter(new LeftWeightValueFormatter());
        chart.getAxisRight().setValueFormatter(new RightWeightValueFormatter());
        if (Prefs.getBoolean("show_marker", true))
            chart.setMarker(new MyMarkerView(this, R.layout.tv_content));
        drawLines();
        startSecuence();
    }

    private void setUnits() {
        if (wei_unit == Units.KILOGRAM)
            chkKgPeso.setChecked(true);
        else
            chkLbPeso.setChecked(true);
        if (len_unit == Units.CENTIMETER) {
            chkCmAltura.setChecked(true);
            chkCmPerimetro.setChecked(true);
        } else {
            chkInAltura.setChecked(true);
            chkInPerimetro.setChecked(true);
        }
    }

    private void setAds() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        NativeExpressAdView ads = new NativeExpressAdView(this);
        ads.setAdSize(new AdSize(280, 80));
        ads.setAdUnitId(getResources().getString(R.string.admob_s001));
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

    private void setupWidgets() {

        spinner.setItems(getString(R.string.weightForage), getString(R.string.len_for_age),
                getString(R.string.weightForlen), getString(R.string.imc_for_age),
                getString(R.string.cefalic_perimeter_for_age));
        RxView.clicks(calendar).subscribe(aVoid -> {
            Calendar now = Calendar.getInstance();
            Calendar ago = Calendar.getInstance();
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    this,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );
            dpd.setMaxDate(now);
            ago.set(Calendar.YEAR, now.get(Calendar.YEAR) - 19);
            dpd.setMinDate(ago);
            dpd.show(getFragmentManager(), "Datepickerdialog");
        });

        childGender.setOnClickedButtonPosition(position -> {
            if (position == 0)
                gender = Gender.MALE;
            else
                gender = Gender.FEMALE;
            drawLines();
        });
        spinner.setOnItemSelectedListener((view1, position, id, item) -> {
            drawLines();
        });
        zoomOut.setOnClickListener(view2 -> {
            chart.zoomOut();
            chart.centerViewTo(x, y, chart.getAxisLeft().getAxisDependency());
        });
        zoomIn.setOnClickListener(view3 -> {
            zoomIn();
        });

        RxTextView.textChangeEvents(txtPeso)
                .debounce(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(charSequence ->{
                    if(totalCalls>2)
                        drawLines();
                    totalCalls++;
                }, throwable -> {});
        RxTextView.textChangeEvents(txtAltura)
                .debounce(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(charSequence ->{
                    if(totalCalls>2)
                        drawLines();
                    totalCalls++;
                }, throwable -> {});
        RxTextView.textChangeEvents(txtPerimetro)
                .debounce(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(charSequence -> {
                    if(totalCalls>2)
                        drawLines();
                    totalCalls++;
                }, throwable -> {});

        chkKgPeso.setOnCheckedChangeListener(this);
        chkLbPeso.setOnCheckedChangeListener(this);
        chkCmPerimetro.setOnCheckedChangeListener(this);
        chkInPerimetro.setOnCheckedChangeListener(this);
        chkCmAltura.setOnCheckedChangeListener(this);
        chkInAltura.setOnCheckedChangeListener(this);
    }

    private void drawLines() {
        switch (spinner.getSelectedIndex()) {
            case 0:
                setXY();
                RealmResults<WeightForAge> results = realm.where(WeightForAge.class).findAll();
                chart.setData(QuickChart.getWeigtPercentiles(results, gender, this));
                chart.getAxisRight().setAxisMinimum(1f);
                chart.getAxisLeft().setAxisMinimum(1f);
                chart.getXAxis().setValueFormatter(new XDaysValuesFormatter(chart, this));
                chart.getAxisLeft().setValueFormatter(new LeftWeightValueFormatter());
                chart.getAxisRight().setValueFormatter(new RightWeightValueFormatter());
                chart.getData().addDataSet(addPoint());
                if(Prefs.getBoolean("show_marker", true))
                    chart.setMarker(new MyMarkerView(this, R.layout.tv_content));
                break;
            case 1:
                setXY();
                RealmResults<HeightForAge> results2 = realm.where(HeightForAge.class).findAll();
                chart.setData(QuickChart.getHeigtPercentiles(results2, gender, this));
                chart.getAxisRight().setAxisMinimum(35f);
                chart.getAxisLeft().setAxisMinimum(35f);
                chart.getXAxis().setValueFormatter(new XDaysValuesFormatter(chart, this));
                chart.getAxisLeft().setValueFormatter(new LeftHeightValueFormatter());
                chart.getAxisRight().setValueFormatter(new RightHeightValueFormatter());
                chart.getData().addDataSet(addPoint());
                if(Prefs.getBoolean("show_marker", true))
                    chart.setMarker(new HeightMarkerView(this, R.layout.tv_content));
                chart.invalidate();
                break;
            case 2:
                setXY();
                RealmResults<WeightForHeight> results4 = realm.where(WeightForHeight.class).findAll();
                chart.setData(QuickChart.getWeigt_x_HeightPercentiles(results4, gender, this));
                chart.getAxisRight().setAxisMinimum(2f);
                chart.getAxisLeft().setAxisMinimum(2f);
                chart.getXAxis().setValueFormatter(new TopHeightValueFormatter());
                chart.getAxisLeft().setValueFormatter(new LeftWeightValueFormatter());
                chart.getAxisRight().setValueFormatter(new RightWeightValueFormatter());
                chart.getData().addDataSet(addPoint());
                if(Prefs.getBoolean("show_marker", true))
                    chart.setMarker(new WxHMarkerView(this, R.layout.tv_content));chart.invalidate();
                break;
            case 3:
                setXY();
                RealmResults<BmiForAge> results3 = realm.where(BmiForAge.class).findAll();
                chart.setData(QuickChart.getIMCPercentiles(results3, gender, this));
                chart.getAxisRight().setAxisMinimum(9f);
                chart.getAxisLeft().setAxisMinimum(9f);
                chart.getXAxis().setValueFormatter(new XDaysValuesFormatter(chart, this));
                chart.getAxisLeft().setValueFormatter(new BmiValueFormatter(this));
                chart.getAxisRight().setValueFormatter(new BmiValueFormatter(this));
                chart.getData().addDataSet(addPoint());
                if(Prefs.getBoolean("show_marker", true))
                    chart.setMarker(new BmiMarkerView(this));chart.invalidate();
                break;
            case 4:
                setXY();
                RealmResults<HeadCircForAge> results5 = realm.where(HeadCircForAge.class).findAll();
                chart.setData(QuickChart.getPerimeterPercentiles(results5, gender, this));
                chart.getAxisRight().setAxisMinimum(30f);
                chart.getAxisLeft().setAxisMinimum(30f);
                chart.getXAxis().setValueFormatter(new XDaysValuesFormatter(chart, this));
                chart.getAxisLeft().setValueFormatter(new BmiValueFormatter(this));
                chart.getAxisRight().setValueFormatter(new BmiValueFormatter(this));
                chart.getData().addDataSet(addPoint());
                if(Prefs.getBoolean("show_marker", true))
                    chart.setMarker(new PerimeterMarkerView(this, R.layout.tv_content));
                chart.invalidate();
                break;
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar ca = Calendar.getInstance();
        ca.set(year, monthOfYear, dayOfMonth);
        SimpleDateFormat time_format = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        txtChildBirthday.setText(time_format.format(ca.getTime()));
        birthdate = new LocalDate(ca.getTime());
        p = new Period(birthdate, new LocalDate(), PeriodType.days());
        x = p.getDays();
        drawLines();
    }

    public void zoomIn() {
        chart.zoom(1.2f, 1.2f, x, y);
        chart.centerViewTo(x, y, chart.getAxisLeft().getAxisDependency());
    }

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }

    LineDataSet addPoint() {
        List<Entry> valsComp1 = new ArrayList<Entry>();
        Entry c1e2 = new Entry(x, y); // 1 == quarter 2 ...
        valsComp1.add(c1e2);
        LineDataSet setComp1 = new LineDataSet(valsComp1, getResources().getString(R.string.measure));
        setComp1.setColors(measuereColro);
        setComp1.setCircleColor(measuereColro);
        setComp1.setCircleColorHole(measuereColro);

        setComp1.setHighlightLineWidth(1.5f);
        setComp1.setHighLightColor(measuereColro);
        setComp1.setHighlightEnabled(true);
        return setComp1;
    }

    void setXY() {
        switch (spinner.getSelectedIndex()) {
            case 0:
                x = p.getDays();
                if (!txtPeso.getText().toString().isEmpty()) {
                    if (chkLbPeso.isChecked()) {
                        y = Float.parseFloat(txtPeso.getText().toString());
                        y = Units.lb_to_kg(y);
                    } else
                        y = Float.parseFloat(txtPeso.getText().toString());
                } else
                    y = 0;
                break;
            case 1:
                x = p.getDays();
                if (!txtAltura.getText().toString().isEmpty()) {
                    if (chkInAltura.isChecked()) {
                        y = Float.parseFloat(txtAltura.getText().toString());
                        y = Units.inches_to_cm(y);
                    } else
                        y = Float.parseFloat(txtAltura.getText().toString());
                } else
                    y = 0;
                break;
            case 2:
                if (!txtPeso.getText().toString().isEmpty()) {
                    if (chkLbPeso.isChecked()) {
                        y = Float.parseFloat(txtPeso.getText().toString());
                        y = Units.lb_to_kg(y);
                    } else {
                        y = Float.parseFloat(txtPeso.getText().toString());
                    }
                } else {
                    y = 0;
                }
                if (!txtAltura.getText().toString().isEmpty()) {
                    if (chkInAltura.isChecked()) {
                        x = Float.parseFloat(txtAltura.getText().toString());
                        x = Units.inches_to_cm(x);
                    } else {
                        x = Float.parseFloat(txtAltura.getText().toString());
                    }
                } else {
                    x = 0;
                }
                break;
            case 3:
                if (!txtPeso.getText().toString().isEmpty()) {
                    if (chkLbPeso.isChecked()) {
                        y = Float.parseFloat(txtPeso.getText().toString());
                        y = Units.lb_to_kg(y);
                    } else
                        y = Float.parseFloat(txtPeso.getText().toString());
                } else
                    y = 0;
                if (!txtAltura.getText().toString().isEmpty()) {
                    if (chkInAltura.isChecked()) {
                        x = Float.parseFloat(txtAltura.getText().toString());
                        x = Units.inches_to_cm(x);
                    } else
                        x = Float.parseFloat(txtAltura.getText().toString());
                } else
                    x = 0;
                if (x > 0 && y > 0) {
                    float meter = x / 100f;
                    y = (y / (meter * meter));
                    x = p.getDays();
                }
                break;
            case 4:
                x = p.getDays();
                if (!txtPerimetro.getText().toString().isEmpty()) {
                    if (chkInPerimetro.isChecked()) {
                        y = Float.parseFloat(txtPerimetro.getText().toString());
                        y = Units.inches_to_cm(y);
                    } else
                        y = Float.parseFloat(txtPerimetro.getText().toString());
                } else
                    y = 0;
                break;
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        drawLines();
    }

    private void startSecuence() {
        if(!Prefs.getBoolean("secuence_quick_percentil", false)) {
            new TapTargetSequence(this)
                    .targets(
                            TapTarget.forView(this.findViewById(R.id.calendar),
                                    getResources().getString(R.string.sec_new_childs_title2))
                                    .dimColor(R.color.md_black_1000)
                                    .outerCircleColor(R.color.md_red_500_25).cancelable(false),
                            TapTarget.forView(findViewById(R.id.txtPeso),
                                    getResources().getString(R.string.sec_new_history_title))
                                    .dimColor(R.color.md_black_1000)
                                    .outerCircleColor(R.color.md_light_blue_500),
                            TapTarget.forView(this.findViewById(R.id.rdGroup_peso),
                                    getResources().getString(R.string.sec_new_history_title2))
                                    .dimColor(R.color.md_black_1000)
                                    .outerCircleColor(R.color.md_purple_500),
                            TapTarget.forView(this.findViewById(R.id.spinner),
                                    getResources().getString(R.string.sec_quick_percentil_title))
                                    .dimColor(R.color.md_black_1000)
                                    .outerCircleColor(R.color.md_teal_500_25),
                            TapTarget.forView(this.findViewById(R.id.chart),
                                    getResources().getString(R.string.sec_quick_percentil_title2))
                                    .dimColor(R.color.md_black_1000)
                                    .outerCircleColor(R.color.md_cyan_500)
                    ).start();
            Prefs.putBoolean("secuence_quick_percentil", true);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_launch, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ac_share:
                analytics.logEvent("share_launch_activity", null);
                try
                { Intent rate_intent = new Intent(Intent.ACTION_SEND);
                    rate_intent.setType("text/plain");
                    rate_intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
                    String sAux = getResources().getString(R.string.share_app_msg);
                    sAux = sAux + "https://play.google.com/store/apps/details?id="+ getPackageName()+" \n\n";
                    rate_intent.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(rate_intent, getResources().getString(R.string.share_using)));
                }
                catch(Exception ignored){}
                break;
            case R.id.ac_rate:
                analytics.logEvent("rate_launch_activity", null);
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to refresh following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}