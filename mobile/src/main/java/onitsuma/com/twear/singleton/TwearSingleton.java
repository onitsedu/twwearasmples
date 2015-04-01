package onitsuma.com.twear.singleton;

import com.twitter.sdk.android.core.TwitterSession;

/**
 * Created by onitsuma on 01/04/15.
 */
public enum TwearSingleton {

    INSTANCE;

    private TwitterSession twSession;

    public TwitterSession getTwSession() {
        return twSession;
    }

    public void setTwSession(TwitterSession twSession) {
        this.twSession = twSession;
    }
}
