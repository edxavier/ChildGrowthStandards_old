package com.edxavier.childgrowthstandards.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.edxavier.childgrowthstandards.R;
import com.edxavier.childgrowthstandards.db.ChildHistory;
import com.edxavier.childgrowthstandards.helpers.AdapterReadings;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordsFragment extends Fragment {

    @BindView(R.id.recycler_readings)
    RecyclerView recyclerReadings;
    @BindView(R.id.message_body)
    LinearLayout messageBody;
    Realm realm;
    SimpleDateFormat time_format = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
    private EditText child_weight;
    private EditText child_height;
    private EditText child_perimeter;

    public RecordsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.measure_records_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();

        realm = Realm.getDefaultInstance();

        RealmResults<ChildHistory> results = realm.where(ChildHistory.class)
                .equalTo("child.id", args.getString("id"))
                .findAll().sort("created", Sort.DESCENDING);
        showEmptyMessage(results.isEmpty());
        AdapterReadings adapterReadings = new AdapterReadings(this, results);
        recyclerReadings.setAdapter(adapterReadings);
        recyclerReadings.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }


    public void delete(ChildHistory entry){
        new MaterialDialog.Builder(getContext())
                .title(time_format.format(entry.getCreated()))
                .content(R.string.delete_measure_dialog_content)
                .onPositive((dialog1, which) -> {
                    realm.beginTransaction();
                    entry.deleteFromRealm();
                    realm.commitTransaction();
                })
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .negativeColor(getResources().getColor(R.color.md_blue_grey_500))
                .show();
    }

    public void edit(ChildHistory entry){
        MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                .title(time_format.format(entry.getCreated()))
                .customView(R.layout.custom_dialog, true)
                .positiveText(R.string.save)
                .positiveColor(getResources().getColor(R.color.md_green_700))
                .negativeText(android.R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        saveChanges(entry);
                    }
                }).build();

        child_weight =  dialog.getCustomView().findViewById(R.id.txtPeso);
        child_height = dialog.getCustomView().findViewById(R.id.txtAltura);
        child_perimeter = dialog.getCustomView().findViewById(R.id.txtPerimetro);

        child_weight.setText(String.valueOf(entry.getWeight_pnds()));
        child_height.setText(String.valueOf(entry.getHeight_cms()));
        child_perimeter.setText(String.valueOf(entry.getHead_circ()));
        dialog.show();
    }

    private void saveChanges(ChildHistory entry) {
        realm.beginTransaction();
        if(!child_weight.getText().toString().isEmpty())
            entry.setWeight_pnds(Float.valueOf(child_weight.getText().toString()));
        else
            entry.setWeight_pnds(0);

        if(!child_height.getText().toString().isEmpty())
            entry.setHeight_cms(Float.valueOf(child_height.getText().toString()));
        else
            entry.setHeight_cms(0);

        if(!child_perimeter.getText().toString().isEmpty())
            entry.setHead_circ(Float.valueOf(child_perimeter.getText().toString()));
        else
            entry.setHead_circ(0);
        realm.commitTransaction();
    }

    public void showEmptyMessage(boolean show){
        if(show)
            messageBody.setVisibility(View.VISIBLE);
        else
            messageBody.setVisibility(View.GONE);
    }
}
