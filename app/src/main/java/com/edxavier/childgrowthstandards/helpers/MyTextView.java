package com.edxavier.childgrowthstandards.helpers;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import com.edxavier.childgrowthstandards.R;

/**
 * Created by Eder Xavier Rojas on 06/10/2015.
 */
public class MyTextView extends TextView {
    Typeface roboto;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MyTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    public MyTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);

    }

    public MyTextView(Context context) {
        super(context);
        init(null);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MyTextView);
            String fontName = a.getString(R.styleable.MyTextView_font);

            try {
                if (fontName != null) {
                    Typeface myTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/" + fontName);
                    setTypeface(myTypeface);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            a.recycle();
        }
    }

    /*

        public MyTextView(Context context) {
            super(context);
            setRobotoRegular();
        }
        public MyTextView(Context context, AttributeSet attrs) {
            super(context, attrs);
            setRobotoRegular();
        }

        public MyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            setRobotoRegular();
        }
    */
    public void setPierBold(){
        roboto = Typeface.createFromAsset(getContext().getAssets(), "fonts/pierBold.otf");
        this.setTypeface(roboto);
    }
    public void setPierRegular(){
        roboto = Typeface.createFromAsset(getContext().getAssets(), "fonts/pierRegular.otf");
        this.setTypeface(roboto);
    }

    public void setSlimJoe(){
        roboto = Typeface.createFromAsset(getContext().getAssets(), "fonts/Slimjoe.otf");
        this.setTypeface(roboto);
    }
    public void setBigJoe(){
        roboto = Typeface.createFromAsset(getContext().getAssets(), "fonts/BigJohn.otf");
        this.setTypeface(roboto);
    }
    public void setMadras(){
        roboto = Typeface.createFromAsset(getContext().getAssets(), "fonts/MadrasExtraLight.otf");
        this.setTypeface(roboto);
    }

    public static Typeface getRobotoRegular(Context context){
        return Typeface.createFromAsset(context.getAssets(), "fonts/RobotoRegular.ttf");
    }
    public void setRobotoCondensedRegular(){
        roboto = Typeface.createFromAsset(getContext().getAssets(), "fonts/RobotoCondensedRegular.ttf");
        this.setTypeface(roboto);
    }
    public static Typeface getRobotoLight(Context context){
       return Typeface.createFromAsset(context.getAssets(), "fonts/RobotoLight.ttf");
    }

    public void setRobotoItalic(){
        roboto = Typeface.createFromAsset(getContext().getAssets(), "fonts/RobotoItalic.ttf");
        this.setTypeface(roboto);
    }

    public void setRobotoBold(){
        roboto = Typeface.createFromAsset(getContext().getAssets(), "fonts/RobotoBold.ttf");
        this.setTypeface(roboto);
    }

    public void setRobotoMedium(){
        roboto = Typeface.createFromAsset(getContext().getAssets(), "fonts/RobotoMedium.ttf");
        this.setTypeface(roboto);
    }
    public void setRobotoThin(){
        roboto = Typeface.createFromAsset(getContext().getAssets(), "fonts/RobotoThin.ttf");
        this.setTypeface(roboto);
    }

}
