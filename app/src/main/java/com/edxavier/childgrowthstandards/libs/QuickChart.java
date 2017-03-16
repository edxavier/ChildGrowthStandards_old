package com.edxavier.childgrowthstandards.libs;

import android.content.Context;

import com.edxavier.childgrowthstandards.R;
import com.edxavier.childgrowthstandards.db.percentiles.BmiForAge;
import com.edxavier.childgrowthstandards.db.percentiles.HeadCircForAge;
import com.edxavier.childgrowthstandards.db.percentiles.HeightForAge;
import com.edxavier.childgrowthstandards.db.percentiles.WeightForAge;
import com.edxavier.childgrowthstandards.db.percentiles.WeightForHeight;
import com.edxavier.childgrowthstandards.helpers.constans.Gender;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.realm.implementation.RealmLineDataSet;

import java.util.ArrayList;

import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 13/03/2017.
 */

public class QuickChart {


    public static LineData getWeigtPercentiles(RealmResults<WeightForAge> results,
                                               int gender, Context context){
        int edgesColor = context.getResources().getColor(R.color.md_red_500);
        int innnerEdgeColor = context.getResources().getColor(R.color.md_orange_500);
        int mediaColor = context.getResources().getColor(R.color.md_green_500);

        ArrayList<Float> mods = new ArrayList<Float>();
        //para no sobrecargar el grafico de los primeros 1850 registros mostraremos cada 7 registros
        for (int i = 0; i < results.size(); i++) {
            if(i<1850) {
                if (i % 7 == 0) {
                    mods.add(results.get(i).getDay());
                }
            }else
                mods.add(results.get(i).getDay());
        }
        Float [] mods7 = new Float[mods.size()];
        mods7 = mods.toArray(mods7);
        results =  results.where().in("day", mods7).findAll();
        RealmLineDataSet<WeightForAge> mediansDataSet = new RealmLineDataSet<>(results, "day", "median_girls");
        RealmLineDataSet<WeightForAge> ninetySevenDataSet = new RealmLineDataSet<>(results, "day", "ninetySeven_girls");
        RealmLineDataSet<WeightForAge> thirdLineDataSet = new RealmLineDataSet<>(results, "day", "third_girls");
        RealmLineDataSet<WeightForAge> eightyFiveDataSet = new RealmLineDataSet<>(results, "day", "eightyFive_girls");
        RealmLineDataSet<WeightForAge> fifteenDataSet = new RealmLineDataSet<>(results, "day", "fifteen_girls");
        LineData lineData = null;
        if(gender == Gender.MALE){
            mediansDataSet = new RealmLineDataSet<>(results, "day", "median_boys");
            ninetySevenDataSet = new RealmLineDataSet<>(results, "day", "ninetySeven_boys");
            thirdLineDataSet = new RealmLineDataSet<>(results, "day", "third_boys");
            eightyFiveDataSet = new RealmLineDataSet<>(results, "day", "eightyFive_boys");
            fifteenDataSet = new RealmLineDataSet<>(results, "day", "fifteen_boys");
        }

        lineData= new LineData(ChartStyler.setupCommonLineDataset(ninetySevenDataSet, edgesColor,
                context.getResources().getString(R.string.p97)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(eightyFiveDataSet, innnerEdgeColor,
                context.getResources().getString(R.string.p85)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(mediansDataSet, mediaColor,
                context.getResources().getString(R.string.p50)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(fifteenDataSet, innnerEdgeColor,
                context.getResources().getString(R.string.p15)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(thirdLineDataSet, edgesColor,
                context.getResources().getString(R.string.p3)));
        return lineData;
    }

    public static LineData getHeigtPercentiles(RealmResults<HeightForAge> results,
                                               int gender, Context context){
        int edgesColor = context.getResources().getColor(R.color.md_red_500);
        int innnerEdgeColor = context.getResources().getColor(R.color.md_orange_500);
        int mediaColor = context.getResources().getColor(R.color.md_green_500);

        ArrayList<Float> mods = new ArrayList<Float>();
        //para no sobrecargar el grafico de los primeros 1850 registros mostraremos cada 7 registros
        for (int i = 0; i < results.size(); i++) {
            if(i<1850) {
                if (i % 7 == 0) {
                    mods.add(results.get(i).getDay());
                }
            }else
                mods.add(results.get(i).getDay());
        }
        Float [] mods7 = new Float[mods.size()];
        mods7 = mods.toArray(mods7);
        results =  results.where().in("day", mods7).findAll();
        RealmLineDataSet<HeightForAge> mediansDataSet = new RealmLineDataSet<>(results, "day", "median_girls");
        RealmLineDataSet<HeightForAge> ninetySevenDataSet = new RealmLineDataSet<>(results, "day", "ninetySeven_girls");
        RealmLineDataSet<HeightForAge> thirdLineDataSet = new RealmLineDataSet<>(results, "day", "third_girls");
        RealmLineDataSet<HeightForAge> eightyFiveDataSet = new RealmLineDataSet<>(results, "day", "eightyFive_girls");
        RealmLineDataSet<HeightForAge> fifteenDataSet = new RealmLineDataSet<>(results, "day", "fifteen_girlsv");
        LineData lineData = null;
        if(gender == Gender.MALE){
            mediansDataSet = new RealmLineDataSet<>(results, "day", "median_boys");
            ninetySevenDataSet = new RealmLineDataSet<>(results, "day", "ninetySeven_boys");
            thirdLineDataSet = new RealmLineDataSet<>(results, "day", "third_boys");
            eightyFiveDataSet = new RealmLineDataSet<>(results, "day", "eightyFive_boys");
            fifteenDataSet = new RealmLineDataSet<>(results, "day", "fifteen_boys");
        }

        lineData = new LineData(ChartStyler.setupCommonLineDataset(ninetySevenDataSet, edgesColor,
                context.getResources().getString(R.string.p97)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(eightyFiveDataSet, innnerEdgeColor,
                context.getResources().getString(R.string.p85)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(mediansDataSet, mediaColor,
                context.getResources().getString(R.string.p50)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(fifteenDataSet, innnerEdgeColor,
                context.getResources().getString(R.string.p15)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(thirdLineDataSet, edgesColor,
                context.getResources().getString(R.string.p3)));
        return lineData;
    }

    public static LineData getIMCPercentiles(RealmResults<BmiForAge> results,
                                               int gender, Context context){
        int edgesColor = context.getResources().getColor(R.color.md_red_500);
        int innnerEdgeColor = context.getResources().getColor(R.color.md_orange_500);
        int mediaColor = context.getResources().getColor(R.color.md_green_500);

        ArrayList<Float> mods = new ArrayList<Float>();
        //para no sobrecargar el grafico de los primeros 1850 registros mostraremos cada 7 registros
        for (int i = 0; i < results.size(); i++) {
            if(i<1850) {
                if (i % 7 == 0) {
                    mods.add(results.get(i).getDay());
                }
            }else
                mods.add(results.get(i).getDay());
        }
        Float [] mods7 = new Float[mods.size()];
        mods7 = mods.toArray(mods7);
        results =  results.where().in("day", mods7).findAll();
        RealmLineDataSet<BmiForAge> mediansDataSet = new RealmLineDataSet<>(results, "day", "median_girls");
        RealmLineDataSet<BmiForAge> ninetySevenDataSet = new RealmLineDataSet<>(results, "day", "ninetySeven_girls");
        RealmLineDataSet<BmiForAge> thirdLineDataSet = new RealmLineDataSet<>(results, "day", "third_girls");
        RealmLineDataSet<BmiForAge> eightyFiveDataSet = new RealmLineDataSet<>(results, "day", "eightyFive_girls");
        RealmLineDataSet<BmiForAge> fifteenDataSet = new RealmLineDataSet<>(results, "day", "fifteen_girls");
        LineData lineData = null;
        if(gender == Gender.MALE){
            mediansDataSet = new RealmLineDataSet<>(results, "day", "median_boys");
            ninetySevenDataSet = new RealmLineDataSet<>(results, "day", "ninetySeven_boys");
            thirdLineDataSet = new RealmLineDataSet<>(results, "day", "third_boys");
            eightyFiveDataSet = new RealmLineDataSet<>(results, "day", "eightyFive_boys");
            fifteenDataSet = new RealmLineDataSet<>(results, "day", "fifteen_boys");
        }

        lineData= new LineData(ChartStyler.setupCommonLineDataset(ninetySevenDataSet, edgesColor,
                context.getResources().getString(R.string.p97)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(eightyFiveDataSet, innnerEdgeColor,
                context.getResources().getString(R.string.p85)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(mediansDataSet, mediaColor,
                context.getResources().getString(R.string.p50)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(fifteenDataSet, innnerEdgeColor,
                context.getResources().getString(R.string.p15)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(thirdLineDataSet, edgesColor,
                context.getResources().getString(R.string.p3)));
        return lineData;
    }

    public static LineData getWeigt_x_HeightPercentiles(RealmResults<WeightForHeight> results,
                                               int gender, Context context){
        int edgesColor = context.getResources().getColor(R.color.md_red_500);
        int innnerEdgeColor = context.getResources().getColor(R.color.md_orange_500);
        int mediaColor = context.getResources().getColor(R.color.md_green_500);

        RealmLineDataSet<WeightForHeight> mediansDataSet = new RealmLineDataSet<>(results, "height", "median_girls");
        RealmLineDataSet<WeightForHeight> ninetySevenDataSet = new RealmLineDataSet<>(results, "height", "ninetySeven_girls");
        RealmLineDataSet<WeightForHeight> thirdLineDataSet = new RealmLineDataSet<>(results, "height", "third_girls");
        RealmLineDataSet<WeightForHeight> eightyFiveDataSet = new RealmLineDataSet<>(results, "height", "eightyFive_girls");
        RealmLineDataSet<WeightForHeight> fifteenDataSet = new RealmLineDataSet<>(results, "height", "fifteen_girlsv");
        LineData lineData = null;
        if(gender == Gender.MALE){
            mediansDataSet = new RealmLineDataSet<>(results, "height", "median_boys");
            ninetySevenDataSet = new RealmLineDataSet<>(results, "height", "ninetySeven_boys");
            thirdLineDataSet = new RealmLineDataSet<>(results, "height", "third_boys");
            eightyFiveDataSet = new RealmLineDataSet<>(results, "height", "eightyFive_boys");
            fifteenDataSet = new RealmLineDataSet<>(results, "height", "fifteen_boys");
        }

        lineData= new LineData(ChartStyler.setupCommonLineDataset(ninetySevenDataSet, edgesColor,
                context.getResources().getString(R.string.p97)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(eightyFiveDataSet, innnerEdgeColor,
                context.getResources().getString(R.string.p85)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(mediansDataSet, mediaColor,
                context.getResources().getString(R.string.p50)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(fifteenDataSet, innnerEdgeColor,
                context.getResources().getString(R.string.p15)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(thirdLineDataSet, edgesColor,
                context.getResources().getString(R.string.p3)));
        return lineData;
    }

    public static LineData getPerimeterPercentiles(RealmResults<HeadCircForAge> results,
                                               int gender, Context context){
        int edgesColor = context.getResources().getColor(R.color.md_red_500);
        int innnerEdgeColor = context.getResources().getColor(R.color.md_orange_500);
        int mediaColor = context.getResources().getColor(R.color.md_green_500);

        RealmLineDataSet<HeadCircForAge> mediansDataSet = new RealmLineDataSet<>(results, "day", "median_girls");
        RealmLineDataSet<HeadCircForAge> ninetySevenDataSet = new RealmLineDataSet<>(results, "day", "ninetySeven_girls");
        RealmLineDataSet<HeadCircForAge> thirdLineDataSet = new RealmLineDataSet<>(results, "day", "third_girls");
        RealmLineDataSet<HeadCircForAge> eightyFiveDataSet = new RealmLineDataSet<>(results, "day", "eightyFive_girls");
        RealmLineDataSet<HeadCircForAge> fifteenDataSet = new RealmLineDataSet<>(results, "day", "fifteen_girls");
        LineData lineData = null;
        if(gender == Gender.MALE){
            mediansDataSet = new RealmLineDataSet<>(results, "day", "median_boys");
            ninetySevenDataSet = new RealmLineDataSet<>(results, "day", "ninetySeven_boys");
            thirdLineDataSet = new RealmLineDataSet<>(results, "day", "third_boys");
            eightyFiveDataSet = new RealmLineDataSet<>(results, "day", "eightyFive_boys");
            fifteenDataSet = new RealmLineDataSet<>(results, "day", "fifteen_boys");
        }

        lineData= new LineData(ChartStyler.setupCommonLineDataset(ninetySevenDataSet, edgesColor,
                context.getResources().getString(R.string.p97)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(eightyFiveDataSet, innnerEdgeColor,
                context.getResources().getString(R.string.p85)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(mediansDataSet, mediaColor,
                context.getResources().getString(R.string.p50)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(fifteenDataSet, innnerEdgeColor,
                context.getResources().getString(R.string.p15)));
        lineData.addDataSet(ChartStyler.setupCommonLineDataset(thirdLineDataSet, edgesColor,
                context.getResources().getString(R.string.p3)));
        return lineData;
    }

}
