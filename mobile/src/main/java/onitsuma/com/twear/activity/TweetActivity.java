package onitsuma.com.twear.activity;

import android.content.Context;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.LoadCallback;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;

import onitsuma.com.twear.R;
import onitsuma.com.twear.utils.TwearConstants;

public class TweetActivity extends BaseTwearActivity implements TwearConstants {

    private Context mActivityContext;

    private FrameLayout tweetLayout;


    @Override
    public String getInterstitialUnitId() {
        return getString(R.string.tweet_interstitial);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_tweet);
        super.onCreate(savedInstanceState);
        tweetLayout = (FrameLayout) findViewById(R.id.tweet_layout);
        mActivityContext = this;
        Long twId = (Long) getIntent().getExtras().get(TWEET_ID);

        TweetUtils.loadTweet(twId, new LoadCallback<Tweet>() {
            @Override
            public void success(Tweet tweet) {
                TweetView tv = new TweetView(mActivityContext, tweet);
                tweetLayout.addView(tv);
            }

            @Override
            public void failure(TwitterException e) {

            }
        });
    }


}
