package com.twitterStatReporter;

import twitter4j.TwitterStream;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

/**
 * GatheringThread is used to create a thread that will gather tweets from the stream using TwitterStream.
 * The constructor takes the users credentials, totalRunTime, IntervalRunTime, and a flag to indicate that
 * the user would like the reports written to file or outputted to the terminal. It then creates an authenticated
 * stream for the program to sample tweets from. When a thread is created using this class it will create a new
 * rawSampleStream used to gather tweets in their JSON form and add them to a blocking queue that the intervalThread
 * will gather from. It then creates a future object to interrupt the gathering thread after totalRunTime has elapsed.
 * Finally it calls IntervalScheduler to schedule the interval and report generator threads.
 */
public class GatheringThread implements Runnable{

    // Authenticated stream.
    private final TwitterStream authStream;

    // Total runtime of the program.
    private final int totalRunTime;

    // Interval runtime that the intervalThread will use.
    private final int intervalRunTime;

    // The number of reports that will need to be generated.
    private final int numberOfReports;

    // Flag used to indicate that the user wants the reports written to the terminal or to a file.
    private final int writeToFile;

    /**
     *
     * @param userCreds - Hashmap that holds the user credentials in the form of (tokentype, key)
     * @param totalRunTime - total runtime, in seconds, that the program should run.
     * @param intervalRunTime - total run time an interval thread should last gathering tweets.
     * @param writeToFile - Flag used to indicate if a user would like the report written to the terminal (1) or
     *                    to file (2).
     */
    public GatheringThread(HashMap<String, String> userCreds, int totalRunTime, int intervalRunTime, int writeToFile){

        // Create a new AuthStreamBuilder
        AuthStreamBuilder authStreamBuilder = new AuthStreamBuilder();

        // Create the authenticated auth stream.
        this.authStream = authStreamBuilder.authenticate(userCreds);

        // Set total run time of the gathering thread.
        this.totalRunTime = totalRunTime;

        // Set the interval run time that the interval threads will use.
        this.intervalRunTime = intervalRunTime;

        // Calculate the total number of reports.
        this.numberOfReports = totalRunTime/intervalRunTime;

        // Store the users option to have the reports written to file or to the terminal.
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
            // Blocking queue to hold the raw tweets.
            BlockingQueue<String> rawTweets = sampleStream.rawTweets;

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

            }, this.totalRunTime, TimeUnit.SECONDS);
            executor.shutdown();

        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }
}
