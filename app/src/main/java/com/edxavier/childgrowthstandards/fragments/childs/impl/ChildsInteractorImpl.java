package com.edxavier.childgrowthstandards.fragments.childs.impl;

import android.util.Log;


import com.edxavier.childgrowthstandards.db.Child;
import com.edxavier.childgrowthstandards.db.ChildHistory;
import com.edxavier.childgrowthstandards.fragments.childs.Contracts;
import com.edxavier.childgrowthstandards.helpers.RxBus;

import org.reactivestreams.Subscription;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Eder Xavier Rojas on 17/09/2016.
 */
public class ChildsInteractorImpl implements Contracts.ChildsInteractor {
    Realm realm;
    private Subscription apiSubscription;

    public ChildsInteractorImpl() {
        RxBus eventBus = RxBus.getInstance();
        this.realm = Realm.getDefaultInstance();
    }

    @Override
    public RealmResults<Child> getChilds() {
        return realm.where(Child.class)
        .findAll().sort("created", Sort.DESCENDING);
    }

    @Override
    public void onDestroy() {
        realm.close();
        //if(!apiSubscription.isUnsubscribed())
            //apiSubscription.unsubscribe();
    }

}
