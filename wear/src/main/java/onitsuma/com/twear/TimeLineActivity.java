package onitsuma.com.twear;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

import onitsuma.com.twear.adapter.Row;
import onitsuma.com.twear.adapter.SampleGridPagerAdapter;
import onitsuma.com.twear.fragment.FavouriteFragment;
import onitsuma.com.twear.fragment.FragmentImageView;
import onitsuma.com.twear.fragment.OpenOnDeviceFragment;
import onitsuma.com.twear.fragment.RetweetFragment;
import onitsuma.com.twear.model.TweetRow;
import onitsuma.com.twear.singleton.TwearWearableSingleton;
import onitsuma.com.twear.task.RequestTweetsActivityTask;
import onitsuma.com.twear.utils.TwearConstants;

public class TimeLineActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener, MessageApi.MessageListener,
        NodeApi.NodeListener, TwearConstants {


    private static final String TAG = "TLWearActivity";

    private GoogleApiClient mGoogleApiClient;
    private SampleGridPagerAdapter pagerAdapter;

    private ProgressBar loading;

    private SwipeRefreshLayout refreshLayout;


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

        TwearWearableSingleton.INSTANCE.setGoogleApiClient(mGoogleApiClient);
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

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LOGD(TAG, "refresh");
                Long maxId = null;
                if (TwearWearableSingleton.INSTANCE.getRowsMap() != null && TwearWearableSingleton.INSTANCE.getRowsMap().size() > 0) {
                    maxId = TwearWearableSingleton.INSTANCE.getRowsMap().firstEntry().getKey();
                }
                new RequestTweetsActivityTask(10, maxId, null).execute();
            }
        });
        mGoogleApiClient.connect();


    }


    private Fragment cardFragment(String title, String text) {
        Resources res = this.getResources();
        CardFragment fragment =
                CardFragment.create(title, text, R.drawable.tw__ic_logo_blue);
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
            new RequestTweetsActivityTask(10, null, null).execute();
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
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        for (DataEvent event : events) {
            final Uri uri = event.getDataItem().getUri();
            final String path = uri != null ? uri.getPath() : null;
            if (TWEETS_DATA_ITEMS.equals(path)) {

                final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();

                dismissRefreshLoadingLayout(View.INVISIBLE);
                Row row;

                Bundle idBundle = new Bundle();
                idBundle.putLong(TWEET_ID, map.getLong("id"));
                Fragment favFragment = new FavouriteFragment();
                favFragment.setArguments(idBundle);
                Fragment rtwFragment = new RetweetFragment();
                rtwFragment.setArguments(idBundle);
                Fragment openOnDeviceFragment = new OpenOnDeviceFragment();
                openOnDeviceFragment.setArguments(idBundle);

                if (map.getByteArray("image") != null) {
                    FragmentImageView imageFragment = new FragmentImageView();
                    Bundle b = new Bundle();
                    b.putByteArray("image", map.getByteArray("image"));
                    imageFragment.setArguments(b);
                    row = new Row(cardFragment(map.getString("user"), map.getString("text")), imageFragment, favFragment, rtwFragment, openOnDeviceFragment);
                } else {
                    row = new Row(cardFragment(map.getString("user"), map.getString("text")), favFragment, rtwFragment, openOnDeviceFragment);
                }
                addNewRow(new TweetRow(map.getLong("id"), map.getLong("timestamp"), row));

            } else if (TWEETS_DATA_ITEMS_EMPTY.equals(path)) {
                dismissRefreshLoadingLayout(View.INVISIBLE);

            }
        }

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        LOGD(TAG, "onMessageReceived: " + messageEvent);
        DataMap map = DataMap.fromByteArray(messageEvent.getData());
        if (messageEvent.getPath().equals(SEND_TWEETS_PATH)) {
            dismissRefreshLoadingLayout(View.INVISIBLE);
            Row row;

            Bundle idBundle = new Bundle();
            idBundle.putLong(TWEET_ID, map.getLong("id"));
            Fragment favFragment = new FavouriteFragment();
            favFragment.setArguments(idBundle);
            Fragment rtwFragment = new RetweetFragment();
            rtwFragment.setArguments(idBundle);
            Fragment openOnDeviceFragment = new OpenOnDeviceFragment();
            openOnDeviceFragment.setArguments(idBundle);

            if (map.getByteArray("image") != null) {
                FragmentImageView imageFragment = new FragmentImageView();
                Bundle b = new Bundle();
                b.putByteArray("image", map.getByteArray("image"));
                imageFragment.setArguments(b);
                row = new Row(cardFragment(map.getString("user"), map.getString("text")), imageFragment, favFragment, rtwFragment, openOnDeviceFragment);
            } else {
                row = new Row(cardFragment(map.getString("user"), map.getString("text")), favFragment, rtwFragment, openOnDeviceFragment);
            }
            addNewRow(new TweetRow(map.getLong("id"), map.getLong("timestamp"), row));

        } else if (messageEvent.getPath().equals(SEND_NO_TWEETS_PATH)) {
            dismissRefreshLoadingLayout(View.INVISIBLE);

        }

    }

    private void dismissRefreshLoadingLayout(final int visibility) {
        Runnable changeStatus = new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(false);
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
        Log.d(tag, message);
    }


}
