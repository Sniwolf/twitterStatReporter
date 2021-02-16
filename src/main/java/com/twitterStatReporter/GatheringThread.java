package com.twitterStatReporter;

import twitter4j.TwitterStream;

import java.util.HashMap;
import java.util.concurrent.*;

public class GatheringThread implements Runnable{

    private final TwitterStream authStream;

    private final int totalRunTime;

    // Blocking queue to hold the raw tweets.
    BlockingQueue<String> rawTweets;

    int intervalRunTime;

    int numberOfReports;

    HashMap<String, String> userCreds;

    int writeToFile;

    public GatheringThread(HashMap<String, String> userCreds, int totalRunTime, int intervalRunTime, int writeToFile){

        // Create a new AuthStreamBuilder
        AuthStreamBuilder authStreamBuilder = new AuthStreamBuilder();

        // Create the authenticated auth stream.
        this.authStream = authStreamBuilder.authenticate(userCreds);

        this.totalRunTime = totalRunTime;

        this.intervalRunTime = intervalRunTime;

        this.numberOfReports = totalRunTime/intervalRunTime;

        this.userCreds = userCreds;

        this.writeToFile = writeToFile;

    }
    @Override
    public void run() {

        try{
            //Create a new executor service to stop the stream after a period of time.
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
            // Reporters stream sampler.
            gatherRawSampleStream sampleStream = new gatherRawSampleStream(authStream);

            // Set the blocking queue here to the main queue in the gatherRawSampleStream.
            this.rawTweets = sampleStream.rawTweets;

            // Submit the sampleStream job to be stopped after the given amount of time.
            Future<?> future = executor.submit(sampleStream);

            // We need to wait for tweets to start filling up the blocking queue in order to start processing them.
            while(rawTweets.isEmpty()){
                Thread.sleep(500);
            }

            // Create an interval scheduler to schedule a thread to gather tweets in a given interval.
            IntervalScheduler scheduler = new IntervalScheduler(numberOfReports, intervalRunTime, rawTweets, writeToFile);

            // Create the schedules.
            scheduler.scheduler();

            //Set the total runtime the stream should be open.
            executor.schedule(() -> {
                future.cancel(true);
                // System.out.println("Number of tweets captured: " + rawTweets.size());

            }, this.totalRunTime, TimeUnit.SECONDS);
            executor.shutdown();

        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }
}
