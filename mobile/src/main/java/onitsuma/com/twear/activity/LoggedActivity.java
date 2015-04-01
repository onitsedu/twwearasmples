package onitsuma.com.twear.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;

import java.util.List;

import onitsuma.com.twear.R;
import onitsuma.com.twear.singleton.TwearSingleton;

public class LoggedActivity extends ActionBarActivity {

    private TextView loggedName;

    private LinearLayout timelineLayout;

    private TwitterSession twSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged);
        twSession = TwearSingleton.INSTANCE.getTwSession();
        loggedName = (TextView) findViewById(R.id.logged_in_name);
        loggedName.setText("  " + twSession.getUserName());

        timelineLayout = (LinearLayout) findViewById(R.id.timelineLayout);

        Callback<List<Tweet>> callbackTweets = new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> listResult) {
                TextView tweet = null;
                for (Tweet tuit : listResult.data) {
                    tweet = new TextView(getApplicationContext());
                    tweet.setText(tuit.text + "\n");
                    tweet.setTextColor(Color.parseColor("#000000"));
                    timelineLayout.addView(tweet);
                }
            }

            @Override
            public void failure(TwitterException e) {

            }
        };
        TwitterApiClient twClient = new TwitterApiClient(twSession);
        twClient.getStatusesService().homeTimeline(50, null, null, null, null, null, null, callbackTweets);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
