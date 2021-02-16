package com.twitterStatReporter;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IntervalScheduler {

    // The number of reports calculated from the total run time / interval run time.
    int numberOfReports;

    // Interval runtime set by the user.
    int intervalRunTime;

    // The streams blocking queue where all the tweets in json for are stored.
    BlockingQueue<String> rawTweets;

    // Array list to hold a number of intervalThread objects up to the total number of reports.
    ArrayList<IntervalThread> intervalThreads;

    // Array list to hold a number of intervalTerminator objects up to the total number of reports.
    ArrayList<IntervalTerminator> intervalTerminators;

    // Arraylist to hold a number of reportGenerator objects up to the total number of reports.
    ArrayList<ReportGenerator> reportGenerators;

    int writeToFile;

    public IntervalScheduler(int numberOfReports, int intervalRunTime, BlockingQueue<String> rawTweets, int writeToFile){

        // Set number of reports
        this.numberOfReports = numberOfReports;

        // Set interval run time.
        this.intervalRunTime = intervalRunTime;

        // Set the rawTweets blocking queue.
        this.rawTweets = rawTweets;

        // Create a new arraylist for the interval threads, terminators, and report generators.
        this.intervalThreads = new ArrayList<>();
        this.intervalTerminators = new ArrayList<>();
        this.reportGenerators = new ArrayList<>();
        this.writeToFile = writeToFile;
    }

    public void scheduler(){

        // Keep track of the interval delay that each interval thread will need to start at
        int delay = 0;

        for(int i = 0; i < numberOfReports; i++){
            // Create a number of interval threads equal to the number of reports.
            IntervalThread intervalThread = new IntervalThread(rawTweets, intervalRunTime, delay);

            // Create a number of interval terminators equal to the number of reports using the created interval thread.
            IntervalTerminator intervalTerminator = new IntervalTerminator(intervalThread, intervalRunTime, delay);

            // Create a number of report generators equal to the number of reports. Pass each the that intervals blocking queue and future.
            ReportGenerator reportGenerator = new ReportGenerator(intervalThread.intervalQueue, intervalTerminator.future, i, delay, writeToFile);

            // Add the objects to their respective list.
            intervalThreads.add(intervalThread);
            intervalTerminators.add(intervalTerminator);
            reportGenerators.add(reportGenerator);

            // Increase the delay by the interval run time.
            delay += intervalRunTime;
        }

        // Create a schedule executor with a thread pool equal to the number of reports that need to run.
        ScheduledExecutorService scheduledIntervals = Executors.newScheduledThreadPool(numberOfReports);

        // Create a schedule executor with a thread pool equal to the number of reports that need to run.
        ScheduledExecutorService scheduledReports = Executors.newScheduledThreadPool(numberOfReports);

        //Schedule each ot the interval terminators
        for(IntervalTerminator scheduler : intervalTerminators){
            scheduledIntervals.schedule(scheduler, scheduler.intervalDelay, TimeUnit.SECONDS);
        }
        scheduledIntervals.shutdown();

        // Schedule each of the reportGenerators
        for(ReportGenerator reportGenerator : reportGenerators){
            scheduledReports.schedule(reportGenerator, reportGenerator.delay, TimeUnit.SECONDS);
        }
        scheduledReports.shutdown();
    }
}
