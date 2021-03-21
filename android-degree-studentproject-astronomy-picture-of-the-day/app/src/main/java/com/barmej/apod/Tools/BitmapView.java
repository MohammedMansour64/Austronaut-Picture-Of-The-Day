package com.barmej.apod.Tools;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

public class BitmapView {

    public Bitmap getBitmapFromView(View view)
    {
        // creating the bitmap
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth() , view.getHeight() , Bitmap.Config.ARGB_8888 );
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();

        if (bgDrawable!=null)
        {
            bgDrawable.draw(canvas);
        }
        view.draw(canvas);
        return returnedBitmap;
    }
}
