# TwitterStatReporter

TwitterStatReport makes use of the twitter4j library to gather a sample of the stream and collect various statistics about those tweets. It currently reports back the number of tweets that were captured in a given time interval, the number of tweets that contained an URL, the number of tweets that contained a photo, the top three hashtags that appeared during the given interval, the top three most common language occurrences, the top three most common domain occurrences, the top three user mention occurrences, and the percentage of tweets that were retweeted during a given interval.

Unfortunately, I was unable to grab acquire the timezone information from the tweets. The fields containing the UTC offset and timezone information have been either depreciated or nullified as explained in these links. It looks like this choice was done to meet GDPR standards.

https://developer.twitter.com/en/docs/twitter-api/v1/data-dictionary/object-model/user

https://twittercommunity.com/t/utc-offset-and-time-zone-data-missing/106734

## Getting Started

This project was built using Maven to compile and build a jar file that can be used to run the program, this readme is written has Maven and Java already installed on their system. If you do not have either installed on your system you can find documentation at these locations to install them:
 - Java download and documentation - https://www.oracle.com/java/technologies/javase-downloads.html
 - How to install Maven: https://maven.apache.org/install.html
 - How to Set up and run Maven to compile a Java project: https://maven.apache.org/install.html
Start by cloning this repository to your machine, once cloned navigate to the twitterStatReporter project from your terminal, and enter the following commands to build and run this project. It is important to note that before you do this you will need to update Twitter. config file found in the directory with your credentials as explained in the next section.
- Compile the program with: 
```
mvn compile
```
- Package the project into a jar with: 
```
mvn package
```
- Execute the program with: 
```
- java -jar target/twitterStatReporter-0.1.0.jar
```

### Prerequisites

Before running this program you will need to generate the following tokens from the Twitter developer page: https://developer.twitter.com/en
Here you will need a Twitter account to log in and create a project to generate the required tokens. This program can accept this information via user interaction from the terminal or by editing the config file titled twitter.config. You will need to following tokens from Twitter: Consumer API Key, Consumer Secret Key, Authentication Access token, and Authentication Secret token.

### Running the program

You need to have the following information ready before you execute the program:

```
TotalRunTime = Total runtime you would like the program to gather tweets for, this value is in seconds.
IntervalRunTime = The interval length that you would like to gather tweets for, this value is in seconds and must be greater than or equal to totalRunTime
WriteFlag = The value used here will determine if you would like to receive reports from the terminal (1) or have them written to a file (2)
AccessToken = Your access token generated from Twitter.
AccessSecret = Your access secret token generated from Twitter.
ConsumerKey = Your consumer key generated from Twitter.
ConsumerSecret = Your consumer secret key generated from Twitter.
```

This information can be provided in the twitter.config file. If it is not provided or it is not valid the program will ask for the user to input the required information. Here is an example set up for the config file:

```
TotalRunTime = 15
IntervalRunTime = 5
WriteFlag = 2
AccessToken = xxxx
AccessSecret = xxxx
ConsumerKey = xxxx
ConsumerSecret = xxx
```
Once you have this information or you have updated the config file you can use the following commands to compile and run the jar file:
- Compile the program with: 
```
mvn compile
```
- Package the project into a jar with: 
```
mvn package
```
- Execute the program with: 
```
- java -jar target/twitterStatReporter-0.1.0.jar
```


## Built With

* [twitter4j](http://twitter4j.org/en/index.html) - Library to interact with the Twitter api
* [Maven](https://maven.apache.org/) - Dependency Management
* [JSON-java](https://github.com/stleary/JSON-java) - Used parse the Twitter JSON objects

## Authors

* **Eric Turnbull** - [Sniwolf](https://github.com/Sniwolf)

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
