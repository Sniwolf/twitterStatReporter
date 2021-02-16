package com.twitterStatReporter;

import java.util.Objects;
import java.util.concurrent.*;

public class IntervalThread implements Runnable{

    // The main blocking queue.
    BlockingQueue<String> mainQueue;

    // The report interval queue.
    BlockingQueue<String> intervalQueue;

    // Interval run time.
    int intervalTime;

    // Delay time till the interval needs to start.
    int delayTime;

    public IntervalThread(BlockingQueue<String> mainQueue, int intervalTime, int delayTime){

        // Set the main queue
        this.mainQueue = mainQueue;

        // Create this intervals blocking queue.
        this.intervalQueue = new LinkedBlockingQueue<>();

        // Set the interval time.
        this.intervalTime = intervalTime;

        // Set the delay time.
        this.delayTime = delayTime;
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
