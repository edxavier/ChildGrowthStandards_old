package com.edxavier.childgrowthstandards.db.initializer;

import android.content.Context;
import android.util.Log;

import com.edxavier.childgrowthstandards.R;
import com.edxavier.childgrowthstandards.db.percentiles.HeightForAge;
import com.edxavier.childgrowthstandards.db.percentiles.WeightForAge;
import com.edxavier.childgrowthstandards.db.percentiles.WeightForHeight;
import com.edxavier.childgrowthstandards.helpers.CSVreader;

import java.io.InputStream;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 16/08/2016.
 */
public class InitWeigthForHeight {

    public static Integer initializeTable(Context context){
        Realm realm = Realm.getDefaultInstance();
        RealmResults<WeightForHeight> isEmpty = realm.where(WeightForHeight.class).findAll();
        if(isEmpty.size()<2024){
            ArrayList<float[]> boys = InitWeigthForHeight.getBoysPercentiles(context);
            ArrayList<float[]> girls = InitWeigthForHeight.getGirlsPercentiles(context);
            ArrayList<WeightForHeight> records = new ArrayList<>();
            realm.executeTransaction(realm1 -> {
                realm.delete(WeightForHeight.class);
            });

            for (int i = 0; i < boys.size(); i++) {

                final float height;
                final float girl3, girl15, girl50, girl85, girl97;
                final float boy3, boy15, boy50, boy85, boy97;
                height =  girls.get(i)[0];
                girl3 = girls.get(i)[1];
                girl15 = girls.get(i)[2];
                girl50 = girls.get(i)[3];
                girl85 = girls.get(i)[4];
                girl97 = girls.get(i)[5];

                boy3 = boys.get(i)[1];
                boy15 = boys.get(i)[2];
                boy50 = boys.get(i)[3];
                boy85 = boys.get(i)[4];
                boy97 = boys.get(i)[5];

                WeightForHeight weightForHeight = new WeightForHeight();
                weightForHeight.setId(WeightForHeight.getUniqueId());
                weightForHeight.setHeight(height);

                weightForHeight.setThird_girls(girl3);
                weightForHeight.setFifteen_girlsv(girl15);
                weightForHeight.setMedian_girls(girl50);
                weightForHeight.setEightyFive_girls(girl85);
                weightForHeight.setNinetySeven_girls(girl97);

                weightForHeight.setThird_boys(boy3);
                weightForHeight.setFifteen_boys(boy15);
                weightForHeight.setMedian_boys(boy50);
                weightForHeight.setEightyFive_boys(boy85);
                weightForHeight.setNinetySeven_boys(boy97);
                records.add(weightForHeight);
            }

            realm.executeTransaction(realm1 ->  {
                realm.copyToRealm(records);
            });
        }
        realm.close();
        return 0;
    }

    private static ArrayList<float[]> getGirlsPercentiles(Context context){
        //height, 3rd, 15th, 50th, 85th, 97th
        InputStream inputStream = context.getResources().openRawResource(R.raw.weight_for_height_girls);
        CSVreader csvReader = new CSVreader(inputStream);
        return csvReader.read();
    }

    private static ArrayList<float[]> getBoysPercentiles(Context context){
        //height, 3rd, 15th, 50th, 85th, 97th
        InputStream inputStream = context.getResources().openRawResource(R.raw.weight_for_height_boys);
        CSVreader csvReader = new CSVreader(inputStream);
        return csvReader.read();
    }
}
