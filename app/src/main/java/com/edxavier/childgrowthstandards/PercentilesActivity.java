package com.edxavier.childgrowthstandards;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.edxavier.childgrowthstandards.fragments.CefalicFragment;
import com.edxavier.childgrowthstandards.fragments.ImcFragment;
import com.edxavier.childgrowthstandards.fragments.WeightForHeightFragment;
import com.edxavier.childgrowthstandards.fragments.WeightFragment;
import com.edxavier.childgrowthstandards.fragments.HeightFragment;
import com.google.firebase.analytics.FirebaseAnalytics;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PercentilesActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.sliding_tabs)
    TabLayout slidingTabs;
    @BindView(R.id.frg_container)
    FrameLayout frgContainer;

    Fragment[] fragments;
    FragmentManager manager;

    WeightFragment weightFragment;
    HeightFragment heightFragment = new HeightFragment();
    WeightForHeightFragment weightForHeightFragment = new WeightForHeightFragment();
    CefalicFragment cefalicFragment = new CefalicFragment();
    ImcFragment imcFragment = new ImcFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_percentiles);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        weightFragment = new WeightFragment();

        FirebaseAnalytics mFirebaseAnalytics;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        mFirebaseAnalytics.logEvent("show_child_percentiles", bundle);
        setupPageAdapter();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        slidingTabs.addOnTabSelectedListener(this);
    }


    private void setupPageAdapter() {
        manager = getSupportFragmentManager();


        Bundle bundl = getIntent().getExtras();
        weightFragment.setArguments(bundl);
        weightForHeightFragment.setArguments(bundl);
        heightFragment.setArguments(bundl);
        imcFragment.setArguments(bundl);
        cefalicFragment.setArguments(bundl);

        //manager.beginTransaction().replace(R.id.frg_container, weightFragment).commit();
        manager.beginTransaction()
                .add(R.id.frg_container, weightFragment, "weight")
                //.add(R.id.frg_container, heightFragment, "height").hide(heightFragment)
                //.add(R.id.frg_container, imcFragment, "imc").hide(imcFragment)
                //.add(R.id.frg_container, weightForHeightFragment, "WxH").hide(weightForHeightFragment)
                //.add(R.id.frg_container, cefalicFragment, "perimeter").hide(cefalicFragment)
                .commit();

        fragments = new Fragment[]{weightFragment, heightFragment, imcFragment, cefalicFragment, weightForHeightFragment};
        String[] titles = new String[]{ getString(R.string.weight),
                getString(R.string.height), getString(R.string.imc), getString(R.string.weightHeight), getString(R.string.cefalic_perimeter)};

        for (String title : titles) {
            slidingTabs.addTab(slidingTabs.newTab().setText(title));
        }
        //PagerAdapter pagerAdapter = new PagerAdapter(manager, titles, fragments);
        //viewPager.setAdapter(pagerAdapter);
        // slidingTabs.setupWithViewPager(viewPager);

    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        switch (tab.getPosition()){
            case 0:
                //manager.beginTransaction().replace(R.id.frg_container, weightFragment).commit();
                hideFragment("height");
                hideFragment("imc");
                hideFragment("WxH");
                hideFragment("perimeter");
                manager.beginTransaction()
                            .show(weightFragment)
                            .commit();
                break;
            case 1:
                //manager.beginTransaction().replace(R.id.frg_container, heightFragment).commit();
                hideFragment("weight");
                hideFragment("imc");
                hideFragment("WxH");
                hideFragment("perimeter");
                if(manager.findFragmentByTag("height")==null) {
                    manager.beginTransaction()
                            .add(R.id.frg_container, heightFragment, "height")
                            .commit();
                }else
                    manager.beginTransaction().show(heightFragment).commit();
                break;
            case 2:
                //manager.beginTransaction().replace(R.id.frg_container, imcFragment).commit();
                hideFragment("weight");
                hideFragment("height");
                hideFragment("WxH");
                hideFragment("perimeter");
                if(manager.findFragmentByTag("imc")==null) {
                    manager.beginTransaction()
                            .add(R.id.frg_container, imcFragment, "imc")
                            .commit();
                }else
                    manager.beginTransaction().show(imcFragment).commit();
                break;
            case 3:
                //manager.beginTransaction().replace(R.id.frg_container, weightForHeightFragment).commit();
                hideFragment("weight");
                hideFragment("imc");
                hideFragment("height");
                hideFragment("perimeter");
                if(manager.findFragmentByTag("WxH")==null) {
                    manager.beginTransaction()
                            .add(R.id.frg_container, weightForHeightFragment, "WxH")
                            .commit();
                }else
                    manager.beginTransaction().show(weightForHeightFragment).commit();
                break;
            case 4:
                //manager.beginTransaction().replace(R.id.frg_container, cefalicFragment).commit();
                hideFragment("weight");
                hideFragment("imc");
                hideFragment("WxH");
                hideFragment("height");
                if(manager.findFragmentByTag("perimeter")==null) {
                    manager.beginTransaction()
                            .add(R.id.frg_container, cefalicFragment, "perimeter")
                            .commit();
                }else
                    manager.beginTransaction().show(cefalicFragment).commit();
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

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

    void hideFragment(String tag){
        Fragment fragment = manager.findFragmentByTag(tag);
        if(fragment!=null) {
            manager.beginTransaction()
                    .hide(fragment)
                    .commit();
        }
    }

}
