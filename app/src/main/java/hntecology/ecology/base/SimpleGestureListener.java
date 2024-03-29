package hntecology.ecology.base;

/**
 * Created by theclub on 23/10/2017.
 */

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by CoXier on 17-2-21.
 */

public class SimpleGestureListener extends GestureDetector.SimpleOnGestureListener {
    private static final String TAG = "SimpleGestureListener";
    private Listener mListener;

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // Log.i(TAG,e1.toString()+"\n"+e2.toString());
        Log.d(TAG,"distanceX = "+distanceX+",distanceY = "+distanceY);
        if (mListener == null)
            return true;

        if (distanceX > 50 && Math.abs(distanceY) < 50){
            mListener.onScrollHorizontal(distanceX);
        } else {
            mListener.onScrollVertical(distanceY);
        }
        return true;
    }


    public void setListener(Listener mListener) {
        this.mListener = mListener;
    }

    public interface Listener{
        /**
         * left scroll dx >0
         * right scroll dx <0
         * @param dx
         */
        void onScrollHorizontal(float dx);

        /**
         * upward scroll dy > 0
         * downward scroll dy < 0
         * @param dy
         */
        void onScrollVertical(float dy);
    }
}