package com.edxavier.childgrowthstandards.main;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.edxavier.childgrowthstandards.HelpActivity;
import com.edxavier.childgrowthstandards.MyPreferencesActivity;
import com.edxavier.childgrowthstandards.R;
import com.edxavier.childgrowthstandards.fragments.PercentilesFragment;
import com.edxavier.childgrowthstandards.fragments.childs.Childs;
import com.edxavier.childgrowthstandards.helpers.MyTextView;
import com.edxavier.childgrowthstandards.main.interfaces.MainView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.pixplicity.easyprefs.library.Prefs;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, MainView {

    @BindView(R.id.toolbar)
    public Toolbar toolbar;
    @BindView(R.id.appbar)
    AppBarLayout appbar;
    @BindView(R.id.main_content)
    CoordinatorLayout mainContent;
    @BindView(R.id.toolbar_title)
    MyTextView toolbarTitle;
    @BindView(R.id.navview)
    NavigationView navview;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    Realm realm;
    @BindView(R.id.fabBtn)
    public FloatingActionButton fabBtn;

    private FragmentManager fragmentManager;

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
        String rate = getString(R.string.rate2);
        String later = getString(R.string.later);
        String no_tnx = getString(R.string.no_thank);
        String dlg_title = getString(R.string.rate_dlg_title);
        String dlg_desc = getString(R.string.rate_dlg_desc);


        if(!Prefs.getBoolean("rated", false) &&  Prefs.getInt("num_excecutions", 0) == Prefs.getInt("show_after", 5)){
            new LovelyStandardDialog(this)
                    .setTopColorRes(R.color.md_teal_600)
                    .setIcon(R.drawable.ic_star)
                    .setTitle(dlg_title)
                    .setMessage(dlg_desc)
                    .setPositiveButton(rate, view -> {
                        rate();
                        Prefs.putBoolean("rated", true);

                        FirebaseAnalytics mFirebaseAnalytics;
                        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
                        Bundle bundle = new Bundle();
                        mFirebaseAnalytics.logEvent("rate_app_dlg", bundle);
                    })
                    .setNegativeButtonColor(getResources().getColor(R.color.deep_orange_400))
                    .setNeutralButtonColor(getResources().getColor(R.color.md_blue_grey_600))
                    .setPositiveButtonColor(getResources().getColor(R.color.md_teal_500))
                    .setNegativeButton(no_tnx, view -> {
                        Prefs.putBoolean("rated", true);

                        FirebaseAnalytics mFirebaseAnalytics;
                        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
                        Bundle bundle = new Bundle();
                        mFirebaseAnalytics.logEvent("negative_response_dlg", bundle);

                    })
                    .setNeutralButton(later, view -> {
                        Prefs.putInt("num_excecutions", 0);
                        Random r = new Random();
                        int Low = 6;int High = 9;
                        int rnd = r.nextInt(High-Low) + Low;
                        Prefs.putInt("show_after", rnd);

                        FirebaseAnalytics mFirebaseAnalytics;
                        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
                        Bundle bundle = new Bundle();
                        mFirebaseAnalytics.logEvent("neutral_response_on_dlg", bundle);
                    })
                    .show();
        }

    }


    void rate(){
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
    }
    /**
     * Initializing collapsing toolbar
     * Will show and hide the toolbar title on scroll
     */
    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    //collapsingToolbar.setTitle(getStr(R.string.app_name));
                    toolbarTitle.setText(getString(R.string.app_name));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    toolbarTitle.setText(" ");
                    isShow = false;
                }
            }
        });
    }

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
            case R.id.menu_opcion_2:
                Intent i2 = new Intent(this, HelpActivity.class);
                startActivity(i2);
                break;
            case R.id.menu_opcion_3:
                rate();
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
}
