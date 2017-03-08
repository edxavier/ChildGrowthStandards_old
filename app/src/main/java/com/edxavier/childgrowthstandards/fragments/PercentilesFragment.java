package com.edxavier.childgrowthstandards.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.edxavier.childgrowthstandards.R;
import com.edxavier.childgrowthstandards.db.percentiles.BmiForAge;
import com.edxavier.childgrowthstandards.db.percentiles.HeadCircForAge;
import com.edxavier.childgrowthstandards.db.percentiles.HeightForAge;
import com.edxavier.childgrowthstandards.db.percentiles.WeightForAge;
import com.edxavier.childgrowthstandards.db.percentiles.WeightForHeight;
import com.edxavier.childgrowthstandards.helpers.MyTextView;
import com.edxavier.childgrowthstandards.helpers.constans.Gender;
import com.edxavier.childgrowthstandards.helpers.constans.Units;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.realm.implementation.RealmLineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.pixplicity.easyprefs.library.Prefs;
import com.trello.rxlifecycle.components.RxFragment;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.joda.time.Interval;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class PercentilesFragment extends com.trello.rxlifecycle.components.support.RxFragment implements DatePickerDialog.OnDateSetListener {


    @BindView(R.id.calendar)
    FloatingActionButton calendar;
    @BindView(R.id.txtChildBirthday)
    EditText txtChildBirthday;
    @BindView(R.id.childGender)
    RadioRealButtonGroup childGender;
    @BindView(R.id.spinner)
    MaterialSpinner spinner;
    @BindView(R.id.chart)
    LineChart chart;
    @BindView(R.id.zoom_in_btn)
    ImageButton zoomInBtn;
    @BindView(R.id.zoom_in)
    MaterialRippleLayout zoomIn;
    @BindView(R.id.zoom_out_btn)
    ImageButton zoomOutBtn;
    @BindView(R.id.zoom_out)
    MaterialRippleLayout zoomOut;
    @BindView(R.id.admob_container)
    LinearLayout admobContainer;

    int edgesColor;
    int innnerEdgeColor;
    int mediaColor;
    int measuereColro;
    Realm realm = Realm.getDefaultInstance();
    LineData lineData = null;

    Period p;
    LocalDate birthdate;
    int gender = Gender.MALE;
    boolean kg_has_focus = false;
    boolean inches_has_focus = false;
    boolean inches2_has_focus = false;
    boolean is_first_time = true;
    float weight = 3.3f;
    float height = 49f;
    float cefalic = 35;

    @BindView(R.id.txtPounds)
    EditText txtPounds;
    @BindView(R.id.txtKilogram)
    EditText txtKilogram;
    @BindView(R.id.txtEstaturaCms)
    EditText txtEstaturaCms;
    @BindView(R.id.txtEstaturaInches)
    EditText txtEstaturaInches;
    @BindView(R.id.txtCefalCm)
    EditText txtCefalCm;
    @BindView(R.id.txtcefalInch)
    EditText txtcefalInch;

    public PercentilesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_percentiles, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        edgesColor = getResources().getColor(R.color.md_red_500);
        innnerEdgeColor = getResources().getColor(R.color.md_orange_500);
        mediaColor = getResources().getColor(R.color.md_green_500);
        measuereColro = getResources().getColor(R.color.md_blue_grey_500);

        p = new Period(new LocalDate(), new LocalDate(), PeriodType.days());

        SimpleDateFormat time_format = new SimpleDateFormat("dd-MMM-yy", Locale.getDefault());
        txtChildBirthday.setText(time_format.format(new Date()));
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("45D8AEB3B66116F8F24E001927292BD5")
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        NativeExpressAdView ads = new NativeExpressAdView(getActivity());
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

        MaterialSpinner spinner = (MaterialSpinner) getActivity().findViewById(R.id.spinner);
        spinner.setItems(getContext().getString(R.string.weightForage), getContext().getString(R.string.len_for_age),
                getContext().getString(R.string.weightForlen), getContext().getString(R.string.imc_for_age),
                getContext().getString(R.string.cefalic_perimeter_for_age));


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
            dpd.show(getActivity().getFragmentManager(), "Datepickerdialog");
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


        chart.setDrawGridBackground(true);
        chart.getLegend().setEnabled(true);
        chart.animateXY(1900, 0);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.TOP);
        chart.getXAxis().setTextColor(getContext().getResources().getColor(R.color.secondary_text));
        chart.getXAxis().setDrawLabels(true);
        chart.getXAxis().setLabelRotationAngle(20);
        chart.getXAxis().setGranularity(1f);
        chart.getAxisRight().setTextColor(getContext().getResources().getColor(R.color.secondary_text));
        chart.getAxisLeft().setTextColor(getContext().getResources().getColor(R.color.secondary_text));
        chart.setNoDataText(getContext().getString(R.string.graph_empty));

        //drawWeightLines();

        setupInputs();
        setupFocusListener();
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Date date = new Date(year, monthOfYear, dayOfMonth);
        SimpleDateFormat time_format = new SimpleDateFormat("dd-MMM-yy", Locale.getDefault());
        txtChildBirthday.setText(time_format.format(date));
        birthdate = new LocalDate(date.getYear(), date.getMonth() + 1, date.getDate());
        p = new Period(birthdate, new LocalDate(), PeriodType.days());
        if(!is_first_time)
            drawLines();
    }

    public void setupInputs(){
        RxTextView.textChangeEvents(txtPounds)
                .compose(bindToLifecycle())
                .subscribe(value -> {
            if (!kg_has_focus) {
                if (value.text().length() > 0) {
                    float pnds = Float.valueOf(value.text().toString());
                    float kg = pnds / Units.KG_HAS_POUNDS;
                    txtKilogram.setText(String.format("%.2f", kg).replace(',', '.'));
                }
            }
        });
        RxTextView.textChangeEvents(txtKilogram)
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle()).subscribe(value -> {
            if (value.text().length() > 0) {
                weight = Float.valueOf(value.text().toString());
                if(!is_first_time)
                    drawLines();
            }
            if (kg_has_focus) {
                if (value.text().length() > 0) {
                    float kgs = Float.valueOf(value.text().toString());
                    float pnds = kgs * (float) Units.KG_HAS_POUNDS;
                    txtPounds.setText(String.format("%.2f", pnds).replace(',', '.'));
                }
            }
        });
        RxTextView.textChangeEvents(txtEstaturaCms)
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle()).subscribe(value -> {
            if (value.text().length() > 0) {
                height = Float.valueOf(value.text().toString());
                if(!is_first_time)
                    drawLines();
            }
            if (!inches_has_focus) {
                if (value.text().length() > 0) {
                    float cms = Float.valueOf(value.text().toString());
                    float in = cms / Units.INCHES_HAS_CM;
                    txtEstaturaInches.setText(String.format("%.2f", in).replace(',', '.'));
                }
            }
        });
        RxTextView.textChangeEvents(txtEstaturaInches)
                .compose(bindToLifecycle()).subscribe(value -> {
            if (inches_has_focus) {
                if (value.text().length() > 0) {
                    try {
                        float inches = Float.valueOf(value.text().toString());
                        float cms = inches * (float) Units.INCHES_HAS_CM;
                        txtEstaturaCms.setText(String.format("%.2f", cms).replace(',', '.'));
                    }catch (Exception ignored){

                    }
                }
            }
        });
        RxTextView.textChangeEvents(txtCefalCm)
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle()).subscribe(value -> {
            if (value.text().length() > 0) {
                cefalic = Float.valueOf(value.text().toString());
                drawLines();
                is_first_time = false;
            }
            if (!inches2_has_focus) {
                if (value.text().length() > 0) {
                    float cms = Float.valueOf(value.text().toString());
                    float inches = cms / Units.INCHES_HAS_CM;
                    txtcefalInch.setText(String.format("%.2f", inches).replace(',', '.'));
                }
            }
        });
        RxTextView.textChangeEvents(txtcefalInch)
                .compose(bindToLifecycle()).subscribe(value -> {
            if (inches2_has_focus) {
                if (value.text().length() > 0) {
                    float inches = Float.valueOf(value.text().toString());
                    float cms = inches * (float) Units.INCHES_HAS_CM;
                    txtCefalCm.setText(String.format("%.2f", cms).replace(',', '.'));
                    drawLines();
                }
            }
        });

    }

    public void setupFocusListener(){
        txtPounds.setOnFocusChangeListener((view, focus) -> {
            if (focus)
                kg_has_focus = false;
        });
        txtKilogram.setOnFocusChangeListener((view, focus) -> {
            if (focus)
                kg_has_focus = true;
        });
        txtEstaturaCms.setOnFocusChangeListener((view, focus) -> {
            if (focus)
                inches_has_focus = false;
        });
        txtEstaturaInches.setOnFocusChangeListener((view, focus) -> {
            if (focus)
                inches_has_focus = true;
        });
        txtCefalCm.setOnFocusChangeListener((view, focus) -> {
            if (focus)
                inches2_has_focus = false;
        });
        txtcefalInch.setOnFocusChangeListener((view, focus) -> {
            if (focus)
                inches2_has_focus = true;
        });
    }

    public void drawLines(){
        switch (spinner.getSelectedIndex()){
            case 0:
                drawWeightLines();
                break;
            case 1:
                drawHeightLines();
                break;
            case 2:
                drawWeightforHeightLines();
                break;
            case 3:
                drawIMCLines();
                break;
            case 4:
                drawCefalicLines();
                break;
        }
    }

    public void drawWeightLines() {
        final RealmResults<WeightForAge> results = realm.where(WeightForAge.class).findAll();
        RealmLineDataSet<WeightForAge> mediansDataSet = new RealmLineDataSet<WeightForAge>(results, "day", "median_girls");
        RealmLineDataSet<WeightForAge> ninetySevenDataSet = new RealmLineDataSet<WeightForAge>(results, "day", "ninetySeven_girls");
        RealmLineDataSet<WeightForAge> thirdLineDataSet = new RealmLineDataSet<WeightForAge>(results, "day", "third_girls");
        RealmLineDataSet<WeightForAge> eightyFiveDataSet = new RealmLineDataSet<WeightForAge>(results, "day", "eightyFive_girls");
        RealmLineDataSet<WeightForAge> fifteenDataSet = new RealmLineDataSet<WeightForAge>(results, "day", "fifteen_girls");

        if (gender == Gender.MALE) {
            mediansDataSet = new RealmLineDataSet<WeightForAge>(results, "day", "median_boys");
            ninetySevenDataSet = new RealmLineDataSet<WeightForAge>(results, "day", "ninetySeven_boys");
            thirdLineDataSet = new RealmLineDataSet<WeightForAge>(results, "day", "third_boys");
            eightyFiveDataSet = new RealmLineDataSet<WeightForAge>(results, "day", "eightyFive_boys");
            fifteenDataSet = new RealmLineDataSet<WeightForAge>(results, "day", "fifteen_boys");
            chart.getAxisRight().setGridColor(getResources().getColor(R.color.md_blue_500_50));

        }

        mediansDataSet.setColors(new int[]{mediaColor});
        mediansDataSet.setLabel("P50");
        mediansDataSet.setDrawCircles(false);

        ninetySevenDataSet.setColors(new int[]{edgesColor});
        ninetySevenDataSet.setDrawCircles(false);
        ninetySevenDataSet.setLabel("P97");

        thirdLineDataSet.setColors(new int[]{edgesColor});
        thirdLineDataSet.setDrawCircles(false);
        thirdLineDataSet.setLabel("P3");

        eightyFiveDataSet.setColors(new int[]{innnerEdgeColor});
        eightyFiveDataSet.setDrawCircles(false);
        eightyFiveDataSet.setLabel("P85");


        fifteenDataSet.setColors(new int[]{innnerEdgeColor});
        fifteenDataSet.setDrawCircles(false);
        fifteenDataSet.setLabel("P15");

        lineData = new LineData(mediansDataSet);

        List<Entry> valsComp1 = new ArrayList<Entry>();
        Entry c1e2 = new Entry((float) p.getDays(), weight); // 1 == quarter 2 ...
        valsComp1.add(c1e2);
        LineDataSet setComp1 = new LineDataSet(valsComp1, "Medida");
        setComp1.setColors(new int[]{measuereColro});
        setComp1.setCircleColor(measuereColro);
        setComp1.setCircleColorHole(measuereColro);

        setComp1.setHighlightLineWidth(1.5f);
        setComp1.setHighLightColor(measuereColro);
        setComp1.setHighlightEnabled(true);
        lineData.addDataSet(setComp1);
       /* AxisValueFormatter formatter = new AxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                value = value * Units.KG_HAS_POUNDS;
                return String.format(Locale.getDefault(), "%.0f lb", value);
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        };

        AxisValueFormatter formatter_right = new AxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.format(Locale.getDefault(), "%.1f kg", value);
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        };
        chart.getAxisLeft().setValueFormatter(formatter);
        chart.getAxisRight().setValueFormatter(formatter_right);
*/
        if(Prefs.getBoolean("show_marker", true)) {
            PercentilesFragment.WeightMarkerView mv = new PercentilesFragment.WeightMarkerView(getActivity(), R.layout.tv_content);
            chart.setMarkerView(mv);
        }
        chart.setDrawMarkerViews(true);

        //chart.getXAxis().setValueFormatter(new DayAxisValueFormatter(chart));
        lineData.addDataSet(ninetySevenDataSet);
        lineData.addDataSet(thirdLineDataSet);
        lineData.addDataSet(eightyFiveDataSet);
        lineData.addDataSet(fifteenDataSet);
        chart.setData(lineData);
        chart.invalidate();
    }

    public void drawHeightLines() {
        final RealmResults<HeightForAge> results = realm.where(HeightForAge.class).findAll();
        RealmLineDataSet<HeightForAge> mediansDataSet = new RealmLineDataSet<HeightForAge>(results, "day", "median_girls");

        RealmLineDataSet<HeightForAge> ninetySevenDataSet = new RealmLineDataSet<HeightForAge>(results, "day", "ninetySeven_girls");
        RealmLineDataSet<HeightForAge> thirdLineDataSet = new RealmLineDataSet<HeightForAge>(results, "day", "third_girls");
        RealmLineDataSet<HeightForAge> eightyFiveDataSet = new RealmLineDataSet<HeightForAge>(results, "day", "eightyFive_girls");
        RealmLineDataSet<HeightForAge> fifteenDataSet = new RealmLineDataSet<HeightForAge>(results, "day", "fifteen_girlsv");

        if (gender == Gender.MALE) {
            mediansDataSet = new RealmLineDataSet<HeightForAge>(results, "day", "median_boys");
            ninetySevenDataSet = new RealmLineDataSet<HeightForAge>(results, "day", "ninetySeven_boys");
            thirdLineDataSet = new RealmLineDataSet<HeightForAge>(results, "day", "third_boys");
            eightyFiveDataSet = new RealmLineDataSet<HeightForAge>(results, "day", "eightyFive_boys");
            fifteenDataSet = new RealmLineDataSet<HeightForAge>(results, "day", "fifteen_boys");
            chart.getAxisRight().setGridColor(getResources().getColor(R.color.md_blue_500_50));

        }

        mediansDataSet.setColors(new int[]{mediaColor});
        mediansDataSet.setLabel("P50");
        mediansDataSet.setDrawCircles(false);

        ninetySevenDataSet.setColors(new int[]{edgesColor});
        ninetySevenDataSet.setDrawCircles(false);
        ninetySevenDataSet.setLabel("P97");

        thirdLineDataSet.setColors(new int[]{edgesColor});
        thirdLineDataSet.setDrawCircles(false);
        thirdLineDataSet.setLabel("P3");

        eightyFiveDataSet.setColors(new int[]{innnerEdgeColor});
        eightyFiveDataSet.setDrawCircles(false);
        eightyFiveDataSet.setLabel("P85");


        fifteenDataSet.setColors(new int[]{innnerEdgeColor});
        fifteenDataSet.setDrawCircles(false);
        fifteenDataSet.setLabel("P15");

        lineData = new LineData(mediansDataSet);

        List<Entry> valsComp1 = new ArrayList<Entry>();
        Entry c1e2 = new Entry((float) p.getDays(), height); // 1 == quarter 2 ...
        valsComp1.add(c1e2);
        LineDataSet setComp1 = new LineDataSet(valsComp1, "Medida");
        setComp1.setHighlightLineWidth(1.5f);
        setComp1.setHighLightColor(measuereColro);
        setComp1.setHighlightEnabled(true);
        setComp1.setColors(new int[]{measuereColro});
        setComp1.setCircleColor(measuereColro);
        setComp1.setCircleColorHole(measuereColro);
        setComp1.setHighlightLineWidth(1.5f);
        setComp1.setHighLightColor(measuereColro);
        setComp1.setHighlightEnabled(true);
        lineData.addDataSet(setComp1);

    /*    AxisValueFormatter formatter = new AxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.format("%.0f cm", value);
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        };

        AxisValueFormatter formatter_right = new AxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                value = value / Units.INCHES_HAS_CM;
                return String.format("%.1f in", value);
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        };

        chart.getAxisLeft().setValueFormatter(formatter);
        chart.getAxisRight().setValueFormatter(formatter_right);*/
        if(Prefs.getBoolean("show_marker", true)) {
            PercentilesFragment.HeightMarkerView mv = new PercentilesFragment.HeightMarkerView(getActivity(), R.layout.tv_content);
            chart.setMarkerView(mv);
        }
        chart.setDrawMarkerViews(true);

        lineData.addDataSet(ninetySevenDataSet);
        lineData.addDataSet(eightyFiveDataSet);
        lineData.addDataSet(fifteenDataSet);
        lineData.addDataSet(thirdLineDataSet);
        chart.setData(lineData);
        chart.invalidate();
    }

    public void drawCefalicLines() {
        final RealmResults<HeadCircForAge> results = realm.where(HeadCircForAge.class).findAll();
        RealmLineDataSet<HeadCircForAge> mediansDataSet = new RealmLineDataSet<HeadCircForAge>(results, "day", "median_girls");

        RealmLineDataSet<HeadCircForAge> ninetySevenDataSet = new RealmLineDataSet<HeadCircForAge>(results, "day", "ninetySeven_girls");
        RealmLineDataSet<HeadCircForAge> thirdLineDataSet = new RealmLineDataSet<HeadCircForAge>(results, "day", "third_girls");
        RealmLineDataSet<HeadCircForAge> eightyFiveDataSet = new RealmLineDataSet<HeadCircForAge>(results, "day", "eightyFive_girls");
        RealmLineDataSet<HeadCircForAge> fifteenDataSet = new RealmLineDataSet<HeadCircForAge>(results, "day", "fifteen_girls");

        if (gender == Gender.MALE) {
            mediansDataSet = new RealmLineDataSet<HeadCircForAge>(results, "day", "median_boys");
            ninetySevenDataSet = new RealmLineDataSet<HeadCircForAge>(results, "day", "ninetySeven_boys");
            thirdLineDataSet = new RealmLineDataSet<HeadCircForAge>(results, "day", "third_boys");
            eightyFiveDataSet = new RealmLineDataSet<HeadCircForAge>(results, "day", "eightyFive_boys");
            fifteenDataSet = new RealmLineDataSet<HeadCircForAge>(results, "day", "fifteen_boys");
            chart.getAxisRight().setGridColor(getResources().getColor(R.color.md_blue_500_50));

        }

        mediansDataSet.setColors(new int[]{mediaColor});
        mediansDataSet.setLabel("P50");
        mediansDataSet.setDrawCircles(false);

        ninetySevenDataSet.setColors(new int[]{edgesColor});
        ninetySevenDataSet.setDrawCircles(false);
        ninetySevenDataSet.setLabel("P97");

        thirdLineDataSet.setColors(new int[]{edgesColor});
        thirdLineDataSet.setDrawCircles(false);
        thirdLineDataSet.setLabel("P3");

        eightyFiveDataSet.setColors(new int[]{innnerEdgeColor});
        eightyFiveDataSet.setDrawCircles(false);
        eightyFiveDataSet.setLabel("P85");


        fifteenDataSet.setColors(new int[]{innnerEdgeColor});
        fifteenDataSet.setDrawCircles(false);
        fifteenDataSet.setLabel("P15");

        lineData = new LineData(mediansDataSet);

        List<Entry> valsComp1 = new ArrayList<Entry>();
        Entry c1e2 = new Entry((float) p.getDays(), cefalic); // 1 == quarter 2 ...
        valsComp1.add(c1e2);
        LineDataSet setComp1 = new LineDataSet(valsComp1, "Medida");
        setComp1.setColors(new int[]{measuereColro});
        setComp1.setCircleColor(measuereColro);
        setComp1.setCircleColorHole(measuereColro);
        setComp1.setHighlightLineWidth(1.5f);
        setComp1.setHighLightColor(measuereColro);
        setComp1.setHighlightEnabled(true);
        lineData.addDataSet(setComp1);

        if(Prefs.getBoolean("show_marker", true)) {
            PercentilesFragment.CefalicMarkerView mv = new PercentilesFragment.CefalicMarkerView(getActivity(), R.layout.tv_content);
            chart.setMarkerView(mv);
        }
        chart.setDrawMarkerViews(true);


        lineData.addDataSet(ninetySevenDataSet);
        lineData.addDataSet(eightyFiveDataSet);
        lineData.addDataSet(fifteenDataSet);
        lineData.addDataSet(thirdLineDataSet);
        chart.setData(lineData);
        chart.invalidate();
    }

    public void drawIMCLines() {
        final RealmResults<BmiForAge> results = realm.where(BmiForAge.class).findAll();
        RealmLineDataSet<BmiForAge> mediansDataSet = new RealmLineDataSet<BmiForAge>(results, "day", "median_girls");

        RealmLineDataSet<BmiForAge> ninetySevenDataSet = new RealmLineDataSet<BmiForAge>(results, "day", "ninetySeven_girls");
        RealmLineDataSet<BmiForAge> thirdLineDataSet = new RealmLineDataSet<BmiForAge>(results, "day", "third_girls");
        RealmLineDataSet<BmiForAge> eightyFiveDataSet = new RealmLineDataSet<BmiForAge>(results, "day", "eightyFive_girls");
        RealmLineDataSet<BmiForAge> fifteenDataSet = new RealmLineDataSet<BmiForAge>(results, "day", "fifteen_girls");

        if (gender == Gender.MALE) {
            mediansDataSet = new RealmLineDataSet<BmiForAge>(results, "day", "median_boys");
            ninetySevenDataSet = new RealmLineDataSet<BmiForAge>(results, "day", "ninetySeven_boys");
            thirdLineDataSet = new RealmLineDataSet<BmiForAge>(results, "day", "third_boys");
            eightyFiveDataSet = new RealmLineDataSet<BmiForAge>(results, "day", "eightyFive_boys");
            fifteenDataSet = new RealmLineDataSet<BmiForAge>(results, "day", "fifteen_boys");
            chart.getAxisRight().setGridColor(getResources().getColor(R.color.md_blue_500_50));

        }

        mediansDataSet.setColors(new int[]{mediaColor});
        mediansDataSet.setLabel("P50");
        mediansDataSet.setDrawCircles(false);

        ninetySevenDataSet.setColors(new int[]{edgesColor});
        ninetySevenDataSet.setDrawCircles(false);
        ninetySevenDataSet.setLabel("P97");

        thirdLineDataSet.setColors(new int[]{edgesColor});
        thirdLineDataSet.setDrawCircles(false);
        thirdLineDataSet.setLabel("P3");

        eightyFiveDataSet.setColors(new int[]{innnerEdgeColor});
        eightyFiveDataSet.setDrawCircles(false);
        eightyFiveDataSet.setLabel("P85");


        fifteenDataSet.setColors(new int[]{innnerEdgeColor});
        fifteenDataSet.setDrawCircles(false);
        fifteenDataSet.setLabel("P15");

        lineData = new LineData(mediansDataSet);

        List<Entry> valsComp1 = new ArrayList<Entry>();
        float meter = (float) (height / 100f);
        float bmi = (float) (weight / (meter * meter));
        Entry c1e2 = new Entry((float) p.getDays(), bmi); // 1 == quarter 2 ...
        valsComp1.add(c1e2);
        LineDataSet setComp1 = new LineDataSet(valsComp1, "Medida");
        setComp1.setColors(new int[]{measuereColro});
        setComp1.setCircleColor(measuereColro);
        setComp1.setCircleColorHole(measuereColro);
        setComp1.setHighlightLineWidth(1.5f);
        setComp1.setHighLightColor(measuereColro);
        setComp1.setHighlightEnabled(true);
        lineData.addDataSet(setComp1);


        if(Prefs.getBoolean("show_marker", true)) {
            PercentilesFragment.ImcMarkerView mv = new PercentilesFragment.ImcMarkerView(getActivity(), R.layout.tv_content);
            chart.setMarkerView(mv);
        }
        chart.setDrawMarkerViews(true);


        lineData.addDataSet(ninetySevenDataSet);
        lineData.addDataSet(eightyFiveDataSet);
        lineData.addDataSet(fifteenDataSet);
        lineData.addDataSet(thirdLineDataSet);
        chart.setData(lineData);
        chart.invalidate();
    }

    public void drawWeightforHeightLines() {
        final RealmResults<WeightForHeight> results = realm.where(WeightForHeight.class).findAll();
        RealmLineDataSet<WeightForHeight> mediansDataSet = new RealmLineDataSet<WeightForHeight>(results, "height", "median_girls");

        RealmLineDataSet<WeightForHeight> ninetySevenDataSet = new RealmLineDataSet<WeightForHeight>(results, "height", "ninetySeven_girls");
        RealmLineDataSet<WeightForHeight> thirdLineDataSet = new RealmLineDataSet<WeightForHeight>(results, "height", "third_girls");
        RealmLineDataSet<WeightForHeight> eightyFiveDataSet = new RealmLineDataSet<WeightForHeight>(results, "height", "eightyFive_girls");
        RealmLineDataSet<WeightForHeight> fifteenDataSet = new RealmLineDataSet<WeightForHeight>(results, "height", "fifteen_girlsv");

        if (gender == Gender.MALE) {
            mediansDataSet = new RealmLineDataSet<WeightForHeight>(results, "height", "median_boys");
            ninetySevenDataSet = new RealmLineDataSet<WeightForHeight>(results, "height", "ninetySeven_boys");
            thirdLineDataSet = new RealmLineDataSet<WeightForHeight>(results, "height", "third_boys");
            eightyFiveDataSet = new RealmLineDataSet<WeightForHeight>(results, "height", "eightyFive_boys");
            fifteenDataSet = new RealmLineDataSet<WeightForHeight>(results, "height", "fifteen_boys");
            chart.getAxisRight().setGridColor(getResources().getColor(R.color.md_blue_500_50));

        }

        mediansDataSet.setColors(new int[]{mediaColor});
        mediansDataSet.setLabel("P50");
        mediansDataSet.setDrawCircles(false);

        ninetySevenDataSet.setColors(new int[]{edgesColor});
        ninetySevenDataSet.setDrawCircles(false);
        ninetySevenDataSet.setLabel("P97");

        thirdLineDataSet.setColors(new int[]{edgesColor});
        thirdLineDataSet.setDrawCircles(false);
        thirdLineDataSet.setLabel("P3");

        eightyFiveDataSet.setColors(new int[]{innnerEdgeColor});
        eightyFiveDataSet.setDrawCircles(false);
        eightyFiveDataSet.setLabel("P85");


        fifteenDataSet.setColors(new int[]{innnerEdgeColor});
        fifteenDataSet.setDrawCircles(false);
        fifteenDataSet.setLabel("P15");

        lineData = new LineData(mediansDataSet);

        List<Entry> valsComp1 = new ArrayList<Entry>();
        float h = height;
        Entry c1e2 = new Entry(h, weight); // 1 == quarter 2 ...
        valsComp1.add(c1e2);
        LineDataSet setComp1 = new LineDataSet(valsComp1, "Medida");
        setComp1.setColors(new int[]{measuereColro});
        setComp1.setHighlightLineWidth(1.5f);
        setComp1.setHighLightColor(measuereColro);
        setComp1.setHighlightEnabled(true);
        setComp1.setCircleColor(measuereColro);
        setComp1.setCircleColorHole(measuereColro);
        lineData.addDataSet(setComp1);


        if(Prefs.getBoolean("show_marker", true)) {
            PercentilesFragment.WeightForHeigthMarkerView mv = new PercentilesFragment.WeightForHeigthMarkerView(getActivity(), R.layout.tv_content);
            chart.setMarkerView(mv);
        }
        chart.setDrawMarkerViews(true);


        lineData.addDataSet(ninetySevenDataSet);
        lineData.addDataSet(eightyFiveDataSet);
        lineData.addDataSet(fifteenDataSet);
        lineData.addDataSet(thirdLineDataSet);
        chart.setData(lineData);
        chart.invalidate();
    }




    public class WeightForHeigthMarkerView extends MarkerView {

        private TextView tvContent;
        private MyTextView age;

        public WeightForHeigthMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            // this markerview only displays a textview
            tvContent = (TextView) findViewById(R.id.weight);
            age = (MyTextView) findViewById(R.id.age);
        }

        // callbacks everytime the MyMarkerView is redrawn, can be used to update the
        // content (user-interface)
        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            int wei_unit = Integer.valueOf(Prefs.getString("weight_unit", "0"));
            int len_unit = Integer.valueOf(Prefs.getString("height_unit", "2"));
            String und = " Kg";
            String und2 = " Cm";
            float w = highlight.getY();
            float h = highlight.getX();

            if(wei_unit == Units.POUND) {
                w = Units.kg_to_pnds(highlight.getY());
                und = " Lb";
            }
            if(len_unit == Units.INCH){
                h = Units.cm_to_inches(highlight.getX());
                und2 = " In";
            }
            tvContent.setText(getString(R.string.weight) +
                    String.format(Locale.getDefault(), "%.1f", w) + und); // set the entry-value as the display text

            age.setText(getString(R.string.height) +
                    String.format(Locale.getDefault(), "%.1f", h) + und2);

        }



    }
    public class WeightMarkerView extends MarkerView {

        private TextView tvContent;
        private MyTextView age;

        public WeightMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            // this markerview only displays a textview
            tvContent = (TextView) findViewById(R.id.weight);
            age = (MyTextView) findViewById(R.id.age);
        }

        // callbacks everytime the MyMarkerView is redrawn, can be used to update the
        // content (user-interface)
        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            int wei_unit = Integer.valueOf(Prefs.getString("weight_unit", "0"));
            String und = " Kg";
            float w = highlight.getY();
            if(wei_unit == Units.POUND) {
                w = Units.kg_to_pnds(highlight.getY());
                und = " Lb";
            }
            tvContent.setText(getString(R.string.weight) +
                    String.format(Locale.getDefault(), "%.1f", w) + und); // set the entry-value as the display text

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, (int) highlight.getX());
            Date now = new Date();
            Interval interval = new Interval(now.getTime(), calendar.getTime().getTime());
            Period p = interval.toPeriod();
            age.setText(getString(R.string.age) + p.getYears() + getString(R.string.years)
                    + p.getMonths() + getString(R.string.months) + p.getDays() + getString(R.string.days));
        }




    }
    public class HeightMarkerView extends MarkerView {

        private TextView tvContent;
        private MyTextView age;

        public HeightMarkerView(Context context, int layoutResource) {
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
            tvContent.setText(getString(R.string.height) +
                    String.format(Locale.getDefault(), "%.1f", h)+und2); // set the entry-value as the display text
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, (int) highlight.getX());
            Date now = new Date();
            Interval interval = new Interval(now.getTime(), calendar.getTime().getTime());
            Period p = interval.toPeriod(PeriodType.yearMonthDay());
            age.setText(getString(R.string.age) + p.getYears() + getString(R.string.years)
                    + p.getMonths() + getString(R.string.months) + p.getDays() + getString(R.string.days));
        }


    }

    public class ImcMarkerView extends MarkerView {

        private TextView tvContent;
        private MyTextView age;

        public ImcMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            // this markerview only displays a textview
            tvContent = (TextView) findViewById(R.id.weight);
            age = (MyTextView) findViewById(R.id.age);
        }

        // callbacks everytime the MyMarkerView is redrawn, can be used to update the
        // content (user-interface)
        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            tvContent.setText(getString(R.string.imc) + String.format(Locale.getDefault(),
                    "%.1f", highlight.getY())); // set the entry-value as the display text
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, (int) highlight.getX());
            Date now = new Date();
            Interval interval = new Interval(now.getTime(), calendar.getTime().getTime());
            Period p = interval.toPeriod(PeriodType.yearMonthDay());
            age.setText(getString(R.string.age) + p.getYears() + getString(R.string.years)
                    + p.getMonths() + getString(R.string.months) + p.getDays() + getString(R.string.days));
        }


    }
    public class CefalicMarkerView extends MarkerView {

        private TextView tvContent;
        private MyTextView age;

        public CefalicMarkerView(Context context, int layoutResource) {
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


    }
}
