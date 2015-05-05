package onitsuma.com.twear.utils;

/**
 * Created by csuay on 13/04/15.
 */
public interface TwearConstants {


    Long LOADER_ID_VALUE = 0L;
    Long ERROR_ID_VALUE = 1L;


    Integer TWEETS_REQUEST_SIZE = 30;

    String TWEET_IMAGE = "image";
    String TWEET_TEXT = "text";
    String TWEET_USERNAME = "user";
    String TWEET_ID = "id";
    String TWEET_TIMESTAMP = "timestamp";
    String TWEET_DATE = "date";


    String START_ACTIVITY_PATH = "/start-activity-twear";
    String RETRIEVE_TWEETS_PATH = "/twear-retrieve-tweets";
    String SEND_TWEETS_PATH = "/send-tweets-twear";
    String SEND_NO_TWEETS_PATH = "/send-no-tweets-twear";
    String FAVOURITE_TWEET_PATH = "/favourite_tweet-twear";
    String RETWEET_PATH = "/retweet-twear";
    String OPEN_ON_DEVICE_PATH = "/open-on-device-twear";

    String TWEETS_DATA_ITEMS = "/tweet-data-item-twear";
    String TWEETS_DATA_ITEMS_EMPTY = "/tweet-data-item-empty-twear";
    String TWEETS_NOT_LOGGED = "/tweet-not-logged-empty-twear";


    String MESSAGE_OFFSET = "offset";
    String MESSAGE_MAX_ID = "maxId";
    String MESSAGE_SINCE_ID = "sinceId";
    String MESSAGE_TWEET_ID = "tweetId";
}
