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
package name.wildswift.android.libs.ui.slideview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;
import name.wildswift.android.libs.R;

/**
 * @author Wild Swift
 */
public class SlideView extends ViewGroup implements GestureDetector.OnGestureListener {
    private boolean down = false;

    public static enum Direction{
        top, bottom, left, right
    }

    private State state = State.closed;

    public static final int DURATION = 300;
    private int sideOffset;

    protected View header;
    protected View content;
    protected View keyboard;

    protected Direction direction;
    protected boolean allowManualOpen;

    protected Scroller scroller;
    protected GestureDetector detector;
    private float anchorX;
    private float anchorY;
    private int anchorScrollX;
    private int anchorScrollY;

    private SlideListener listener;

    public SlideView(Context context) {
        super(context);
        scroller = new Scroller(context);
        detector = new GestureDetector(context, this);

    }

    public SlideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        scroller = new Scroller(context);
        detector = new GestureDetector(context, this);

        initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlideView);
        TypedValue outValue = new TypedValue();
        typedArray.getValue(R.styleable.SlideView_sideOffset, outValue);
        if (outValue.type == TypedValue.TYPE_DIMENSION) {
            sideOffset = typedArray.getDimensionPixelSize(R.styleable.SlideView_sideOffset, (int) (100 * getResources().getDisplayMetrics().density));
        } else {
            sideOffset = -1;
        }

        allowManualOpen = typedArray.getBoolean(R.styleable.SlideView_allowManualOpen, false);
        int directionInt = typedArray.getInt(R.styleable.SlideView_direction, 4);
        switch (directionInt) {
            case 1:
                direction = Direction.top;
                break;
            case 2:
                direction = Direction.bottom;
                break;
            case 3:
                direction = Direction.left;
                break;
            case 4:
                direction = Direction.right;
                break;
        }
        typedArray.recycle();
    }

    public SlideView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        scroller = new Scroller(context);
        detector = new GestureDetector(context, this);

        initAttrs(context, attrs);
    }

    public void setSideOffset(int sideOffset) {
        this.sideOffset = sideOffset;
        requestLayout();
    }

    public void setSlideListener(SlideListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = detector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                down = true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                down = false;
                break;
        }
        if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) && !scroller.computeScrollOffset()) {
            endScroll();
        }
        return result;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getId() == R.id.page1) {
                    header = child;
            }
            if (child.getId() == R.id.page2) {
                content = child;
            }
            if (child.getId() == R.id.page3) {
                keyboard = child;
            }
        }

        if (header == null) throw new IllegalArgumentException("No page1 view");
        if (content == null) throw new IllegalArgumentException("No page2 view");
        if (keyboard == null) throw new IllegalArgumentException("No page3 view");
        int sideOffset = 0;
        if (this.sideOffset >= 0) {
            sideOffset = this.sideOffset;
        }
        if (direction == Direction.right || direction == Direction.left) {
            if (this.sideOffset < 0) {
                sideOffset = r-l;
            }
            header.measure(MeasureSpec.makeMeasureSpec(sideOffset, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(b - t, MeasureSpec.EXACTLY));
            content.measure(MeasureSpec.makeMeasureSpec(r - l - sideOffset, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(b - t, MeasureSpec.EXACTLY));
            keyboard.measure(MeasureSpec.makeMeasureSpec(sideOffset, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(b - t, MeasureSpec.EXACTLY));
        } else {
            if (this.sideOffset < 0) {
                sideOffset = b-t;
            }
            header.measure(MeasureSpec.makeMeasureSpec(r - l, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(sideOffset, MeasureSpec.EXACTLY));
            content.measure(MeasureSpec.makeMeasureSpec(r - l, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(b - t - sideOffset, MeasureSpec.EXACTLY));
            keyboard.measure(MeasureSpec.makeMeasureSpec(r - l, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(sideOffset, MeasureSpec.EXACTLY));
        }

        switch (direction) {
            case top:
                header.layout(0, b - t - sideOffset, r - l, b - t);
                content.layout(0, 0, r - l, b - t - sideOffset);
                keyboard.layout(r - l, - sideOffset, r - l, 0);
                break;
            case bottom:
                header.layout(0, 0, r - l, sideOffset);
                content.layout(0, sideOffset, r - l, b - t);
                keyboard.layout(0, b - t, r - l, b - t + sideOffset);
                break;
            case left:
                header.layout(r - l - sideOffset, 0, r - l, b - t);
                content.layout(0, 0, r - l - sideOffset, b - t);
                keyboard.layout(-sideOffset, 0, 0, b - t);
                break;
            case right:
                header.layout(0, 0, sideOffset, b - t);
                content.layout(sideOffset, 0, r - l, b - t);
                keyboard.layout(r - l, 0, r - l + sideOffset, b - t);
                break;
        }

    }

    public void showPage3() {
        if (direction == Direction.top) {
            scroller.startScroll(0, getScrollY(), 0, -computeSideOffset() - getScrollY(), DURATION);
        }
        if (direction == Direction.bottom) {
            scroller.startScroll(0, getScrollY(), 0, computeSideOffset() - getScrollY(), DURATION);
        }
        if (direction == Direction.left) {
            scroller.startScroll(getScrollX(), 0, -computeSideOffset() - getScrollX(), 0, DURATION);
        }
        if (direction == Direction.right) {
            scroller.startScroll(getScrollX(), 0, computeSideOffset() - getScrollX(), 0, DURATION);
        }
        post(new ScrollUpdater());
    }

    private int computeSideOffset() {
        return sideOffset >= 0?sideOffset:(direction == Direction.right || direction == Direction.left?getWidth():getHeight());
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        if (getScrollX() == 0 && getScrollY() == 0 && !allowManualOpen) return false;
        scroller.abortAnimation();
        anchorX = motionEvent.getX();
        anchorY = motionEvent.getY();
        anchorScrollX = getScrollX();
        anchorScrollY = getScrollY();
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {}

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        if (direction == Direction.top &&  anchorY < motionEvent2.getY() && getScrollY() <= -computeSideOffset()) return false;
        if (direction == Direction.top &&  anchorY > motionEvent2.getY() && getScrollY() >= 0) return false;
        if (direction == Direction.bottom &&  anchorY < motionEvent2.getY() && getScrollY() <= 0) return false;
        if (direction == Direction.bottom &&  anchorY > motionEvent2.getY() && getScrollY() >= computeSideOffset()) return false;
        if (direction == Direction.left &&  anchorX < motionEvent2.getX() && getScrollX() <= -computeSideOffset()) return false;
        if (direction == Direction.left &&  anchorX > motionEvent2.getX() && getScrollX() >= 0) return false;
        if (direction == Direction.right &&  anchorX < motionEvent2.getX() && getScrollX() <= 0) return false;
        if (direction == Direction.right &&  anchorX > motionEvent2.getX() && getScrollX() >= computeSideOffset()) return false;
        float scroll;
        switch (direction) {
            case top:
                scroll = anchorScrollY - motionEvent2.getY() + anchorY;
                scrollTo(0, (int) Math.min(0, Math.max(scroll, -computeSideOffset())));
                break;
            case bottom:
                scroll = anchorScrollY - motionEvent2.getY() + anchorY;
                scrollTo(0, (int) Math.min(computeSideOffset(), Math.max(scroll, 0)));
                break;
            case left:
                scroll = anchorScrollX - motionEvent2.getX() + anchorX;
                scrollTo((int) Math.min(0, Math.max(scroll, -computeSideOffset())), 0);
                break;
            case right:
                scroll = anchorScrollX - motionEvent2.getX() + anchorX;
                scrollTo((int) Math.min(computeSideOffset(), Math.max(scroll, 0)), 0);
                break;
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (Math.abs(velocityX) <= Math.abs(velocityY) && (direction == Direction.left || direction == Direction.right)) return false;
        if (Math.abs(velocityX) >= Math.abs(velocityY) && (direction == Direction.bottom || direction == Direction.top)) return false;
        if (direction == Direction.right && getScrollX() >= computeSideOffset() && velocityX < 0 ) return false;
        if (direction == Direction.left && getScrollX() >= 0 && velocityX < 0 ) return false;
        if (direction == Direction.right && getScrollX() <= 0 && velocityX > 0 ) return false;
        if (direction == Direction.left && getScrollX() <= -computeSideOffset() && velocityX > 0 ) return false;
        if (direction == Direction.bottom && getScrollY() >= computeSideOffset() && velocityY < 0 ) return false;
        if (direction == Direction.top && getScrollY() >= 0 && velocityY < 0 ) return false;
        if (direction == Direction.bottom && getScrollY() <= 0 && velocityY > 0 ) return false;
        if (direction == Direction.top && getScrollY() <= -computeSideOffset() && velocityY > 0 ) return false;
        if (direction == Direction.left || direction == Direction.right) {
            velocityY = 0;
        } else {
            velocityX = 0;
        }

        int minX = 0;
        int minY = 0;
        int maxX = 0;
        int maxY = 0;
        switch (direction) {
            case bottom:
                minX = 0;
                maxX = 0;
                minY = 0;
                maxY = computeSideOffset();
                break;
            case top:
                minX = 0;
                maxX = 0;
                minY = -computeSideOffset();
                maxY = 0;
                break;
            case left:
                minX = -computeSideOffset();
                maxX = 0;
                minY = 0;
                maxY = 0;
                break;
            case right:
                minX = 0;
                maxX = computeSideOffset();
                minY = 0;
                maxY = 0;
                break;
        }

        scroller.fling(getScrollX(), getScrollY(), (int) -velocityX, (int) -velocityY, minX, maxX, minY, maxY);
        post(new ScrollUpdater());
        return true;
    }

    private void endScroll() {
        if (down) return;
        int minX = 0;
        int minY = 0;
        int maxX = 0;
        int maxY = 0;
        boolean isVertical = false;
        switch (direction) {
            case bottom:
                minX = 0;
                maxX = 0;
                minY = 0;
                maxY = computeSideOffset();
                isVertical = true;
                break;
            case top:
                minX = 0;
                maxX = 0;
                minY = -computeSideOffset();
                maxY = 0;
                isVertical = true;
                break;
            case left:
                minX = -computeSideOffset();
                maxX = 0;
                minY = 0;
                maxY = 0;
                isVertical = false;
                break;
            case right:
                minX = 0;
                maxX = computeSideOffset();
                minY = 0;
                maxY = 0;
                isVertical = false;
                break;
        }

        if (!isVertical && getScrollX() > minX && getScrollX() < (minX + maxX) / 2) {
            scroller.startScroll(getScrollX(), 0, minX - getScrollX(), 0, DURATION);
            post(new ScrollUpdater());
        } else if (!isVertical && getScrollX() < maxX && getScrollX() >= (minX + maxX) / 2) {
            scroller.startScroll(getScrollX(), 0, maxX - getScrollX(), 0, DURATION);
            post(new ScrollUpdater());
        } else if (isVertical && getScrollY() > minY && getScrollY() < (minY + maxY) / 2) {
            scroller.startScroll(0, getScrollY(), 0, minY - getScrollY(), DURATION);
            post(new ScrollUpdater());
        } else if (isVertical && getScrollY() < maxY && getScrollY() >= (minY + maxY) / 2) {
            scroller.startScroll(0, getScrollY(), 0, maxY - getScrollY(), DURATION);
            post(new ScrollUpdater());
        }
        if (!isVertical && getScrollX() == 0 && listener != null && state == State.opened) {
            listener.onClosed();
            state = State.closed;
        }
        if (isVertical && getScrollY() == 0 && listener != null && state == State.opened) {
            listener.onClosed();
            state = State.closed;
        }
        if (!isVertical && Math.abs(getScrollX()) == computeSideOffset() && listener != null && state == State.closed) {
            listener.onOpen();
            state = State.opened;
        }
        if (isVertical && Math.abs(getScrollY()) == computeSideOffset() && listener != null && state == State.closed) {
            listener.onOpen();
            state = State.opened;
        }
    }

    private class ScrollUpdater implements Runnable {
        @Override
        public void run() {
            if (scroller.computeScrollOffset()) {
                post(this);
            } else {
                endScroll();
            }
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
        }
    }

    private static enum State {
        opened, closed;
    }

    // TODO
    public void showPage1() {
        if (direction == Direction.top || direction == Direction.bottom) {
            scroller.startScroll(0, getScrollY(), 0, - getScrollY(), DURATION);
        } else if (direction == Direction.left || direction == Direction.right) {
            scroller.startScroll(getScrollX(), 0, - getScrollX(), 0, DURATION);
        }
        post(new ScrollUpdater());
    }
}
