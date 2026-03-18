package edu.hitsz.application;

import android.content.Context;
import android.util.DisplayMetrics;

public final class Main {

    public static int WINDOW_WIDTH = 512;
    public static int WINDOW_HEIGHT = 768;

    private Main() {
    }

    public static void initWindowSize(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        setWindowSize(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }

    public static void setWindowSize(int width, int height) {
        if (width > 0) {
            WINDOW_WIDTH = width;
        }
        if (height > 0) {
            WINDOW_HEIGHT = height;
        }
    }
}
