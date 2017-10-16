package com.edxavier.childgrowthstandards;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.edxavier.childgrowthstandards.db.initializer.InitBmiForAge;
import com.edxavier.childgrowthstandards.db.initializer.InitHeadCircForAge;
import com.edxavier.childgrowthstandards.db.initializer.InitHeightForAge;
import com.edxavier.childgrowthstandards.db.initializer.InitWeigthForAge;
import com.edxavier.childgrowthstandards.db.initializer.InitWeigthForHeight;
import com.edxavier.childgrowthstandards.db.percentiles.HeadCircForAge;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

public class SplashScreen extends AppCompatActivity {
    Thread splashTread;
    @BindView(R.id.appName)
    TextView appName;
    @BindView(R.id.appVersion)
    TextView appVersion;
    @BindView(R.id.splash)
    ImageView splash;
    @BindView(R.id.avi)
    AVLoadingIndicatorView avi;
    @BindView(R.id.container)
    RelativeLayout container;
    @BindView(R.id.init_text)
    TextView initText;
    Realm realm;

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    private Integer initializeTables() {
        //Inicializa las tablas de peso para la edad
        Flowable.fromCallable(() -> InitWeigthForAge.initializeTable(SplashScreen.this))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(t -> {}, throwable -> {});

        //Inicializa las tablas de altura para la edad
        Flowable.fromCallable(() -> InitHeightForAge.initializeTable(SplashScreen.this))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(t -> {}, throwable -> {});

        //Inicializa las tablas de BMI para la edad
        Flowable.fromCallable(() -> InitBmiForAge.initializeTable(SplashScreen.this))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(t -> {}, throwable -> {});

        //Inicializa las tablas de Peso para la altura para la edad
        Flowable.fromCallable(() -> InitWeigthForHeight.initializeTable(SplashScreen.this))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(t -> {}, throwable -> {});
        //Inicializa las tablas de Peso para Circunferencia cabesa para la edad
        Flowable.fromCallable(() -> InitHeadCircForAge.initializeTable(SplashScreen.this))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(t -> {}, throwable -> {});
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        ButterKnife.bind(this);

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

        realm = Realm.getDefaultInstance();
        int takeMillis = 100;
        boolean isEmpty = realm.where(HeadCircForAge.class).findAll().isEmpty();
        if(isEmpty)
            takeMillis = 3000;
        initializeTables();
        //InitWeigthForAge.initializeTable(SplashScreen.this);
        //Wait 2 seconds before launch LoginActivity
        Observable.interval(1, TimeUnit.MILLISECONDS).take(takeMillis)
                .subscribe(aLong -> {
                        }, Throwable::printStackTrace,
                        () -> {
                            Intent intent = new Intent(SplashScreen.this,
                                    WelcomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent);
                            SplashScreen.this.finish();
                        });
    }

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }
}
