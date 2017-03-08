package com.edxavier.childgrowthstandards;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.edxavier.childgrowthstandards.db.Child;
import com.edxavier.childgrowthstandards.helpers.ChildPicture;
import com.edxavier.childgrowthstandards.helpers.MyTextView;
import com.edxavier.childgrowthstandards.helpers.RxBus;
import com.edxavier.childgrowthstandards.helpers.ThemeSnackbar;
import com.edxavier.childgrowthstandards.helpers.constans.Gender;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.rohitarya.glide.facedetection.transformation.FaceCenterCrop;
import com.rohitarya.glide.facedetection.transformation.core.GlideFaceDetector;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.ceryle.radiorealbutton.library.RadioRealButtonGroup;
import io.realm.Realm;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

import static android.text.TextUtils.isEmpty;

public class NewChild extends RxAppCompatActivity implements DatePickerDialog.OnDateSetListener {

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
    @BindView(R.id.chk_record_after_save)
    CheckBox chkRecordAfterSave;
    @BindView(R.id.btnSave)
    Button btnSave;
    @BindView(R.id.calendar)
    FloatingActionButton calendar;

    Child newChild;
    @BindView(R.id.ic_camera)
    ImageView icCamera;
    @BindView(R.id.childPicture)
    ImageView childPicture;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.coordinator)
    CoordinatorLayout coordinator;
    @BindView(R.id.admob_container)
    LinearLayout admobContainer;
    @BindView(R.id.toolbar_title)
    MyTextView toolbarTitle;
    private Observable<CharSequence> nameObservable;
    private Observable<CharSequence> dateObservable;
    private Subscription mSubscription;
    private Subscription pictureSubs;
    FirebaseAnalytics mFirebaseAnalytics;

    Bundle args;
    Realm realm;
    Child existentChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_child);
        ButterKnife.bind(this);
        args = getIntent().getExtras();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        if (args != null) {
            bundle.putString("edit_child", "");
            toolbarTitle.setText(getResources().getString(R.string.edit));
        } else
            bundle.putString("new_child", "");
        mFirebaseAnalytics.logEvent("child_activity", bundle);
        realm = Realm.getDefaultInstance();
        GlideFaceDetector.initialize(this);
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
                txtChildName.setText(existentChild.getChild_name());
                SimpleDateFormat time_format = new SimpleDateFormat("dd-MMM-yyy", Locale.getDefault());
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

            } catch (Exception e) {
                bundle = new Bundle();
                bundle.putString("edit_except", "");
                mFirebaseAnalytics.logEvent("child_activity_except", bundle);
            }
        } else {
            newChild.setGender(Gender.MALE);
        }


        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("45D8AEB3B66116F8F24E001927292BD5")
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        NativeExpressAdView ads = new NativeExpressAdView(this);
        ads.setAdSize(new AdSize(280, 80));
        ads.setAdUnitId(getResources().getString(R.string.admob_s001));
        ads.loadAd(adRequest);
        admobContainer.addView(ads);

    }


    void loadPic(ChildPicture PicUri) {
        try {
            Glide.with(this).load(PicUri.uri)
                    .asBitmap()
                    .centerCrop()
                    .transform(new FaceCenterCrop())
                    .placeholder(getResources().getDrawable(R.drawable.kid_on_beach))
                    .listener(new RequestListener<Uri, Bitmap>() {
                        @Override
                        public boolean onException(Exception e, Uri model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Uri model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            int primary = getResources().getColor(R.color.primary);
                            Palette.from(resource).generate(palette -> {
                                collapsingToolbar.setContentScrimColor(palette.getVibrantColor(ContextCompat.getColor(NewChild.this, R.color.primary)));
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    Window window = getWindow();
                                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                                    window.setStatusBarColor(palette.getDarkVibrantColor(ContextCompat.getColor(NewChild.this, R.color.primary_dark)));
                                }
                            });
                            return false;
                        }
                    })
                    .into(childPicture);
            newChild.setPhoto_uri(PicUri.uri.toString());
        } catch (Exception e) {
            ThemeSnackbar.alert(coordinator, "No fue posible cargar la imagen, intenta neuvamente por favor", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void listenForPictures() {
        pictureSubs = RxBus.getInstance()
                .registerWithDebounce(500, ChildPicture.class, this::loadPic);
    }

    private void setupWidgets() {

        RxView.clicks(icCamera).subscribe(aVoid1 -> {
            PictureSheetDialog bsdFragment = PictureSheetDialog.newInstance();
            bsdFragment.show(getSupportFragmentManager(), "BSDialog");
        });
        RxView.clicks(btnSave).subscribe(aVoid -> {

            realm.beginTransaction();
            if (args != null) {
                existentChild.setChild_name(newChild.getChild_name());
                existentChild.setGender(newChild.getGender());
                existentChild.setBirthday(newChild.getBirthday());
                existentChild.setPhoto_uri(newChild.getPhoto_uri());
                mFirebaseAnalytics.logEvent("child_edited", null);
            } else {
                Bundle bundle = new Bundle();
                if (newChild.getGender() == Gender.MALE)
                    bundle.putString("new_child_gender", "MALE");
                else
                    bundle.putString("new_child_gender", "FEMALE");
                SimpleDateFormat time_year = new SimpleDateFormat("yyy", Locale.getDefault());
                SimpleDateFormat time_month = new SimpleDateFormat("MM", Locale.getDefault());
                bundle.putString("new_child_birth_year", time_year.format(newChild.getBirthday()));
                bundle.putString("new_child_birth_month", time_month.format(newChild.getBirthday()));

                mFirebaseAnalytics.setUserProperty("gender", bundle.getString("new_child_gender"));
                mFirebaseAnalytics.setUserProperty("month", bundle.getString("new_child_birth_month"));
                mFirebaseAnalytics.setUserProperty("year", bundle.getString("new_child_birth_year"));

                mFirebaseAnalytics.logEvent("new_child_record", bundle);
                realm.copyToRealm(newChild);
            }
            realm.commitTransaction();
            if (chkRecordAfterSave.isChecked()) {
                Intent intent = new Intent(this, NewHistoryRecord.class);
                Bundle args2 = new Bundle();
                if (args != null) {
                    args2.putString("name", existentChild.getChild_name());
                    args2.putString("id", existentChild.getId());
                    args2.putString("pic_uri", existentChild.getPhoto_uri());
                } else {
                    args2.putString("name", newChild.getChild_name());
                    args2.putString("id", newChild.getId());
                    args2.putString("pic_uri", newChild.getPhoto_uri());
                }
                intent.putExtras(args2);
                startActivity(intent);
            }
            this.finish();
        });

        txtChildBirthday.setText("dd-mmm-yyyy");
        childGender.setOnClickedButtonPosition(position -> {
            if (position == 0)
                newChild.setGender(Gender.MALE);
            else if (position == 1)
                newChild.setGender(Gender.FEMALE);
        });

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
    }

    private void setupInputObservers() {
        nameObservable.debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
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
        /*dateObservable.debounce(50, TimeUnit.MILLISECONDS)
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(charSequence -> {
        });*/
    }


    private void combineLatestEvents() {
        mSubscription = Observable.combineLatest(nameObservable, dateObservable, (name, date) -> {
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
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar ca = Calendar.getInstance();
        ca.set(year, monthOfYear, dayOfMonth);
        SimpleDateFormat time_format = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        txtChildBirthday.setText(time_format.format(ca.getTime()));
        newChild.setBirthday(ca.getTime());
    }

    @Override
    protected void onDestroy() {
        GlideFaceDetector.releaseDetector();
        if (!mSubscription.isUnsubscribed())
            mSubscription.unsubscribe();
        if (!pictureSubs.isUnsubscribed())
            pictureSubs.unsubscribe();
        super.onDestroy();
    }
}
