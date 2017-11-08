package com.edxavier.childgrowthstandards;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.edxavier.childgrowthstandards.helpers.ChildPicture;
import com.edxavier.childgrowthstandards.helpers.RxBus;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mlsdev.rximagepicker.RxImagePicker;
import com.mlsdev.rximagepicker.Sources;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Eder Xavier Rojas on 31/08/2016.
 */
public class PictureSheetDialog extends BottomSheetDialogFragment {


    @BindView(R.id.ic_gallery)
    LinearLayout icGallery;
    @BindView(R.id.ic_camera)
    LinearLayout icCamera;
    FirebaseAnalytics mFirebaseAnalytics;
    Bundle params = new Bundle();


    public static PictureSheetDialog newInstance() {
        return new PictureSheetDialog();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_new_child, container, false);
        ButterKnife.bind(this, view);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        icCamera.setOnClickListener(view2 -> {
            RxImagePicker.with(getContext()).requestImage(Sources.CAMERA).subscribe(uri -> {
                RxBus.getInstance().post(new ChildPicture(uri));
                params.putString("source", "Camera");
                mFirebaseAnalytics.logEvent("picture_choose", params);
                PictureSheetDialog.this.dismiss();
            }, Throwable::printStackTrace);
        });

        icGallery.setOnClickListener(view1 -> {
            RxImagePicker.with(getContext()).requestImage(Sources.GALLERY).subscribe(uri -> {
                RxBus.getInstance().post(new ChildPicture(uri));
                params.putString("source", "Gallery");
                mFirebaseAnalytics.logEvent("picture_choose", params);
                PictureSheetDialog.this.dismiss();
            }, Throwable::printStackTrace);
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
                FrameLayout bottomSheet = (FrameLayout)
                        dialog.findViewById(android.support.design.R.id.design_bottom_sheet);
                BottomSheetBehavior behavior = null;
                if (bottomSheet != null) {
                    behavior = BottomSheetBehavior.from(bottomSheet);
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    behavior.setPeekHeight(0);
                }
            }
        });
    }
}
