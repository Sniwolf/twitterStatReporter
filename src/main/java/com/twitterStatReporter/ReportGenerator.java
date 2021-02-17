package com.twitterStatReporter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * ReportGenerator class is used to generate reports from the raw tweets captured from the gathering thread by
 * the interval thread. The thread used to run this class starts to process the tweets in the passed rawJSON blocking
 * queue, it then uses a number of functions to grab the language, hashtag, url domain, user mention, and if the
 * tweet was a retweet. As it does this it keeps track of the number of tweets processed and breaks them down into
 * those that were deleted and those that are not. Once all the tweets from the blocking queue have been processed
 * it calls writeReport. This function will either write out the report to the terminal or to a series of files
 * depending on the users request in the config file. Unfortunately this class was unable to parse out the timezone
 * fields as requested, this is due to a number of fields being either deprecated or nullified for user privacy.
 * https://twittercommunity.com/t/utc-offset-and-time-zone-data-missing/106734
 * https://developer.twitter.com/en/docs/twitter-api/v1/data-dictionary/object-model/user
 */
public class ReportGenerator implements Runnable{

    // blocking queue shared with the intervalThread to consume tweets from.
    BlockingQueue<String> tweets;

    // Future object to check if the interval thread has finished.
    Future<?> futureStream;

    //HashMap to hold language and occurrences of that language.
    private final HashMap<String, Integer> languageOccurrences;

    //HashMap to hold hashtags and their occurrences
    private final HashMap<String, Integer> hashtagOccurrences;

    //Hashmap to hold hostnames and their occurrences
    private final HashMap<String, Integer> hostnameOccurrences;

    // Hashmap to hold the user mentions and their occurrences.
    private final HashMap<String, Integer> userMentionOccurrences;

    // This reports number
    private final int reportNumber;

    // The delay until this report runs.
    int delay;

    // The number of tweets that contain a url.
    private float numberOfTweetsWithURL = 0;

    // The number of tweets that contain a photo url.
    private float numberOfTweetsWithPhoto = 0;

    // The number of tweets that have been retweeted.
    private float numberOfTweetsRetweeted = 0;

    // Flag to indicate we are writing the report to file vs the terminal.
    private final int writeToFile;

    /**
     *
     * @param rawJSON - Blocking queue shared from the interval thread. The tweets gathered here are tweets from
     *                the specified time interval.
     * @param futureStream - The future object that is used to interrupt the IntervalThread, used here to determine
     *                     when the interval has completed.
     * @param reportNumber - This reports number
     * @param delay - Delay until this reportGenerator should start running, in seconds.
     * @param writeToFile - Flag to indicate if the user would like the reports written to the terminal or to file.
     */
    public ReportGenerator(BlockingQueue<String> rawJSON, Future<?> futureStream, int reportNumber, int delay,
                           int writeToFile){

        // Set tweets
        this.tweets = rawJSON;

        // Set futureStream
        this.futureStream = futureStream;

        // Create a hashmap to hold the language occurrences
        this.languageOccurrences = new HashMap<>();

        // Create a hashmap to hold the hashtag occurrences
        this.hashtagOccurrences = new HashMap<>();

        // Create a hashmap to hold the hostname occurrences
        this.hostnameOccurrences = new HashMap<>();

        // Create a hashmap to hold the user mention occurrences.
        this.userMentionOccurrences = new HashMap<>();

        // Set this reports number.
        this.reportNumber = reportNumber;

        // Set this reports delay in seconds.
        this.delay = delay;

        // Set the flag to indicate if the user wants the reports written to file or to the terminal.
        this.writeToFile = writeToFile;
    }

