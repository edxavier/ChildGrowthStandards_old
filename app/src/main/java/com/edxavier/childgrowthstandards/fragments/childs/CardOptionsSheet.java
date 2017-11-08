package com.edxavier.childgrowthstandards.fragments.childs;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.edxavier.childgrowthstandards.ChildDetailsActivity;
import com.edxavier.childgrowthstandards.MeasuresList;
import com.edxavier.childgrowthstandards.NewChild;
import com.edxavier.childgrowthstandards.NewHistoryRecord;
import com.edxavier.childgrowthstandards.PercentilesActivity;
import com.edxavier.childgrowthstandards.R;
import com.edxavier.childgrowthstandards.db.Child;
import com.edxavier.childgrowthstandards.db.ChildHistory;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

/**
 * Created by Eder Xavier Rojas on 31/08/2016.
 */
public class CardOptionsSheet extends BottomSheetDialogFragment {

    @BindView(R.id.add_measures)
    LinearLayout addMeasures;
    @BindView(R.id.edit_child)
    LinearLayout editChild;
    @BindView(R.id.delete_child)
    LinearLayout deleteChild;
    @BindView(R.id.childName)
    TextView childName;

    Bundle args;
    @BindView(R.id.show_child_details)
    LinearLayout show_child_details;

    public static CardOptionsSheet newInstance() {
        return new CardOptionsSheet();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_main, container, false);
        ButterKnife.bind(this, view);
        addMeasures.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), NewHistoryRecord.class);
            intent.putExtras(args);
            startActivity(intent);
            CardOptionsSheet.this.dismiss();
        });
        editChild.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), NewChild.class);
            intent.putExtras(args);
            startActivity(intent);
            CardOptionsSheet.this.dismiss();
        });
        show_child_details.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), ChildDetailsActivity.class);
            intent.putExtras(args);
            startActivity(intent);
            CardOptionsSheet.this.dismiss();
        });

        deleteChild.setOnClickListener(view1 -> {
            new MaterialDialog.Builder(view.getContext())
                    .title(R.string.delete_dialog_title)
                    .content(R.string.delete_dialog_content)
                    .positiveColor(Color.RED)
                    .positiveText(R.string.aggre)
                    .negativeText(R.string.cancel)
                    .onPositive((dialog, which) -> {
                        CardOptionsSheet.this.dismiss();
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        realm.where(ChildHistory.class)
                                .equalTo("child.id", args.getString("id"))
                                .findAll().deleteAllFromRealm();
                        realm.where(Child.class)
                                .equalTo("id", args.getString("id"))
                                .findAll().deleteAllFromRealm();
                        realm.commitTransaction();
                    })
                    .show();
        });
        args = getArguments();
        childName.setText(args.getString("name"));
        return view;
    }


}
