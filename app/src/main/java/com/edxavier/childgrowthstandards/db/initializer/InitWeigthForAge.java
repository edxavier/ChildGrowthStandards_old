package com.edxavier.childgrowthstandards.db.initializer;

import android.content.Context;
import android.util.Log;

import com.edxavier.childgrowthstandards.R;
import com.edxavier.childgrowthstandards.db.percentiles.WeightForAge;
import com.edxavier.childgrowthstandards.helpers.CSVreader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 16/08/2016.
 */
public class InitWeigthForAge {

    public static Integer initializeTable(Context context){
        Realm realm = Realm.getDefaultInstance();
        RealmResults<WeightForAge> isEmpty = realm.where(WeightForAge.class).findAll();
        final int[] saved = {0};
        if(isEmpty.size() < 1916){
            realm.executeTransaction(realm1 -> {
                realm.delete(WeightForAge.class);
            });
            ArrayList<float[]> boys = InitWeigthForAge.getBoysPercentiles(context);
            ArrayList<float[]> girls = InitWeigthForAge.getGirlsPercentiles(context);
            ArrayList<WeightForAge> records = new ArrayList<>();
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

                WeightForAge weightForAge = new WeightForAge();
                weightForAge.setId(WeightForAge.getUniqueId());
                weightForAge.setDay(day);
                weightForAge.setThird_girls(girl3);
                weightForAge.setFifteen_girls(girl15);
                weightForAge.setMedian_girls(girl50);
                weightForAge.setEightyFive_girls(girl85);
                weightForAge.setNinetySeven_girls(girl97);

                weightForAge.setThird_boys(boy3);
                weightForAge.setFifteen_boys(boy15);
                weightForAge.setMedian_boys(boy50);
                weightForAge.setEightyFive_boys(boy85);
                weightForAge.setNinetySeven_boys(boy97);
                records.add(weightForAge);

            }
            realm.executeTransaction(realm1 -> {
                List<WeightForAge> res = realm.copyToRealm(records);
                if(res!=null)
                    saved[0] = res.size();
            });
        }
        realm.close();
        return saved[0];
    }

    private static ArrayList<float[]> getGirlsPercentiles(Context context){
        //day, 3rd, 15th, 50th, 85th, 97th
        InputStream inputStream = context.getResources().openRawResource(R.raw.weight_for_age_girls);
        CSVreader csvReader = new CSVreader(inputStream);
        return csvReader.read();
    }

    private static ArrayList<float[]> getBoysPercentiles(Context context){
        //day, 3rd, 15th, 50th, 85th, 97th
        InputStream inputStream = context.getResources().openRawResource(R.raw.weight_for_age_boys);
        CSVreader csvReader = new CSVreader(inputStream);
        return csvReader.read();
    }
}
