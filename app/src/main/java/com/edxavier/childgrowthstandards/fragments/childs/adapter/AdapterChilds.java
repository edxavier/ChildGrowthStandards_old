package com.edxavier.childgrowthstandards.fragments.childs.adapter;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.edxavier.childgrowthstandards.R;
import com.edxavier.childgrowthstandards.db.Child;
import com.edxavier.childgrowthstandards.db.ChildHistory;
import com.edxavier.childgrowthstandards.db.percentiles.BmiForAge;
import com.edxavier.childgrowthstandards.db.percentiles.HeadCircForAge;
import com.edxavier.childgrowthstandards.db.percentiles.HeightForAge;
import com.edxavier.childgrowthstandards.db.percentiles.WeightForAge;
import com.edxavier.childgrowthstandards.fragments.childs.CardOptionsSheet;
import com.edxavier.childgrowthstandards.fragments.childs.Contracts;
import com.edxavier.childgrowthstandards.helpers.Res;
import com.edxavier.childgrowthstandards.helpers.constans.Gender;
import com.edxavier.childgrowthstandards.helpers.constans.Units;
import com.edxavier.childgrowthstandards.main.MainActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.jakewharton.rxbinding2.view.RxView;
import com.pixplicity.easyprefs.library.Prefs;
import java.text.SimpleDateFormat;
import java.time.format.TextStyle;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
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
    private RealmResults<Child> list;
    private Realm realm;
    ChildHistory h;
    public AdapterChilds(MainActivity context, RealmResults<Child> list, Contracts.ChildsView view, Realm realm) {
        this.context = context;
        this.view = view;
        this.list = list;
        list.addChangeListener(this);
        this.realm = realm;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        //declarar los Widgets
        @BindView(R.id.measuresContainer)
        LinearLayout measuresContainer;
        @BindView(R.id.measures)
        TextView measures;
        @BindView(R.id.weight)
        TextView weight;
        @BindView(R.id.height)
        TextView height;
        @BindView(R.id.perimeter)
        TextView perimeter;
        @BindView(R.id.bmi)
        TextView bmi;
        //@BindView(R.id.gradient)
        //View gradient;

        @BindView(R.id.txt_childName)
        TextView textView;
        @BindView(R.id.lastMeasure)
        TextView txtLastMeasure;
        @BindView(R.id.card_container)
        CardView cardContainer;
        @BindView(R.id.childImage)
        CircleImageView childImage;

        @BindView(R.id.txtChildAge)
        TextView txtChildAge;

        @BindView(R.id.fechaUltimoPeso)
        TextView fechaUltimoPeso;
        @BindView(R.id.fechaUltimaAltura)
        TextView fechaUltimaAltura;
        @BindView(R.id.fechaUltimoPC)
        TextView fechaUltimoPC;
        @BindView(R.id.fechaUltimoIMC)
        TextView fechaUltimoIMC;

        @BindView(R.id.tendenciaPeso)
        AppCompatImageView tendenciaPeso;
        @BindView(R.id.tendenciaAltura)
        AppCompatImageView tendenciaAltura;
        @BindView(R.id.tendenciaPC)
        AppCompatImageView tendenciaPC;
        @BindView(R.id.tendenciaIMC)
        AppCompatImageView tendenciaIMC;

        ViewHolder(View itemView) {
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
        Child child = list.get(position);
        holder.txtChildAge.setText(Res.getAgeString(child.getBirthday(), context));
        holder.textView.setText(child.getChild_name());
        loadImage(holder.childImage, child.getPhoto_uri());

        RealmResults<ChildHistory> results = realm.where(ChildHistory.class)
                .equalTo("child.id", child.getId())
                .findAll().sort("created", Sort.DESCENDING);

        SimpleDateFormat time_format = new SimpleDateFormat("dd-MMM-yyy", Locale.getDefault());

        try {
            int len_unit = Integer.valueOf(Prefs.getString("height_unit", "2"));
            int wei_unit = Integer.valueOf(Prefs.getString("weight_unit", "0"));

            if (results.size() > 0) {
                ChildHistory ultimoPeso = results.where().greaterThan("weight_pnds", Float.valueOf("0")).findFirst();
                ChildHistory ultimaAltura = results.where().greaterThan("height_cms", Float.valueOf("0")).findFirst();
                ChildHistory ultimoPC = results.where().greaterThan("head_circ", Float.valueOf("0")).findFirst();
                ChildHistory ultimoIMC = results.where().greaterThan("bmi", Float.valueOf("0")).findFirst();
                if (ultimoPeso != null) {
                    if(wei_unit == Units.KILOGRAM) {
                            holder.weight.setText(context.getString(R.string.kg,
                                    String.format(Locale.getDefault(), "%.1f", ultimoPeso.getWeight_pnds()).replace(",", ".")));
                    }else {
                        float w = Units.kg_to_pnds(ultimoPeso.getWeight_pnds());
                        holder.weight.setText(context.getString(R.string.pnd,
                                String.format(Locale.getDefault(), "%.1f", w).replace(",", ".")));
                    }
                    WeightForAge wfa = realm.where(WeightForAge.class)
                            .equalTo("day", ultimoPeso.getLiving_days()).findFirst();
                    if(wfa!=null){
                        holder.tendenciaPeso.setVisibility(View.VISIBLE);
                        float p85, p97, p15, p3;
                        if(child.getGender()==Gender.MALE){
                            p85 = wfa.getEightyFive_boys();
                            p97 = wfa.getNinetySeven_boys();
                            p15 = wfa.getFifteen_boys();
                            p3 = wfa.getFifteen_boys();
                        }else {
                            p85 = wfa.getEightyFive_girls();
                            p97 = wfa.getNinetySeven_girls();
                            p15 = wfa.getFifteen_girls();
                            p3 = wfa.getFifteen_girls();
                        }
                        if(ultimoPeso.getWeight_pnds() > p85 && ultimoPeso.getWeight_pnds() <= p97) {
                            holder.tendenciaPeso.setColorFilter(context.getResources().getColor(R.color.md_amber_600));
                            holder.tendenciaPeso.setImageResource(R.drawable.ic_arrow_up);
                        }else if(ultimoPeso.getWeight_pnds() < p15 && ultimoPeso.getWeight_pnds() >= p3) {
                            holder.tendenciaPeso.setColorFilter(context.getResources().getColor(R.color.md_amber_600));
                            holder.tendenciaPeso.setImageResource(R.drawable.ic_arrow_down);
                        }else if(ultimoPeso.getWeight_pnds() > p97) {
                            holder.tendenciaPeso.setColorFilter(context.getResources().getColor(R.color.md_red_600));
                            holder.tendenciaPeso.setImageResource(R.drawable.ic_arrow_up);
                        }else if(ultimoPeso.getWeight_pnds() < p3){
                            holder.tendenciaPeso.setColorFilter(context.getResources().getColor(R.color.md_red_600));
                            holder.tendenciaPeso.setImageResource(R.drawable.ic_arrow_down);
                        }else {
                            holder.tendenciaPeso.setImageResource(R.drawable.ic_done_all);
                            holder.tendenciaPeso.setColorFilter(context.getResources().getColor(R.color.md_green_600));
                        }
                    }else
                        holder.tendenciaPeso.setVisibility(View.GONE);

                    holder.fechaUltimoPeso.setText(time_format.format(ultimoPeso.getCreated()));
                }

                if(ultimaAltura != null) {
                    if (len_unit == Units.CENTIMETER) {
                        holder.height.setText(context.getString(R.string.cm,
                                String.format(Locale.getDefault(), "%.1f", ultimaAltura.getHeight_cms()).replace(",", ".")));
                    } else {
                        float in_height = Units.cm_to_inches(ultimaAltura.getHeight_cms());
                        holder.height.setText(context.getString(R.string.in,
                                String.format(Locale.getDefault(), "%.1f", in_height).replace(",", ".")));
                    }
                    HeightForAge hfa = realm.where(HeightForAge.class)
                            .equalTo("day", ultimaAltura.getLiving_days()).findFirst();
                    if(hfa!=null){
                        holder.tendenciaAltura.setVisibility(View.VISIBLE);
                        float p85, p97, p15, p3;
                        if(child.getGender()==Gender.MALE){
                            p85 = hfa.getEightyFive_boys();
                            p97 = hfa.getNinetySeven_boys();
                            p15 = hfa.getFifteen_boys();
                            p3 = hfa.getFifteen_boys();
                        }else {
                            p85 = hfa.getEightyFive_girls();
                            p97 = hfa.getNinetySeven_girls();
                            p15 = hfa.getFifteen_girlsv();
                            p3 = hfa.getFifteen_girlsv();
                        }
                        if(ultimaAltura.getHeight_cms() > p85 && ultimaAltura.getHeight_cms() <= p97) {
                            holder.tendenciaAltura.setColorFilter(context.getResources().getColor(R.color.md_amber_600));
                            holder.tendenciaAltura.setImageResource(R.drawable.ic_arrow_up);
                        }else if(ultimaAltura.getHeight_cms() < p15 && ultimaAltura.getHeight_cms() >= p3) {
                            holder.tendenciaAltura.setColorFilter(context.getResources().getColor(R.color.md_amber_600));
                            holder.tendenciaAltura.setImageResource(R.drawable.ic_arrow_down);
                        }else if(ultimaAltura.getHeight_cms() > p97) {
                            holder.tendenciaAltura.setColorFilter(context.getResources().getColor(R.color.md_red_600));
                            holder.tendenciaAltura.setImageResource(R.drawable.ic_arrow_up);
                        }else if(ultimaAltura.getHeight_cms() < p3){
                            holder.tendenciaAltura.setColorFilter(context.getResources().getColor(R.color.md_red_600));
                            holder.tendenciaAltura.setImageResource(R.drawable.ic_arrow_down);
                        }else {
                            holder.tendenciaAltura.setImageResource(R.drawable.ic_done_all);
                            holder.tendenciaAltura.setColorFilter(context.getResources().getColor(R.color.md_green_600));
                        }
                    }else
                        holder.tendenciaAltura.setVisibility(View.GONE);

                    holder.fechaUltimaAltura.setText(time_format.format(ultimaAltura.getCreated()));
                }

                if(ultimoPC != null) {
                    if (len_unit == Units.CENTIMETER) {
                        holder.perimeter.setText(context.getString(R.string.cm,
                                String.format(Locale.getDefault(), "%.1f", ultimoPC.getHead_circ()).replace(",", ".")));
                    } else {
                        float in_hcirc = Units.cm_to_inches(ultimoPC.getHead_circ());
                        holder.perimeter.setText(context.getString(R.string.in,
                                String.format(Locale.getDefault(), "%.1f", in_hcirc).replace(",", ".")));
                    }
                    HeadCircForAge hcfa = realm.where(HeadCircForAge.class)
                            .equalTo("day", ultimoPC.getLiving_days()).findFirst();
                    if(hcfa!=null){
                        holder.tendenciaPC.setVisibility(View.VISIBLE);
                        float p85, p97, p15, p3;
                        if(child.getGender()==Gender.MALE){
                            p85 = hcfa.getEightyFive_boys();
                            p97 = hcfa.getNinetySeven_boys();
                            p15 = hcfa.getFifteen_boys();
                            p3 = hcfa.getFifteen_boys();
                        }else {
                            p85 = hcfa.getEightyFive_girls();
                            p97 = hcfa.getNinetySeven_girls();
                            p15 = hcfa.getFifteen_girls();
                            p3 = hcfa.getFifteen_girls();
                        }
                        if(ultimoPC.getHead_circ() > p85 && ultimoPC.getHead_circ() <= p97) {
                            holder.tendenciaPC.setColorFilter(context.getResources().getColor(R.color.md_amber_600));
                            holder.tendenciaPC.setImageResource(R.drawable.ic_arrow_up);
                        }else if(ultimoPC.getHead_circ() < p15 && ultimoPC.getHead_circ() >= p3) {
                            holder.tendenciaPC.setColorFilter(context.getResources().getColor(R.color.md_amber_600));
                            holder.tendenciaPC.setImageResource(R.drawable.ic_arrow_down);
                        }else if(ultimoPC.getHead_circ() > p97) {
                            holder.tendenciaPC.setColorFilter(context.getResources().getColor(R.color.md_red_600));
                            holder.tendenciaPC.setImageResource(R.drawable.ic_arrow_up);
                        }else if(ultimoPC.getHead_circ() < p3){
                            holder.tendenciaPC.setColorFilter(context.getResources().getColor(R.color.md_red_600));
                            holder.tendenciaPC.setImageResource(R.drawable.ic_arrow_down);
                        }else {
                            holder.tendenciaPC.setImageResource(R.drawable.ic_done_all);
                            holder.tendenciaPC.setColorFilter(context.getResources().getColor(R.color.md_green_600));
                        }
                    }else
                        holder.tendenciaPC.setVisibility(View.GONE);


                    holder.fechaUltimoPC.setText(time_format.format(ultimoPC.getCreated()));
                }
                if(ultimoIMC != null) {
                    holder.bmi.setText(String.format(Locale.getDefault(), "%.1f", ultimoIMC.getBmi()));
                    holder.fechaUltimoIMC.setText(time_format.format(ultimoIMC.getCreated()));
                    BmiForAge bmifa = realm.where(BmiForAge.class)
                            .equalTo("day", ultimoIMC.getLiving_days()).findFirst();
                    if(bmifa!=null){
                        holder.tendenciaIMC.setVisibility(View.VISIBLE);
                        float p85, p97, p15, p3;
                        if(child.getGender()==Gender.MALE){
                            p85 = bmifa.getEightyFive_boys();
                            p97 = bmifa.getNinetySeven_boys();
                            p15 = bmifa.getFifteen_boys();
                            p3 = bmifa.getFifteen_boys();
                        }else {
                            p85 = bmifa.getEightyFive_girls();
                            p97 = bmifa.getNinetySeven_girls();
                            p15 = bmifa.getFifteen_girls();
                            p3 = bmifa.getFifteen_girls();
                        }
                        if(ultimoIMC.getBmi() > p85 && ultimoIMC.getBmi() <= p97) {
                            holder.tendenciaIMC.setColorFilter(context.getResources().getColor(R.color.md_amber_600));
                            holder.tendenciaIMC.setImageResource(R.drawable.ic_arrow_up);
                        }else if(ultimoIMC.getBmi() < p15 && ultimoIMC.getBmi() >= p3) {
                            holder.tendenciaIMC.setColorFilter(context.getResources().getColor(R.color.md_amber_600));
                            holder.tendenciaIMC.setImageResource(R.drawable.ic_arrow_down);
                        }else if(ultimoIMC.getBmi() > p97) {
                            holder.tendenciaIMC.setColorFilter(context.getResources().getColor(R.color.md_red_600));
                            holder.tendenciaIMC.setImageResource(R.drawable.ic_arrow_up);
                        }else if(ultimoIMC.getBmi() < p3){
                            holder.tendenciaIMC.setColorFilter(context.getResources().getColor(R.color.md_red_600));
                            holder.tendenciaIMC.setImageResource(R.drawable.ic_arrow_down);
                        }else {
                            holder.tendenciaIMC.setImageResource(R.drawable.ic_done_all);
                            holder.tendenciaIMC.setColorFilter(context.getResources().getColor(R.color.md_green_600));
                        }
                    }else
                        holder.tendenciaIMC.setVisibility(View.GONE);

                }
            }
        } catch (Exception e) {
            Log.e("EDER", e.getMessage());
        }


        if (child.getGender() == Gender.MALE) {
            //holder.gradient.setBackground(context.getResources().getDrawable(R.drawable.blue_gradient));
            holder.childImage.setBorderColor(context.getResources().getColor(R.color.md_blue_500_75));
            holder.measuresContainer.setBackgroundColor(context.getResources().getColor(R.color.md_blue_500_75));

        } else {
            //holder.gradient.setBackground(context.getResources().getDrawable(R.drawable.pink_gradient));
            holder.childImage.setBorderColor(context.getResources().getColor(R.color.md_pink_500_75));
            holder.measuresContainer.setBackgroundColor(context.getResources().getColor(R.color.md_pink_500_75));
        }


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



    private void loadImage(CircleImageView imgContainer, String uriStr){
        Uri uri = Uri.parse(uriStr);
        Glide.with(context)
                .asBitmap()
                .load(uri)
                .apply(new RequestOptions()
                    .centerCrop()
                        .placeholder(context.getResources().getDrawable(R.drawable.baby_feets))
                ).into(imgContainer);
    }

}
