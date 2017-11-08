package com.edxavier.childgrowthstandards;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.edxavier.childgrowthstandards.fragments.ChartsFragment;
import com.edxavier.childgrowthstandards.fragments.ProfileFragment;
import com.edxavier.childgrowthstandards.fragments.RecordsFragment;
import com.edxavier.childgrowthstandards.helpers.constans.Units;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.TabSelectionInterceptor;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ChildDetailsActivity extends AppCompatActivity implements TabSelectionInterceptor {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.appbarLayout)
    AppBarLayout appbarLayout;
    @BindView(R.id.bottomBar)
    BottomBar bottomBar;
    @BindView(R.id.coordinator)
    CoordinatorLayout coordinator;
    @BindView(R.id.fragmentContainer)
    FrameLayout fragmentContainer;
    private Bundle args;
    private FragmentManager manager;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_details);
        ButterKnife.bind(this);
        args = getIntent().getExtras();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(args.getString("name"));
        toolbar.setTitle(args.getString("name"));
        manager = getSupportFragmentManager();
        bottomBar.setTabSelectionInterceptor(this);

        ProfileFragment profile = new ProfileFragment();
        profile.setArguments(args);
        manager.beginTransaction()
                .replace(R.id.fragmentContainer, profile)
                .commit();
        if (Units.isTimeToAds()) {
            requestInterstical();
            showInterstical();
        }
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(this);
        analytics.logEvent("ChildDetailsActivity", null);
    }

    @Override
    public boolean shouldInterceptTabSelection(@IdRes int oldTabId, @IdRes int newTabId) {
        switch (newTabId) {
            case R.id.tab_profile:
                ProfileFragment profile = new ProfileFragment();
                profile.setArguments(args);
                manager.beginTransaction()
                        .replace(R.id.fragmentContainer, profile)
                        .commit();
                break;
            case R.id.tab_percentils:
                ChartsFragment perc = new ChartsFragment();
                perc.setArguments(args);
                manager.beginTransaction()
                        .replace(R.id.fragmentContainer, perc)
                        .commit();
                break;

            case R.id.tab_measures:
                RecordsFragment rf = new RecordsFragment();
                rf.setArguments(args);
                manager.beginTransaction()
                        .replace(R.id.fragmentContainer, rf)
                        .commit();
                break;
        }
        return false;
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

    public void showInterstical() {
        if (mInterstitialAd.isLoaded()) {
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
                        .subscribe(aLong -> {
                                },
                                throwable -> {
                                }, () -> {
                                    dlg.dismiss();
                                    mInterstitialAd.show();
                                });
            } catch (Exception ignored) {
            }
        } else {
            Observable.interval(1, TimeUnit.SECONDS).take(4)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> {
                            },
                            throwable -> {
                            }, this::showInterstical);
        }
    }

    public void requestInterstical() {
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
                        .subscribe(aLong -> {
                                },
                                throwable -> {
                                }, () -> {
                                    requestInterstical();
                                });
            }
        });
        if (!mInterstitialAd.isLoaded())
            mInterstitialAd.loadAd(adRequest);
    }
}
