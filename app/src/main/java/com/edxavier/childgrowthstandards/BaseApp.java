package com.edxavier.childgrowthstandards;

import android.app.Application;
import android.content.ContextWrapper;

import com.google.android.gms.ads.MobileAds;
import com.pixplicity.easyprefs.library.Prefs;

/**
 * Created by Eder Xavier Rojas on 08/10/2016.
 */
public class BaseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(this, getResources().getString(R.string.admob_app_id));

        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
    }
}
