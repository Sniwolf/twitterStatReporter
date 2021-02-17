package com.twitterStatReporter;

import java.io.File;
import java.io.IOException;

/**
 * TwitterStatReporter is used to gather various statistics on tweets from a stream and create a report on them.
 * This program makes use of the twitter4j library to create an authenticated twitter stream from which to gather
 * tweets from. The program takes a users Access Token, Access Secret token, Consumer Key, and Consumer Secret token
 * created on Twitters website and uses them to create an authenticated stream from which to sample from. Once the
 * stream has been created it continues to gather from the stream for a predefined time and at predefined intervals
 * set by the user. As the stream is being gathered a report is being created that will be show to the user after
 * each interval has finished.
 */
public class Main {

    /**
     * Main starts by trying to log the config file and parse our the users credentials and parameters. It the
     * validates that the values set by the user are valid otherwise it asks the user to enter them via the
     * terminal. Once the users credentials have been accepted it starts by creating a gatheringThread to start
     * gathering tweets and create reports.
     * @param args -
     * @throws IOException -
     */
    public static void main(String[] args) throws IOException {

        File configCheck = new File("twitter.config");

        // Create a object to gather user input.
        UserInputAndValidation userInputAndValidation = new UserInputAndValidation();

        //If the config file exists, verify it is valid or ask the user to input valid values. If it doesn't, force
        // the user to enter their parameters and credentials.
        if(configCheck.exists()){

            System.out.println("Loading Config File");

            // Load the config file
            ConfigFileReader configFile = new ConfigFileReader();
            configFile.loadConfig();


            // If the total run time is not present in the config have the user set a total run time.
            if(configFile.totalRunTime == 0){
                userInputAndValidation.setTotalRunTime();
            }else{
                userInputAndValidation.validateTotalRunTime(configFile.totalRunTime);
            }

            // If the interval run time is not present in the config have the user set an interval run time.
            if(configFile.intervalRunTime == 0){
                userInputAndValidation.setIntervalRunTime();
            }else{
                userInputAndValidation.validateIntervalRunTime(configFile.intervalRunTime);
            }

            // If the interval run time is not present in the config have the user set an interval run time.
            if(configFile.writeToFile == 0){
                userInputAndValidation.setWriteFlag();
            }else{
                userInputAndValidation.validateWriteFlag(configFile.writeToFile);
            }

            // Have the user set their Access token.
            if(configFile.accessToken == null){
                userInputAndValidation.setAccessToken();
            }else{
                userInputAndValidation.authTokenMap.put("AccessToken", configFile.accessToken);
                System.out.println("Using access token from config file.");
            }

            // Have the user set their Access Secret token.
            if(configFile.accessSecret == null){
                userInputAndValidation.setAccessSecret();
            }else{
                userInputAndValidation.authTokenMap.put("AccessSecret", configFile.accessSecret);
                System.out.println("Using Access Secret token from config file");
            }

            // Have the user set their Consumer Key token.
            if(configFile.consumerKey == null){
                userInputAndValidation.setConsumerKey();
            }else{
                userInputAndValidation.authTokenMap.put("ConsumerKey", configFile.consumerKey);
                System.out.println("Using Consumer Key token from config file");
            }

            // Have the user set their Consumer Secret token.
            if(configFile.consumerSecret == null){
                userInputAndValidation.setConsumerSecret();
            }else{
                userInputAndValidation.authTokenMap.put("ConsumerSecret", configFile.consumerSecret);
                System.out.println("Using Consumer Secret token from config file.");
            }
        }else {
            System.out.println("No twitter.config file found, you will now be asked to enter your parameters and " +
                    "credentials");

            // Ask the user to set the total runtime.
            userInputAndValidation.setTotalRunTime();

            // Ask the user to set the interval run time
            userInputAndValidation.setIntervalRunTime();

            // Ask the user to have the report written to file or to the terminal.
            userInputAndValidation.setWriteFlag();

            // Ask for the users access token
            userInputAndValidation.setAccessToken();

            // Ask for the users access secret token
            userInputAndValidation.setAccessSecret();

            // Ask for the users consumer key
            userInputAndValidation.setConsumerKey();

            // Ask for the users consumer secret key
            userInputAndValidation.setConsumerSecret();
        }



        // Create the gathering thread.
        GatheringThread gatherer = new GatheringThread(userInputAndValidation.authTokenMap,
                userInputAndValidation.totalRunTime, userInputAndValidation.intervalRunTime,
                userInputAndValidation.writeFlag);

        Thread gathering = new Thread(gatherer);
        gathering.start();
    }
}
