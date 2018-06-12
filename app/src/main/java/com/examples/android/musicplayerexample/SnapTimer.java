package com.examples.android.musicplayerexample;

import android.os.Handler;

/**
 * {@link SnapTimer} - Created by Jeff Palutke on 6/11/2018
 * <p>
 * <pre>
 * Declare in your code:
 *      <b>SnapTimer</b> <i>snap</i> = <b>new SnapTimer(</b><i>msDelay</i><b>,</b> [[<i>true</i>]/[<i>false</i>]]<b>);</b>
 *
 * Public Methods:
 *      <i>snap</i><b>.start();</b>
 *      <i>snap</i><b>.stop();</b>
 *
 *      <i>snap</i><b>.setInterval(</b>int <i>msInterval</i><b>);</b>
 *      <b>int</b> <i>yourInt</i> <b>=</b> <i>snap</i><b>.getInterval();</b>
 *
 *      <b>Boolean</b> <i>yourBoolean</i> <b>=</b> <i>snap</i><b>.isActive();</b>
 *      <b>Boolean</b> <i>yourBoolean</i> <b>=</b> <i>snap</i><b>.isInactive();</b>
 *
 * Example:
 *      // Declare and initialize the timer
 *      snap = new SnapTimer(50, false);
 *
 *      // add a listener to your activity's onCreate method
 *      snap.setCustomObjectListener(new SnapTimer.SnapTimerListener() {
 *          &#54Override
 *          public void onTick() {
 *              //
 *              //   Insert code to be performed each time the SnapTimer ticks
 *              //
 *          }
 *      });
 * </pre>
 */

@SuppressWarnings("unused")
class SnapTimer {

    private final Handler tick = new Handler();
    private boolean mTimerRunning = false;
    private int mTickInterval_ms = 1000;
    private SnapTimerListener listener;
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            tick.postDelayed(this, mTickInterval_ms);
            // fire an event here
            if (listener != null) {
                listener.onTick();
            }
        }
    };

    // SnapTimer creation
    public SnapTimer(int msDelay, boolean startImmediately) {
        initTimer(msDelay, startImmediately);
    }

    // initializes the timer and starts it if specified
    private void initTimer(int msDelay, boolean startImmediately) {
        this.listener = null;
        mTickInterval_ms = msDelay;
        if (startImmediately) start();
    }

    // starts the timer if it is not already running
    public void start() {
        if (!mTimerRunning) {
            mTimerRunning = true;
            tick.postDelayed(timerRunnable, 0);
        }
    }

    // returns a boolean value as to whether or not the timer is running
    public boolean isActive() {
        return mTimerRunning;
    }

    // returns a boolean value as to whether or not the timer is running
    public boolean isInactive() {
        return !mTimerRunning;
    }

    // cancels the timer and sets the timer's status
    public void stop() {
        tick.removeCallbacks(timerRunnable);
        mTimerRunning = false;
    }

    // returns an integer containing the milliseconds that the timer interval is set to
    public int getInterval() {
        return mTickInterval_ms;
    }

    // sets the milliseconds that the timer interval will be set to on
    // it's next cycle or at SnapTimer.start(), whichever occurs first.
    // This will NOT shorten the current tick.
    public void setInterval(int msInterval) {
        mTickInterval_ms = msInterval;
    }

    // activates listener
    public void setCustomObjectListener(SnapTimerListener listener) {
        this.listener = listener;
    }

    // define the listener event: onTick
    public interface SnapTimerListener {
        void onTick();
    }
}
