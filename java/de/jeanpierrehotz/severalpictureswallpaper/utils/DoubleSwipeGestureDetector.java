/*
 *      Copyright 2016 Jean-Pierre Hotz
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.jeanpierrehotz.severalpictureswallpaper.utils;

import android.view.MotionEvent;

/**
 * This class may be used to detect swipes with two fingers across a view or other component that is able to receive onTouch-events.
 */
public class DoubleSwipeGestureDetector{

    /**
     * This boolean indicates whether exactly two fingers are on the component or not
     */
    private boolean mDoubleDown;
    /**
     * This boolean indicates whether a event has already been fired by this detector with
     * the fingers currently being on te component
     */
    private boolean mEventFired;

    /**
     * The x coordinate of the previous onTouch-event for the first finger down
     */
    private float mPrevX0;
    /**
     * The x coordinate of the previous onTouch-event for the second finger down
     */
    private float mPrevX1;

    /**
     * The listener that receives events when the fingers both swiped to the left
     */
    private OnDoubleLeftSwipeListener mOnDoubleLeftSwipeListener;
    /**
     * The listener that receives events when the fingers both swiped to the right
     */
    private OnDoubleRightSwipeListener mOnDoubleRightSwipeListener;

    /**
     * This method lets you assign a listener to this GestureDetector, that receives events as soon as
     * a swipe to the left with two fingers has been detected.
     * @param listener the listener to assign
     */
    public void addOnDoubleLeftSwipeListener(OnDoubleLeftSwipeListener listener){
        mOnDoubleLeftSwipeListener = listener;
    }

    /**
     * This method removes the listener that receives events as soon as a swipe to the left with two
     * fingers has been detected from this GestureDetector.
     */
    public void removeOnDoubleLeftSwipeListener(){
        mOnDoubleLeftSwipeListener = null;
    }

    /**
     * This method lets you assign a listener to this GestureDetector, that receives events as soon as
     * a swipe to the right with two fingers has been detected.
     * @param listener the listener to assign
     */
    public void addOnDoubleRightSwipeListener(OnDoubleRightSwipeListener listener){
        mOnDoubleRightSwipeListener = listener;
    }

    /**
     * This method removes the listener that receives events as soon as a swipe to the right with two
     * fingers has been detected from this GestureDetector.
     */
    public void removeOnDoubleRightSwipeListener(){
        mOnDoubleRightSwipeListener = null;
    }

    /**
     * This method initializes a DoubleSwipeGestureDetector.<br/>
     * To use it you'll have to call its {@link #onTouchEvent(MotionEvent)}-method as soon as your component receives
     * a onTouch-event. To disable detection you can simply interrupt these events from being passed to the DoubleSwipeGestureDetector.
     */
    public DoubleSwipeGestureDetector(){
        mDoubleDown = false;
        mEventFired = false;

        mPrevX0 = 0;
        mPrevX1 = 0;
    }

    /**
     * This method analyzes al the MotionEvents given to it, and fires events, if appropriate.<br/>
     * You'll have to call this method as soon as your component receives a MotionEvent, for the GestureDetector to work.
     * @param evt the MotionEvent that has been fired
     */
    public void onTouchEvent(MotionEvent evt){
        mDoubleDown = evt.getPointerCount() == 2;

        if(mDoubleDown){
            if(evt.getAction() == MotionEvent.ACTION_DOWN || evt.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN){
                mPrevX0 = evt.getX(0);
                mPrevX1 = evt.getX(1);
            }else if(evt.getX(0) - mPrevX0 < 0 && evt.getX(1) - mPrevX1 < 0){
                if(!mEventFired && mOnDoubleLeftSwipeListener != null){
                    mEventFired = true;
                    mOnDoubleLeftSwipeListener.onDoubleLeftSwiped();
                }
            }else if(evt.getX(0) - mPrevX0 > 0 && evt.getX(1) - mPrevX1 > 0){
                if(!mEventFired && mOnDoubleRightSwipeListener != null){
                    mEventFired = true;
                    mOnDoubleRightSwipeListener.onDoubleRightSwiped();
                }
            }
        }else {
            mEventFired = false;
        }
    }

    /**
     * This interface lets you implement what to do as soon as a DoubleLeftSwipe-event has been fired
     * by a DoubleSwipeGestureDetector.
     */
    public interface OnDoubleLeftSwipeListener{

        /**
         * This method is called as soon as a DoubleLeftSwipe-event has been fired
         */
        void onDoubleLeftSwiped();
    }

    /**
     * This interface lets you implement what to do as soon as a DoubleRightSwipe-event has been fired
     * by a DoubleSwipeGestureDetector.
     */
    public interface OnDoubleRightSwipeListener{

        /**
         * This method is called as soon as a DoubleRightSwipe-event has been fired
         */
        void onDoubleRightSwiped();
    }
}