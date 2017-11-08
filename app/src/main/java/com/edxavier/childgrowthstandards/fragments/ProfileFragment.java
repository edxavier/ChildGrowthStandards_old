package com.edxavier.childgrowthstandards.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.edxavier.childgrowthstandards.helpers.constans.Gender;
import com.edxavier.childgrowthstandards.helpers.constans.Units;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.pixplicity.easyprefs.library.Prefs;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    Realm realm;
    SimpleDateFormat time_format = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
    @BindView(R.id.childImage)
    CircleImageView childImage;
    @BindView(R.id.txtEdad)
    TextView txtEdad;
    @BindView(R.id.txtPeso)
    TextView txtPeso;
    @BindView(R.id.txtAvisoPeso)
    TextView txtAvisoPeso;
    @BindView(R.id.txtAltura)
    TextView txtAltura;
    @BindView(R.id.txtAvisoAltura)
    TextView txtAvisoAltura;
    @BindView(R.id.txtPC)
    TextView txtPC;
    @BindView(R.id.txtAvisoPC)
    TextView txtAvisoPC;
    @BindView(R.id.txtIMC)
    TextView txtIMC;
    @BindView(R.id.txtAvisoIMC)
    TextView txtAvisoIMC;
    @BindView(R.id.adView)
    AdView adView;
    private Child children;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.profile_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        int len_unit = Integer.valueOf(Prefs.getString("height_unit", "2"));
        int wei_unit = Integer.valueOf(Prefs.getString("weight_unit", "0"));
        SimpleDateFormat time_format2 = new SimpleDateFormat("  (dd-MMM-yyy)", Locale.getDefault());

        realm = Realm.getDefaultInstance();
        children = realm.where(Child.class).equalTo("id", args.getString("id")).findFirst();

        RealmResults<ChildHistory> results = realm.where(ChildHistory.class)
                .equalTo("child.id", args.getString("id"))
                .findAll().sort("created", Sort.DESCENDING);

        if (children != null) {
            txtEdad.setText(time_format.format(children.getBirthday()));
            Glide.with(getContext())
                    .asBitmap()
                    .load(children.getPhoto_uri())
                    .apply(new RequestOptions()
                            .centerCrop()
                            .placeholder(getContext().getResources().getDrawable(R.drawable.baby_feets))
                    ).into(childImage);
            if (children.getGender() == Gender.MALE)
                childImage.setBorderColor(getContext().getResources().getColor(R.color.md_blue_500_75));
            else
                childImage.setBorderColor(getContext().getResources().getColor(R.color.md_pink_500_75));

            if (results.size() > 0) {
                Context context = getContext();
                ChildHistory ultimoPeso = results.where().greaterThan("weight_pnds", Float.valueOf("0")).findFirst();
                ChildHistory ultimaAltura = results.where().greaterThan("height_cms", Float.valueOf("0")).findFirst();
                ChildHistory ultimoPC = results.where().greaterThan("head_circ", Float.valueOf("0")).findFirst();
                ChildHistory ultimoIMC = results.where().greaterThan("bmi", Float.valueOf("0")).findFirst();
                if (ultimoPeso != null) {
                    String pesoNormal;
                    if (wei_unit == Units.KILOGRAM) {
                        txtPeso.setText(context.getString(R.string.kg,
                                String.format(Locale.getDefault(), "%.1f", ultimoPeso.getWeight_pnds()).replace(",", ".")));
                    } else {
                        float w = Units.kg_to_pnds(ultimoPeso.getWeight_pnds());
                        txtPeso.setText(context.getString(R.string.pnd,
                                String.format(Locale.getDefault(), "%.1f", w).replace(",", ".")));
                    }
                    txtPeso.setText(txtPeso.getText().toString() + time_format2.format(ultimoPeso.getCreated()));
                    WeightForAge wfa = realm.where(WeightForAge.class)
                            .equalTo("day", ultimoPeso.getLiving_days()).findFirst();
                    if (wfa != null) {
                        float p85, p97, p15, p3;
                        if (children.getGender() == Gender.MALE) {
                            p85 = wfa.getEightyFive_boys();
                            p97 = wfa.getNinetySeven_boys();
                            p15 = wfa.getFifteen_boys();
                            p3 = wfa.getFifteen_boys();
                        } else {
                            p85 = wfa.getEightyFive_girls();
                            p97 = wfa.getNinetySeven_girls();
                            p15 = wfa.getFifteen_girls();
                            p3 = wfa.getFifteen_girls();
                        }
                        if (wei_unit == Units.KILOGRAM) {
                            pesoNormal = context.getString(R.string.kg, String.format(Locale.getDefault(), "%.1f", p15).replace(",", ".") + " - " + String.format(Locale.getDefault(), "%.1f", p85).replace(",", "."));
                        } else {
                            pesoNormal = (context.getString(R.string.pnd,
                                    String.format(Locale.getDefault(), "%.1f", Units.kg_to_pnds(p15)).replace(",", ".")));
                            pesoNormal += " - " + (context.getString(R.string.pnd,
                                    String.format(Locale.getDefault(), "%.1f", Units.kg_to_pnds(p85)).replace(",", ".")));
                        }
                        if (ultimoPeso.getWeight_pnds() > p85 && ultimoPeso.getWeight_pnds() <= p97) {
                            txtAvisoPeso.setTextColor(context.getResources().getColor(R.color.md_amber_600));
                            txtAvisoPeso.setText(getString(R.string.above_normal) + pesoNormal);
                        } else if (ultimoPeso.getWeight_pnds() < p15 && ultimoPeso.getWeight_pnds() >= p3) {
                            txtAvisoPeso.setTextColor(context.getResources().getColor(R.color.md_amber_600));
                            txtAvisoPeso.setText(getString(R.string.well_above_normal) + pesoNormal);
                        } else if (ultimoPeso.getWeight_pnds() > p97) {
                            txtAvisoPeso.setTextColor(context.getResources().getColor(R.color.md_red_600));
                            txtAvisoPeso.setText(getString(R.string.below_normal) + pesoNormal);
                        } else if (ultimoPeso.getWeight_pnds() < p3) {
                            txtAvisoPeso.setTextColor(context.getResources().getColor(R.color.md_red_600));
                            txtAvisoPeso.setText(getString(R.string.well_below_normal) + pesoNormal);
                        } else {
                            txtAvisoPeso.setTextColor(context.getResources().getColor(R.color.md_green_600));
                            txtAvisoPeso.setText(getString(R.string.normal) + pesoNormal);
                        }
                    }
                    //holder.fechaUltimoPeso.setText(time_format.format(ultimoPeso.getCreated()));
                }
                if (ultimaAltura != null) {
                    if (len_unit == Units.CENTIMETER) {
                        txtAltura.setText(context.getString(R.string.cm,
                                String.format(Locale.getDefault(), "%.1f", ultimaAltura.getHeight_cms()).replace(",", ".")));
                    } else {
                        float in_height = Units.cm_to_inches(ultimaAltura.getHeight_cms());
                        txtAltura.setText(context.getString(R.string.in,
                                String.format(Locale.getDefault(), "%.1f", in_height).replace(",", ".")));
                    }
                    txtAltura.setText(txtAltura.getText().toString() + time_format2.format(ultimaAltura.getCreated()));
                    HeightForAge hfa = realm.where(HeightForAge.class)
                            .equalTo("day", ultimaAltura.getLiving_days()).findFirst();
                    if (hfa != null) {
                        float p85, p97, p15, p3;
                        if (children.getGender() == Gender.MALE) {
                            p85 = hfa.getEightyFive_boys();
                            p97 = hfa.getNinetySeven_boys();
                            p15 = hfa.getFifteen_boys();
                            p3 = hfa.getFifteen_boys();
                        } else {
                            p85 = hfa.getEightyFive_girls();
                            p97 = hfa.getNinetySeven_girls();
                            p15 = hfa.getFifteen_girlsv();
                            p3 = hfa.getFifteen_girlsv();
                        }
                        String alturaNormal;
                        if (len_unit == Units.CENTIMETER) {
                            alturaNormal = context.getString(R.string.cm, String.format(Locale.getDefault(), "%.1f", p15).replace(",", ".") + " - " + String.format(Locale.getDefault(), "%.1f", p85).replace(",", "."));
                        } else {
                            alturaNormal = (context.getString(R.string.in,
                                    String.format(Locale.getDefault(), "%.1f", Units.cm_to_inches(p15)).replace(",", ".")));
                            alturaNormal += " - " + (context.getString(R.string.in,
                                    String.format(Locale.getDefault(), "%.1f", Units.cm_to_inches(p85)).replace(",", ".")));
                        }
                        if (ultimaAltura.getHeight_cms() > p85 && ultimaAltura.getHeight_cms() <= p97) {
                            txtAvisoAltura.setTextColor(context.getResources().getColor(R.color.md_amber_600));
                            txtAvisoAltura.setText(getString(R.string.above_normal) + alturaNormal);
                        } else if (ultimaAltura.getHeight_cms() < p15 && ultimaAltura.getHeight_cms() >= p3) {
                            txtAvisoAltura.setTextColor(context.getResources().getColor(R.color.md_amber_600));
                            txtAvisoAltura.setText(getString(R.string.below_normal) + alturaNormal);
                        } else if (ultimaAltura.getHeight_cms() > p97) {
                            txtAvisoAltura.setTextColor(context.getResources().getColor(R.color.md_red_600));
                            txtAvisoAltura.setText(getString(R.string.well_above_normal) + alturaNormal);
                        } else if (ultimaAltura.getHeight_cms() < p3) {
                            txtAvisoAltura.setTextColor(context.getResources().getColor(R.color.md_red_600));
                            txtAvisoAltura.setText(getString(R.string.well_below_normal) + alturaNormal);
                        } else {
                            txtAvisoAltura.setText(getString(R.string.normal) + alturaNormal);
                            txtAvisoAltura.setTextColor(context.getResources().getColor(R.color.md_green_600));
                        }
                    }
                }

                if (ultimoPC != null) {
                    if (len_unit == Units.CENTIMETER) {
                        txtPC.setText(context.getString(R.string.cm,
                                String.format(Locale.getDefault(), "%.1f", ultimoPC.getHead_circ()).replace(",", ".")));
                    } else {
                        float in_hcirc = Units.cm_to_inches(ultimoPC.getHead_circ());
                        txtPC.setText(context.getString(R.string.in,
                                String.format(Locale.getDefault(), "%.1f", in_hcirc).replace(",", ".")));
                    }
                    txtPC.setText(txtPC.getText().toString() + time_format2.format(ultimoPC.getCreated()));
                    HeadCircForAge hcfa = realm.where(HeadCircForAge.class)
                            .equalTo("day", ultimoPC.getLiving_days()).findFirst();
                    if (hcfa != null) {
                        float p85, p97, p15, p3;
                        if (children.getGender() == Gender.MALE) {
                            p85 = hcfa.getEightyFive_boys();
                            p97 = hcfa.getNinetySeven_boys();
                            p15 = hcfa.getFifteen_boys();
                            p3 = hcfa.getFifteen_boys();
                        } else {
                            p85 = hcfa.getEightyFive_girls();
                            p97 = hcfa.getNinetySeven_girls();
                            p15 = hcfa.getFifteen_girls();
                            p3 = hcfa.getFifteen_girls();
                        }
                        String pcNormal;
                        if (len_unit == Units.CENTIMETER) {
                            pcNormal = context.getString(R.string.cm, String.format(Locale.getDefault(), "%.1f", p15).replace(",", ".") + " - " + String.format(Locale.getDefault(), "%.1f", p85).replace(",", "."));
                        } else {
                            pcNormal = (context.getString(R.string.in,
                                    String.format(Locale.getDefault(), "%.1f", Units.cm_to_inches(p15)).replace(",", ".")));
                            pcNormal += " - " + (context.getString(R.string.in,
                                    String.format(Locale.getDefault(), "%.1f", Units.cm_to_inches(p85)).replace(",", ".")));
                        }
                        if (ultimoPC.getHead_circ() > p85 && ultimoPC.getHead_circ() <= p97) {
                            txtAvisoPC.setTextColor(context.getResources().getColor(R.color.md_amber_600));
                            txtAvisoPC.setText(getString(R.string.above_normal) + pcNormal);
                        } else if (ultimoPC.getHead_circ() < p15 && ultimoPC.getHead_circ() >= p3) {
                            txtAvisoPC.setTextColor(context.getResources().getColor(R.color.md_amber_600));
                            txtAvisoPC.setText(getString(R.string.below_normal) + pcNormal);
                        } else if (ultimoPC.getHead_circ() > p97) {
                            txtAvisoPC.setTextColor(context.getResources().getColor(R.color.md_red_600));
                            txtAvisoPC.setText(getString(R.string.well_above_normal) + pcNormal);
                        } else if (ultimoPC.getHead_circ() < p3) {
                            txtAvisoPC.setTextColor(context.getResources().getColor(R.color.md_red_600));
                            txtAvisoPC.setText(getString(R.string.well_below_normal) + pcNormal);
                        } else {
                            txtAvisoPC.setText(getString(R.string.normal) + pcNormal);
                            txtAvisoPC.setTextColor(context.getResources().getColor(R.color.md_green_600));
                        }
                    }
                }
                if (ultimoIMC != null) {
                    txtIMC.setText(String.format(Locale.getDefault(), "%.1f", ultimoIMC.getBmi()));
                    //holder.fechaUltimoIMC.setText(time_format.format(ultimoIMC.getCreated()));
                    BmiForAge bmifa = realm.where(BmiForAge.class)
                            .equalTo("day", ultimoIMC.getLiving_days()).findFirst();
                    txtIMC.setText(txtIMC.getText().toString() + time_format2.format(ultimoIMC.getCreated()));

                    if (bmifa != null) {
                        float p85, p97, p15, p3;
                        if (children.getGender() == Gender.MALE) {
                            p85 = bmifa.getEightyFive_boys();
                            p97 = bmifa.getNinetySeven_boys();
                            p15 = bmifa.getFifteen_boys();
                            p3 = bmifa.getFifteen_boys();
                        } else {
                            p85 = bmifa.getEightyFive_girls();
                            p97 = bmifa.getNinetySeven_girls();
                            p15 = bmifa.getFifteen_girls();
                            p3 = bmifa.getFifteen_girls();
                        }
                        String imcNormal = context.getString(R.string.cm, String.format(Locale.getDefault(), "%.1f", p15).replace(",", ".") + " - " + String.format(Locale.getDefault(), "%.1f", p85).replace(",", "."));

                        if (ultimoIMC.getBmi() > p85 && ultimoIMC.getBmi() <= p97) {
                            txtAvisoIMC.setTextColor(context.getResources().getColor(R.color.md_amber_600));
                            txtAvisoIMC.setText(getString(R.string.above_normal) + imcNormal);
                        } else if (ultimoIMC.getBmi() < p15 && ultimoIMC.getBmi() >= p3) {
                            txtAvisoIMC.setTextColor(context.getResources().getColor(R.color.md_amber_600));
                            txtAvisoIMC.setText(getString(R.string.below_normal) + imcNormal);
                        } else if (ultimoIMC.getBmi() > p97) {
                            txtAvisoIMC.setTextColor(context.getResources().getColor(R.color.md_red_600));
                            txtAvisoIMC.setText(getString(R.string.well_above_normal) + imcNormal);
                        } else if (ultimoIMC.getBmi() < p3) {
                            txtAvisoIMC.setTextColor(context.getResources().getColor(R.color.md_red_600));
                            txtAvisoIMC.setText(getString(R.string.well_below_normal) + imcNormal);
                        } else {
                            txtAvisoIMC.setText(getString(R.string.normal) + imcNormal);
                            txtAvisoIMC.setTextColor(context.getResources().getColor(R.color.md_green_600));
                        }
                    }

                }

            }

        }
        setupAds();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    private void setupAds() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                try {
                    adView.setVisibility(View.VISIBLE);
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
