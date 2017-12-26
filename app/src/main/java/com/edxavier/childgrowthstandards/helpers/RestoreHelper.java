package com.edxavier.childgrowthstandards.helpers;

import android.content.Context;

import com.edxavier.childgrowthstandards.R;
import com.edxavier.childgrowthstandards.db.Child;
import com.edxavier.childgrowthstandards.db.ChildHistory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;

/**
 * Created by Eder Xavier Rojas on 14/12/2017.
 */

public class RestoreHelper {

    private static Child getChild(String child_id){
        Realm realm = Realm.getDefaultInstance();
        Child exist = realm.where(Child.class)
                .equalTo("id", child_id)
                .findFirst();
        realm.close();
        return exist;
    }

    private static ChildHistory getHistory(String _id){
        Realm realm = Realm.getDefaultInstance();
        ChildHistory exist = realm.where(ChildHistory.class)
                .equalTo("id", _id)
                .findFirst();
        realm.close();
        return exist;
    }


    static Child saveChild(String child_id, String child_name, String photo_uri, String birthday, String gender, String created, Context context){
        Realm realm = Realm.getDefaultInstance();
        Child child = getChild(child_id);
        SimpleDateFormat dateFormat = new SimpleDateFormat(context.getString(R.string.date_time_format), Locale.getDefault());
        if(child==null){
            try {
                child = realm.copyToRealm(new Child(child_id, child_name, photo_uri, dateFormat.parse(birthday),
                        Integer.parseInt(gender), dateFormat.parse(created)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        realm.close();
        return child;
    }



    static void saveMeasures(String id, Child child, String weight_pnds, String height_cms,
                             String head_circ, String bmi, String living_days, String created, Context context){
        Realm realm = Realm.getDefaultInstance();
        ChildHistory lectura = getHistory(id);
        if(lectura==null){
            SimpleDateFormat dateFormat = new SimpleDateFormat(context.getString(R.string.date_time_format), Locale.getDefault());
            Date dateLectura = null;
            try {
                dateLectura = dateFormat.parse(created);
                realm.copyToRealm(new ChildHistory(id, child, Float.parseFloat(weight_pnds), Float.parseFloat(height_cms),
                        Float.parseFloat(head_circ), Float.parseFloat(bmi), Float.parseFloat(living_days), dateLectura));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        realm.close();
    }

/*    */
}
