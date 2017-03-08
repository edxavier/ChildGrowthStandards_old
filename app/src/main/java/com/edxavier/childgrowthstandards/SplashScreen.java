package com.edxavier.childgrowthstandards;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Observable;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.edxavier.childgrowthstandards.db.initializer.InitBmiForAge;
import com.edxavier.childgrowthstandards.db.initializer.InitHeadCircForAge;
import com.edxavier.childgrowthstandards.db.initializer.InitHeightForAge;
import com.edxavier.childgrowthstandards.db.initializer.InitWeigthForAge;
import com.edxavier.childgrowthstandards.db.initializer.InitWeigthForHeight;
import com.edxavier.childgrowthstandards.db.percentiles.HeadCircForAge;
import com.edxavier.childgrowthstandards.helpers.MyTextView;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

public class SplashScreen extends AppCompatActivity {
    Thread splashTread;
    @BindView(R.id.appName)
    MyTextView appName;
    @BindView(R.id.appVersion)
    MyTextView appVersion;
    @BindView(R.id.splash)
    ImageView splash;
    @BindView(R.id.avi)
    AVLoadingIndicatorView avi;
    @BindView(R.id.container)
    RelativeLayout container;
    @BindView(R.id.init_text)
    MyTextView initText;

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    private Integer initializeTables() {
        rx.Observable.defer(new Func0<rx.Observable<Integer>>() {
            @Override
            public rx.Observable<Integer> call() {
                return rx.Observable.just(InitWeigthForAge.initializeTable(SplashScreen.this));
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {}, throwable -> {});

        rx.Observable.defer(new Func0<rx.Observable<Integer>>() {
            @Override
            public rx.Observable<Integer> call() {
                return rx.Observable.just(InitHeightForAge.initializeTable(SplashScreen.this));
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {}, throwable -> {});
        rx.Observable.defer(new Func0<rx.Observable<Integer>>() {
            @Override
            public rx.Observable<Integer> call() {
                return rx.Observable.just(InitBmiForAge.initializeTable(SplashScreen.this));
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {}, throwable -> {});

        rx.Observable.defer(new Func0<rx.Observable<Integer>>() {
            @Override
            public rx.Observable<Integer> call() {
                return rx.Observable.just(InitWeigthForHeight.initializeTable(SplashScreen.this));
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {}, throwable -> {});

        rx.Observable.defer(new Func0<rx.Observable<Integer>>() {
            @Override
            public rx.Observable<Integer> call() {
                return rx.Observable.just(InitHeadCircForAge.initializeTable(SplashScreen.this));
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {}, throwable -> {});

        //InitHeightForAge.initializeTable(this);
        //InitBmiForAge.initializeTable(this);
        //InitWeigthForHeight.initializeTable(this);
        //InitHeadCircForAge.initializeTable(this);
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseApplication) getApplication()).getMainComponent().inject(this);
        setContentView(R.layout.activity_splash_screen);
        ButterKnife.bind(this);

        appName.setBigJoe();
        appVersion.setPierRegular();
        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            appVersion.setText(getString(R.string.app_version, info.versionName));
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        startAnimations();
    }

    private void startAnimations() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.alpha);
        anim.reset();
        RelativeLayout l = (RelativeLayout) findViewById(R.id.container);
        l.clearAnimation();
        l.startAnimation(anim);

        anim = AnimationUtils.loadAnimation(this, R.anim.translate);
        anim.reset();
        ImageView iv = (ImageView) findViewById(R.id.splash);
        iv.clearAnimation();
        iv.startAnimation(anim);

        initializeTables();
        Realm realm = Realm.getDefaultInstance();
        int takeMillis = 10;
        boolean isEmpty = realm.where(HeadCircForAge.class).findAll().isEmpty();
        if(isEmpty)
            takeMillis = 3000;
        //Wait 2 seconds before launch LoginActivity
        rx.Observable.interval(1, TimeUnit.MILLISECONDS).take(takeMillis)
                .subscribe(aLong -> {
                        }, Throwable::printStackTrace,
                        () -> {
                            Intent intent = new Intent(SplashScreen.this,
                                    WelcomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent);
                            SplashScreen.this.finish();
                        });
        /*
        splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    //initializeTables();
                    int waited = 0;
                    // Splash screen pause time
                    while (waited < 1500) {
                        sleep(100);
                        waited += 100;
                    }
                    Intent intent = new Intent(SplashScreen.this,
                            WelcomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    SplashScreen.this.finish();
                } catch (InterruptedException e) {
                    // do nothing
                } finally {
                    SplashScreen.this.finish();
                }

            }
        };
        splashTread.start();
        */
    }

}
