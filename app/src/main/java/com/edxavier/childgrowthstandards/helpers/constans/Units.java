package com.edxavier.childgrowthstandards.helpers.constans;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.edxavier.childgrowthstandards.R;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Eder Xavier Rojas on 15/08/2016.
 */
public class Units {
    public static float KG_HAS_POUNDS = 2.20462f;
    public static float INCHES_HAS_CM = 2.54f;

    public static int KILOGRAM = 0;
    public static int POUND = 1;
    public static int CENTIMETER = 2;
    public static int INCH = 3;

    public static float cm_to_inches(float cms){
        return cms / INCHES_HAS_CM;
    }

    public static float kg_to_pnds(float kg){
        return kg * KG_HAS_POUNDS;
    }
    public static float inches_to_cm(float in){
        return in * INCHES_HAS_CM;
    }

    public static float lb_to_kg(float lb){
        return lb / KG_HAS_POUNDS;
    }

    private static int INSTERSTICAL_SHOW_AFTER = 5;
    private static int INSTERSTICAL_MIN = 7;
    private static int INSTERSTICAL_MAX = 12;


    public static boolean isTimeToAds(){
        int ne = Prefs.getInt("num_show_interstical", 0);
        Prefs.putInt("num_show_interstical", ne + 1);
        if (Prefs.getInt("num_show_interstical", 0) >= Prefs.getInt("show_after", Units.INSTERSTICAL_SHOW_AFTER)) {
            Prefs.putInt("num_show_interstical", 0);
            Random r = new Random();
            int rnd = r.nextInt(Units.INSTERSTICAL_MAX - Units.INSTERSTICAL_MIN) + Units.INSTERSTICAL_MAX;
            Prefs.putInt("show_after", rnd);
            return true;
        }else
            return false;
    }

    private static boolean timeToRequestRate(){
        return (!Prefs.getBoolean("rated", false) && Prefs.getInt("num_excecutions", 0) >= Prefs.getInt("show_dialog_after", 5));
    }

    public static void requesRate(Context context){
        if(Units.timeToRequestRate()){
            FirebaseAnalytics mFirebaseAnalytics;
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
            Bundle bundle = new Bundle();

            try {
                MaterialDialog dlg = new MaterialDialog.Builder(context)
                        .title(R.string.rate_dlg_title)
                        .titleColor(context.getResources().getColor(R.color.md_teal_700))
                        .icon(context.getResources().getDrawable(R.drawable.ic_star))
                        .content(R.string.rate_dlg_desc)
                        .cancelable(false)
                        .positiveText(R.string.rate2)
                        .negativeText(R.string.no_thank)
                        .neutralText(R.string.later)
                        .negativeColor(context.getResources().getColor(R.color.md_deep_orange_400))
                        .neutralColor(context.getResources().getColor(R.color.md_blue_grey_600))
                        .positiveColor(context.getResources().getColor(R.color.md_teal_500))
                        .onPositive((dialog, which) -> {
                            Prefs.putBoolean("rated", true);
                            Units.rate(context);
                            bundle.putString("response", "positive");
                            mFirebaseAnalytics.logEvent("show_rate_request", bundle);
                        })
                        .onNegative((dialog, which) -> {
                            Prefs.putBoolean("rated", true);
                            bundle.putString("response", "neagtive");
                            mFirebaseAnalytics.logEvent("show_rate_request", bundle);
                        })
                        .onNeutral((dialog, which) -> {
                            Prefs.putInt("num_excecutions", 0);
                            Random r = new Random();
                            int Low = 6;int High = 9;
                            int rnd = r.nextInt(High-Low) + Low;
                            Prefs.putInt("show_dialog_after", rnd);
                            bundle.putString("response", "neutral");
                            mFirebaseAnalytics.logEvent("show_rate_request", bundle);
                        })
                        .build();

                Observable.interval(1, TimeUnit.MILLISECONDS).take(1500)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aLong -> {
                                },
                                throwable -> {
                                }, dlg::show);
            } catch (Exception ignored) {
            }
        }
    }

    public static void  rate(Context context){
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to refresh following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
    }
}
