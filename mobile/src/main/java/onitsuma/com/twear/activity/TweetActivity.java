package onitsuma.com.twear.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.LoadCallback;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;

import onitsuma.com.twear.R;
import onitsuma.com.twear.utils.TwearConstants;

public class TweetActivity extends ActionBarActivity implements TwearConstants {

    private Context mActivityContext;

    private FrameLayout tweetLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tweet, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
