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
package name.wildswift.android.libs.ui.gesture.helpers;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Because Android GestureDetector stops handling any events after onLongPress,
 * I create 2 detectors.
 *
 * 12.02.12
 *
 * @author Wild Swift
 */
public class DetectorsElement {
    private GestureDetector detector;
    private GestureDetector longPressDetector;

    public <T extends GestureDetector.OnGestureListener & GestureDetector.OnDoubleTapListener> DetectorsElement(Context context, T listener) {
        this.detector = new GestureDetector(context, new OnGestureListenerWrapper(listener));
        this.detector.setIsLongpressEnabled(false);
        this.longPressDetector = new GestureDetector(context, new LongPressWrapper(listener));
        this.longPressDetector.setIsLongpressEnabled(true);
        this.longPressDetector.setOnDoubleTapListener(listener);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        boolean detectorResult = detector.onTouchEvent(ev);
        boolean longPressDetectorResult = longPressDetector.onTouchEvent(ev);
        return detectorResult || longPressDetectorResult;
    }
}
