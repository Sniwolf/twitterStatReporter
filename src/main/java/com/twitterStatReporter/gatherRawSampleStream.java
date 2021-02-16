package com.twitterStatReporter;

import twitter4j.RawStreamListener;
import twitter4j.TwitterStream;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class gatherRawSampleStream implements Runnable{

    // Blocking queue to hold the raw tweets.
    BlockingQueue<String> rawTweets;

    // Auth stream to gather a sample of tweets from.
    TwitterStream authStream;

    // Stream listener to listen to the sample stream.
    RawStreamListener streamListener;

    public gatherRawSampleStream(TwitterStream authenticatedStream){

        this.authStream = authenticatedStream;

        this.rawTweets = new LinkedBlockingQueue<>();

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