    @Override
    public void run() {

        // Number of tweets captured this interval
        int reportTweetCount = 0;

        // Number of tweets that weren't deleted
        float actualTweetCount = 0;

        // Number of tweets that were deleted.
        int deleteTweets = 0;

        while(true){
            String tweet = null;
            try {
                // Poll the tweets queue to verify a tweet is available, if so grab it.
                tweet = tweets.poll(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(tweet != null){

                // Keep track of the number of tweets processed during this interval.
                reportTweetCount++;

                if(tweet.contains("delete")){

                    // Keep track of the number of "Deleted" tweets processed during this interval.
                    deleteTweets++;

                }else{
                    //Convert the tweet from string to JSON object.
                    JSONObject jsonTweet = new JSONObject(tweet);

                    // Parse out the language and store it.
                    languageGrabber(jsonTweet);

                    // Grab the entities section
                    try {
                        entitiesGrabber(jsonTweet);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                    // Document if this was an actual tweet and not a deleted one.
                    actualTweetCount++;
                }


            }
            //Once all the tweets have been processed start creating the report
            else if(tweets.isEmpty()){

                try {
                    writeReport(reportNumber, reportTweetCount, actualTweetCount, deleteTweets,writeToFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }

        }
    }

    /**
     * Language grabber is used to grab the lang field from the twitters json. This field holds the language of that
     * tweet. Here we grab the field and check if the language is already in the languageOccurances hashMap. If it is
     * we increment it otherwise we add it to the hashmap with a value of 1.
     * @param jsonTweet - Tweet in the form of a JSON object.
     */
    private void languageGrabber(JSONObject jsonTweet){

        // Grab the lang section and cast it to a string
        String language = (String) jsonTweet.get("lang");

        // Check if the language is already in the hashmap, if so increment the value by 1 otherwise add it to the
        // hashmap with a value of 1.
        if(languageOccurrences.containsKey(language)){
            languageOccurrences.replace(language, languageOccurrences.get(language) + 1);
        }
        else{
            languageOccurrences.put(language, 1);
        }
    }

    /**
     * entitiesGrabber is used to grab the entities and extended_entities sections of a tweet and parse out the
     * the required information such as hashtags, urls, user_mentions, retweet_count. It then passes these
     * json objects to corresponding functions to process the information contained within.
     * @param jsonTweet - tweet in the form of a JSON object.
     * @throws URISyntaxException - Thrown when trying to parse out the url of the urls object.
     */
    private void entitiesGrabber(JSONObject jsonTweet) throws URISyntaxException {

        // Grab the entities object.
        JSONObject entities = (JSONObject) jsonTweet.get("entities");

        // The extended entities object that contains the media information contained in a tweet.
        JSONObject extendedEntities;

        // If the extended entities exist set it and calculate the number tweets that contain photos..
        if(!jsonTweet.isNull("extended_entities")){
            extendedEntities = (JSONObject) jsonTweet.get("extended_entities");
            numberOfTweetsWithPhoto(extendedEntities);
        }

        // Grab the hashtag object
        JSONArray hashtags = (JSONArray) entities.get("hashtags");

        // Grab the URLs object
        JSONArray urls = (JSONArray) entities.get("urls");

        // Grab the user_mentions object
        JSONArray userMentions = (JSONArray) entities.get("user_mentions");

        // Grab retweet to count the number of times a a tweet was retweeted.
        int retweetCount = jsonTweet.getInt("retweet_count");

        // If there are hashtags present parse them.
        if(hashtags.length() > 0){
            // Parse our the hashtags from the hashtag object array.
            hashtagsArrayParser(hashtags);
        }

        // If the urls section contains urls parse them.
        if(urls.length() > 0){
            this.numberOfTweetsWithURL++;
            urlDomainParser(urls);
        }

        // If the user mentions section contains user mentions parse them.
        if(userMentions.length() > 0){
            userMentionParser(userMentions);
        }

        // If the tweet was retweeted, increment the counter.
        if(retweetCount > 0){
            numberOfTweetsRetweeted++;
        }
    }

    /**
     * hashtagsArrayParser parses the JSON array that contains the hashtag strings. It checks if these
     * hashtags are already present in the hashtagOccurrences hashmap. If they are then it increments the
     * value by one, otherwise it adds the hashtag to the hashmap with a value of 1.
     * @param hashtagsArray - JSON array that holds the hashtag objects from the tweet.
     */
    private void hashtagsArrayParser(JSONArray hashtagsArray){

        for(int i = 0; i < hashtagsArray.length(); i++){

            // Grab the text field from the hashtag object, this is the actual hashtag minus the # symbol
            String hashtagText = (String) hashtagsArray.getJSONObject(i).get("text");

            // If the hashtagOccurrences does contain the hashtag increment its count by one,
            // otherwise add it to the hashmap.
            if(hashtagOccurrences.containsKey(hashtagText)){
                hashtagOccurrences.replace(hashtagText, hashtagOccurrences.get(hashtagText) + 1);
            }
            else{
                hashtagOccurrences.put(hashtagText, 1);
            }
        }
    }

    /**
     * urlDomainParser attempts to parse the url information contained in the urls section of a tweet. This function
     * attempts to grab the url that is contained within the expanded_url section. It then uses a URI object to call
     * getHost to try and get the domain information. If this hostname is contained within the hostnameOccurrences
     * hashmap then its value is increased by one otherwise a new entry is created with a value of 1.
     * @param urlArray - JSON array containing the urls section of a tweet.
     * @throws URISyntaxException - thrown if we cannot parse out the hostname from the expanded_url value.
     */
    private void urlDomainParser(JSONArray urlArray) throws URISyntaxException {

        for(int i = 0; i < urlArray.length(); i++){

            // Get the expanded url from the urls section.
            String urlText = (String) urlArray.getJSONObject(i).get("expanded_url");

            URI uri = new URI(urlText);
            String hostname = uri.getHost();

            // If the hashtagOccurrences does contain the hashtag increment its count by one,
            // otherwise add it to the hashmap.
            if(hostnameOccurrences.containsKey(hostname)){
                hostnameOccurrences.replace(hostname, hostnameOccurrences.get(hostname) + 1);
            }
            else{
                hostnameOccurrences.put(hostname, 1);
            }


        }
    }

    /**
     * userMentionParser is used to try and parse out the users that mention in a tweet from the user_mentions section.
     * The function grabs the users screen name and checks if the it already exists in the userMentionOccurrences
     * hashmap. If it does then the value is increased by one, otherwise it creates an entry and sets its value to 1.
     * @param userMentions - JSON array that holds the userMentions objects
     */
    private void userMentionParser(JSONArray userMentions){

        for(int i = 0; i < userMentions.length(); i++){

            // Grab the text field from the hashtag object, this is the actual hashtag minus the # symbol
            String userName = (String) userMentions.getJSONObject(i).get("screen_name");

            // If the userMentionOccurrences does contain the user mention then increment its count by one,
            // otherwise add it to the hashmap.
            if(userMentionOccurrences.containsKey(userName)){
                userMentionOccurrences.replace(userName, userMentionOccurrences.get(userName) + 1);
            }
            else{
                userMentionOccurrences.put(userName, 1);
            }
        }

    }

    /**
     * numberOfTweetsWithPhoto checks the extended_entities section of a tweet to see if a photo was attached to the
     * tweet. It does this by looping through the media objects and getting their type. Once it has found a photo
     * it increments a counter used to count the number of tweets with a photo and breaks.
     * @param extended_entities - JSON object that holds the extended_entities section of a tweet.
     */
    private void numberOfTweetsWithPhoto(JSONObject extended_entities){

        // Get the media section the the extended entities section.
        JSONArray media = (JSONArray) extended_entities.get("media");

        for(int i = 0; i < media.length(); i++){

            // Get the media object.
            JSONObject medium = (JSONObject) media.get(i);

            // Get the media type for comparison
            String mediaType = (String) medium.get("type");

            // If the media type was a photo we increment then break so not to count multiple photos.
            if(mediaType.equals("photo")){
                numberOfTweetsWithPhoto++;
                break;
            }
        }
    }

    /**
     * calculateOccurrences is used to calculate the top three occurrences of a field from a given hashmap and either
     * writes them to a file or to the terminal depending on the users preferences. It starts by creating a new hashmap
     * to hold the top three occurrences of the report field and checking the value of the fields in the occurrences
     * hashmap. Once the top three fields have been found it checks to see if the user wants this information written
     * to a file or to the terminal.
     * @param occurrences - Hashmap of a given report field that will be used to determine the top three occurrences
     *                    of that field.
     * @param reportField - String of the report field, used when writing out the report.
     * @param writeToFile - Flag used to determine if the user wants the report written to a file or to the terminal.
     * @param reportWriter - FileWrite object that will be used if the user wants the report written to a file, null
     *                     otherwise.
     * @throws IOException -
     */
    private void calculateOccurrences(HashMap<String, Integer> occurrences, String reportField,
                                      boolean writeToFile, FileWriter reportWriter) throws IOException {

        // Create a hashmap to hold the top three occurrences of the report field
        HashMap<String, Integer> topThreeOccurrences = new HashMap<>();

        for(String key : occurrences.keySet()){

            // Add entries to the hashmap to fill it up to 3
            if(topThreeOccurrences.size() < 3){
                topThreeOccurrences.put(key, occurrences.get(key));
            }else{
                // Loop through the entries in the top three currently and replace then entry if its occurrences
                // are more then the current one.
                for(String topKey : topThreeOccurrences.keySet()){
                    if(topThreeOccurrences.get(topKey) < occurrences.get(key)){
                        topThreeOccurrences.remove(topKey);
                        topThreeOccurrences.put(key, occurrences.get(key));
                        break;
                    }
                }
            }
        }

        if(writeToFile){
            reportWriter.write("Top three most common " +  reportField + " " +
                    "occurrences - report " + reportNumber +"\n");
            for(String occurrencesKey : topThreeOccurrences.keySet()) {
                reportWriter.write("Report: " + reportNumber + " - " + reportField + ": " +
                        occurrencesKey + ", Occurrences: " + topThreeOccurrences.get(occurrencesKey) + "\n");
            }

        }else{
            // Print out the top three report field occurrences.
            System.out.println("Top three most common " +  reportField + " occurrences - report " + reportNumber);
            for(String occurrencesKey : topThreeOccurrences.keySet()){
                System.out.println("Report: "+ reportNumber + " - " + reportField +": " +
                        occurrencesKey + ", Occurrences: " + topThreeOccurrences.get(occurrencesKey));
            }
        }
    }

    /**
     * writeReport is used to write the final report either to the terminal or to a text file depending on the users
     * preference. It starts by calculating the percentage of tweets with a url, photo, and those that were retweeted.
     * To calculate these values the actual tweet count is used, these are the tweets that don't show as deleted. From
     * here depending on the writeToFile flag it will write the required information to a file or to the terminal.
     * @param reportNumber - The number of this report, used when writing the report to distinguish it from other
     *                     reports.
     * @param reportTweetCount - The number of tweets that were captured during this interval.
     * @param actualTweetCount - The number of tweets that contained information and weren't deleted.
     * @param deleteTweets - The number of tweets that were captured that were deleted tweets.
     * @param writeToFile - Flag to indicate if the information should be written to a file or to the terminal.
     * @throws IOException -
     */
    private void writeReport(int reportNumber, int reportTweetCount, float actualTweetCount,
                             int deleteTweets, int writeToFile) throws IOException {

        // Calculate the percentage of tweets with a url in the url section of each tweets entities section
        float percentageOfTweetsWithURL = (numberOfTweetsWithURL/actualTweetCount) * 100;

        // Calculate the percentage of tweets with a photo url in the extended entities section.
        float percentageOfTweetsWithPhoto = (numberOfTweetsWithPhoto/actualTweetCount) * 100;

        // Calculate the percentage of tweets that were retweeted in this interval.
        float percentageOfTweetsRetweeted = (numberOfTweetsRetweeted/actualTweetCount) * 100;

        if(writeToFile == 2){
            // Create a new file object with the report number
            File report = new File("Report " + reportNumber +".txt");

            // Create the new file
            report.createNewFile();

            FileWriter reportWriter = new FileWriter(report,false);

            reportWriter.write("=============== Report "+reportNumber+ " ===============\n");
            reportWriter.write("Total Tweets this interval count for report " + reportNumber + " = "
                    + reportTweetCount +"\n");
            reportWriter.write("Number of actual tweets this interval for report "+ reportNumber + " = "
                    + actualTweetCount + "\n");
            reportWriter.write("Number of deleted tweets this interval for report " + reportNumber+ " = "
                    + deleteTweets + "\n");
            reportWriter.write("\n");

            // Calculating the top three hashtags
            calculateOccurrences(hashtagOccurrences, "Hashtag", true,reportWriter );

            reportWriter.write("\n");

            // Calculating the top three languages
            calculateOccurrences(languageOccurrences, "Language", true, reportWriter);

            reportWriter.write("\n");

            reportWriter.write("Percentage of tweets that contained a url during report " + reportNumber
                    + " interval: " + percentageOfTweetsWithURL + "%\n");
            reportWriter.write("Percentage of tweets that contained a photo url during report "
                    + reportNumber + " interval: " + percentageOfTweetsWithPhoto + "%\n");

            reportWriter.write("\n");

            // Calculate the top three domains
            calculateOccurrences(hostnameOccurrences, "Domain", true, reportWriter);

            reportWriter.write("\n");

            // Calculate the top three user mentions
            calculateOccurrences(userMentionOccurrences, "User Mentions", true, reportWriter);

            reportWriter.write("\n");

            reportWriter.write("Percentage of tweets that were retweets during report " + reportNumber
                    + " interval: " + percentageOfTweetsRetweeted + "%\n");

            reportWriter.flush();
            reportWriter.close();

            System.out.println("Finished writing report "+ reportNumber +"!");

        }else{
            System.out.println("Total Tweets this interval count for report " + reportNumber + " = "
                    + reportTweetCount);
            System.out.println("Number of actual tweets this interval for report "+ reportNumber + " = "
                    + actualTweetCount);
            System.out.println("Number of deleted tweets this interval for report " + reportNumber+ " = "
                    + deleteTweets);

            // Calculating the top three hashtags
            calculateOccurrences(hashtagOccurrences, "Hashtag", false, null );

            // Calculating the top three languages
            calculateOccurrences(languageOccurrences, "Language", false, null);


            System.out.println("Percentage of tweets that contained a url during report " + reportNumber
                    + " interval: " + percentageOfTweetsWithURL + "%");
            System.out.println("Percentage of tweets that contained a photo url during report " + reportNumber
                    + " interval: " + percentageOfTweetsWithPhoto + "%");

            // Calculate the top three domains
            calculateOccurrences(hostnameOccurrences, "Domain", false, null);

            // Calculate the top three user mentions
            calculateOccurrences(userMentionOccurrences, "User Mentions", false, null);

            System.out.println("Percentage of tweets that were retweets during report " + reportNumber
                    + " interval: " + percentageOfTweetsRetweeted + "%");
        }
    }

}
