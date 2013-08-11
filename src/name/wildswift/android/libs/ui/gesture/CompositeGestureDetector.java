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
package name.wildswift.android.libs.ui.gesture;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import name.wildswift.android.libs.ui.gesture.helpers.DetectorsElement;
import name.wildswift.android.libs.ui.gesture.helpers.FingerState;

import java.util.List;


/**
 * 11.02.12
 *
 * @author Wild Swift
 */
public class CompositeGestureDetector {

    private GestureStateListener listener;

    private DetectorsElement[] detectors;
    private FingerState[] states;

    private byte maxFingers;

    public <T extends GestureDetector.OnGestureListener & GestureDetector.OnDoubleTapListener> CompositeGestureDetector(Context context, List<T> fingersListener, GestureStateListener listener) {
        this.maxFingers = (byte) fingersListener.size();

        detectors = new DetectorsElement[maxFingers];
        for (byte i = 0; i < maxFingers; i++) {
            detectors[i] = new DetectorsElement(context, fingersListener.get(i));
        }

        states = new FingerState[maxFingers];
        for (byte i = 0; i < maxFingers; i++) {
            states[i] = new FingerState();
        }

        this.listener = listener;
    }

    public boolean onMotionEvent(MotionEvent event) {
        boolean result;
        int actionIndexExternal;
        int actionIndex;
        switch (event.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                // if (event.getActionMasked() == MotionEvent.ACTION_DOWN)
                listener.onGestureStart(event);
            case MotionEvent.ACTION_POINTER_DOWN:
                // find empty slot for new finger, mark it as busy
                // and increment indexes of all fingers that more or equals than new
                // formula gets from source (getActionIndex)
                actionIndexExternal = (event.getAction() & 0xff00) >> 8;
                boolean foundEmptySlot = false;
                actionIndex = -1;
                for (int i = 0; i < maxFingers; i++) {
                    if (states[i].isDown() && states[i].getFingerNum() >= actionIndexExternal) {
                        states[i].setFingerNum(states[i].getFingerNum() + 1);
                    }
                    if (!states[i].isDown() && !foundEmptySlot) {
                        foundEmptySlot = true;
                        states[i].setDown(true);
                        states[i].setDownTime(event.getEventTime());
                        states[i].setFingerNum(actionIndexExternal);
                        actionIndex = i;
                    }
                }
                if (actionIndex == -1) return true;

                // send event to associated GestureDetector group
                result = detectors[actionIndex].onTouchEvent(MotionEvent.obtain(event.getEventTime(), event.getEventTime(), MotionEvent.ACTION_DOWN,
                        event.getX(states[actionIndex].getFingerNum()), event.getY(states[actionIndex].getFingerNum()), event.getMetaState()));


                // notify listener about new finger in gesture
                listener.onFingerStart(actionIndex, event);

                // if more then one finger is down
                for (int i1 = 0; i1 < states.length; i1++) {
                    FingerState state = states[i1];
                    if (state.isDown() && i1 != actionIndex) result = true;
                }
                return result;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                // check index of finger
                // formula gets from source (getActionIndex)
                actionIndexExternal = (event.getAction() & 0xff00) >> 8;
                // up event generated in 0 - event.getPointerCount() interval
                // you may receive all up events with 0 actionIndex
                actionIndex = -1;
                for (int i = 0; i < maxFingers; i++) {
                    if (states[i].isDown() && states[i].getFingerNum() == actionIndexExternal) {
                        actionIndex = i;
                        states[i].setDown(false);
                    }
                    if (states[i].isDown() && states[i].getFingerNum() >= actionIndexExternal) {
                        states[i].setFingerNum(states[i].getFingerNum() - 1);
                    }
                }
                // if action index not changed action not for our detector
                if (actionIndex == -1) return false;


                // send event to associated GestureDetector group
                result = detectors[actionIndex].onTouchEvent(MotionEvent.obtain(states[actionIndex].getDownTime(), event.getEventTime(), MotionEvent.ACTION_UP,
                        event.getX(states[actionIndex].getFingerNum()), event.getY(states[actionIndex].getFingerNum()), event.getMetaState()));

                // notify listener about finger is up
                listener.onFingerEnd(actionIndex, event);
                // if we receive action UP that means that all
                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                    listener.onGestureEnd(event);
                }
                // if more then one finger is down
                for (int i1 = 0; i1 < states.length; i1++) {
                    FingerState state = states[i1];
                    if (state.isDown() && i1 != actionIndex) result = true;
                }
                return result;

            case MotionEvent.ACTION_MOVE:
                result = false;
                for(int i = 0; i < maxFingers; i++) {
                    FingerState state = states[i];
                    if (!state.isDown()) continue;
                    if (detectors[i].onTouchEvent(MotionEvent.obtain(states[i].getDownTime(), event.getEventTime(), MotionEvent.ACTION_MOVE,
                            event.getX(states[i].getFingerNum()), event.getY(states[i].getFingerNum()), event.getMetaState()))) result = true;
                }
                return result;
            case MotionEvent.ACTION_CANCEL:
                for(int i = 0; i < maxFingers; i++) {
                    FingerState state = states[i];
                    if (!state.isDown()) continue;
                    detectors[i].onTouchEvent(MotionEvent.obtain(states[i].getDownTime(), event.getEventTime(), MotionEvent.ACTION_CANCEL,
                            event.getX(states[i].getFingerNum()), event.getY(states[i].getFingerNum()), event.getMetaState()));
                    state.setDown(false);
                }



        }

        return false;
    }


}
