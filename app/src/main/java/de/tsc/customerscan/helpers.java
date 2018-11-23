package de.tsc.customerscan;

import android.content.Context;
import android.util.DisplayMetrics;

class Helpers {

    public Helpers() {
    }

    static int dpToPx(Context context, int dp) {
        //DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        //return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int)((dp * displayMetrics.density) + 0.5);
    }

    static int pxToDp(Context context, int px) {
        //DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        //return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) ((px/displayMetrics.density)+0.5);
    }}
