package com.twitterStatReporter;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * IntervalTerminator is used to start an intervalThread after intervalDelay seconds and then stop it
 * after intervalTime seconds has elapsed.
 */
public class IntervalTerminator implements Runnable{

    // An interval thread object used to gather tweets from the gatheringThread object.
    private final IntervalThread intervalThread;

    // The total time the intervalThread should live, in seconds.
    private final int intervalTime;

    // The delay time till the interval thread needs to start.
    int intervalDelay;

    // Future object used to schedule when the intervalThread needs to be interrupted.
    Future<?> future;

    /**
     *
     * @param intervalThread - IntervalThread object that is scheduled to start after intevalDelay seconds and stop
     *                       after intervalTime seconds has elapsed.
     * @param intervalTime - IntervalTime indicates how long an interval thread should run, in seconds.
     * @param intervalDelay - The delay in seconds till the interval thread needs to start.
     */
    public IntervalTerminator(IntervalThread intervalThread, int intervalTime, int intervalDelay){
        this.intervalThread = intervalThread;
        this.intervalTime = intervalTime;
        this.intervalDelay = intervalDelay;
    }
    @Override
    public void run() {
        //Create a new executor service to stop the stream after a period of time.
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

        this.future = executor.submit(intervalThread);
        executor.schedule(() -> {
            future.cancel(true);
        }, this.intervalTime, TimeUnit.SECONDS);
        executor.shutdown();
    }
}
