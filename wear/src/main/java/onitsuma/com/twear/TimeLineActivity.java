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

public class TimeLineActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener, MessageApi.MessageListener,
        NodeApi.NodeListener, TwearConstants {


    private static final String TAG = "TLWearActivity";

    private GoogleApiClient mGoogleApiClient;
    private SampleGridPagerAdapter pagerAdapter;


    private SwipeRefreshLayout refreshLayout;


    private boolean requestTweets;

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

        pager.setOnPageChangeListener(new GridViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int row, int column, float rowOffset, float columnOffset, int rowOffsetPixels, int columnOffsetPixels) {

                Log.d(TAG, "Page Scrolled " + pagerAdapter.getRowCount() + " row " + row);
                if (row == pagerAdapter.getRowCount() - 3 && rowOffset > 0.1f) {
                    Log.d(TAG, "load more tweets");
                    Long maxId = null;
                    if (TwearWearableSingleton.INSTANCE.getRowsMap() != null && TwearWearableSingleton.INSTANCE.getRowsMap().size() > 0) {
                        maxId = TwearWearableSingleton.INSTANCE.getRowsMap().lastEntry().getKey();
                    }
                    new RequestTweetsActivityTask(10, null, maxId).execute();
                }

            }

            @Override
            public void onPageSelected(int row, int column) {
                Log.d(TAG, "this row = " + row + " Row Count " + pagerAdapter.getRowCount());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d(TAG, "onPageScrollStateChanged state " + state);
            }
        });

        DotsPageIndicator dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
        dotsPageIndicator.setPager(pager);

        requestTweets = true;

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "refresh");
                Long sinceId = null;
                if (TwearWearableSingleton.INSTANCE.getRowsMap() != null && TwearWearableSingleton.INSTANCE.getRowsMap().size() > 0) {
                    sinceId = TwearWearableSingleton.INSTANCE.getRowsMap().firstEntry().getKey();
                }
                new RequestTweetsActivityTask(10, sinceId, null).execute();
            }
        });

        if (TwearWearableSingleton.INSTANCE.getRowsMap() == null || TwearWearableSingleton.INSTANCE.getRowsMap().size() == 0) {
            addNewRow(new TweetRow(LOADER_ID_VALUE, new Row(new LoadingFragment())));
        }
        mGoogleApiClient.connect();
    }


    private Fragment cardFragment(String title, String text) {
        Resources res = this.getResources();
        CardFragment fragment =
                CardFragment.create(title, text, R.drawable.tw__ic_logo_blue);
        // Add some extra bottom margin to leave room for the page indicator
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

        LOGD(TAG, "onDataChanged: " + dataEvents.getCount());
        if (TwearWearableSingleton.INSTANCE.getRowsMap().size() > 0 && TwearWearableSingleton.INSTANCE.getRowsMap().containsKey(LOADER_ID_VALUE)) {
            Log.d(TAG, "Removing Loader...");
            removeRow(LOADER_ID_VALUE);
        }

        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        for (DataEvent event : events) {
            final Uri uri = event.getDataItem().getUri();
            final String path = uri != null ? uri.getPath() : null;
            if (TWEETS_DATA_ITEMS.equals(path)) {

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

                if (map.getByteArray("image") != null) {
                    FragmentImageView imageFragment = new FragmentImageView();
                    Bundle b = new Bundle();
                    b.putByteArray("image", map.getByteArray("image"));
                    imageFragment.setArguments(b);
                    row = new Row(cardFragment(map.getString("user"), map.getString("text")), imageFragment, favFragment, rtwFragment, openOnDeviceFragment);
                } else {
                    row = new Row(cardFragment(map.getString("user"), map.getString("text")), favFragment, rtwFragment, openOnDeviceFragment);
                }
                addNewRow(new TweetRow(map.getLong("id"), row));

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
                pagerAdapter.addRow(row);
                pagerAdapter.notifyDataSetChanged();
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
        Log.d(TAG, "messageReceived");
    }
}
