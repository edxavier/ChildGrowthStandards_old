package com.edxavier.childgrowthstandards;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;

import com.edxavier.childgrowthstandards.libs.AppModule;
import com.edxavier.childgrowthstandards.main.di.DaggerMainComponent;
import com.edxavier.childgrowthstandards.main.di.MainComponent;
import com.google.android.gms.ads.MobileAds;
import com.pixplicity.easyprefs.library.Prefs;

import javax.inject.Singleton;

import dagger.Provides;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Eder Xavier Rojas on 15/08/2016.
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().build();
        Realm.setDefaultConfiguration(config);

        MobileAds.initialize(this, getResources().getString(R.string.admob_app_id));

        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

    }

    public MainComponent getMainComponent(){
        return DaggerMainComponent.builder()
                .appModule(new AppModule(this)).build();
    }
}
