package com.edxavier.childgrowthstandards.fragments.childs.impl;


import android.util.Log;

import com.edxavier.childgrowthstandards.db.Child;
import com.edxavier.childgrowthstandards.db.ChildHistory;
import com.edxavier.childgrowthstandards.fragments.childs.Contracts;

import io.realm.RealmResults;
import rx.Subscription;

/**
 * Created by Eder Xavier Rojas on 17/09/2016.
 */
public class ChildsPresenterImpl implements Contracts.ChildsPresenter {
    private Contracts.ChildsView view;
    private Contracts.ChildsInteractor interactor;

    public ChildsPresenterImpl(Contracts.ChildsView view) {
        this.interactor = new ChildsInteractorImpl();
        this.view = view;
    }

    @Override
    public void getChilds() {
        RealmResults<Child> results = interactor.getChilds();
        Log.e("EDER", "getChilds "+ results.size());

        if(results.isEmpty())
            view.showEmptyMessage();
        else
            view.setChilds(results);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {
    }
}
