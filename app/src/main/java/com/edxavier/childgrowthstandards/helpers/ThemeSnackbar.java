package com.edxavier.childgrowthstandards.helpers;

import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.edxavier.childgrowthstandards.R;


/**
 * @author  : Eder Xavier Rojas
 * @date    : 26/08/2016 - 11:45
 * @package : com.vynil.helper
 * @project : Vynil
 */
public class ThemeSnackbar {

    private static Snackbar colorSnackBar(View container, String message, int background, int textColor , int length) {
        Snackbar snack = Snackbar.make(container, message,length);
        View view = snack.getView();
        view.setBackgroundColor(background);
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(textColor);
        return snack;
    }

    public static Snackbar info(View container, String message, int length ) {
        int background = container.getResources().getColor(R.color.md_cyan_500);
        int textColor = container.getResources().getColor(R.color.md_white_1000);
        Snackbar snack = colorSnackBar(container, message, background,textColor, length);
        return snack;
    }

    public static Snackbar warning(View container, String message, int length) {
        int background = container.getResources().getColor(R.color.md_orange_500);
        int textColor = container.getResources().getColor(R.color.md_white_1000);
        Snackbar snack = colorSnackBar(container, message, background,textColor, length);
        return snack;
    }

    public static Snackbar alert(View container, String message, int length) {
        int background = container.getResources().getColor(R.color.md_pink_500);
        int textColor = container.getResources().getColor(R.color.md_white_1000);
        Snackbar snack = colorSnackBar(container, message, background,textColor, length);
        return snack;
    }

    public static Snackbar confirm(View container, String message, int length) {
        int background = container.getResources().getColor(R.color.md_blue_grey_500);
        int textColor = container.getResources().getColor(R.color.md_white_1000);
        Snackbar snack = colorSnackBar(container, message, background,textColor, length);
        return snack;
    }
}
