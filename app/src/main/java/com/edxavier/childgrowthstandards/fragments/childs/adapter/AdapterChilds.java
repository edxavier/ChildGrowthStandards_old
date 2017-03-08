package com.edxavier.childgrowthstandards.fragments.childs.adapter;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.edxavier.childgrowthstandards.R;
import com.edxavier.childgrowthstandards.db.Child;
import com.edxavier.childgrowthstandards.db.ChildHistory;
import com.edxavier.childgrowthstandards.fragments.childs.CardOptionsSheet;
import com.edxavier.childgrowthstandards.fragments.childs.Contracts;
import com.edxavier.childgrowthstandards.helpers.MyTextView;
import com.edxavier.childgrowthstandards.helpers.constans.Gender;
import com.edxavier.childgrowthstandards.helpers.constans.Units;
import com.edxavier.childgrowthstandards.main.MainActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.NativeExpressAdView;
import com.jakewharton.rxbinding.view.RxView;
import com.pixplicity.easyprefs.library.Prefs;
import com.rohitarya.glide.facedetection.transformation.FaceCenterCrop;
import com.rohitarya.glide.facedetection.transformation.core.GlideFaceDetector;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Eder Xavier Rojas on 19/09/2016.
 */

public class AdapterChilds extends RecyclerView.Adapter<AdapterChilds.ViewHolder> implements RealmChangeListener<RealmResults<Child>> {

    private MainActivity context;
    private Contracts.ChildsView view;
    RealmResults<Child> list;

