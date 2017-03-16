package com.edxavier.childgrowthstandards.db.initializer;

import android.content.Context;

import com.edxavier.childgrowthstandards.R;
import com.edxavier.childgrowthstandards.db.percentiles.BmiForAge;
import com.edxavier.childgrowthstandards.db.percentiles.HeadCircForAge;
import com.edxavier.childgrowthstandards.helpers.CSVreader;

import java.io.InputStream;
import java.util.ArrayList;

import io.realm.Realm;

/**
 * Created by Eder Xavier Rojas on 06/09/2016.
 */
public class InitHeadCircForAge {

    public static Integer initializeTable(Context context){
        Realm realm = Realm.getDefaultInstance();
        boolean isEmpty = realm.where(HeadCircForAge.class).findAll().isEmpty();
        if(isEmpty){
            ArrayList<float[]> boys = InitHeadCircForAge.getBoysPercentiles(context);
            ArrayList<float[]> girls = InitHeadCircForAge.getGirlsPercentiles(context);

            for (int i = 0; i < boys.size(); i++) {

                final float day;
                final float girl3, girl15, girl50, girl85, girl97;
                final float boy3, boy15, boy50, boy85, boy97;
                day =  girls.get(i)[0];
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

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        HeadCircForAge headCircForAge = realm.createObject(HeadCircForAge.class,HeadCircForAge.getUniqueId());
                        //headCircForAge.setId(HeadCircForAge.getUniqueId());
                        headCircForAge.setDay(day);

                        headCircForAge.setThird_girls(girl3);
                        headCircForAge.setFifteen_girls(girl15);
                        headCircForAge.setMedian_girls(girl50);
                        headCircForAge.setEightyFive_girls(girl85);
                        headCircForAge.setNinetySeven_girls(girl97);

                        headCircForAge.setThird_boys(boy3);
                        headCircForAge.setFifteen_boys(boy15);
                        headCircForAge.setMedian_boys(boy50);
                        headCircForAge.setEightyFive_boys(boy85);
                        headCircForAge.setNinetySeven_boys(boy97);
                    }
                });
            }
        }
        realm.close();
        return 0;
    }

    public static  ArrayList<float[]> getGirlsPercentiles(Context context){
        //day, 3rd, 15th, 50th, 85th, 97th
        InputStream inputStream = context.getResources().openRawResource(R.raw.head_circ_for_age_girls);
        CSVreader csvReader = new CSVreader(inputStream);
        return csvReader.read();
    }

    public static ArrayList<float[]> getBoysPercentiles(Context context){
        //day, 3rd, 15th, 50th, 85th, 97th
        InputStream inputStream = context.getResources().openRawResource(R.raw.head_circ_for_age_boys);
        CSVreader csvReader = new CSVreader(inputStream);
        return csvReader.read();
    }
}
