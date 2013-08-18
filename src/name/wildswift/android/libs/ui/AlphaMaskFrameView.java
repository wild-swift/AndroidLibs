/*
 * Copyright (c) 2013.
 * This file is part of Wild Swift Solutions For Android library.
 *
 * Wild Swift Solutions For Android is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Wild Swift Solutions For Android is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Android Interface Toolkit.  If not, see <http://www.gnu.org/licenses/>.
 */
package name.wildswift.android.libs.ui;


import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * @author Wild Swift 1:14 28.03.11
 */
public class AlphaMaskFrameView extends FrameLayout {
    private Drawable alphaDrawable;
    private Bitmap alphaMask;
    private Bitmap cache;


    public AlphaMaskFrameView(Context context) {
        super(context);
        if (getBackground() == null){
            setBackgroundColor(0x00000000);
        }
    }

    public AlphaMaskFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (getBackground() == null){
            setBackgroundColor(0x00000000);
        }
    }

    public AlphaMaskFrameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (getBackground() == null){
            setBackgroundColor(0x00000000);
        }
    }

    public void setAlphaMask(Drawable drawable) {
        this.alphaDrawable = drawable;
        requestLayout();
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (alphaMask != null){
            alphaMask.recycle();
        }
        if (cache != null){
            cache.recycle();
        }
        if (alphaDrawable != null) {
            Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            alphaDrawable.setBounds(0, 0, getWidth(), getHeight());
            alphaDrawable.draw(new Canvas(bitmap));
            alphaMask = bitmap.extractAlpha();
            bitmap.recycle();
            cache = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(cache);
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
            canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC);
            canvas.drawBitmap(alphaMask, 0, 0, paint);
            alphaMask.recycle();
            alphaMask = cache.extractAlpha();
            cache.recycle();
            cache = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (alphaMask == null) {
            super.draw(canvas);
            return;
        }
        Canvas canvas1 = new Canvas(cache);
        canvas1.drawColor(0x00000000, PorterDuff.Mode.SRC);
        super.draw(canvas1);

        Paint paint = new Paint();

        paint.setColor(Color.WHITE);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        canvas1.drawBitmap(alphaMask, 0, 0, paint);


        canvas.drawBitmap(cache, 0,0, null);
    }
}
