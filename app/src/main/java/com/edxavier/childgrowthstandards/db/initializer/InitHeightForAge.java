package com.edxavier.childgrowthstandards.db.initializer;

import android.content.Context;
import android.util.Log;

import com.edxavier.childgrowthstandards.R;
import com.edxavier.childgrowthstandards.db.percentiles.HeightForAge;
import com.edxavier.childgrowthstandards.db.percentiles.WeightForAge;
import com.edxavier.childgrowthstandards.helpers.CSVreader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 06/09/2016.
 */
public class InitHeightForAge {

    public static Integer initializeTable(Context context){
        Realm realm = Realm.getDefaultInstance();

        RealmResults<HeightForAge> isEmpty = realm.where(HeightForAge.class).findAll();
        final int[] saved = {0};
        if(isEmpty.size() < 2024){
            ArrayList<float[]> boys = InitHeightForAge.getBoysPercentiles(context);
            ArrayList<float[]> girls = InitHeightForAge.getGirlsPercentiles(context);
            ArrayList<HeightForAge> records = new ArrayList<>();
            realm.executeTransaction(realm1 -> {
                realm.delete(HeightForAge.class);
            });
            boolean castMonthsToDays = false;

            for (int i = 0; i < boys.size(); i++) {

                final float day;
                final float girl3, girl15, girl50, girl85, girl97;
                final float boy3, boy15, boy50, boy85, boy97;

                if(castMonthsToDays) {
                    day = girls.get(i)[0] * 30.43f;
                }else {
                    day = girls.get(i)[0];
                }

                if(girls.get(i)[0] == 1856) {
                    castMonthsToDays = true;
                }

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

                HeightForAge heightForAge = new HeightForAge();
                heightForAge.setId(HeightForAge.getUniqueId());
                heightForAge.setDay(day);

                heightForAge.setThird_girls(girl3);
                heightForAge.setFifteen_girlsv(girl15);
                heightForAge.setMedian_girls(girl50);
                heightForAge.setEightyFive_girls(girl85);
                heightForAge.setNinetySeven_girls(girl97);

                heightForAge.setThird_boys(boy3);
                heightForAge.setFifteen_boys(boy15);
                heightForAge.setMedian_boys(boy50);
                heightForAge.setEightyFive_boys(boy85);
                heightForAge.setNinetySeven_boys(boy97);
                records.add(heightForAge);
            }
            realm.executeTransaction(realm1 -> {
                List<HeightForAge> res = realm.copyToRealm(records);
                if(res!=null)
                    saved[0] = res.size();
            });
        }
        realm.close();
        return saved[0];
    }

    private static  ArrayList<float[]> getGirlsPercentiles(Context context){
        //day, 3rd, 15th, 50th, 85th, 97th
        InputStream inputStream = context.getResources().openRawResource(R.raw.height_for_age_girls);
        CSVreader csvReader = new CSVreader(inputStream);
        return csvReader.read();
    }

    private static ArrayList<float[]> getBoysPercentiles(Context context){
        //day, 3rd, 15th, 50th, 85th, 97th
        InputStream inputStream = context.getResources().openRawResource(R.raw.height_for_age_boys);
        CSVreader csvReader = new CSVreader(inputStream);
        return csvReader.read();
    }
}
