package com.edxavier.childgrowthstandards.fragments.childs;


import com.edxavier.childgrowthstandards.db.Child;
import com.edxavier.childgrowthstandards.db.ChildHistory;
import com.edxavier.childgrowthstandards.helpers.PresenterBase;

import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 17/09/2016.
 */
public class Contracts {

    public interface ChildsView {
        void showEmptyMessage();
        void hideEmptyMessage();
        void setChilds(RealmResults<Child> childs);
    }

    public interface ChildsPresenter extends PresenterBase{
        void getChilds();
    }

    public interface ChildsInteractor {
        RealmResults<Child> getChilds();
        void onDestroy();
    }
}
