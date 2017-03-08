package com.edxavier.childgrowthstandards.helpers;

import android.content.Context;

/**
 * Created by Eder Xavier Rojas on 18/10/2016.
 */

public class Res {
    public static String getStr(int ressourceId, Context context){
        return context.getResources().getString(ressourceId);
    }
}
