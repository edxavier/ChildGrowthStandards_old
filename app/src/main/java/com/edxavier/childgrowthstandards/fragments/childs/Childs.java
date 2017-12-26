package com.edxavier.childgrowthstandards.fragments.childs;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.edxavier.childgrowthstandards.NewChild;
import com.edxavier.childgrowthstandards.R;
import com.edxavier.childgrowthstandards.db.Child;
import com.edxavier.childgrowthstandards.fragments.childs.adapter.AdapterChilds;
import com.edxavier.childgrowthstandards.fragments.childs.impl.ChildsPresenterImpl;
import com.edxavier.childgrowthstandards.main.MainActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;


/**
 * A simple {@link Fragment} subclass.
 */
public class Childs extends Fragment implements Contracts.ChildsView, RealmChangeListener<Realm> {


    @BindView(R.id.recycler_childs)
    RecyclerView recyclerChilds;
    @BindView(R.id.message_body)
    LinearLayout messageBody;
    Contracts.ChildsPresenter presenter;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.image_bgnd)
    ImageView imageBgnd;
    @BindView(R.id.image_bgnd2)
    ImageView imageBgnd2;
    private Realm realm;

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
        realm = Realm.getDefaultInstance();
        setChilds(realm.where(Child.class).findAll());
        presenter.getChilds();
        setupFabBtn();
        realm.addChangeListener(this);
        listenEvents();
        swipe.setOnRefreshListener(() -> {
            presenter.getChilds();
        });

        setupAds();
    }

    private void setupFabBtn() {
        MainActivity activity = (MainActivity) getActivity();
        activity.fabBtn.setOnClickListener(view1 -> {
            startActivity(new Intent(activity, NewChild.class));
        });
    }

    @Override
    public void showEmptyMessage() {
        messageBody.setVisibility(View.VISIBLE);
        imageBgnd.setVisibility(View.VISIBLE);
        imageBgnd2.setVisibility(View.VISIBLE);
        swipe.setRefreshing(false);
    }

    @Override
    public void hideEmptyMessage() {
        messageBody.setVisibility(View.GONE);
        imageBgnd.setVisibility(View.GONE);
        imageBgnd2.setVisibility(View.GONE);
    }

    @Override
    public void setChilds(RealmResults<Child> childs) {
        if (!childs.isEmpty())
            hideEmptyMessage();
        swipe.setRefreshing(false);
        AdapterChilds adapterChilds = new AdapterChilds((MainActivity) getActivity(), childs, this, realm);
        recyclerChilds.setAdapter(adapterChilds);
        recyclerChilds.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public void onDestroy() {
        realm.removeChangeListener(this);
        realm.close();
        //mEvents.unsubscribe();
        super.onDestroy();
    }

    @Override
    public void onChange(Realm element) {
        presenter.getChilds();
    }

    void listenEvents() {
        //mEvents = eventBus.register(Child.class, child -> {
        //presenter.getChilds();
        //});
    }


    private void setupAds() {
        adView.setVisibility(View.GONE);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                try {
                    adView.setVisibility(View.VISIBLE);
                    recyclerChilds.setPadding(8, 120, 8, 8);
                } catch (Exception ignored) {
                }
            }
        });
        adView.loadAd(adRequest);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
