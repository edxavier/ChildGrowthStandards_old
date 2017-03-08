package com.edxavier.childgrowthstandards.helpers.constans;

/**
 * Created by Eder Xavier Rojas on 15/08/2016.
 */
public class Units {
    public static float KG_HAS_POUNDS = 2.20462f;
    public static float INCHES_HAS_CM = 2.54f;

    public static int KILOGRAM = 0;
    public static int POUND = 1;
    public static int CENTIMETER = 2;
    public static int INCH = 3;

    public static float cm_to_inches(float cms){
        return cms / INCHES_HAS_CM;
    }

    public static float kg_to_pnds(float kg){
        return kg * KG_HAS_POUNDS;
    }
    public static float inches_to_cm(float in){
        return in * INCHES_HAS_CM;
    }

    public static float lb_to_kg(float lb){
        return lb / KG_HAS_POUNDS;
    }
}
