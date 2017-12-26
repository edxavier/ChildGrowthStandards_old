package com.edxavier.childgrowthstandards.main;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.codekidlabs.storagechooser.StorageChooser;
import com.edxavier.childgrowthstandards.HelpActivity;
import com.edxavier.childgrowthstandards.MyPreferencesActivity;
import com.edxavier.childgrowthstandards.R;
import com.edxavier.childgrowthstandards.fragments.childs.Childs;
import com.edxavier.childgrowthstandards.helpers.CSVHelper;
import com.edxavier.childgrowthstandards.helpers.constans.Units;
import com.edxavier.childgrowthstandards.main.interfaces.MainView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.pixplicity.easyprefs.library.Prefs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, MainView {

    @BindView(R.id.toolbar)
    public Toolbar toolbar;
    @BindView(R.id.appbar)
    AppBarLayout appbar;
    @BindView(R.id.main_content)
    CoordinatorLayout mainContent;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.navview)
    NavigationView navview;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    Realm realm;
    @BindView(R.id.fabBtn)
    public FloatingActionButton fabBtn;

    private FragmentManager fragmentManager;
    private InterstitialAd mInterstitialAd;
    private boolean activityVisible;
    private static final int REQUEST_WRITE_STORAGE_PERMISSIONS = 100;
    private static final int REQUEST_READ_STORAGE_PERMISSIONS = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();
        navview.setNavigationItemSelectedListener(this);
        //+++++++++manipular los controles dentro de la cabecera+++++++++
        FrameLayout headerNav = (FrameLayout) navview.getHeaderView(0);
        TextView version = (TextView) headerNav.findViewById(R.id.drawer_veersion);
        //version.setText("ESTO FUnciona");
        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            version.setText(getString(R.string.app_version, info.versionName));
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        //+++++++++++++++++++++++++++++++++++++++++++++++++++
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.primary_dark));
        }
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, new Childs(), "childs")
                .commit();
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_navigation_menu);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        toolbarTitle.setText(getString(R.string.app_name));
        //initCollapsingToolbar();
        int ne =  Prefs.getInt("num_excecutions", 0);
        Prefs.putInt("num_excecutions", ne + 1);
        if (Units.isTimeToAds()) {
            requestInterstical();
            showInterstical();
        }else {
            Units.requesRate(this);
        }
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(this);
        analytics.logEvent("main_activity", null);

    }


    /**
     * Initializing collapsing toolbar
     * Will show and hide the toolbar title on scroll
     */

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Bundle bundle = new Bundle();
        switch (item.getItemId()){
            case R.id.menu_seguimiento:
                item.setChecked(true);
                fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, new Childs(), "childs")
                        .commit();
                fabBtn.setVisibility(View.VISIBLE);
                break;
            /*case R.id.menu_percentiles:
                item.setChecked(true);
                fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, new PercentilesFragment(), "perc")
                        .commit();
                fabBtn.setVisibility(View.GONE);
                break;*/
            case R.id.menu_opcion_1:
                Intent i = new Intent(this, MyPreferencesActivity.class);
                startActivityForResult(i, 0);
                break;
            case R.id.nav_export:
                if( ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    showFolderChooseDialog();
                }else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_WRITE_STORAGE_PERMISSIONS);
                }
                break;
            case R.id.nav_import:
                if( ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    showFileChooseDialog();
                }else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_WRITE_STORAGE_PERMISSIONS);
                }
                break;
            case R.id.menu_opcion_2:
                Intent i2 = new Intent(this, HelpActivity.class);
                startActivity(i2);
                break;
            case R.id.menu_opcion_3:
                Units.rate(this);
                FirebaseAnalytics mFirebaseAnalytics2;
                mFirebaseAnalytics2 = FirebaseAnalytics.getInstance(this);
                mFirebaseAnalytics2.logEvent("rate_app_nav", bundle);
                break;
            case R.id.menu_opcion_4:
                FirebaseAnalytics mFirebaseAnalytics;
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
                mFirebaseAnalytics.logEvent("share_app", bundle);
                try
                { Intent rate_intent = new Intent(Intent.ACTION_SEND);
                    rate_intent.setType("text/plain");
                    rate_intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
                    String sAux = getResources().getString(R.string.share_app_msg);
                    sAux = sAux + "https://play.google.com/store/apps/details?id="+ getPackageName()+" \n\n";
                    rate_intent.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(rate_intent, getResources().getString(R.string.share_using)));
                }
                catch(Exception ignored){}
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0){
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, new Childs(), "childs")
                    .commit();
        }
    }

    public void openDrawer(){
        drawerLayout.openDrawer(GravityCompat.START);
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

    private void showFolderChooseDialog() {
        // Initialize Builder
        StorageChooser chooser = new StorageChooser.Builder()
                .withActivity(this)
                .withFragmentManager(getFragmentManager())
                .withMemoryBar(true)
                .allowAddFolder(true)
                .allowCustomPath(true)
                .setType(StorageChooser.DIRECTORY_CHOOSER)
                .build();
        chooser.show();
        // get path that the user has chosen
        chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
            @Override
            public void onSelect(String path) {
                SimpleDateFormat time_format = new SimpleDateFormat(getString(R.string.date_format_save), Locale.getDefault());
                String name = getString(R.string.app_name) + time_format.format(new Date());
                name = name.replace(' ', '_');
                new MaterialDialog.Builder(MainActivity.this)
                        .title(R.string.save_as)
                        .content(path)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input("", name, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                // Do something
                                if (!CSVHelper.saveAllToCSV(path, input.toString(), getApplicationContext())){
                                    new MaterialDialog.Builder(MainActivity.this)
                                            .title("Error!")
                                            .content(R.string.export_error)
                                            .positiveText(R.string.agree)
                                            .show();
                                }else {
                                    Prefs.putString("last_path", path);
                                    new MaterialDialog.Builder(MainActivity.this)
                                            .title(R.string.export_succes)
                                            .content(path+"/"+input.toString())
                                            .positiveText(R.string.agree)
                                            .show();
                                }
                            }
                        }).show();
            }
        });
    }


    private void showFileChooseDialog() {
        // Initialize Builder
        StorageChooser chooser = new StorageChooser.Builder()
                .withActivity(this)
                .withFragmentManager(getFragmentManager())
                .withMemoryBar(true)
                .allowCustomPath(true)
                .setType(StorageChooser.FILE_PICKER)
                .build();
        // Show dialog whenever you want by
        chooser.show();
        // get path that the user has chosen
        chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
            @Override
            public void onSelect(String path) {

                if (!CSVHelper.restoreAllFromCSV(path, getApplicationContext())){
                    new MaterialDialog.Builder(MainActivity.this)
                            .title("Error!")
                            .content(R.string.import_error)
                            .positiveText(R.string.agree)
                            .show();
                }else {
                    new MaterialDialog.Builder(MainActivity.this)
                            .title(R.string.import_succes)
                            .content(path)
                            .positiveText(R.string.agree)
                            .show();
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    showFolderChooseDialog();
                } else {
                    Toast.makeText(this, "NO PERMISSIONS GRANTED", Toast.LENGTH_LONG).show();
                }
            }
            case REQUEST_READ_STORAGE_PERMISSIONS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    showFileChooseDialog();
                } else {
                    Toast.makeText(this, "NO PERMISSIONS GRANTED", Toast.LENGTH_LONG).show();
                }
                break;

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}
