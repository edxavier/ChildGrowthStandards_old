package com.edxavier.childgrowthstandards.db;

import android.util.Log;

import com.edxavier.childgrowthstandards.helpers.UUIDGenerator;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Eder Xavier Rojas on 15/08/2016.
 */
public class Child extends RealmObject{

    @PrimaryKey
    public String id;
    public String child_name;
    public String photo_uri;
    public double weight_pnds;
    public double height_cms;
    public Date birthday;
    public int gender;
    public Date created;

    public Child(String id, String child_name, String photo_uri, Date birthday, int gender, Date created) {
        this.id = id;
        this.child_name = child_name;
        this.photo_uri = photo_uri;
        this.weight_pnds = weight_pnds;
        this.height_cms = height_cms;
        this.birthday = birthday;
        this.gender = gender;
        this.created = created;
    }

    public Child() {
        this.id = UUIDGenerator.nextUUID();
        this.created = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChild_name() {
        return child_name;
    }

    public void setChild_name(String child_name) {
        this.child_name = child_name;
    }

    public double getWeight_pnds() {
        return weight_pnds;
    }

    public void setWeight_pnds(double weight_pnds) {
        this.weight_pnds = weight_pnds;
    }

    public double getHeight_cms() {
        return height_cms;
    }

    public void setHeight_cms(double height_cms) {
        this.height_cms = height_cms;
    }

    public Date getBirthday() {
        return this.birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getPhoto_uri() {
        if(photo_uri==null)
            return  "no picture";
        return photo_uri;
    }

    public void setPhoto_uri(String photo_uri) {
        this.photo_uri = photo_uri;
    }
}
