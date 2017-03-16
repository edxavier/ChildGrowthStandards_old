package com.edxavier.childgrowthstandards.helpers;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.edxavier.childgrowthstandards.MeasuresList;
import com.edxavier.childgrowthstandards.R;
import com.edxavier.childgrowthstandards.db.ChildHistory;
import com.edxavier.childgrowthstandards.helpers.constans.Gender;
import com.edxavier.childgrowthstandards.helpers.constans.Units;
import com.jakewharton.rxbinding.view.RxView;
import com.pixplicity.easyprefs.library.Prefs;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 19/09/2016.
 */

public class AdapterReadings extends RecyclerView.Adapter<AdapterReadings.ViewHolder>
        implements RealmChangeListener<RealmResults<ChildHistory>> {

    private MeasuresList context;
    RealmResults<ChildHistory> list;

    public AdapterReadings(MeasuresList context, @NonNull RealmResults<ChildHistory> list) {
        this.context = context;
        this.list = list;
        list.addChangeListener(this);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //declarar los Widgets
        @BindView(R.id.txtFecha)
        TextView txtFecha;
        @BindView(R.id.txtPeso)
        TextView txtPeso;
        @BindView(R.id.txtAltura)
        TextView txtAltura;
        @BindView(R.id.txtPerimetro)
        TextView txtPerimetro;
        @BindView(R.id.txtImc)
        TextView txtImc;
        @BindView(R.id.reading_row_container)
        LinearLayout readingRowContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.reading_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        SimpleDateFormat time_format = new SimpleDateFormat("ddMMMyy", Locale.getDefault());
        ChildHistory entry = list.get(position);
        if( (position + 1) % 2 !=0) {
            holder.readingRowContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.md_blue_grey_50));
        }
        int len_unit = Integer.valueOf(Prefs.getString("height_unit", "2"));
        int wei_unit = Integer.valueOf(Prefs.getString("weight_unit", "0"));
        String wu = "kg";
        String lu = "cm";
        float peso = entry.getWeight_pnds();
        float altura = entry.getHeight_cms();
        float cabeza = entry.getHead_circ();

        if(wei_unit == Units.POUND) {
            wu = "lb";
            peso = Units.kg_to_pnds(peso);
        }
        if(len_unit == Units.INCH) {
            lu = "in";
            altura = Units.cm_to_inches(altura);
            cabeza = Units.cm_to_inches(cabeza);
        }

        holder.txtFecha.setText(time_format.format(entry.getCreated()));
        holder.txtPeso.setText(String.format(Locale.getDefault(), "%.1f "+wu, peso));
        holder.txtAltura.setText(String.format(Locale.getDefault(), "%.1f "+lu, altura));
        holder.txtPerimetro.setText(String.format(Locale.getDefault(), "%.1f "+lu, cabeza));
        holder.txtImc.setText(String.format(Locale.getDefault(), "%.1f", entry.getBmi()));

        RxView.clicks(holder.readingRowContainer).subscribe(aVoid -> {
            new MaterialDialog.Builder(context)
                    .title(time_format.format(entry.getCreated()))
                    .items(R.array.measure_options)
                    .itemsCallback((dialog, itemView, position1, text) -> {
                        switch (position1) {
                            case 0:
                                context.edit(entry);
                                break;
                            case 1:
                                context.delete(entry);
                                break;
                        }
                    })
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    @Override
    public void onChange(@NonNull RealmResults<ChildHistory> element) {
        notifyDataSetChanged();
        context.showEmptyMessage(element.isEmpty());
    }

}
