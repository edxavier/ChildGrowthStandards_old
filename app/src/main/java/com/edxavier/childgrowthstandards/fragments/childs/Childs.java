package com.edxavier.childgrowthstandards.fragments.childs;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.edxavier.childgrowthstandards.NewChild;
import com.edxavier.childgrowthstandards.R;
import com.edxavier.childgrowthstandards.db.Child;
import com.edxavier.childgrowthstandards.db.ChildHistory;
import com.edxavier.childgrowthstandards.fragments.childs.adapter.AdapterChilds;
import com.edxavier.childgrowthstandards.fragments.childs.impl.ChildsPresenterImpl;
import com.edxavier.childgrowthstandards.main.MainActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;


/**
 * A simple {@link Fragment} subclass.
 */
public class Childs extends Fragment implements Contracts.ChildsView {


    @BindView(R.id.recycler_childs)
    RecyclerView recyclerChilds;
    @BindView(R.id.message_body)
    LinearLayout messageBody;
    Contracts.ChildsPresenter presenter;

    public Childs() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_childs, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter = new ChildsPresenterImpl(this);
        Realm realm = Realm.getDefaultInstance();
        setChilds(realm.where(Child.class).findAll());
        presenter.getChilds();
        setupFabBtn();
    }

    private void setupFabBtn() {
        MainActivity activity = (MainActivity) getActivity();
        activity.fabBtn.setOnClickListener(view1 -> {
            startActivity(new Intent(activity, NewChild.class));
            //Realm realm = Realm.getDefaultInstance();
            //realm.beginTransaction();
            //Child child = realm.createObject(Child.class);
            //child.setChild_name("Liam Rojas");
            //realm.commitTransaction();
        });
    }

    @Override
    public void showEmptyMessage() {
        messageBody.setVisibility(View.VISIBLE);
    }
    @Override
    public void hideEmptyMessage() {
        messageBody.setVisibility(View.GONE);
    }

    @Override
    public void setChilds(RealmResults<Child> childs) {
        if(!childs.isEmpty())
            hideEmptyMessage();
        AdapterChilds adapterChilds = new AdapterChilds((MainActivity) getActivity(), childs, this);
        recyclerChilds.setAdapter(adapterChilds);
        recyclerChilds.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }
}
