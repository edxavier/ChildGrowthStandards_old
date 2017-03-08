package com.edxavier.childgrowthstandards;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.edxavier.childgrowthstandards.db.Child;
import com.edxavier.childgrowthstandards.helpers.ChildPicture;
import com.edxavier.childgrowthstandards.helpers.RxBus;
import com.mlsdev.rximagepicker.RxImagePicker;
import com.mlsdev.rximagepicker.Sources;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.functions.Action1;

/**
 * Created by Eder Xavier Rojas on 31/08/2016.
 */
public class PictureSheetDialog extends BottomSheetDialogFragment {


    @BindView(R.id.ic_gallery)
    MaterialRippleLayout icGallery;
    @BindView(R.id.ic_camera)
    MaterialRippleLayout icCamera;

    public static PictureSheetDialog newInstance() {
        return new PictureSheetDialog();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_new_child, container, false);
        ButterKnife.bind(this, view);
        icCamera.setOnClickListener(view2 -> {
            RxImagePicker.with(getContext()).requestImage(Sources.CAMERA).subscribe(uri -> {
                RxBus.getInstance().post(new ChildPicture(uri));
                PictureSheetDialog.this.dismiss();
            });
        });

        icGallery.setOnClickListener(view1 -> {
            RxImagePicker.with(getContext()).requestImage(Sources.GALLERY).subscribe(uri -> {
                RxBus.getInstance().post(new ChildPicture(uri));
                PictureSheetDialog.this.dismiss();
            });
        });
        return view;
    }
}
