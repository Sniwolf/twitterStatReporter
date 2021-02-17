package com.twitterStatReporter;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * IntervalScheduler is use to create and schedule intervalThreads, intervalTerminators, and ReportGenerator Objects.
 * Using the numberOfReports, IntervalScheduler creates an intervalThread, intervalTerminator, and reportGenerator
 * objects. It passes the intervalThread and reportGenerator objects a blocking queue that is shared by both to produce
 * and consume tweets respectively. The intervalTerminator objects are passed the intervalThread objects and their
 * intervalRunTime to schedule them to stop after the given time. Once the intervalThread and reportGenerator objects
 * are created it schedules the time for them to run via intervalRunTime*reportNumber.
 */
public class IntervalScheduler {

    // The number of reports calculated from the total run time / interval run time.
    private final int numberOfReports;

    // Interval runtime set by the user.
    private final int intervalRunTime;

    // The streams blocking queue where all the tweets in json for are stored.
    private final BlockingQueue<String> rawTweets;

    // Array list to hold a number of intervalThread objects up to the total number of reports.
    private final ArrayList<IntervalThread> intervalThreads;

    // Array list to hold a number of intervalTerminator objects up to the total number of reports.
    private final ArrayList<IntervalTerminator> intervalTerminators;

    // Arraylist to hold a number of reportGenerator objects up to the total number of reports.
    private final ArrayList<ReportGenerator> reportGenerators;

    // Flag used to indicate if a user would like their report written to file or printed on the terminal.
    private final int writeToFile;

    /**
     *
     * @param numberOfReports - The total number of reports that need to be created, calculated from
     *                        totalRunTime/intervalRunTime
     * @param intervalRunTime - The user set intervalRunTime in seconds.
     * @param rawTweets - The blocking queue created in gatheringThread that holds the tweets in a json format.
     * @param writeToFile - flag used to indicate if a user would like the file written to the terminal or to a file.
     */
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

    /**
     * Scheduler is used to create the intervalThread, intervalTerminator, and Report generator objects and then
     * schedule when they should run. Using the number of reports it creates a number of intervalThread,
     * intervalTerminator, and reportGenerator objects. In the interval thread objects a blocking queue is created
     * that is shared between itself and the corresponding report generator object. Each intervalTerminator object
     * is passed its corresponding intervalThread along with the intervalRuntime and delay. It uses this information
     * to stop the interval thread after the intervalRunTime has elapsed. After all the objects have been created,
     * scheduler schedules each intervalTerminator and ReportGenerator to run.
     */
    public void scheduler(){

        // Keep track of the interval delay that each interval thread will need to start at
        int delay = 0;

        for(int i = 0; i < numberOfReports; i++){
            // Create a number of interval threads equal to the number of reports.
            IntervalThread intervalThread = new IntervalThread(rawTweets, intervalRunTime);

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
