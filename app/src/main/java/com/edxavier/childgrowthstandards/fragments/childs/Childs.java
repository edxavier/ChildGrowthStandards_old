package com.edxavier.childgrowthstandards.fragments.childs;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.graphics.Target;
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
import com.edxavier.childgrowthstandards.fragments.childs.adapter.AdapterChilds;
import com.edxavier.childgrowthstandards.fragments.childs.impl.ChildsPresenterImpl;
import com.edxavier.childgrowthstandards.helpers.RxBus;
import com.edxavier.childgrowthstandards.main.MainActivity;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.pixplicity.easyprefs.library.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import rx.Subscription;


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
    private Realm realm;
    private RxBus eventBus;
    private Subscription mEvents;

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
        eventBus = RxBus.getInstance();
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

        startSecuence();
    }

    private void startSecuence() {
        if(!Prefs.getBoolean("secuence_childs", false)) {
            MainActivity activity = (MainActivity) getActivity();
            final int[] sec = {0};
            new TapTargetSequence(getActivity())
                    .targets(
                            TapTarget.forView(getActivity().findViewById(R.id.fabBtn),
                                    getActivity().getResources().getString(R.string.sec_childs_title))
                                    .dimColor(R.color.md_black_1000)
                                    .outerCircleColor(R.color.md_green_500_25)
                                    .cancelable(false),
                            TapTarget.forToolbarNavigationIcon(activity.toolbar,
                                    getActivity().getResources().getString(R.string.sec_childs_title2))
                    ).listener(new TapTargetSequence.Listener() {
                @Override
                public void onSequenceFinish() {}

                @Override
                public void onSequenceStep(TapTarget lastTarget) {
                    sec[0]++;
                    if(sec[0]==2)
                        activity.openDrawer();
                }
                @Override
                public void onSequenceCanceled(TapTarget lastTarget) {}
            }).start();
            Prefs.putBoolean("secuence_childs", true);
        }
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
        swipe.setRefreshing(false);
    }

    @Override
    public void hideEmptyMessage() {
        messageBody.setVisibility(View.GONE);
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
        mEvents.unsubscribe();
        super.onDestroy();
    }

    @Override
    public void onChange(Realm element) {
        presenter.getChilds();
    }

    void listenEvents() {
        mEvents = eventBus.register(Child.class, child -> {
            presenter.getChilds();
        });
    }
}
