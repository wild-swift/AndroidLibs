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
package name.wildswift.android.libs.ui.creepingline;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.Scroller;
import name.wildswift.android.libs.R;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Wild Swift
 */
public class CreepingLine extends AdapterView<CreepingLineAdapter> implements GestureDetector.OnGestureListener{
    private CreepingLineAdapter adapter;
    private int count = -1;
    private int zeroChildNum = 0;
    private RecycleBin bin = new RecycleBin();
    private Scroller scroller;
    private int speed = 40;
    private boolean aborted = true;
    private GestureDetector detector;


    public CreepingLine(Context context) {
        super(context);
        scroller = new Scroller(context, new LinearInterpolator());
        detector = new GestureDetector(context, this);
    }

    public CreepingLine(Context context, AttributeSet attrs) {
        super(context, attrs);
        scroller = new Scroller(context, new LinearInterpolator());
        detector = new GestureDetector(context, this);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CreepingLine);
        speed = typedArray.getDimensionPixelSize(R.styleable.CreepingLine_speed, 40);
        typedArray.recycle();
    }

    public CreepingLine(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        scroller = new Scroller(context, new LinearInterpolator());
        detector = new GestureDetector(context, this);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CreepingLine);
        speed = typedArray.getDimensionPixelSize(R.styleable.CreepingLine_speed, 40);
        typedArray.recycle();
    }

    public int getCount() {
        if (count < 0 && adapter == null) {
            return 0;
        } else if (count < 0) {
            count = adapter.getCount();

        }
        return count;
    }

    private static int mod(int in, int mod){
        return ((in % mod) + mod) % mod;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!changed && count >= 0) return;
        if (left == right || top == bottom) return;
        if (getCount() == 0) {
            return;
        }
        if (getChildCount() == 0) {
            int start = getScrollX();
            int end = right - left + getScrollX();
            int child = 0;
            zeroChildNum = Integer.MAX_VALUE;
            int childLeft = 0;
            while (childLeft < end) {
                View view = getView(mod(child, getCount()));
                view.measure(MeasureSpec.makeMeasureSpec(Math.max(right - left, 50), MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(Math.max(bottom - top, 10), MeasureSpec.EXACTLY));
                if (childLeft + view.getMeasuredWidth() > start) {
                    addViewInLayout(view, getChildCount(), new LayoutParams(-1, -1));
                    bin.setViewInUse(adapter.getItemViewType(mod(child, getCount())), view);
                    if (child < zeroChildNum) zeroChildNum = child;
                    view.layout(childLeft, 0, childLeft + view.getMeasuredWidth(), bottom - top);
                }
                childLeft += view.getMeasuredWidth();
                child++;
            }


            child = - 1;
            int childRight = 0;
            while (childRight > start) {
                View view = getView(mod(child, getCount()));
                view.measure(MeasureSpec.makeMeasureSpec(right - left, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(bottom - top, MeasureSpec.EXACTLY));
                if (childRight - view.getMeasuredWidth() < end) {
                    addViewInLayout(view, 0, new LayoutParams(-1, -1));
                    bin.setViewInUse(adapter.getItemViewType(mod(child, getCount())), view);
                    if (child < zeroChildNum) zeroChildNum = child;
                    view.layout(childRight - view.getMeasuredWidth(), 0, childRight, bottom - top);
                }
                childRight -= view.getMeasuredWidth();
                child--;
            }
        } else {
            validateLayout(left, top, right, bottom);
        }

        // TODO Добавлено для случая, когда layout вызывается после onAttachedToWindow
        if (getCount() == 0 || getChildCount() == 0) return;
        if (!aborted) return;
        scroller.startScroll(getScrollX(), 0, 1000 * speed, 0, 1000000);
        aborted = false;
        post(new ComputeScrollOffsetTask());

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getCount() == 0 || getChildCount() == 0) return;
        if (!aborted) return;
        scroller.startScroll(getScrollX(), 0, 1000 * speed, 0, 1000000);
        aborted = false;
        post(new ComputeScrollOffsetTask());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        aborted = true;
        scroller.abortAnimation();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean b = detector.onTouchEvent(event);
        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
/*
            if (!scroller.computeScrollOffset()) {
                scroller.startScroll(getScrollX(), 0, 1000 * speed, 0, 1000000);
                aborted = false;
                post(new ComputeScrollOffsetTask());
            }
*/
        }
        return b;
    }

    private void validateLayout(int left, int top, int right, int bottom) {
        int start = getScrollX();
        int end = right - left + getScrollX();
        int child = zeroChildNum + getChildCount();
        int childLeft = getChildAt(getChildCount() - 1).getRight();

        while (childLeft < end) {
            View view = getView(mod(child, getCount()));
            view.measure(MeasureSpec.makeMeasureSpec(right - left, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(bottom - top, MeasureSpec.EXACTLY));
            if (childLeft + view.getMeasuredWidth() > start) {
                addViewInLayout(view, getChildCount(), new LayoutParams(-1, -1));
                bin.setViewInUse(adapter.getItemViewType(mod(child, getCount())), view);
                view.layout(childLeft, 0, childLeft + view.getMeasuredWidth(), bottom - top);
            }
            childLeft += view.getMeasuredWidth();
            child++;
        }

        child = zeroChildNum - 1;
        int childRight = getChildAt(0).getLeft();
        while (childRight > start) {
            View view = getView(mod(child, getCount()));
            view.measure(MeasureSpec.makeMeasureSpec(right - left, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(bottom - top, MeasureSpec.EXACTLY));
            if (childRight - view.getMeasuredWidth() < end) {
                addViewInLayout(view, 0, new LayoutParams(-1, -1));
                bin.setViewInUse(adapter.getItemViewType(mod(child, getCount())), view);
                if (child < zeroChildNum) zeroChildNum = child;
                view.layout(childRight - view.getMeasuredWidth(), 0, childRight, bottom - top);
            }
            childRight -= view.getMeasuredWidth();
            child--;
        }

        while (getChildAt(0).getRight() <= start) {
            View childAt0 = getChildAt(0);
            removeViewInLayout(childAt0);
            bin.setViewOutOfUse(adapter.getItemViewType(mod(child, getCount())), childAt0);
            zeroChildNum += 1;
        }

        while (getChildAt(getChildCount() - 1).getLeft() >= end) {
            View childAtLast = getChildAt(getChildCount() - 1);
            removeViewInLayout(childAtLast);
            bin.setViewOutOfUse(adapter.getItemViewType(mod(child, getCount())), childAtLast);
        }
    }

    private View getView(int index) {
        int itemViewType = adapter.getItemViewType(index);
        View freeView = bin.getFreeView(itemViewType);
        View view = adapter.getView(index, freeView, this);
        bin.setViewOutOfUse(itemViewType, view);
        return view;
    }

    @Override
    public CreepingLineAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void setAdapter(CreepingLineAdapter adapter) {
        this.adapter = adapter;
        adapter.registerDataSetObserver(new DataSetObserver(){
            @Override
            public void onChanged() {
                count = -1;
                requestLayout();
            }

            @Override
            public void onInvalidated() {
                count = -1;
                requestLayout();
            }
        });
        count = -1;
        requestLayout();
    }

    @Override
    public View getSelectedView() {
        return null;
    }

    @Override
    public void setSelection(int i) {}

    @Override
    public boolean onDown(MotionEvent e) {
        aborted = true;
        scroller.abortAnimation();
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (getChildCount() == 0) return false;
        scrollTo((int) (getScrollX() + distanceX), 0);
        validateLayout(0, 0, getWidth(), getHeight());
        invalidate();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (getChildCount() == 0) return false;
        scroller.abortAnimation();
        scroller.fling(getScrollX(), 0, -(int) velocityX, 0, Integer.MIN_VALUE, 0, Integer.MAX_VALUE, 0);
        aborted = false;
        post(new ComputeScrollOffsetTask());
        return true;
    }

    private static final class RecycleBin {
        private SparseArray<List<View>> usedViews = new SparseArray<List<View>>();
        private SparseArray<List<View>> unusedViews = new SparseArray<List<View>>();

        public void setViewInUse(int type, View view) {
            List<View> views = unusedViews.get(type);
            if (views != null) {
                views.remove(view);
            }

            views = usedViews.get(type);
            if (views == null) {
                views = new LinkedList<View>();
                usedViews.put(type, views);
            }
            views.add(view);
        }

        public void setViewOutOfUse(int type, View view) {
            List<View> views = usedViews.get(type);
            if (views != null) {
                views.remove(view);
            }

            views = unusedViews.get(type);
            if (views == null) {
                views = new LinkedList<View>();
                usedViews.put(type, views);
            }
            views.add(view);
            while (views.size() > 0) {
                views.remove(0);
            }
        }

        public View getFreeView(int type) {
            List<View> views = unusedViews.get(type);
            if (views == null) return null;
            return views.get(0);
        }
    }

    private final class ComputeScrollOffsetTask implements Runnable {

        @Override
        public void run() {
            if (scroller.computeScrollOffset()) {
                post(this);
            } else if (!aborted) {
                scroller.startScroll(getScrollX(), 0, 1000 * speed, 0, 1000000);
                post(this);
            } else {
                return;
            }

            scrollTo(scroller.getCurrX(), 0);

            validateLayout(0, 0, getWidth(), getHeight());
            invalidate();
        }
    }


}
