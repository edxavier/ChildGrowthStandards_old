package com.edxavier.childgrowthstandards;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.appyvet.rangebar.RangeBar;
import com.bumptech.glide.Glide;
import com.edxavier.childgrowthstandards.db.Child;
import com.edxavier.childgrowthstandards.db.ChildHistory;
import com.edxavier.childgrowthstandards.db.percentiles.HeadCircForAge;
import com.edxavier.childgrowthstandards.db.percentiles.HeightForAge;
import com.edxavier.childgrowthstandards.db.percentiles.WeightForAge;
import com.edxavier.childgrowthstandards.helpers.MyTextView;
import com.edxavier.childgrowthstandards.helpers.ThemeSnackbar;
import com.edxavier.childgrowthstandards.helpers.constans.Gender;
import com.edxavier.childgrowthstandards.helpers.constans.Units;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.pixplicity.easyprefs.library.Prefs;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import io.realm.RealmResults;

public class NewHistoryRecord extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    @BindView(R.id.toolbar_title)
    MyTextView toolbarTitle;
    @BindView(R.id.txtPounds)
    EditText txtPounds;
    @BindView(R.id.txtKilogram)
    EditText txtKilogram;
    @BindView(R.id.rangebarWeight)
    RangeBar rangebarWeight;
    @BindView(R.id.rangebarHeight)
    RangeBar rangebarHeight;
    @BindView(R.id.rangebarCefalicPerimeter)
    RangeBar rangebarCefalicPerimeter;
    @BindView(R.id.fabSave)
    FloatingActionButton fabSave;

    boolean kg_has_focus = false;
    @BindView(R.id.poundsContainer)
    TextInputLayout poundsContainer;
    @BindView(R.id.kilogramContainer)
    TextInputLayout kilogramContainer;
    @BindView(R.id.profile_image)
    CircleImageView profileImage;
    @BindView(R.id.childName)
    MyTextView childName;

    Bundle args;
    Realm realm;
    Child children;
    ChildHistory history = new ChildHistory();
    @BindView(R.id.calendar)
    FloatingActionButton calendar;
    @BindView(R.id.txtDate)
    MyTextView txtDate;
    @BindView(R.id.container)
    CoordinatorLayout container;
    Period p;
    LocalDate birthdate;
    @BindView(R.id.dateContainer)
    LinearLayout dateContainer;
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
    @BindView(R.id.chk_altura_cm)
    RadioButton chkAlturaCm;
    @BindView(R.id.chk_altura_pulg)
    RadioButton chkAlturaPulg;
    @BindView(R.id.rdGroup_altura)
    RadioGroup rdGroupAltura;
    @BindView(R.id.txtPerimetro)
    EditText txtPerimetro;
    @BindView(R.id.periemterContainer)
    TextInputLayout periemterContainer;
    @BindView(R.id.chk_perim_cm)
    RadioButton chkPerimCm;
    @BindView(R.id.chk_perim_pulg)
    RadioButton chkPerimPulg;

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }

    int len_unit = Integer.valueOf(Prefs.getString("height_unit", "2"));
    int wei_unit = Integer.valueOf(Prefs.getString("weight_unit", "0"));
    boolean firstCheckPesoKg, firstCheckPesoLb,
            firstCheckAlturaCm, firstCheckAlturaIn, firstCheckPerimCm, firstCheckPerimIn  = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_history_record);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        startSecuence();
        FirebaseAnalytics mFirebaseAnalytics;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        mFirebaseAnalytics.logEvent("History_record_activity", bundle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        realm = Realm.getDefaultInstance();
        args = getIntent().getExtras();

        children = realm.where(Child.class).equalTo("id", args.getString("id")).findFirst();
        history.setChild(children);
        SimpleDateFormat time_format = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        txtDate.setText(time_format.format(history.getCreated()));

        Date age = children.getBirthday();
        String third = "";
        birthdate = new LocalDate(age);
        p = new Period(birthdate, new LocalDate(), PeriodType.days());
        history.setCreated(new Date());
        history.setLiving_days(p.getDays());
        childName.setText(args.getString("name"));
        Glide.with(this).load(args.getString("pic_uri"))
                .placeholder(R.drawable.baby_feets)
                .into(profileImage);

        setupWidgets();
        presetValues();
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("45D8AEB3B66116F8F24E001927292BD5")
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        NativeExpressAdView ads = new NativeExpressAdView(this);
        ads.setAdSize(new AdSize(280, 80));
        ads.setAdUnitId(getResources().getString(R.string.admob_s001));
        ads.loadAd(adRequest);
        dateContainer.addView(ads);


    }

    void presetValues() {
        WeightForAge weightForAge = realm.where(WeightForAge.class)
                .equalTo("day", Float.valueOf(p.getDays()))
                .findFirst();

        RealmResults<WeightForAge> res = realm.where(WeightForAge.class).findAll();


        HeightForAge heightForAge = realm.where(HeightForAge.class)
                .equalTo("day", Float.valueOf(p.getDays()))
                .findFirst();

        HeadCircForAge headCircForAge = realm.where(HeadCircForAge.class)
                .equalTo("day", Float.valueOf(p.getDays()))
                .findFirst();
        txtAltura.setText("0");
        txtPounds.setText("0");
        txtPerimetro.setText("0");

        try {

            if (children.getGender() == Gender.FEMALE) {
                if(wei_unit == Units.KILOGRAM) {
                    txtPounds.setText(String.format(Locale.getDefault(), "%.2f", weightForAge.getMedian_girls()).replace(",", "."));
                    chkKgPeso.setChecked(true);
                }else {
                    float w = Units.kg_to_pnds(weightForAge.getMedian_girls());
                    txtPounds.setText(String.format(Locale.getDefault(), "%.2f", w).replace(",", "."));
                    chkLbPeso.setChecked(true);
                }
                if(len_unit == Units.CENTIMETER) {
                    chkAlturaCm.setChecked(true);
                    chkPerimCm.setChecked(true);
                    txtAltura.setText(String.format(Locale.getDefault(), "%.2f", heightForAge.getMedian_girls()).replace(",", "."));
                    txtPerimetro.setText(String.format(Locale.getDefault(), "%.2f", headCircForAge.getMedian_girls()).replace(",", "."));
                }else {
                    chkPerimPulg.setChecked(true);
                    chkAlturaPulg.setChecked(true);
                    float h = Units.cm_to_inches(heightForAge.getMedian_girls());
                    float hc = Units.cm_to_inches(headCircForAge.getMedian_girls());
                    txtAltura.setText(String.format(Locale.getDefault(), "%.2f", h).replace(",", "."));
                    txtPerimetro.setText(String.format(Locale.getDefault(), "%.2f", hc).replace(",", "."));
                }
            } else {

                if(wei_unit == Units.KILOGRAM) {
                    chkKgPeso.setChecked(true);
                    txtPounds.setText(String.format(Locale.getDefault(), "%.2f", weightForAge.getMedian_boys()).replace(",", "."));
                }else {
                    float w = Units.kg_to_pnds(weightForAge.getMedian_boys());
                    txtPounds.setText(String.format(Locale.getDefault(), "%.2f", w).replace(",", "."));
                    chkLbPeso.setChecked(true);
                }
                if(len_unit == Units.CENTIMETER) {
                    chkAlturaCm.setChecked(true);
                    chkPerimCm.setChecked(true);
                    txtPerimetro.setText(String.format(Locale.getDefault(), "%.2f", headCircForAge.getMedian_boys()).replace(",", "."));
                    txtAltura.setText(String.format(Locale.getDefault(), "%.2f", heightForAge.getMedian_boys()).replace(",", "."));
                }else {
                    chkPerimPulg.setChecked(true);
                    chkAlturaPulg.setChecked(true);
                    float hc = Units.cm_to_inches(headCircForAge.getMedian_boys());
                    float h = Units.cm_to_inches(heightForAge.getMedian_boys());
                    txtPerimetro.setText(String.format(Locale.getDefault(), "%.2f", hc).replace(",", "."));
                    txtAltura.setText(String.format(Locale.getDefault(), "%.2f", h).replace(",", "."));
                }
            }
        } catch (Exception ignored) {
            Log.e("EDER_EXC", ignored.getMessage());
        }
    }

    private void setupWidgets() {
        RxView.clicks(calendar).subscribe(aVoid -> {
            Child children = realm.where(Child.class).equalTo("id", args.getString("id")).findFirst();
            Calendar now = Calendar.getInstance();
            Calendar ago = Calendar.getInstance();
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    this,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );
            dpd.setMaxDate(now);
            //ago.set(Calendar.YEAR, now.get(Calendar.YEAR)-5);
            ago.setTime(children.getBirthday());
            dpd.setMinDate(ago);
            dpd.show(getFragmentManager(), "Datepickerdialog");
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabSave);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isTextInputEmpty(txtPerimetro) || zeroInput(txtPerimetro) &&
                        isTextInputEmpty(txtAltura) || zeroInput(txtAltura) &&
                        isTextInputEmpty(txtPounds)|| zeroInput(txtPounds)){
                    Toast.makeText(NewHistoryRecord.this,
                            "Ingrese al menos uno de los 3 datos requeridos", Toast.LENGTH_LONG).show();
                    return;
                }
                if(!isTextInputEmpty(txtPounds)) {
                    if (chkLbPeso.isChecked()) {
                        history.setWeight_pnds(Units.lb_to_kg(Float.valueOf(txtPounds.getText().toString())));
                    } else {
                        history.setWeight_pnds(Float.valueOf(txtPounds.getText().toString()));
                    }
                }else
                    history.setWeight_pnds(0);

                //Log.e("EDER_W", String.valueOf(history.getWeight_pnds()));
                if(!isTextInputEmpty(txtAltura)) {
                    if (chkAlturaPulg.isChecked()) {
                        history.setHeight_cms(Units.inches_to_cm(Float.valueOf(txtAltura.getText().toString())));
                    } else {
                        history.setHeight_cms(Float.valueOf(txtAltura.getText().toString()));
                    }
                }else
                    history.setHeight_cms(0);

                //Log.e("EDER_H", String.valueOf(history.getHeight_cms()));
                if(!isTextInputEmpty(txtPerimetro)) {
                    if (chkPerimPulg.isChecked())
                        history.setHead_circ(Units.inches_to_cm(Float.valueOf(txtPerimetro.getText().toString())));
                    else
                        history.setHead_circ(Float.valueOf(txtPerimetro.getText().toString()));
                }else
                    history.setHead_circ(0);

                //Log.e("EDER_P", String.valueOf(history.getHead_circ()));

                if(!isTextInputEmpty(txtPounds) && !isTextInputEmpty(txtAltura)) {
                    float meter = history.getHeight_cms() / 100f;
                    float bmi = history.getWeight_pnds() / (meter * meter);
                    history.setBmi(bmi);
                }else
                    history.setBmi(0);

                Calendar from = Calendar.getInstance();
                Calendar to = Calendar.getInstance();
                from.setTime(history.getCreated());
                to.setTime(history.getCreated());
                from.set(Calendar.HOUR_OF_DAY, 0);
                from.set(Calendar.MINUTE, 0);
                from.set(Calendar.SECOND, 0);
                to.set(Calendar.HOUR_OF_DAY, 23);
                to.set(Calendar.MINUTE, 59);
                to.set(Calendar.SECOND, 59);

                RealmResults<ChildHistory> childHistory = realm.where(ChildHistory.class)
                        .equalTo("child.id", args.getString("id"))
                        .between("created", from.getTime(), to.getTime())
                        .findAll();
                if (childHistory.isEmpty()) {
                    realm.beginTransaction();
                    realm.copyToRealm(history);
                    realm.commitTransaction();
                    finish();
                } else {
                    ThemeSnackbar.warning(container, getString(R.string.duplicate_register), Snackbar.LENGTH_LONG).show();
                }
            }
        });

    }

    boolean isTextInputEmpty(TextView input){
        return input.getText().toString().isEmpty();
    }
    boolean zeroInput(TextView input){
        return input.getText().toString().equals("0");
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

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        SimpleDateFormat time_format = new SimpleDateFormat("dd-MMM-yyy", Locale.getDefault());
        LocalDate recDate = new LocalDate(year, monthOfYear + 1, dayOfMonth);
        txtDate.setText(time_format.format(recDate.toDate()));

        p = new Period(birthdate, recDate, PeriodType.days());
        history.setLiving_days(p.getDays());
        history.setCreated(recDate.toDate());
        presetValues();
    }
    private void startSecuence() {
        if(!Prefs.getBoolean("secuence_new_history", false)) {
            new TapTargetSequence(this)
                    .targets(
                            TapTarget.forView(findViewById(R.id.txtPounds),
                                    getResources().getString(R.string.sec_new_history_title), getString(R.string.sec_new_history_content))
                                    .dimColor(R.color.md_black_1000)
                                    .outerCircleColor(R.color.md_light_blue_500)
                                    .cancelable(false),
                            TapTarget.forView(this.findViewById(R.id.rdGroup_peso),
                                    getResources().getString(R.string.sec_new_history_title2))
                                    .dimColor(R.color.md_black_1000)
                                    .outerCircleColor(R.color.md_purple_500)
                                    .cancelable(false),
                            TapTarget.forView(this.findViewById(R.id.calendar),
                                    getResources().getString(R.string.sec_new_history_title3))
                                    .dimColor(R.color.md_black_1000)
                                    .outerCircleColor(R.color.md_blue_grey_500_25)
                                    .cancelable(false),
                            TapTarget.forView(this.findViewById(R.id.fabSave),
                                    getResources().getString(R.string.sec_new_history_title4))
                                    .dimColor(R.color.md_black_1000)
                                    .outerCircleColor(R.color.md_cyan_500_25)
                                    .cancelable(false)
                    ).start();
            Prefs.putBoolean("secuence_new_history", true);
        }
    }

}
