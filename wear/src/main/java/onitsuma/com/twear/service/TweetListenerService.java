package onitsuma.com.twear.service;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by onitsuma on 04/04/15.
 */
public class TweetListenerService extends WearableListenerService {
    private final String SEND_TWEETS_PATH = "/tweets";


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        if (SEND_TWEETS_PATH.equals(path)) {
            //TODO load tweet
        }

    }
}
