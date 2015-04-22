package onitsuma.com.twear.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.LoadCallback;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;

import java.util.List;
import java.util.TreeMap;

import onitsuma.com.twear.R;
import onitsuma.com.twear.model.Tuit;
import onitsuma.com.twear.service.TwearListenerService;
import onitsuma.com.twear.singleton.TwearSingleton;
import onitsuma.com.twear.utils.TwearUtils;

public class LoggedActivity extends ActionBarActivity {

    private TextView loggedName;

    private LinearLayout timelineLayout;
    private ScrollView scrollView;

    private TwitterSession twSession;

    private String TAG = "LoggedActivity";

    private SwipeRefreshLayout refreshLayout;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged);
        mContext = this;
        twSession = TwearSingleton.INSTANCE.getTwSession();
        loggedName = (TextView) findViewById(R.id.logged_in_name);
        loggedName.setText("  " + twSession.getUserName());

        TwitterApiClient mTwClient = new TwitterApiClient(twSession);
        TreeMap<Long, Tuit> tuitsMap = new TreeMap<>(new Tuit());
        TwearSingleton.INSTANCE.setTuitsMap(tuitsMap);
        TwearSingleton.INSTANCE.setTwClient(mTwClient);
        timelineLayout = (LinearLayout) findViewById(R.id.timeline_layout);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
//        scrollView.setAdapter();

//        ArrayAdapter<Tuit> adapter = new ArrayAdapter<Tuit>(this, R.layout.);

        mTwClient.getStatusesService().homeTimeline(10, null, null, null, null, null, null, tweetCallback());

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LOGD(TAG, "refresh");
                Long firstID = TwearSingleton.INSTANCE.getTuitsMap().descendingKeySet().first();
                TwearSingleton.INSTANCE.getTwClient().getStatusesService().homeTimeline(10, firstID, null, null, null, null, null, tweetCallback());

            }
        });


        /* Init service*/
        Intent intent = new Intent(this, TwearListenerService.class);
        startService(intent);

    }

    private Callback<List<Tweet>> tweetCallback() {

        return new Callback<List<Tweet>>() {

            @Override
            public void success(Result<List<Tweet>> listResult) {
                Button buttonTweet;


                for (Tweet tweet : listResult.data) {
                    TwearSingleton.INSTANCE.getTuitsMap().put(tweet.id, parseTuit(tweet));
                    TweetView tv = new TweetView(mContext, tweet);
                    tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final TweetView tv = (TweetView) v;
                            TweetUtils.loadTweet(tv.getTweetId(), new LoadCallback<Tweet>() {
                                @Override
                                public void success(Tweet tweet) {
                                    tv.setTweet(tweet);
                                }

                                @Override
                                public void failure(TwitterException e) {

                                }
                            });
                        }
                    });

                    timelineLayout.addView(tv);
                }

//                for (Long tuitId : TwearSingleton.INSTANCE.getTuitsMap().keySet()) {
//                    TwearSingleton.INSTANCE.getTuitsMap().get(tuitId);
//                }

                if (refreshLayout.isRefreshing()) {
                    refreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void failure(TwitterException e) {

            }
        };
    }

    private Tuit parseTuit(Tweet tweet) {
        Tuit tuit = new Tuit();
        tuit.setText(tweet.text);
        tuit.setUserName(tweet.user.name);
        tuit.setTimestamp(TwearUtils.parseTwitterDate(tweet.createdAt).getTime());
        tuit.setId(tweet.id);
        return tuit;
    }


    private void openTweet(Long tweetId) {

        TweetUtils.loadTweet(tweetId, new LoadCallback<Tweet>() {
            @Override
            public void success(Tweet tweet) {
                TweetView tv = new TweetView(mContext, tweet);
                timelineLayout.addView(tv);
            }

            @Override
            public void failure(TwitterException e) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_logged, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.settings_logout) {
            TwearSingleton.INSTANCE.setTwSession(null);
            Twitter.getSessionManager().clearActiveSession();
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private static void LOGD(final String tag, String message) {
        Log.d(tag, message);
    }
}
