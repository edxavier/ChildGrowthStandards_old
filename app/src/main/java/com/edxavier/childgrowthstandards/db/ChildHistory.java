package com.edxavier.childgrowthstandards.db;

import com.edxavier.childgrowthstandards.helpers.UUIDGenerator;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Eder Xavier Rojas on 15/08/2016.
 */
public class ChildHistory extends RealmObject{
    @PrimaryKey
    String id;
    Child child;
    float weight_pnds;
    float height_cms;
    float head_circ;
    float bmi;
    float living_days;
    Date created;

    public ChildHistory() {
        this.id = UUIDGenerator.nextUUID();
        this.created = new Date();
    }


    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }


    public Child getChild() {
        return child;
    }

    public void setChild(Child child) {
        this.child = child;
    }

    public float getWeight_pnds() {
        return weight_pnds;
    }

    public void setWeight_pnds(float weight_pnds) {
        this.weight_pnds = weight_pnds;
    }

    public float getHeight_cms() {
        return height_cms;
    }

    public void setHeight_cms(float height_cms) {
        this.height_cms = height_cms;
    }

    public float getHead_circ() {
        return head_circ;
    }

    public void setHead_circ(float head_circ) {
        this.head_circ = head_circ;
    }

    public float getBmi() {
        return bmi;
    }

    public void setBmi(float bmi) {
        this.bmi = bmi;
    }

    public float getLiving_days() {
        return living_days;
    }

    public void setLiving_days(float living_days) {
        this.living_days = living_days;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
