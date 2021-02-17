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

public class ReportGenerator implements Runnable{

    BlockingQueue<String> tweets;
    Future<?> futureStream;

    //HashMap to hold language and occurrences of that language.
    HashMap<String, Integer> languageOccurrences;

    //HashMap to hold hashtags and their occurrences
    HashMap<String, Integer> hashtagOccurrences;

    //Hashmap to hold hostnames and their occurrences
    HashMap<String, Integer> hostnameOccurrences;

    // Hashmap to hold the user mentions and their occurrences.
    HashMap<String, Integer> userMentionOccurrences;

    // This reports number
    int reportNumber;

    // The delay until this report runs.
    int delay;

    // The number of tweets that contain a url.
    float numberOfTweetsWithURL = 0;

    // The number of tweets that contain a photo url.
    float numberOfTweetsWithPhoto = 0;

    // The number of tweets that have been retweeted.
    float numberOfTweetsRetweeted = 0;

    // Flag to indicate we are writing the report to file vs the terminal.
    int writeToFile;

    public ReportGenerator(BlockingQueue<String> rawJSON, Future<?> futureStream, int reportNumber, int delay, int writeToFile){
        this.tweets = rawJSON;
        this.futureStream = futureStream;
        this.languageOccurrences = new HashMap<>();
        this.hashtagOccurrences = new HashMap<>();
        this.hostnameOccurrences = new HashMap<>();
        this.userMentionOccurrences = new HashMap<>();
        this.reportNumber = reportNumber;
        this.delay = delay;
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

                    //Grab the user section to get the time_zone info.
                    // timezoneGrabber(jsonTweet);

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

    private void languageGrabber(JSONObject jsonTweet){

        String language = (String) jsonTweet.get("lang");

        if(languageOccurrences.containsKey(language)){
            languageOccurrences.replace(language, languageOccurrences.get(language) + 1);
        }
        else{
            languageOccurrences.put(language, 1);
        }
    }

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

    private void hashtagsArrayParser(JSONArray hashtagsArray){

        for(int i = 0; i < hashtagsArray.length(); i++){

            // Grab the text field from the hashtag object, this is the actual hashtag minus the # symbol
            String hashtagText = (String) hashtagsArray.getJSONObject(i).get("text");

            // If the hashtagOccurrences does contain the hashtag increment its count by one, otherwise add it to the hashmap.
            if(hashtagOccurrences.containsKey(hashtagText)){
                hashtagOccurrences.replace(hashtagText, hashtagOccurrences.get(hashtagText) + 1);
            }
            else{
                hashtagOccurrences.put(hashtagText, 1);
            }
        }
    }

    private void urlDomainParser(JSONArray urlArray) throws URISyntaxException {

        for(int i = 0; i < urlArray.length(); i++){

            // Get the expanded url from the urls section.
            String urlText = (String) urlArray.getJSONObject(i).get("expanded_url");

            URI uri = new URI(urlText);
            String hostname = uri.getHost();

            // If the hashtagOccurrences does contain the hashtag increment its count by one, otherwise add it to the hashmap.
            if(hostnameOccurrences.containsKey(hostname)){
                hostnameOccurrences.replace(hostname, hostnameOccurrences.get(hostname) + 1);
            }
            else{
                hostnameOccurrences.put(hostname, 1);
            }


        }
    }

    private void userMentionParser(JSONArray userMentions){

        for(int i = 0; i < userMentions.length(); i++){

            // Grab the text field from the hashtag object, this is the actual hashtag minus the # symbol
            String userName = (String) userMentions.getJSONObject(i).get("screen_name");

            // If the userMentionOccurrences does contain the user mention then increment its count by one, otherwise add it to the hashmap.
            if(userMentionOccurrences.containsKey(userName)){
                userMentionOccurrences.replace(userName, userMentionOccurrences.get(userName) + 1);
            }
            else{
                userMentionOccurrences.put(userName, 1);
            }
        }

    }

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

    private void calculateOccurrences(HashMap<String, Integer> occurrences, String reportField, boolean writeToFile, FileWriter reportWriter) throws IOException {

        // Create a hashmap to hold the top three occurrences of the report field
        HashMap<String, Integer> topThreeOccurrences = new HashMap<>();

        for(String key : occurrences.keySet()){

            // Add entries to the hashmap to fill it up to 3
            if(topThreeOccurrences.size() < 3){
                topThreeOccurrences.put(key, occurrences.get(key));
            }else{
                // Loop through the entries in the top three currently and replace then entry if its occurrences are more then the current one.
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
            reportWriter.write("Top three most common " +  reportField + " occurrences - report " + reportNumber +"\n");
            for(String occurrencesKey : topThreeOccurrences.keySet()) {
                reportWriter.write("Report: " + reportNumber + " - " + reportField + ": " + occurrencesKey + ", Occurrences: " + topThreeOccurrences.get(occurrencesKey) + "\n");
            }

        }else{
            // Print out the top three report field occurrences.
            System.out.println("Top three most common " +  reportField + " occurrences - report " + reportNumber);
            for(String occurrencesKey : topThreeOccurrences.keySet()){
                System.out.println("Report: "+ reportNumber + " - " + reportField +": " + occurrencesKey + ", Occurrences: " + topThreeOccurrences.get(occurrencesKey));
            }
        }
    }

    private void writeReport(int reportNumber, int reportTweetCount, float actualTweetCount, int deleteTweets, int writeToFile) throws IOException {

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
            reportWriter.write("Total Tweets this interval count for report " + reportNumber + " = " + reportTweetCount +"\n");
            reportWriter.write("Number of actual tweets this interval for report "+ reportNumber + " = " + actualTweetCount + "\n");
            reportWriter.write("Number of deleted tweets this interval for report " + reportNumber+ " = " + deleteTweets + "\n");
            reportWriter.write("\n");

            // Calculating the top three hashtags
            calculateOccurrences(hashtagOccurrences, "Hashtag", true,reportWriter );

            reportWriter.write("\n");

            // Calculating the top three languages
            calculateOccurrences(languageOccurrences, "Language", true, reportWriter);

            reportWriter.write("\n");

            reportWriter.write("Percentage of tweets that contained a url during report " + reportNumber + " interval: " + percentageOfTweetsWithURL + "%\n");
            reportWriter.write("Percentage of tweets that contained a photo url during report " + reportNumber + " interval: " + percentageOfTweetsWithPhoto + "%\n");

            reportWriter.write("\n");

            // Calculate the top three domains
            calculateOccurrences(hostnameOccurrences, "Domain", true, reportWriter);

            reportWriter.write("\n");

            // Calculate the top three user mentions
            calculateOccurrences(userMentionOccurrences, "User Mentions", true, reportWriter);

            reportWriter.write("\n");

            reportWriter.write("Percentage of tweets that were retweets during report " + reportNumber + " interval: " + percentageOfTweetsRetweeted + "%\n");

            reportWriter.flush();
            reportWriter.close();

            System.out.println("Finished writing report "+ reportNumber +"!");

        }else{
            System.out.println("Total Tweets this interval count for report " + reportNumber + " = " + reportTweetCount);
            System.out.println("Number of actual tweets this interval for report "+ reportNumber + " = " + actualTweetCount);
            System.out.println("Number of deleted tweets this interval for report " + reportNumber+ " = " + deleteTweets);

            // Calculating the top three hashtags
            calculateOccurrences(hashtagOccurrences, "Hashtag", false, null );

            // Calculating the top three languages
            calculateOccurrences(languageOccurrences, "Language", false, null);


            System.out.println("Percentage of tweets that contained a url during report " + reportNumber + " interval: " + percentageOfTweetsWithURL + "%");
            System.out.println("Percentage of tweets that contained a photo url during report " + reportNumber + " interval: " + percentageOfTweetsWithPhoto + "%");

            // Calculate the top three domains
            calculateOccurrences(hostnameOccurrences, "Domain", false, null);

            // Calculate the top three user mentions
            calculateOccurrences(userMentionOccurrences, "User Mentions", false, null);

            System.out.println("Percentage of tweets that were retweets during report " + reportNumber + " interval: " + percentageOfTweetsRetweeted + "%");
        }
    }

}