    public AdapterChilds(MainActivity context, RealmResults<Child> list, Contracts.ChildsView view) {
        this.context = context;
        this.view = view;
        this.list = list;
        list.addChangeListener(this);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //declarar los Widgets
        @BindView(R.id.measuresContainer)
        LinearLayout measuresContainer;
        @BindView(R.id.measures)
        MyTextView measures;
        @BindView(R.id.divider)
        View divider;
        @BindView(R.id.weight)
        MyTextView weight;
        @BindView(R.id.height)
        MyTextView height;
        @BindView(R.id.perimeter)
        MyTextView perimeter;
        @BindView(R.id.bmi)
        MyTextView bmi;
        @BindView(R.id.admob_container)
        LinearLayout admobContainer;

        @BindView(R.id.gradient)
        View gradient;

        @BindView(R.id.txt_childName)
        MyTextView textView;
        @BindView(R.id.lastMeasure)
        MyTextView txtLastMeasure;
        @BindView(R.id.card_container)
        CardView cardContainer;
        @BindView(R.id.childImage)
        ImageView childImage;

        @BindView(R.id.txtChildAge)
        MyTextView txtChildAge;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        GlideFaceDetector.initialize(context);
        Child child = list.get(position);
        holder.txtChildAge.setText(getAgeString(child.getBirthday()));
        holder.textView.setText(child.getChild_name());

        Realm realm = Realm.getDefaultInstance();
        RealmResults<ChildHistory> results = realm.where(ChildHistory.class)
                .equalTo("child.id", child.getId())
                .findAll().sort("created", Sort.DESCENDING);
        SimpleDateFormat time_format = new SimpleDateFormat("dd-MMM-yyy", Locale.getDefault());
        loadImage(holder.childImage, child.getPhoto_uri());

        try {
            int len_unit = Integer.valueOf(Prefs.getString("height_unit", "2"));
            int wei_unit = Integer.valueOf(Prefs.getString("weight_unit", "0"));

            ChildHistory h = results.first();
            if (results.size() > 0) {
                if(wei_unit == Units.KILOGRAM) {
                    holder.weight.setText(context.getString(R.string.kg,
                            String.format(Locale.getDefault(), "%.2f", h.getWeight_pnds()).replace(",", ".")));
                }else {
                    float w = Units.kg_to_pnds(h.getWeight_pnds());
                    holder.weight.setText(context.getString(R.string.pnd,
                            String.format(Locale.getDefault(), "%.0f", w).replace(",", ".")));
                }
                if(len_unit == Units.CENTIMETER) {
                    holder.height.setText(context.getString(R.string.cm,
                            String.format(Locale.getDefault(), "%.0f", h.getHeight_cms()).replace(",", ".")));
                    holder.perimeter.setText(context.getString(R.string.cm,
                            String.format(Locale.getDefault(),"%.0f", h.getHead_circ()).replace(",", ".")));

                }else {
                    float in_height = Units.cm_to_inches(h.getHeight_cms());
                    float in_hcirc = Units.cm_to_inches(h.getHead_circ());
                    holder.height.setText(context.getString(R.string.in,
                            String.format(Locale.getDefault(), "%.0f", in_height).replace(",", ".")));
                    holder.perimeter.setText(context.getString(R.string.in,
                            String.format(Locale.getDefault(),"%.0f", in_hcirc).replace(",", ".")));
                }
                holder.bmi.setText(String.format(Locale.getDefault(), "%.2f", results.get(0).getBmi()));
                holder.txtLastMeasure.setText(time_format.format(results.get(0).getCreated()));
            }
        } catch (Exception e) {
            //Log.e("EDER", e.getMessage());
        }


        if (child.getGender() == Gender.MALE) {
            holder.gradient.setBackground(context.getResources().getDrawable(R.drawable.blue_gradient));
            holder.measuresContainer.setBackgroundColor(context.getResources().getColor(R.color.md_blue_500_75));
            holder.divider.setBackgroundColor(context.getResources().getColor(R.color.md_blue_500_50));

        } else {
            holder.gradient.setBackground(context.getResources().getDrawable(R.drawable.pink_gradient));
            holder.measuresContainer.setBackgroundColor(context.getResources().getColor(R.color.md_pink_500_75));
            holder.divider.setBackgroundColor(context.getResources().getColor(R.color.md_pink_500_50));
        }
        setupAds(position, holder);

        RxView.clicks(holder.cardContainer).subscribe(aVoid -> {
            Bundle args = new Bundle();
            CardOptionsSheet bsdFragment = CardOptionsSheet.newInstance();
            args.putString("name", child.getChild_name());
            args.putString("id", child.getId());
            args.putString("pic_uri", child.getPhoto_uri());
            bsdFragment.setArguments(args);
            bsdFragment.show(context.getSupportFragmentManager(), "BSDialog");
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    @Override
    public void onChange(RealmResults<Child> element) {
        notifyDataSetChanged();
        if (element.isEmpty())
            view.showEmptyMessage();
        else
            view.hideEmptyMessage();

    }

    private String getAgeString(Date age) {
        String ageString = "";
        LocalDate birthdate = new LocalDate(age.getYear(), age.getMonth() + 1, age.getDate());
        birthdate = new LocalDate(age);

        Period p = new Period(birthdate, new LocalDate(), PeriodType.yearMonthDay());
        if (p.getYears() == 1)
            ageString +=  p.getYears() + " " +context.getString(R.string.years);
        else if (p.getYears() > 1)
            ageString +=  p.getYears() + " " +context.getString(R.string.years);
        if (p.getMonths() == 1)
            ageString +=  " " +p.getMonths() + " " +context.getString(R.string.months);
        else if (p.getMonths() > 1)
            ageString +=  " " +p.getMonths() + " " +context.getString(R.string.months);

        if (p.getDays() == 1)
            ageString +=  " " +p.getDays() + " " +context.getString(R.string.day);
        else if (p.getDays() >= 1)
            ageString +=  " " +p.getDays() + " " +context.getString(R.string.days);
        return ageString;
    }

    private void loadImage(ImageView imgContainer, String uriStr){
        Uri uri = Uri.parse(uriStr);
        Glide.with(context).load(uri)
                .centerCrop()
                .transform(new FaceCenterCrop())
                .placeholder(context.getResources().getDrawable(R.drawable.baby_feets))
                .crossFade().into(imgContainer);
    }

    private void setupAds(int position, ViewHolder holder){
        if( (position + 1) % 2 !=0) {

            AdRequest adRequest = new AdRequest.Builder()
                    //.addTestDevice("45D8AEB3B66116F8F24E001927292BD5")
                    //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .build();

            NativeExpressAdView ads = new NativeExpressAdView(context);
            //AdView adView = new AdView(context);
            //adView.setAdSize(AdSize.BANNER);
            ads.setAdSize(new AdSize(280, 80));
            ads.setAdUnitId(context.getResources().getString(R.string.admob_s001));
            //adView.setAdUnitId(context.getResources().getStr(R.string.admob_banner));
            ads.loadAd(adRequest);
            //adView.loadAd(adRequest);
            ads.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    holder.admobContainer.setVisibility(View.VISIBLE);
                }
            });
            if(holder.admobContainer.getVisibility() == View.GONE)
                holder.admobContainer.addView(ads);
            //holder.admobContainer.addView(adView);
        }
    }
}
