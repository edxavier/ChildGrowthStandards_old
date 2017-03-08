package com.edxavier.childgrowthstandards.db.percentiles;

import java.util.concurrent.atomic.AtomicInteger;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Eder Xavier Rojas on 16/08/2016.
 */
public class HeadCircForAge extends RealmObject {
    @Ignore
    static AtomicInteger uniqueId=new AtomicInteger();
    @PrimaryKey
    int id;

    float day;
    float third_girls;
    float fifteen_girls;
    float median_girls;
    float eightyFive_girls;
    float ninetySeven_girls;

    float third_boys;
    float fifteen_boys;
    float median_boys;
    float eightyFive_boys;
    float ninetySeven_boys;

    public static int getUniqueId() {
        return uniqueId.incrementAndGet();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getDay() {
        return day;
    }

    public void setDay(float day) {
        this.day = day;
    }

    public float getThird_girls() {
        return third_girls;
    }

    public void setThird_girls(float third_girls) {
        this.third_girls = third_girls;
    }

    public float getFifteen_girls() {
        return fifteen_girls;
    }

    public void setFifteen_girls(float fifteen_girls) {
        this.fifteen_girls = fifteen_girls;
    }

    public float getMedian_girls() {
        return median_girls;
    }

    public void setMedian_girls(float median_girls) {
        this.median_girls = median_girls;
    }

    public float getEightyFive_girls() {
        return eightyFive_girls;
    }

    public void setEightyFive_girls(float eightyFive_girls) {
        this.eightyFive_girls = eightyFive_girls;
    }

    public float getNinetySeven_girls() {
        return ninetySeven_girls;
    }

    public void setNinetySeven_girls(float ninetySeven_girls) {
        this.ninetySeven_girls = ninetySeven_girls;
    }

    public float getThird_boys() {
        return third_boys;
    }

    public void setThird_boys(float third_boys) {
        this.third_boys = third_boys;
    }

    public float getFifteen_boys() {
        return fifteen_boys;
    }

    public void setFifteen_boys(float fifteen_boys) {
        this.fifteen_boys = fifteen_boys;
    }

    public float getMedian_boys() {
        return median_boys;
    }

    public void setMedian_boys(float median_boys) {
        this.median_boys = median_boys;
    }

    public float getEightyFive_boys() {
        return eightyFive_boys;
    }

    public void setEightyFive_boys(float eightyFive_boys) {
        this.eightyFive_boys = eightyFive_boys;
    }

    public float getNinetySeven_boys() {
        return ninetySeven_boys;
    }

    public void setNinetySeven_boys(float ninetySeven_boys) {
        this.ninetySeven_boys = ninetySeven_boys;
    }
}
