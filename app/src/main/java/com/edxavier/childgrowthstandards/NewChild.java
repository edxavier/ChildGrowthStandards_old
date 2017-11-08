package com.edxavier.childgrowthstandards;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.edxavier.childgrowthstandards.db.Child;
import com.edxavier.childgrowthstandards.helpers.ChildPicture;
import com.edxavier.childgrowthstandards.helpers.RxBus;
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
import com.tsongkha.spinnerdatepicker.DatePicker;
import com.tsongkha.spinnerdatepicker.DatePickerDialog;
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;

import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

import static android.text.TextUtils.isEmpty;

public class NewChild extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.txtChildName)
    EditText txtChildName;
    @BindView(R.id.nameContainer)
    TextInputLayout nameContainer;
    @BindView(R.id.childGender)
    RadioRealButtonGroup childGender;
    @BindView(R.id.txtChildBirthday)
    EditText txtChildBirthday;
    @BindView(R.id.btnSave)
    Button btnSave;
    @BindView(R.id.calendar)
    FloatingActionButton calendar;

    Child newChild;
    @BindView(R.id.ic_camera)
    FloatingActionButton icCamera;
    @BindView(R.id.childPicture)
    ImageView childPicture;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.coordinator)
    CoordinatorLayout coordinator;
    @BindView(R.id.appbarLayout)
    AppBarLayout appbarLayout;
    @BindView(R.id.adView)
    AdView adView;
    private Observable<CharSequence> nameObservable;
    private Observable<CharSequence> dateObservable;
    FirebaseAnalytics mFirebaseAnalytics;
    boolean gotNewPic = false;
    Bundle args;
    Realm realm;
    Child existentChild;
    //private RxBus eventBus;
    private boolean appBarExpanded = false;
    private Menu collapsedMenu;
    private Calendar now;
    private Disposable pictureSubs;
    private InterstitialAd mInterstitialAd;
    private boolean activityVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_child);
        ButterKnife.bind(this);
        //eventBus = RxBus.getInstance();
        args = getIntent().getExtras();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setTitle("");
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        if (args != null) {
            getSupportActionBar().setTitle(getString(R.string.edit));
        }
        realm = Realm.getDefaultInstance();
        nameObservable = RxTextView.textChanges(txtChildName).skip(1);
        dateObservable = RxTextView.textChanges(txtChildBirthday).skip(1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btnSave.setAlpha(0.5f);
        }
        setupWidgets();
        setupInputObservers();
        combineLatestEvents();
        listenForPictures();
        newChild = new Child();
        if (args != null) {
            try {
                existentChild = realm.where(Child.class).equalTo("id", args.getString("id")).findFirst();
                if (existentChild != null) {
                    txtChildName.setText(existentChild.getChild_name());
                    SimpleDateFormat time_format = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    txtChildBirthday.setText(time_format.format(existentChild.getBirthday()));
                    newChild.setBirthday(existentChild.getBirthday());
                    newChild.setGender(existentChild.getGender());
                    newChild.setPhoto_uri(existentChild.getPhoto_uri());
                    ChildPicture childPicture = new ChildPicture(Uri.parse(existentChild.getPhoto_uri()));
                    loadPic(childPicture);
                    if (existentChild.getGender() == Gender.MALE)
                        childGender.setPosition(0);
                    else
                        childGender.setPosition(1);
                }

            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.error), Toast.LENGTH_LONG).show();
            }
        } else {
            newChild.setGender(Gender.MALE);
        }


        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("45D8AEB3B66116F8F24E001927292BD5")
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        adView.setAdListener(new AdListener(){
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

    void loadPic(ChildPicture PicUri) {
        try {
            Glide.with(this).load(PicUri.uri).apply(
                    new RequestOptions()
                            .centerCrop()
                            .placeholder(getResources().getDrawable(R.drawable.kid_on_beach))
            ).into(childPicture);
            newChild.setPhoto_uri(PicUri.uri.toString());
            gotNewPic = true;
        } catch (Exception e) {
            ThemeSnackbar.alert(coordinator, "No fue posible cargar la imagen, intenta neuvamente por favor", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void listenForPictures() {
        pictureSubs = RxBus.getInstance().register(ChildPicture.class, this::loadPic);
    }

    private void setupWidgets() {

        RxView.clicks(icCamera).subscribe(aVoid1 -> {
            PictureSheetDialog bsdFragment = PictureSheetDialog.newInstance();
            bsdFragment.show(getSupportFragmentManager(), "BSDialog");
        });
        RxView.clicks(btnSave).subscribe(aVoid -> {
            realm.executeTransaction(realm1 -> {
                if (args != null) {
                    existentChild.setChild_name(newChild.getChild_name());
                    existentChild.setGender(newChild.getGender());
                    existentChild.setBirthday(newChild.getBirthday());
                    existentChild.setPhoto_uri(newChild.getPhoto_uri());
                    Bundle bundle = new Bundle();
                    if (newChild.getGender() == Gender.MALE)
                        bundle.putString("edited_child_gender", "MALE");
                    else
                        bundle.putString("edited_child_gender", "FEMALE");
                    mFirebaseAnalytics.logEvent("child_edited", bundle);
                } else {
                    Bundle bundle = new Bundle();
                    if (newChild.getGender() == Gender.MALE)
                        bundle.putString("new_child_gender", "MALE");
                    else
                        bundle.putString("new_child_gender", "FEMALE");
                    SimpleDateFormat time_year = new SimpleDateFormat("yyyy", Locale.getDefault());
                    SimpleDateFormat time_month = new SimpleDateFormat("MMM", Locale.getDefault());
                    bundle.putString("new_child_birth_year", time_year.format(newChild.getBirthday()));
                    bundle.putString("new_child_birth_month", time_month.format(newChild.getBirthday()));

                    mFirebaseAnalytics.logEvent("new_child_record", bundle);
                    realm.copyToRealm(newChild);
                }

            });
            this.finish();
        });

        txtChildBirthday.setText("dd mm yyyy");
        childGender.setOnPositionChangedListener((button, currentPosition, lastPosition) -> {
            if (currentPosition == 0)
                newChild.setGender(Gender.MALE);
            else if (currentPosition == 1)
                newChild.setGender(Gender.FEMALE);
        });

        RxView.clicks(calendar).subscribe(aVoid -> {
            now = Calendar.getInstance();
            if (newChild.getBirthday() != null)
                now.setTime(newChild.getBirthday());
            //dpd.show(getFragmentManager(), "Datepickerdialog");
            new SpinnerDatePickerDialogBuilder()
                    .context(this)
                    .callback(this)
                    .year(now.get(Calendar.YEAR))
                    .monthOfYear(now.get(Calendar.MONTH))
                    .dayOfMonth(now.get(Calendar.DAY_OF_MONTH))
                    .build()
                    .show();
        });
    }

    private void setupInputObservers() {
        nameObservable
                .subscribe(name -> {
                    if (!(!isEmpty(name) && name.length() >= 2)) {
                        nameContainer.setErrorEnabled(true);
                        nameContainer.setError(getResources().getString(R.string.activity_chil_name_error));
                    } else {
                        nameContainer.setErrorEnabled(false);
                        nameContainer.setError("");
                        newChild.setChild_name(name.toString());
                    }
                });
    }


    private void combineLatestEvents() {
        //mSubscription =
        Observable.combineLatest(nameObservable, dateObservable, (name, date) -> {
            boolean nameValid = !isEmpty(name) && name.length() >= 2;
            boolean dateValid = !date.toString().startsWith("dd");
            return nameValid && dateValid;
        }).subscribe(valid -> {
            if (valid) {
                btnSave.setEnabled(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    btnSave.setAlpha(1f);
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    btnSave.setAlpha(0.5f);
                }
                btnSave.setEnabled(false);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                supportFinishAfterTransition();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        if (!pictureSubs.isDisposed())
            pictureSubs.dispose();
        if (!realm.isClosed())
            realm.close();
        super.onDestroy();
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
        LocalDate ago = new LocalDate(minDate.getTime());

        if (selectedDate.isBefore(now) && selectedDate.isAfter(ago)) {
            SimpleDateFormat time_format = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            txtChildBirthday.setText(time_format.format(ca.getTime()));
            newChild.setBirthday(ca.getTime());
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
