package com.edxavier.childgrowthstandards.helpers;

import android.content.Context;
import android.util.Log;

import com.edxavier.childgrowthstandards.R;
import com.edxavier.childgrowthstandards.db.Child;
import com.edxavier.childgrowthstandards.db.ChildHistory;
import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Eder Xavier Rojas on 14/12/2017.
 */

public class CSVHelper {
    public static boolean saveAllToCSV(String fullPath, String name, Context context)  {
        CSVWriter writer = null;
        try {
            Realm realm = Realm.getDefaultInstance();
            RealmResults<ChildHistory> res = realm.where(ChildHistory.class)
                    .findAllSorted("created", Sort.DESCENDING);
            List<String[]> data = new ArrayList<String[]>();
            String [] headers ={ "Child_id", "Child_name", "Photo_uri", "Birthday" ,"Gender", "Created",
                    "History_id", "Weight_pnds", "Height_cms", "Head_circ", "BMI", "living_days", "created"
            };
            data.add(headers);
            for (ChildHistory re : res) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(context.getString(R.string.date_time_format), Locale.getDefault());

                data.add(new String[] { re.child.id, re.child.child_name, re.child.photo_uri, dateFormat.format(re.child.birthday),
                    String.valueOf(re.child.gender), dateFormat.format(re.child.created),
                        re.id, String.valueOf(re.weight_pnds),String.valueOf(re.height_cms),String.valueOf(re.head_circ),
                        String.valueOf(re.bmi), String.valueOf(re.living_days), dateFormat.format(re.created)
                });
            }
            writer = new CSVWriter(new FileWriter(fullPath+"/"+name+".csv"));
            writer.writeAll(data);
            writer.close();
            realm.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("EDER", e.getMessage());
            return false;
        }
    }

    public static boolean restoreAllFromCSV(String fullPath, Context context)  {
        Realm realm = Realm.getDefaultInstance();
        try {
            InputStream inputStream = new FileInputStream(fullPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String csvLine;
            reader.readLine();
            while ((csvLine = reader.readLine()) != null) {
                csvLine = csvLine.replace('"', ' ');
                Log.e("EDER", csvLine);
                String[] row = csvLine.split(",");
                realm.executeTransaction(realm1 -> {
                    Child child = RestoreHelper.saveChild(row[0].trim(),row[1].trim(), row[2].trim(), row[3].trim(),row[4].trim(), row[5].trim(), context);
                    RestoreHelper.saveMeasures(row[6].trim(), child, row[7].trim(), row[8].trim(), row[9].trim(),
                            row[10].trim(), row[11].trim(), row[12].trim(), context);
                });
            }
            inputStream.close();
            realm.close();
            return true;
        }
        catch (Exception ex) {
            Log.e("EDER", ex.getMessage());
            if(!realm.isClosed())
                realm.close();
            return false;
        }
    }

}
