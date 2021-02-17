package com.twitterStatReporter;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.HashMap;

/**
 * AuthStreamBuilder attempts to create an authenticated stream object using the users passed credentials.
 */
public final class AuthStreamBuilder {

    /**
     * Authenticate attempts to use the users credentials passed in the form of a hashmap to create an authenticated
     * stream. If successfull it passes the stream object back to be used in the gatheringThread.
     * @param authTokenMap - Hashmap in the form of (Token_type, value)
     * @return an authenticated stream to be used to gather tweets from.
     */
    public TwitterStream authenticate(HashMap<String, String> authTokenMap){

        // Create a new Configuration Builder
        ConfigurationBuilder cb = new ConfigurationBuilder();

        // Enable Debuging
        cb.setDebugEnabled(true);

        // Enable the storage of JSON's for report parsing.
        cb.setJSONStoreEnabled(true);

        // Set the Consumer Key
        cb.setOAuthConsumerKey(authTokenMap.get("ConsumerKey"));

        // Set the Consumer Secret Key
        cb.setOAuthConsumerSecret(authTokenMap.get("ConsumerSecret"));

        // Set the Access Token
        cb.setOAuthAccessToken(authTokenMap.get("AccessToken"));

        // Set the Access Token Secret key
        cb.setOAuthAccessTokenSecret(authTokenMap.get("AccessSecret"));

        // Set a time out on the connection in milliseconds
        cb.setHttpConnectionTimeout(100000);

        // Create a new twitter stream using the configuration builder and return it.
        return new TwitterStreamFactory(cb.build()).getInstance();
    }

}
