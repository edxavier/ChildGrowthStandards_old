package com.edxavier.childgrowthstandards;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.edxavier.childgrowthstandards.db.ChildHistory;
import com.edxavier.childgrowthstandards.db.percentiles.WeightForAge;
import com.edxavier.childgrowthstandards.helpers.AdapterReadings;
import com.edxavier.childgrowthstandards.helpers.MyTextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class MeasuresList extends AppCompatActivity {

    @BindView(R.id.toolbar_title)
    MyTextView toolbarTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_readings)
    RecyclerView recyclerReadings;
    @BindView(R.id.message_body)
    LinearLayout messageBody;
    Realm realm;
    SimpleDateFormat time_format = new SimpleDateFormat("ddMMMyy", Locale.getDefault());
    private EditText child_weight;
    private EditText child_height;
    private EditText child_perimeter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measures_list);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        Bundle args = getIntent().getExtras();

        realm = Realm.getDefaultInstance();

        RealmResults<ChildHistory> results = realm.where(ChildHistory.class)
                .equalTo("child.id", args.getString("id"))
                .findAll().sort("created", Sort.DESCENDING);
        showEmptyMessage(results.isEmpty());
        AdapterReadings adapterReadings = new AdapterReadings(this, results);
        recyclerReadings.setAdapter(adapterReadings);
        recyclerReadings.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }

    public void delete(ChildHistory entry){
        new MaterialDialog.Builder(this)
                .title(time_format.format(entry.getCreated()))
                .content(R.string.delete_measure_dialog_content)
                .onPositive((dialog1, which) -> {
                    realm.beginTransaction();
                    entry.deleteFromRealm();
                    realm.commitTransaction();
                })
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .negativeColor(getResources().getColor(R.color.blue_grey_500))
                .show();
    }

    public void edit(ChildHistory entry){
        MaterialDialog dialog = new MaterialDialog.Builder(this)
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

        child_weight = (EditText) dialog.getCustomView().findViewById(R.id.txtPeso);
        child_height = (EditText) dialog.getCustomView().findViewById(R.id.txtAltura);
        child_perimeter = (EditText) dialog.getCustomView().findViewById(R.id.txtPerimetro);

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
