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

import com.crashlytics.android.Crashlytics;
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
import java.util.TreeMap;

import io.fabric.sdk.android.Fabric;
import onitsuma.com.twear.adapter.Row;
import onitsuma.com.twear.adapter.SampleGridPagerAdapter;
import onitsuma.com.twear.fragment.FavouriteFragment;
import onitsuma.com.twear.fragment.FragmentImageView;
import onitsuma.com.twear.fragment.LoadingFragment;
import onitsuma.com.twear.fragment.OpenOnDeviceFragment;
import onitsuma.com.twear.fragment.RetweetFragment;
import onitsuma.com.twear.model.TweetRow;
import onitsuma.com.twear.singleton.TwearWearableSingleton;
import onitsuma.com.twear.task.RequestTweetsActivityTask;
import onitsuma.com.twear.utils.TwearConstants;
import onitsuma.com.twear.utils.TweetComparator;

public class TimeLineActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener, MessageApi.MessageListener,
        NodeApi.NodeListener, TwearConstants {


    private static final String TAG = "TLWearActivity";

    private GoogleApiClient mGoogleApiClient;
    private SampleGridPagerAdapter pagerAdapter;


    private SwipeRefreshLayout refreshLayout;


    private boolean requestTweets;

    private boolean requestMoreTimeline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_time_line);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        TwearWearableSingleton.INSTANCE.setGoogleApiClient(mGoogleApiClient);


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
        requestMoreTimeline = true;
        pager.setOnPageChangeListener(new GridViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int row, int column, float rowOffset, float columnOffset, int rowOffsetPixels, int columnOffsetPixels) {

                if (row == pagerAdapter.getRowCount() - 1 && rowOffset > 0.1f && requestMoreTimeline) {
                    Log.d(TAG, "load more tweets");
                    Long maxId = null;
                    if (TwearWearableSingleton.INSTANCE.getRowsMap() != null && TwearWearableSingleton.INSTANCE.getRowsMap().size() > 0) {
                        maxId = TwearWearableSingleton.INSTANCE.getRowsMap().lastEntry().getValue().getId();
                    }
                    new RequestTweetsActivityTask(TWEETS_REQUEST_SIZE, null, maxId).execute();
                    requestMoreTimeline = false;
                }

            }

            @Override
            public void onPageSelected(int row, int column) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });


        requestTweets = true;

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "refresh");
                Long sinceId = null;
                if (TwearWearableSingleton.INSTANCE.getRowsMap() != null && TwearWearableSingleton.INSTANCE.getRowsMap().size() > 0) {
                    sinceId = TwearWearableSingleton.INSTANCE.getRowsMap().firstEntry().getValue().getId();
                }
                new RequestTweetsActivityTask(TWEETS_REQUEST_SIZE, sinceId, null).execute();
            }
        });

        if (TwearWearableSingleton.INSTANCE.getRowsMap() == null || TwearWearableSingleton.INSTANCE.getRowsMap().size() == 0) {
            addNewRow(new TweetRow(LOADER_ID_VALUE, 0L, new Row(new LoadingFragment())));
        }
        mGoogleApiClient.connect();
    }


    private Fragment cardFragment(String title, String text) {
        return cardFragment(title, text, R.drawable.tw__ic_logo_blue);
    }

    private Fragment cardFragment(String title, String text, int rid) {
        Resources res = this.getResources();
        CardFragment fragment =
                CardFragment.create(title, text, rid);
        fragment.setCardMarginBottom(res.getDimensionPixelSize(R.dimen.card_margin_bottom));
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
            new RequestTweetsActivityTask(TWEETS_REQUEST_SIZE, null, null).execute();
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

        LOGD(TAG, "onDataChanged: " + dataEvents.getCount());
        if (TwearWearableSingleton.INSTANCE.getRowsMap().size() > 0 && TwearWearableSingleton.INSTANCE.getRowsMap().containsKey(LOADER_ID_VALUE)) {
            Log.d(TAG, "Removing Loader...");
            removeRow(LOADER_ID_VALUE);

        }
        if (TwearWearableSingleton.INSTANCE.getRowsMap().containsKey(ERROR_ID_VALUE)) {
            Log.d(TAG, "Removing error card...");
            reinitRows();
        }

        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        for (DataEvent event : events) {
            final Uri uri = event.getDataItem().getUri();
            final String path = uri != null ? uri.getPath() : null;
            if (TWEETS_DATA_ITEMS.equals(path)) {
                requestMoreTimeline = true;
                final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                Log.d(TAG, " " + map.getLong("id"));
                dismissRefreshLoadingLayout();
                Row row;

                Bundle idBundle = new Bundle();
                idBundle.putLong(TWEET_ID, map.getLong("id"));
                Fragment favFragment = new FavouriteFragment();
                favFragment.setArguments(idBundle);
                Fragment rtwFragment = new RetweetFragment();
                rtwFragment.setArguments(idBundle);
                Fragment openOnDeviceFragment = new OpenOnDeviceFragment();
                openOnDeviceFragment.setArguments(idBundle);

                if (map.getByteArray(TWEET_IMAGE) != null) {
                    FragmentImageView imageFragment = new FragmentImageView();
                    Bundle b = new Bundle();
                    b.putByteArray(TWEET_IMAGE, map.getByteArray(TWEET_IMAGE));
                    imageFragment.setArguments(b);
                    //   row = new Row(cardFragment("" + map.getLong(TWEET_TIMESTAMP), map.getString(TWEET_DATE)), imageFragment, favFragment, rtwFragment, openOnDeviceFragment);
                    row = new Row(cardFragment(map.getString(TWEET_USERNAME), map.getString(TWEET_TEXT)), imageFragment, favFragment, rtwFragment, openOnDeviceFragment);
                } else {
                    // row = new Row(cardFragment("" + map.getLong(TWEET_TIMESTAMP), map.getString(TWEET_DATE)), favFragment, rtwFragment, openOnDeviceFragment);
                    row = new Row(cardFragment(map.getString(TWEET_USERNAME), map.getString(TWEET_TEXT)), favFragment, rtwFragment, openOnDeviceFragment);
                }
                addNewRow(new TweetRow(map.getLong(TWEET_TIMESTAMP), map.getLong(TWEET_ID), row));

            } else if (TWEETS_DATA_ITEMS_EMPTY.equals(path)) {
                dismissRefreshLoadingLayout();

            }
        }

    }


    private void dismissRefreshLoadingLayout() {
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
                Log.d(TAG, "Adding new Row --Total Rows " + TwearWearableSingleton.INSTANCE.getRowsMap().size());
                pagerAdapter.addRow(row);
                pagerAdapter.notifyDataSetChanged();
                Log.d(TAG, "New Row Added " + TwearWearableSingleton.INSTANCE.getRowsMap().size());

            }
        };
        runOnUiThread(addRowView);
    }

    private void removeRow(final Long Id) {
        Runnable addRowView = new Runnable() {
            @Override
            public void run() {
                pagerAdapter.deleteRow(Id);
                pagerAdapter.notifyDataSetChanged();
            }
        };
        runOnUiThread(addRowView);
    }

    private void reinitRows() {
        Runnable reinitRow = new Runnable() {
            @Override
            public void run() {
                TwearWearableSingleton.INSTANCE.setRowsMap(new TreeMap<Long, TweetRow>(new TweetComparator()));
                pagerAdapter.notifyDataSetChanged();
            }
        };
        runOnUiThread(reinitRow);
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

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "messageReceived " + messageEvent);
        if (TWEETS_NOT_LOGGED.equals(messageEvent.getPath())) {
            dismissRefreshLoadingLayout();
            reinitRows();
            addNewRow(new TweetRow(ERROR_ID_VALUE, 0L, new Row(cardFragment(getString(R.string.not_logged_title), getString(R.string.not_logged_text), R.drawable.error))));

        }
    }
}
