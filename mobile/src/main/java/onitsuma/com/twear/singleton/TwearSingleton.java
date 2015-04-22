package onitsuma.com.twear.singleton;

import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;

import java.util.TreeMap;

import onitsuma.com.twear.model.Tuit;

/**
 * Created by onitsuma on 01/04/15.
 */
public enum TwearSingleton {

    INSTANCE;

    private TwitterSession twSession;

    private TwitterApiClient twClient;

    private TreeMap<Long, Tuit> tuitsMap;

    public TwitterApiClient getTwClient() {
        return twClient;
    }

    public void setTwClient(TwitterApiClient twClient) {
        this.twClient = twClient;
    }

    public TreeMap<Long, Tuit> getTuitsMap() {
        return tuitsMap;
    }

    public void setTuitsMap(TreeMap<Long, Tuit> tuitsMap) {
        this.tuitsMap = tuitsMap;
    }

    public TwitterSession getTwSession() {
        return twSession;
    }

    public void setTwSession(TwitterSession twSession) {
        this.twSession = twSession;
    }
}
