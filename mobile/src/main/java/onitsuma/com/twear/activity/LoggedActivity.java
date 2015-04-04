package onitsuma.com.twear.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;

import java.util.List;

import onitsuma.com.twear.R;
import onitsuma.com.twear.singleton.TwearSingleton;

public class LoggedActivity extends ActionBarActivity implements DataApi.DataListener,
        MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private TextView loggedName;

    private LinearLayout timelineLayout;

    private TwitterSession twSession;

    private GoogleApiClient mGoogleApiClient;

    private final String TWEET_AUTHOR = "author";
    private final String TWEET_TEXT = "body";
    private final String TWEET_IMAGE = "image";

    private final String MESSAGE_GIMMIE_TWEETS = "gimmie";
    private final String SEND_TWEETS_PATH = "/tweets";

    private TwitterApiClient mTwClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged);
        twSession = TwearSingleton.INSTANCE.getTwSession();
        loggedName = (TextView) findViewById(R.id.logged_in_name);
        loggedName.setText("  " + twSession.getUserName());


        timelineLayout = (LinearLayout) findViewById(R.id.timelineLayout);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mTwClient = new TwitterApiClient(twSession);

    }

    private DataMap tweetToDataMap(Tweet tweet) {
        DataMap map = new DataMap();
        map.putString(TWEET_AUTHOR, tweet.user.name);
        map.putString(TWEET_TEXT, tweet.text);
        //TODO load image async
        //map.putString(TWEET_IMAGE,tweet.user.profileImageUrl);
        return map;
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

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("LoggedActivity", "connection suspended");

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("LoggedActivity", "message received");
        if (messageEvent.getPath().equals(MESSAGE_GIMMIE_TWEETS)) {
            Callback<List<Tweet>> callbackTweets = new Callback<List<Tweet>>() {
                @Override
                public void success(Result<List<Tweet>> listResult) {
                    TextView tweet = null;
                    //List<DataMap> dataTweets = new ArrayList<>();
                    for (Tweet tuit : listResult.data) {
                        //   dataTweets.add(tweetToDataMap(tuit));

                        //TODO send all list not one by one
                        sendMessageToWearable(SEND_TWEETS_PATH, tweetToDataMap(tuit).toByteArray());
                    }

                }

                @Override
                public void failure(TwitterException e) {

                }
            };
            mTwClient.getStatusesService().homeTimeline(50, null, null, null, null, null, null, callbackTweets);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("LoggedActivity", "connection failed");

    }

    private void sendMessageToWearable(final String path, final byte[] data) {
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(
                new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                        for (Node node : nodes.getNodes()) {
                            Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, data);
                        }
                    }
                });
    }
}
