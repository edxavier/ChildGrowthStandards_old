package com.edxavier.childgrowthstandards;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.edxavier.childgrowthstandards.db.Child;
import com.edxavier.childgrowthstandards.db.ChildHistory;
import com.edxavier.childgrowthstandards.db.percentiles.BmiForAge;
import com.edxavier.childgrowthstandards.db.percentiles.HeadCircForAge;
import com.edxavier.childgrowthstandards.db.percentiles.HeightForAge;
import com.edxavier.childgrowthstandards.db.percentiles.WeightForAge;
import com.edxavier.childgrowthstandards.helpers.ThemeSnackbar;
import com.edxavier.childgrowthstandards.helpers.constans.Gender;
import com.edxavier.childgrowthstandards.helpers.constans.Units;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.pixplicity.easyprefs.library.Prefs;
import com.tsongkha.spinnerdatepicker.DatePicker;
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import belka.us.androidtoggleswitch.widgets.ToggleSwitch;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;

import static android.text.TextUtils.isEmpty;

public class NewHistoryRecord extends AppCompatActivity implements com.tsongkha.spinnerdatepicker.DatePickerDialog.OnDateSetListener {

    @BindView(R.id.txtPounds)
    EditText txtPounds;
    @BindView(R.id.fabSave)
    FloatingActionButton fabSave;
    @BindView(R.id.poundsContainer)
    TextInputLayout poundsContainer;
    Bundle args;
    Realm realm;
    Child children;
    ChildHistory history = new ChildHistory();
    @BindView(R.id.calendar)
    FloatingActionButton calendar;
    @BindView(R.id.txtDate)
    TextView txtDate;
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
    @BindView(R.id.childPicture)
    ImageView childPicture;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.txtPesoIdeal)
    TextView txtPesoIdeal;
    @BindView(R.id.txtAlturaIdeal)
    TextView txtAlturaIdeal;
    @BindView(R.id.txtPCIdeal)
    TextView txtPCIdeal;
    @BindView(R.id.txtImc)
    EditText txtImc;
    @BindView(R.id.txtIMCIdeal)
    TextView txtIMCIdeal;

    @BindView(R.id.unidadPeso)
    ToggleSwitch unidadPeso;
    @BindView(R.id.unidadAltura)
    ToggleSwitch unidadAltura;
    @BindView(R.id.unidadPC)
    ToggleSwitch unidadPC;
    @BindView(R.id.adView)
    AdView adView;
    private Period m;

    private Observable<CharSequence> pesoObservable;
    private Observable<CharSequence> alturaObservable;
    private Observable<CharSequence> pcObservable;
    private InterstitialAd mInterstitialAd;
    private boolean activityVisible;

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }

    int len_unit = Integer.valueOf(Prefs.getString("height_unit", "2"));
    int wei_unit = Integer.valueOf(Prefs.getString("weight_unit", "0"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_history_record);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        FirebaseAnalytics mFirebaseAnalytics;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.logEvent("NewHistoryRecord", null);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        realm = Realm.getDefaultInstance();
        args = getIntent().getExtras();
        getSupportActionBar().setTitle(args.getString("name"));
        toolbar.setTitle(args.getString("name"));

        children = realm.where(Child.class).equalTo("id", args.getString("id")).findFirst();
        history.setChild(children);
        SimpleDateFormat time_format = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        txtDate.setText(time_format.format(history.getCreated()));

        Date age = children.getBirthday();
        birthdate = new LocalDate(age);
        p = new Period(birthdate, new LocalDate(), PeriodType.days());
        m = new Period(birthdate, new LocalDate(), PeriodType.months());
        //guardar cuantos dias de vida tiene
        history.setLiving_days(p.getDays());
        Glide.with(this).load(args.getString("pic_uri"))
                .apply(new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.baby_feets))
                .into(childPicture);
        combineLatestEvents();
        setupWidgets();
        presetValues();
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("45D8AEB3B66116F8F24E001927292BD5")
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView.setVisibility(View.VISIBLE);
            }
        });
        adView.loadAd(adRequest);
        if(Units.isTimeToAds()){
            requestInterstical();
            showInterstical();
        }
    }

    //Inicializa los check de unidades de medidas y sugerencia de valores ideales
    void presetValues() {
        Date age = children.getBirthday();
        birthdate = new LocalDate(age);
        p = new Period(birthdate, new LocalDate(history.getCreated().getTime()), PeriodType.days());
        m = new Period(birthdate, new LocalDate(history.getCreated().getTime()), PeriodType.months());
        history.setLiving_days(p.getDays());

        float living_days = 0;
        if (m.getMonths() >= 62)
            living_days = m.getMonths() * 30.43f;
        else
            living_days = (float) p.getDays();

        WeightForAge weightForAge = realm.where(WeightForAge.class)
                .equalTo("day", living_days)
                .findFirst();
        float pesoIdealBoyKG = weightForAge != null ? weightForAge.getMedian_boys() : 0;
        float pesoIdealGirlKG = weightForAge != null ? weightForAge.getMedian_boys() : 0;

        HeightForAge heightForAge = realm.where(HeightForAge.class)
                .equalTo("day", living_days)
                .findFirst();
        float alturaIdealBoyCM = heightForAge != null ? heightForAge.getMedian_boys() : 0;
        float alturaIdealGirlCM = heightForAge != null ? heightForAge.getMedian_boys() : 0;

        HeadCircForAge headCircForAge = realm.where(HeadCircForAge.class)
                .equalTo("day", Float.valueOf(p.getDays()))
                .findFirst();
        float pcIdealBoyCM = headCircForAge != null ? headCircForAge.getMedian_boys() : 0;
        float pcIdealGirlCM = headCircForAge != null ? headCircForAge.getMedian_boys() : 0;

        BmiForAge imcForAge = realm.where(BmiForAge.class)
                .equalTo("day", living_days)
                .findFirst();

        float imcIdealBoy = imcForAge != null ? imcForAge.getMedian_boys() : 0;
        float imcIdealGirl = imcForAge != null ? imcForAge.getMedian_boys() : 0;

        try {

            if (children.getGender() == Gender.FEMALE) {
                //si esta en KG el check
                if (unidadPeso.getCheckedTogglePosition() == 0) {
                    String pi = String.format(Locale.getDefault(), "%.1f", pesoIdealGirlKG).replace(",", ".");
                    txtPesoIdeal.setText(getString(R.string.peso_ideal_kg, pi));
                } else {
                    float pesoIdealLB = Units.kg_to_pnds(pesoIdealGirlKG);
                    txtPesoIdeal.setText(getString(R.string.peso_ideal_lb, String.format(Locale.getDefault(), "%.1f", pesoIdealLB).replace(",", ".")));
                }
                if (unidadAltura.getCheckedTogglePosition() == 1) {
                    txtAlturaIdeal.setText(getString(R.string.altura_ideal_cm, String.format(Locale.getDefault(), "%.1f", alturaIdealGirlCM).replace(",", ".")));
                } else {
                    float alturaIdealIN = Units.cm_to_inches(alturaIdealGirlCM);
                    txtAlturaIdeal.setText(getString(R.string.altura_ideal_in, String.format(Locale.getDefault(), "%.1f", alturaIdealIN).replace(",", ".")));
                }
                if (unidadPC.getCheckedTogglePosition() == 1) {
                    txtPCIdeal.setText(getString(R.string.pc_ideal_cm, String.format(Locale.getDefault(), "%.1f", pcIdealGirlCM).replace(",", ".")));
                } else {
                    float pcIdealIN = Units.cm_to_inches(pcIdealGirlCM);
                    txtPCIdeal.setText(getString(R.string.pc_ideal_in, String.format(Locale.getDefault(), "%.1f", pcIdealIN).replace(",", ".")));
                }

                txtIMCIdeal.setText(getString(R.string.imc_ideal, String.format(Locale.getDefault(), "%.1f", imcIdealGirl).replace(",", ".")));
                if (imcIdealGirl == 0)
                    txtIMCIdeal.setText("");
                if (pesoIdealGirlKG == 0)
                    txtPesoIdeal.setText("");
                if (alturaIdealGirlCM == 0)
                    txtAlturaIdeal.setText("");
                if (pcIdealGirlCM == 0)
                    txtPCIdeal.setText("");
            } else {
                if (unidadPeso.getCheckedTogglePosition() == Units.KILOGRAM) {
                    String pi = String.format(Locale.getDefault(), "%.1f", pesoIdealBoyKG).replace(",", ".");
                    txtPesoIdeal.setText(getString(R.string.peso_ideal_kg, pi));
                } else {
                    float pesoIdealLB = Units.kg_to_pnds(pesoIdealBoyKG);
                    txtPesoIdeal.setText(getString(R.string.peso_ideal_lb, String.format(Locale.getDefault(), "%.1f", pesoIdealLB).replace(",", ".")));
                }
                if (unidadAltura.getCheckedTogglePosition() == 1) {
                    txtAlturaIdeal.setText(getString(R.string.altura_ideal_cm, String.format(Locale.getDefault(), "%.1f", alturaIdealBoyCM).replace(",", ".")));
                } else {
                    float alturaIdealIN = Units.cm_to_inches(alturaIdealBoyCM);
                    txtAlturaIdeal.setText(getString(R.string.altura_ideal_in, String.format(Locale.getDefault(), "%.1f", alturaIdealIN).replace(",", ".")));
                }
                if (unidadPC.getCheckedTogglePosition() == 1) {
                    txtPCIdeal.setText(getString(R.string.pc_ideal_cm, String.format(Locale.getDefault(), "%.1f", pcIdealBoyCM).replace(",", ".")));
                } else {
                    float pcIdealIN = Units.cm_to_inches(pcIdealBoyCM);
                    txtPCIdeal.setText(getString(R.string.pc_ideal_in, String.format(Locale.getDefault(), "%.1f", pcIdealIN).replace(",", ".")));
                }
                txtIMCIdeal.setText(getString(R.string.imc_ideal, String.format(Locale.getDefault(), "%.1f", imcIdealBoy).replace(",", ".")));
                if (imcIdealBoy == 0)
                    txtIMCIdeal.setText("");
                if (pesoIdealBoyKG == 0)
                    txtPesoIdeal.setText("");
                if (alturaIdealBoyCM == 0)
                    txtAlturaIdeal.setText("");
                if (pcIdealBoyCM == 0)
                    txtPCIdeal.setText("");
            }
        } catch (Exception ignored) {
            Log.e("EDER_EXC", ignored.getMessage());
        }
    }

    private void setupWidgets() {
        //Establecer las unidades de medida en los widgets
        if (wei_unit == Units.KILOGRAM)
            unidadPeso.setCheckedTogglePosition(0);
        else
            unidadPeso.setCheckedTogglePosition(1);

        if (len_unit == Units.INCH) {
            unidadAltura.setCheckedTogglePosition(0);
            unidadPC.setCheckedTogglePosition(0);
        } else {
            unidadAltura.setCheckedTogglePosition(1);
            unidadPC.setCheckedTogglePosition(1);
        }
        RxView.clicks(calendar).subscribe(aVoid -> {
            Calendar now = Calendar.getInstance();
            new SpinnerDatePickerDialogBuilder()
                    .context(this)
                    .callback(this)
                    .year(now.get(Calendar.YEAR))
                    .monthOfYear(now.get(Calendar.MONTH))
                    .dayOfMonth(now.get(Calendar.DAY_OF_MONTH))
                    .build()
                    .show();
        });

        unidadPeso.setOnToggleSwitchChangeListener((position, isChecked) -> {
            String temp = txtPounds.getText().toString();
            if(!temp.isEmpty()) {
                txtPounds.setText("0");
                txtPounds.setText(temp);
            }
            presetValues();
        });
        unidadAltura.setOnToggleSwitchChangeListener((position, isChecked) -> {
            String temp = txtAltura.getText().toString();
            if(!temp.isEmpty()) {
                txtAltura.setText("0");
                txtAltura.setText(temp);
            }
            presetValues();
        });

        unidadPC.setOnToggleSwitchChangeListener((position, isChecked) -> {
            String temp = txtPerimetro.getText().toString();
            if(!temp.isEmpty()) {
                txtPerimetro.setText("0");
                txtPerimetro.setText(temp);
            }
            presetValues();
        });
        FloatingActionButton fab = findViewById(R.id.fabSave);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if ((isTextInputEmpty(txtPerimetro) || zeroInput(txtPerimetro)) &&
                        (isTextInputEmpty(txtAltura) || zeroInput(txtAltura)) &&
                        (isTextInputEmpty(txtPounds) || zeroInput(txtPounds))) {
                    ThemeSnackbar.warning(container,  "Ningun valor fue especificado", Snackbar.LENGTH_LONG).show();

                    return;
                }
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
                    realm.executeTransaction(realm1 ->realm.copyToRealm(history));
                    finish();
                } else {
                    ThemeSnackbar.warning(container, getString(R.string.duplicate_register), Snackbar.LENGTH_LONG).show();
                }
            }
        });

    }

    boolean isTextInputEmpty(TextView input) {
        return input.getText().toString().isEmpty();
    }

    boolean zeroInput(TextView input) {
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
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar ca = Calendar.getInstance();
        Calendar ca2 = Calendar.getInstance();
        Calendar minDate = Calendar.getInstance();
        ca2.add(Calendar.DATE, 1);
        minDate.add(Calendar.YEAR, -19);
        ca.set(year, monthOfYear, dayOfMonth);

        LocalDate selectedDate = new LocalDate(ca.getTime());
        LocalDate now = new LocalDate(ca2.getTime());
        LocalDate ago = new LocalDate(children.getBirthday().getTime());

        if (selectedDate.isBefore(now) && selectedDate.isAfter(ago)) {
            SimpleDateFormat time_format = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            txtDate.setText(time_format.format(ca.getTime()));
            history.setCreated(selectedDate.toDate());
            presetValues();
        } else {
            ThemeSnackbar.warning(container, getString(R.string.date_error), Snackbar.LENGTH_LONG).show();
        }
    }


    private void combineLatestEvents() {
        pesoObservable =  RxTextView.textChanges(txtPounds);
        alturaObservable =  RxTextView.textChanges(txtAltura);
        pcObservable =  RxTextView.textChanges(txtPerimetro);

        pesoObservable
                .observeOn(AndroidSchedulers.mainThread(),false,100)
                .subscribe(peso -> {
                    if (!isEmpty(peso)) {
                        if(unidadPeso.getCheckedTogglePosition()==0){
                            history.setWeight_pnds(Float.valueOf(txtPounds.getText().toString()));
                        }else {
                            history.setWeight_pnds(Units.lb_to_kg(Float.valueOf(txtPounds.getText().toString())));
                        }
                    } else
                        history.setWeight_pnds(0);
                }, Throwable::printStackTrace);

        alturaObservable
                .observeOn(AndroidSchedulers.mainThread(),false,100)
                .subscribe(altura -> {
                    if (!isEmpty(altura)) {
                        //si es cm
                        if(unidadAltura.getCheckedTogglePosition()==1){
                            history.setHeight_cms(Float.valueOf(txtAltura.getText().toString()));
                        }else {
                            history.setHeight_cms(Units.inches_to_cm(Float.valueOf(txtAltura.getText().toString())));
                        }
                    } else
                        history.setHeight_cms(0);
                }, Throwable::printStackTrace);

        pcObservable
                .observeOn(AndroidSchedulers.mainThread(),false,100)
                .subscribe(pc -> {
                    if (!isEmpty(pc)) {
                        //si es cm
                        if(unidadPC.getCheckedTogglePosition()==1){
                            history.setHead_circ(Float.valueOf(txtPerimetro.getText().toString()));
                        }else {
                            history.setHead_circ(Units.inches_to_cm(Float.valueOf(txtPerimetro.getText().toString())));
                        }
                    } else
                        history.setHead_circ(0);
                }, Throwable::printStackTrace);

        //mSubscription =
        Observable.combineLatest(pesoObservable, alturaObservable, (peso, altura) -> !isEmpty(peso) && !isEmpty(altura))
                .subscribe(valid -> {
            if (valid) {
                float altura = 0;
                float peso = 0;
                if(unidadPeso.getCheckedTogglePosition()==0){
                    peso = Float.valueOf(txtPounds.getText().toString());
                }else {
                    peso = Units.lb_to_kg(Float.valueOf(txtPounds.getText().toString()));
                }
                if(unidadAltura.getCheckedTogglePosition()==1){
                    altura = Float.valueOf(txtAltura.getText().toString());
                }else {
                    altura = Units.inches_to_cm(Float.valueOf(txtAltura.getText().toString()));
                }
                float meter = altura / 100f;
                float bmi = peso / (meter * meter);
                if(!Float.isNaN(bmi) && !Float.isInfinite(bmi)) {
                    history.setBmi(bmi);
                    txtImc.setText(String.format(Locale.getDefault(), "%.1f", bmi).replace(",", "."));
                }else {
                    history.setBmi(0);
                    txtImc.setText("0");
                }
            } else {
                history.setBmi(0);
                txtImc.setText("0");
            }
        }, throwable -> {
                    history.setBmi(0);
                    txtImc.setText("0");
                });
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
                if(activityVisible)
                    dlg.show();
                Observable.interval(1, TimeUnit.MILLISECONDS).take(2500)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aLong -> {},
                                throwable -> {}, () -> {
                                    if(activityVisible && dlg.isShowing())
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
    @Override
    protected void onPause() {
        super.onPause();
        activityVisible = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityVisible = true;
    }
}
