package com.twitterStatReporter;

import java.util.HashMap;
import java.util.Scanner;

/**
 * UserInputAndValidation is used to validate the user input either from the config file or from that captured from the
 * terminal.
 */
public class UserInputAndValidation {

    // A scanner to gather user input.
    Scanner inputScanner;

    // Int to hold the total runtime, in seconds, for the program.
    int totalRunTime;

    // Int to hold the interval time, in seconds, for the program.
    int intervalRunTime;

    // Create a Hash Map to store the keys
    HashMap<String, String> authTokenMap;

    // Flag used to indicate if the user would like their reports written to a file or to the terminal.
    int writeFlag;

    public UserInputAndValidation(){
        this.inputScanner = new Scanner(System.in);
        this.authTokenMap = new HashMap<>();
    }

    /**
     * Used to gather the total run time the user would like for the program to run. If the user enters a value
     * less than zero or one that is not a integer then it asks the user to enter a valid amount again.
     */
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

    /**
     * Used to set and check that the interval run time given is an integer and its value is not greater then the total
     * runtime previously set.
     */
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
                System.out.print("Please enter an interval time less than or equal to your current total run time: "
                        + totalRunTime);
                intervalRunTime = inputScanner.nextInt();
            }while(intervalRunTime > totalRunTime);
        }

    }

    /**
     * Used to check the intervalRunTime captured by the config file is not greater then the total run time and is
     * greater then zero. If its not then call setIntervalRunTime to force the user to enter a valid value.
     * @param intervalRunTime - time in seconds the user would like each interval to last.
     */
    public void validateIntervalRunTime(int intervalRunTime){
        if(intervalRunTime > totalRunTime || intervalRunTime <= 0){
            setIntervalRunTime();
        }else{
            this.intervalRunTime = intervalRunTime;
            System.out.println("Interval Run time set from Config: " + intervalRunTime + " seconds");
        }
    }

    /**
     * SetAccessToken is used to check that an access token was given if not it asks the user to enter one.
     */
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

    /**
     * setAccessSecret checks to see if a Access Secret token was given, if not it asks the users to provide one.
     */
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

    /**
     * setConsumerKey checks if a Consumer key was given, if not it asks the user to provide one.
     */
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

    /**
     * setConsumerSecret checks if a Consumer Secret token was given, if not it asks the user to provide one.
     */
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

    /**
     * setWriteFlag asks the user to enter a value of either 1 or 2 to determine if the user would like to have their
     * reports written to a file(2) or to the terminal (1). If the value is outside of this range then the program
     * asks the user to enter a valid value.
     */
    public void setWriteFlag(){

        // Validate that a number was given as input.
        do{
            System.out.print("Please specify how you would like the reports delivered\n");
            System.out.println("1 - To have the report(s) print to the terminal.\n");
            System.out.println("2 - To have the report(s) written to a file(s).\n");
            while(!inputScanner.hasNextInt()){
                System.out.println("1 - To have the report(s) print to the terminal.\n");
                System.out.println("2 - To have the report(s) written to a file(s).\n");
                inputScanner.next();
            }

            writeFlag = inputScanner.nextInt();

        }while (writeFlag <= 0);

        // Validate that the number given was less than the total run time
        if(writeFlag > 2){
            do{
                System.out.print("Please enter a value of: \n");
                System.out.println("1 - To have the report(s) print to the terminal.\n");
                System.out.println("2 - To have the report(s) written to a file(s).\n");
                writeFlag = inputScanner.nextInt();
            }while(writeFlag > totalRunTime);
        }
    }

    /**
     * validateWriteFlag is used to check that the value given in the config file is valid, if not it
     * calls setWriteFlag to force the user to enter a valid value.
     * @param writeFlag - writeFlag value from the config file.
     */
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
