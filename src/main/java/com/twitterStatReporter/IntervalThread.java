package com.twitterStatReporter;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * An intervalThread object is used to consume tweets from the mainQueue and store them in its own intervalQueue that
 * a reportGenerator will use to build a report from. An intervalThread will do this until it is interrupted by its
 * corresponding intervalTerminator. The intervalThread will poll the mainQueue for up to intervalTime waiting for
 * tweets to come in.
 */
public class IntervalThread implements Runnable{

    // The main blocking queue.
    BlockingQueue<String> mainQueue;

    // The report interval queue.
    BlockingQueue<String> intervalQueue;

    // Interval run time.
    private final int intervalTime;

    /**
     *
     * @param mainQueue - The main blocking queue created in a GatheringThread to hold tweets in their json format.
     * @param intervalTime - The amount of time, in seconds, that an interval should run.
     */
    public IntervalThread(BlockingQueue<String> mainQueue, int intervalTime){

        // Set the main queue
        this.mainQueue = mainQueue;

        // Create this intervals blocking queue.
        this.intervalQueue = new LinkedBlockingQueue<>();

        // Set the interval time.
        this.intervalTime = intervalTime;
    }


    @Override
    public void run() {

        // Grabs tweets from the mainQueue and adds them to this intervals blocking queue. It will poll the blocking
        // queue up to the interval time. Once interrupted we break out of the loop to stop gathering tweets.
        while(!Thread.interrupted()){
            try {
                intervalQueue.add(Objects.requireNonNull(mainQueue.poll(intervalTime, TimeUnit.SECONDS)));
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
