package com.twitterStatReporter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;

public class UserInputAndValidation {

    // A scanner to gather user input.
    Scanner inputScanner;

    // Int to hold the total runtime, in seconds, for the program.
    int totalRunTime;

    // Int to hold the interval time, in seconds, for the program.
    int intervalRunTime;

    // Create a Hash Map to store the keys
    HashMap<String, String> authTokenMap;

    int writeFlag;

    public UserInputAndValidation(){
        this.inputScanner = new Scanner(System.in);
        this.authTokenMap = new HashMap<>();
    }

    public void setTotalRunTime(){

        // Validate that a number was given.
        do{
            System.out.print("Please specify how long, in seconds, you would like the program to run: ");
            while(!inputScanner.hasNextInt()){
                System.out.print("Please Enter a valid amount of time: ");
                inputScanner.next();
            }
            totalRunTime = inputScanner.nextInt();
        }while (totalRunTime <= 0);
    }

    /**
     * Used to validate if the total runtime passed by the config file is valid. If not send to set total runtime
     * to force the user to input a valid time.
     * @param totalRunTime - int, the total runtime of the program in seconds.
     */
    public void validateTotalRunTime(int totalRunTime){
        if(totalRunTime <= 0){
            setTotalRunTime();
        }else{
            this.totalRunTime = totalRunTime;
            System.out.println("Total Run Time set from Config: " + totalRunTime + " seconds");
        }
    }

    public void setIntervalRunTime(){

        // Validate that a number was given as input.
        do{
            System.out.print("Please specify how long, in seconds you would like your stream interval to last: ");
            while(!inputScanner.hasNextInt()){
                System.out.print("Please Enter a valid amount of time for the interval: ");
                inputScanner.next();
            }

            intervalRunTime = inputScanner.nextInt();

        }while (intervalRunTime <= 0);

        // Validate that the number given was less than the total run time.
        if(intervalRunTime > totalRunTime){
            do{
                System.out.print("Please enter an interval time less than or equal to your current total run time: " + totalRunTime);
                intervalRunTime = inputScanner.nextInt();
            }while(intervalRunTime > totalRunTime);
        }

    }

    public void validateIntervalRunTime(int intervalRunTime){
        if(intervalRunTime > totalRunTime || intervalRunTime <= 0){
            setIntervalRunTime();
        }else{
            this.intervalRunTime = intervalRunTime;
            System.out.println("Interval Run time set from Config: " + intervalRunTime + " seconds");
        }
    }

    public void setAccessToken(){

        // String to hold the access token.
        String accessToken;

        System.out.print("Please enter your Access token: ");
        accessToken = inputScanner.nextLine().trim();

        // Check that an access token was given and not just white space.
        if(accessToken.length() == 0){

            while(accessToken.length() == 0){
                System.out.print("Please enter a valid Access token: ");
                accessToken = inputScanner.nextLine().trim();
            }
        }
        //Assign the keys in the hash map
        authTokenMap.put("AccessToken", accessToken);

    }

    public void setAccessSecret(){
        // String to hold the access token.
        String accessSecret;

        System.out.print("Please enter your Access Secret token: ");
        accessSecret = inputScanner.nextLine().trim();

        // Check that an access token was given and not just white space.
        if(accessSecret.length() == 0){

            while(accessSecret.length() == 0){
                System.out.print("Please enter a valid Access Secret token: ");
                accessSecret = inputScanner.nextLine().trim();
            }
        }
        //Assign the keys in the hash map
        authTokenMap.put("AccessSecret", accessSecret);
    }

    public void setConsumerKey(){
        // String to hold the access token.
        String consumerKey;

        System.out.print("Please enter your Consumer Key token: ");
        consumerKey = inputScanner.nextLine().trim();

        // Check that an access token was given and not just white space.
        if(consumerKey.length() == 0){

            while(consumerKey.length() == 0){
                System.out.print("Please enter a valid Consumer Key token: ");
                consumerKey = inputScanner.nextLine().trim();
            }
        }
        //Assign the keys in the hash map
        authTokenMap.put("ConsumerKey", consumerKey);
    }

    public void setConsumerSecret(){
        // String to hold the access token.
        String consumerSecret;

        System.out.print("Please enter your Consumer Secret token: ");
        consumerSecret = inputScanner.nextLine().trim();

        // Check that an access token was given and not just white space.
        if(consumerSecret.length() == 0){

            while(consumerSecret.length() == 0){
                System.out.print("Please enter a valid Consumer Secret token: ");
                consumerSecret = inputScanner.nextLine().trim();
            }
        }
        //Assign the keys in the hash map
        authTokenMap.put("ConsumerSecret", consumerSecret);
    }

    public void setWriteFlag(){

        // Validate that a number was given as input.
        do{
            System.out.print("Please specify how you would like the reports delivered: 1 - On Terminal or 2 - Written to file ");
            while(!inputScanner.hasNextInt()){
                System.out.print("Please Enter a value of 1 - On terminal or 2 - written to file: ");
                inputScanner.next();
            }

            writeFlag = inputScanner.nextInt();

        }while (writeFlag <= 0);

        // Validate that the number given was less than the total run time.
        if(writeFlag > 2){
            do{
                System.out.print("Please enter a value less than 2");
                writeFlag = inputScanner.nextInt();
            }while(writeFlag > totalRunTime);
        }
    }

    public void validateWriteFlag(int writeFlag){
        if(writeFlag > 2 || writeFlag <= 0){
            setWriteFlag();
        }else{
            this.writeFlag = writeFlag;

            if(writeFlag == 1){
                System.out.println("Writing reports to terminal");
            }else{
                System.out.println("Writing reports to file");
            }

        }
    }

}
