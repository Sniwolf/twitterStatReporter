package com.twitterStatReporter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * ConfigurationFileReader is used to read the twitter.config file to gather the users set configuration information.
 * loadConfig will attempt to gather the information from the config file so long as the fields are not null.
 */
public class ConfigFileReader {

    // Properties object to get the configuration files properties.
    Properties configProp;

    //Input stream to the data from the configuration file.
    FileInputStream configStream;

    // Total run time of the program from the config file.
    int totalRunTime = 0;

    // Interval run time of the program from the config file.
    int intervalRunTime = 0;

    // Choice to write to file or to the terminal.
    int writeToFile = 0;

    // Access token string.
    String accessToken;

    // Access secret token string.
    String accessSecret;

    // Consumer Key Token string.
    String consumerKey;

    // Consumer Secret token string.
    String consumerSecret;

    /**
     * ConfigurationFileReader constructor, used to create and gather the configuration information in the twitter
     * config file.
     * @throws IOException
     */
    public ConfigFileReader() throws IOException {

        // The config file's set name.
        String fileName = "twitter.config";

        // Create a properties object to get the config settings from.
        this.configProp = new Properties();

        // Create a file input stream.
        this.configStream = new FileInputStream(fileName);


    }

    /**
     * Load the configuration file and validate that the fields are not set to null.
     * @throws IOException throw an exception when one of the fields is not present or is null.
     */
    public void loadConfig() throws IOException {

        configProp.load(configStream);

        // Get the total run time from the config file and convert it to an int for use in the program.
        if(configProp.getProperty("TotalRunTime") != null){
            totalRunTime = Integer.parseInt(configProp.getProperty("TotalRunTime"));
        }

        // Get the interval run time from the config file and convert it to an int for use in the program.
        if(configProp.getProperty("IntervalRunTime") != null){
            intervalRunTime = Integer.parseInt(configProp.getProperty("IntervalRunTime"));
        }

        // Get the access token from the config file.
        if(configProp.getProperty("AccessToken") != null){
            accessToken = configProp.getProperty("AccessToken");
        }

        // Get the access secret token from the config file.
        if(configProp.getProperty("AccessSecret") != null){
            accessSecret = configProp.getProperty("AccessSecret");
        }

        // Get the consumer key from the config file.
        if(configProp.getProperty("ConsumerKey") != null){
            consumerKey = configProp.getProperty("ConsumerKey");
        }

        // Get the consumer secret token from the config file.
        if(configProp.getProperty("ConsumerSecret") != null){
            consumerSecret = configProp.getProperty("ConsumerSecret");
        }

        if(configProp.getProperty("WriteFlag") != null){
            writeToFile = Integer.parseInt(configProp.getProperty("WriteFlag"));
        }

    }
}
