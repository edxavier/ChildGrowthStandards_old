package com.edxavier.childgrowthstandards;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.edxavier.childgrowthstandards.db.initializer.InitBmiForAge;
import com.edxavier.childgrowthstandards.db.initializer.InitHeadCircForAge;
import com.edxavier.childgrowthstandards.db.initializer.InitHeightForAge;
import com.edxavier.childgrowthstandards.db.initializer.InitWeigthForAge;
import com.edxavier.childgrowthstandards.db.initializer.InitWeigthForHeight;
import com.edxavier.childgrowthstandards.db.percentiles.BmiForAge;
import com.edxavier.childgrowthstandards.db.percentiles.HeadCircForAge;
import com.edxavier.childgrowthstandards.db.percentiles.HeightForAge;
import com.edxavier.childgrowthstandards.db.percentiles.WeightForAge;
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
import com.tsongkha.spinnerdatepicker.DatePickerDialog;
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
import co.ceryle.radiorealbutton.RadioRealButtonGroup;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

import static android.text.TextUtils.isEmpty;

public class LaunchPercentil extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.calendar)
    FloatingActionButton calendar;
    @BindView(R.id.txtChildBirthday)
    EditText txtChildBirthday;
    @BindView(R.id.childGender)
    RadioRealButtonGroup childGender;

    FirebaseAnalytics analytics;

    int gender = Gender.MALE;
    Realm realm = Realm.getDefaultInstance();
    Period p;
    LocalDate birthdate;
    float x = 0;
    float y = 0;
    int len_unit = Integer.valueOf(Prefs.getString("height_unit", "2"));
    int wei_unit = Integer.valueOf(Prefs.getString("weight_unit", "0"));
    @BindView(R.id.appbarLayout)
    AppBarLayout appbarLayout;
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.dateContainer)
    LinearLayout dateContainer;
    @BindView(R.id.txtPounds)
    EditText txtPounds;
    @BindView(R.id.poundsContainer)
    TextInputLayout poundsContainer;
    @BindView(R.id.unidadPeso)
    ToggleSwitch unidadPeso;
    @BindView(R.id.txtPesoIdeal)
    TextView txtPesoIdeal;
    @BindView(R.id.txtAltura)
    EditText txtAltura;
    @BindView(R.id.alturaContainer)
    TextInputLayout alturaContainer;
    @BindView(R.id.unidadAltura)
    ToggleSwitch unidadAltura;
    @BindView(R.id.txtAlturaIdeal)
    TextView txtAlturaIdeal;
    @BindView(R.id.txtPerimetro)
    EditText txtPerimetro;
    @BindView(R.id.periemterContainer)
    TextInputLayout periemterContainer;
    @BindView(R.id.unidadPC)
    ToggleSwitch unidadPC;
    @BindView(R.id.txtPCIdeal)
    TextView txtPCIdeal;
    @BindView(R.id.txtImc)
    EditText txtImc;
    @BindView(R.id.imcContainer)
    TextInputLayout imcContainer;
    @BindView(R.id.txtIMCIdeal)
    TextView txtIMCIdeal;
    @BindView(R.id.btnShowPercentile)
    Button btnShowPercentile;
    private float val_peso = 0f;
    private float val_altura = 0f;
    private float val_pc = 0f;
    private float val_imc = 0f;
    private float living_days = 0f;
    private InterstitialAd mInterstitialAd;

    private boolean activityVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_percentil);
        ButterKnife.bind(this);
        analytics = FirebaseAnalytics.getInstance(this);
        analytics.logEvent("quick_percentil_activity", null);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle("");
        initializeTables();
        p = new Period(new LocalDate(), new LocalDate(), PeriodType.days());
        setAds();
        SimpleDateFormat time_format = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        txtChildBirthday.setText(time_format.format(new Date()));
        birthdate = new LocalDate(new Date());

        combineLatestEvents();
        setupWidgets();
        presetValues();
        int ne =  Prefs.getInt("num_excecutions", 0);
        Prefs.putInt("num_excecutions", ne + 1);
        if(Units.isTimeToAds()){
            requestInterstical();
            showInterstical();
        } else {
            Log.e("EDER", "REQUEST RATE2");
            Units.requesRate(this);
        }

    }


    private void initializeTables() {

        //Inicializa las tablas de peso para la edad
        Observable.fromCallable(() -> InitWeigthForAge.initializeTable(this))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread(),false,100)
                .subscribe(t -> {}, throwable -> {});

        //Inicializa las tablas de altura para la edad
        Observable.fromCallable(() -> InitHeightForAge.initializeTable(this))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread(),false,100)
                .subscribe(t -> {}, throwable -> {});

        //Inicializa las tablas de BMI para la edad
        Observable.fromCallable(() -> InitBmiForAge.initializeTable(this))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread(),false,100)
                .subscribe(t -> {}, throwable -> {});

        //Inicializa las tablas de Peso para la altura para la edad
        Observable.fromCallable(() -> InitWeigthForHeight.initializeTable(this))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread(),false,100)
                .subscribe(t -> {}, throwable -> {});
        //Inicializa las tablas de Peso para Circunferencia cabesa para la edad
        Observable.fromCallable(() -> InitHeadCircForAge.initializeTable(this))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread(),false,100)
                .subscribe(t -> {}, throwable -> {});
    }

    private void setAds() {
        AdRequest adRequest = new AdRequest.Builder()
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
        }, throwable -> {});
        childGender.setOnPositionChangedListener((button, currentPosition, lastPosition) -> {
            if (currentPosition == 0)
                gender = Gender.MALE;
            else if (currentPosition == 1)
                gender = Gender.FEMALE;
            presetValues();
        });

        unidadPeso.setOnToggleSwitchChangeListener((position, isChecked) -> {
            String temp = txtPounds.getText().toString();
            if (!temp.isEmpty()) {
                txtPounds.setText("0");
                txtPounds.setText(temp);
            }
            presetValues();
        });
        unidadAltura.setOnToggleSwitchChangeListener((position, isChecked) -> {
            String temp = txtAltura.getText().toString();
            if (!temp.isEmpty()) {
                txtAltura.setText("0");
                txtAltura.setText(temp);
            }
            presetValues();
        });

        unidadPC.setOnToggleSwitchChangeListener((position, isChecked) -> {
            String temp = txtPerimetro.getText().toString();
            if (!temp.isEmpty()) {
                txtPerimetro.setText("0");
                txtPerimetro.setText(temp);
            }
            presetValues();
        });

        RxView.clicks(btnShowPercentile).subscribe(o -> {
            Bundle extras = new Bundle();
            extras.putFloat("peso", val_peso);
            extras.putFloat("altura", val_altura);
            extras.putFloat("imc", val_imc);
            extras.putFloat("pc", val_pc);
            extras.putFloat("dias", living_days);
            extras.putInt("genero", gender);

            Intent intent = new Intent(this, PercentilesActivity.class);
            intent.putExtras(extras);
            Bundle bundle = new Bundle();
            if(gender == Gender.MALE)
                bundle.putString("Genero", "MALE");
            else
                bundle.putString("Genero", "FEMALE");
            SimpleDateFormat time_year = new SimpleDateFormat("yyyy", Locale.getDefault());
            SimpleDateFormat time_month = new SimpleDateFormat("MMM", Locale.getDefault());
            bundle.putString("child_birth_year", time_year.format(birthdate.toDate()));
            bundle.putString("child_birth_month", time_month.format(birthdate.toDate()));
            analytics.logEvent("quick_percentil_review", bundle);
            startActivity(intent);
        }, throwable -> {});

    }

    private void combineLatestEvents() {
        Observable<CharSequence> pesoObservable = RxTextView.textChanges(txtPounds);
        Observable<CharSequence> alturaObservable = RxTextView.textChanges(txtAltura);
        Observable<CharSequence> pcObservable = RxTextView.textChanges(txtPerimetro);

        pesoObservable
                .observeOn(AndroidSchedulers.mainThread(), false, 100)
                .subscribe(peso -> {
                    if (!isEmpty(peso)) {
                        if (unidadPeso.getCheckedTogglePosition() == 0) {
                            val_peso = Float.valueOf(txtPounds.getText().toString());
                        } else {
                            val_peso = (Units.lb_to_kg(Float.valueOf(txtPounds.getText().toString())));
                        }
                    } else
                        val_peso = (0f);
                }, Throwable::printStackTrace);

        alturaObservable
                .observeOn(AndroidSchedulers.mainThread(), false, 100)
                .subscribe(altura -> {
                    if (!isEmpty(altura)) {
                        //si es cm
                        if (unidadAltura.getCheckedTogglePosition() == 1) {
                            val_altura = (Float.valueOf(txtAltura.getText().toString()));
                        } else {
                            val_altura = (Units.inches_to_cm(Float.valueOf(txtAltura.getText().toString())));
                        }
                    } else
                        val_altura = (0f);
                }, Throwable::printStackTrace);

        pcObservable
                .observeOn(AndroidSchedulers.mainThread(), false, 100)
                .subscribe(pc -> {
                    if (!isEmpty(pc)) {
                        //si es cm
                        if (unidadPC.getCheckedTogglePosition() == 1) {
                            val_pc = (Float.valueOf(txtPerimetro.getText().toString()));
                        } else {
                            val_pc = (Units.inches_to_cm(Float.valueOf(txtPerimetro.getText().toString())));
                        }
                    } else
                        val_pc = (0f);
                }, Throwable::printStackTrace);

        //mSubscription =
        Observable.combineLatest(pesoObservable, alturaObservable, (peso, altura) -> !isEmpty(peso) && !isEmpty(altura))
                .subscribe(valid -> {
                    if (valid) {
                        float altura = 0;
                        float peso = 0;
                        if (unidadPeso.getCheckedTogglePosition() == 0) {
                            peso = Float.valueOf(txtPounds.getText().toString());
                        } else {
                            peso = Units.lb_to_kg(Float.valueOf(txtPounds.getText().toString()));
                        }
                        if (unidadAltura.getCheckedTogglePosition() == 1) {
                            altura = Float.valueOf(txtAltura.getText().toString());
                        } else {
                            altura = Units.inches_to_cm(Float.valueOf(txtAltura.getText().toString()));
                        }
                        float meter = altura / 100f;
                        float bmi = peso / (meter * meter);
                        if (!Float.isNaN(bmi) && !Float.isInfinite(bmi)) {
                            val_imc = (bmi);
                            txtImc.setText(String.format(Locale.getDefault(), "%.1f", bmi).replace(",", "."));
                        } else {
                            val_imc = (0f);
                            txtImc.setText("0");
                        }
                    } else {
                        val_imc = (0f);
                        txtImc.setText("0");
                    }
                }, throwable -> {});
    }


    //Inicializa los check de unidades de medidas y sugerencia de valores ideales
    void presetValues() {
        p = new Period(birthdate, new LocalDate(new Date()), PeriodType.days());
        Period m = new Period(birthdate, new LocalDate(new Date()), PeriodType.months());

        living_days = 0;
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

            if (gender == Gender.FEMALE) {
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


    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
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
                try {
                    Intent rate_intent = new Intent(Intent.ACTION_SEND);
                    rate_intent.setType("text/plain");
                    rate_intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
                    String sAux = getResources().getString(R.string.share_app_msg);
                    sAux = sAux + "https://play.google.com/store/apps/details?id=" + getPackageName() + " \n\n";
                    rate_intent.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(rate_intent, getResources().getString(R.string.share_using)));
                } catch (Exception ignored) {
                }
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
            case R.id.action_settings:
                startActivity(new Intent(this, MyPreferencesActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

        Calendar ca = Calendar.getInstance();
        ca.set(year, monthOfYear, dayOfMonth);
        SimpleDateFormat time_format = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        txtChildBirthday.setText(time_format.format(ca.getTime()));
        p = new Period(birthdate, new LocalDate(), PeriodType.days());
        x = p.getDays();

        Calendar ca2 = Calendar.getInstance();
        Calendar minDate = Calendar.getInstance();
        ca2.add(Calendar.DATE, 1);
        minDate.add(Calendar.YEAR, -19);
        ca.set(year, monthOfYear, dayOfMonth);

        LocalDate selectedDate = new LocalDate(ca.getTime());
        LocalDate now = new LocalDate(ca2.getTime());
        LocalDate ago = new LocalDate(minDate.getTime());

        if (selectedDate.isBefore(now) && selectedDate.isAfter(ago)) {
            txtChildBirthday.setText(time_format.format(ca.getTime()));
            birthdate = new LocalDate(ca.getTime());
            presetValues();
        } else {
            new MaterialDialog.Builder(this)
                    .title(R.string.error)
                    .content(R.string.date_error)
                    .positiveText(R.string.ok)
                    .show();
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
                Log.e("EDER", "onAdFailedToLoad");
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