package com.twitterStatReporter;

import twitter4j.RawStreamListener;
import twitter4j.TwitterStream;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * GatherRawSampleStream is used to gather tweets in their json form from a twitterStream object. It adds these
 * raw tweets to a blocking queue that is passed to each intervalThread. Once the gathering stream thread is interrupted
 * it closes and shuts down the stream, this occurs after totalRunTime.
 */
public class gatherRawSampleStream implements Runnable{

    // Blocking queue to hold the raw tweets.
    BlockingQueue<String> rawTweets;

    // Auth stream to gather a sample of tweets from.
    private final TwitterStream authStream;

    // Stream listener to listen to the sample stream.
    private final RawStreamListener streamListener;

    /**
     *
     * @param authenticatedStream - TwitterStream object that has been created using the users credentials.
     */
    public gatherRawSampleStream(TwitterStream authenticatedStream){

        // Set the authStream
        this.authStream = authenticatedStream;

        // Create the blocking queue for the raw tweets.
        this.rawTweets = new LinkedBlockingQueue<>();

        // Create a stream listner and update the onMessage method to add the tweets to the rawTweets queue.
        this.streamListener = new RawStreamListener() {
            @Override
            public void onMessage(String rawJSON) {
                rawTweets.add(rawJSON);
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };
    }

    @Override
    public void run() {

        try{
            // Add the listener
            authStream.addListener(streamListener);

            // Start getting a sample of the stream.
            authStream.sample();

            // This feels a little hacky, but it allows us to catch the interrupt and move towards cleaning up and
            // shutting down the stream.
            while(!Thread.interrupted()){
                Thread.sleep(500);
            }
        }catch (InterruptedException e){
            // Clean up the auth stream
            authStream.cleanUp();

            // Shutdown the auth stream.
            authStream.shutdown();
        }
    }
}
