package com.twitterStatReporter;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        // Load the config file
        ConfigFileReader configFile = new ConfigFileReader();
        configFile.loadConfig();

        // Create a object to gather user input.
        UserInputAndValidation userInputAndValidation = new UserInputAndValidation();

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

        // Create the gathering thread.
        GatheringThread gatherer = new GatheringThread(userInputAndValidation.authTokenMap,
                userInputAndValidation.totalRunTime, userInputAndValidation.intervalRunTime,
                userInputAndValidation.writeFlag);

        Thread gathering = new Thread(gatherer);
        gathering.start();
    }
}
