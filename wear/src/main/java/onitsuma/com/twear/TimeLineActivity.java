package onitsuma.com.twear;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import onitsuma.com.twear.adapter.Row;
import onitsuma.com.twear.adapter.SampleGridPagerAdapter;
import onitsuma.com.twear.fragment.FragmentImageView;
import onitsuma.com.twear.model.TweetRow;
import onitsuma.com.twear.singleton.TwearWearableSingleton;
import onitsuma.com.twear.task.RequestTweetsActivityTask;

public class TimeLineActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener, MessageApi.MessageListener,
        NodeApi.NodeListener {


    private static final String START_ACTIVITY_PATH = "/start-activity-twear";
    private static final String SEND_TWEETS_PATH = "/send-tweets-twear";
    private static final String TAG = "TLWearActivity";

    private GoogleApiClient mGoogleApiClient;
    private SampleGridPagerAdapter pagerAdapter;

    private ProgressBar loading;


    private boolean requestTweets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_line);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        loading = (ProgressBar) findViewById(R.id.tweets_pb);

        final Resources res = getResources();
        final GridViewPager pager = (GridViewPager) findViewById(R.id.pager);
        pager.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                // Adjust page margins:
                //   A little extra horizontal spacing between pages looks a bit
                //   less crowded on a round display.
                final boolean round = insets.isRound();
                int rowMargin = res.getDimensionPixelOffset(R.dimen.page_row_margin);
                int colMargin = res.getDimensionPixelOffset(round ?
                        R.dimen.page_column_margin_round : R.dimen.page_column_margin);
                pager.setPageMargins(rowMargin, colMargin);

                // GridViewPager relies on insets to properly handle
                // layout for round displays. They must be explicitly
                // applied since this listener has taken them over.
                pager.onApplyWindowInsets(insets);
                return insets;
            }
        });


        pagerAdapter = new SampleGridPagerAdapter(this, getFragmentManager());
        pager.setAdapter(pagerAdapter);
        DotsPageIndicator dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
        dotsPageIndicator.setPager(pager);
        requestTweets = true;
        pager.setOnPageChangeListener(new GridViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int row, int column, float rowOffset, float columnOffset, int rowOffsetPixels, int columnOffsetPixels) {


                if (row == 0 && rowOffset < -0.1f) {
                    changeLoadingBarVisibility(View.VISIBLE);
                    LOGD(TAG, "refresh");
                    new RequestTweetsActivityTask(mGoogleApiClient).execute(10L, TwearWearableSingleton.INSTANCE.getRows().get(0).getId());
                } else {
                    changeLoadingBarVisibility(View.INVISIBLE);
                }
                if (row == pagerAdapter.getRowCount() - 1 && rowOffset > 0.1f) {
                    LOGD(TAG, "load more tweets");
                }

            }

            @Override
            public void onPageSelected(int row, int column) {
                LOGD(TAG, "this row = " + row + " Row Count " + pagerAdapter.getRowCount());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                LOGD(TAG, "onPageScrollStateChanged state " + state);
            }
        });

    }


    private Fragment cardFragment(String title, String text) {
        Resources res = this.getResources();
        CardFragment fragment =
                CardFragment.create(title, text);
        // Add some extra bottom margin to leave room for the page indicator
        fragment.setCardMarginBottom(
                res.getDimensionPixelSize(R.dimen.card_margin_bottom));
        return fragment;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        LOGD(TAG, "onConnected(): Successfully connected to Google API client");
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.NodeApi.addListener(mGoogleApiClient, this);
        if (requestTweets) {
            requestTweets = false;
            new RequestTweetsActivityTask(mGoogleApiClient).execute(10L);
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        LOGD(TAG, "onConnectionSuspended(): Connection to Google API client was suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(TAG, "onConnectionFailed(): Failed to connect, with result: " + result);
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        LOGD(TAG, "onDataChanged: " + dataEvents);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        LOGD(TAG, "onMessageReceived: " + messageEvent);
        DataMap map = DataMap.fromByteArray(messageEvent.getData());
        if (messageEvent.getPath().equals(SEND_TWEETS_PATH)) {
            changeLoadingBarVisibility(View.INVISIBLE);
            Row row = null;
            if (map.getByteArray("image") != null) {
                FragmentImageView imageFragment = new FragmentImageView();
                Bundle b = new Bundle();
                b.putByteArray("image", map.getByteArray("image"));
                imageFragment.setArguments(b);
                row = new Row(cardFragment(map.getString("user"), map.getString("text")), imageFragment);
            } else {
                row = new Row(cardFragment(map.getString("user"), map.getString("text")));
            }
            addNewRow(new TweetRow(map.getLong("id"), map.getLong("timestamp"), row));
        }
    }

    private void changeLoadingBarVisibility(final int visibility) {
        Runnable changeStatus = new Runnable() {
            @Override
            public void run() {
                loading.setVisibility(visibility);
            }
        };
        runOnUiThread(changeStatus);
    }

    private void addNewRow(final TweetRow row) {
        Runnable addRowView = new Runnable() {
            @Override
            public void run() {
                pagerAdapter.addRow(row);
                pagerAdapter.notifyDataSetChanged();
            }
        };
        runOnUiThread(addRowView);
    }


    public void onPeerConnected(Node peer) {
        LOGD(TAG, "onPeerConnected: " + peer);
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        LOGD(TAG, "onPeerDisconnected: " + peer);
    }

    public static void LOGD(final String tag, String message) {
//        if (Log.isLoggable(tag, Log.DEBUG)) {
        Log.d(tag, message);
//        }
    }

}
