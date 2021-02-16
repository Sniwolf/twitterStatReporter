package com.twitterStatReporter;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IntervalTerminator implements Runnable{

    IntervalThread intervalThread;

    int intervalTime;

    int intervalDelay;

    Future<?> future;

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
