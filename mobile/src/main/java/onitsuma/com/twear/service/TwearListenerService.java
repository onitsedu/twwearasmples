/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package onitsuma.com.twear.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import onitsuma.com.twear.activity.TweetActivity;
import onitsuma.com.twear.model.Tuit;
import onitsuma.com.twear.singleton.TwearSingleton;
import onitsuma.com.twear.task.BitmapLoadingTask;
import onitsuma.com.twear.utils.TwearConstants;
import onitsuma.com.twear.utils.TwearUtils;

/**
 * Listens to Messages from the Wearable node.
 */
public class TwearListenerService extends IntentService implements DataApi.DataListener,
        MessageApi.MessageListener, NodeApi.NodeListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, TwearConstants {


    private static final String TAG = "LoggedActivity";


    private GoogleApiClient mGoogleApiClient;
    private TwitterSession mTwSession;
    private TwitterApiClient mTwClient;

    public TwearListenerService() {
        super("TwearListenerService");
    }


    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.NodeApi.addListener(mGoogleApiClient, this);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

    }


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        LOGD(TAG, "onMessageReceived() A message from watch was received:" + messageEvent
                .getRequestId() + " " + messageEvent.getPath());
        DataMap map = DataMap.fromByteArray(messageEvent.getData());
        if (messageEvent.getPath().equals(RETRIEVE_TWEETS_PATH)) {
            Long maxId = map.getLong(MESSAGE_MAX_ID) != 0 ? map.getLong(MESSAGE_MAX_ID) : null;
            sendTweetsToWearable(maxId);
        } else if (messageEvent.getPath().equals(FAVOURITE_TWEET_PATH)) {
            Long twId = map.getLong(TWEET_ID);
            if (twId != null) {
                favouriteTweet(twId);
            }
        } else if (messageEvent.getPath().equals(RETWEET_PATH)) {
            Long twId = map.getLong(TWEET_ID);
            if (twId != null) {
                retweetTweet(twId);
            }
        } else if (messageEvent.getPath().equals(OPEN_ON_DEVICE_PATH)) {
            Long twId = map.getLong(TWEET_ID);
            if (twId != null) {
                Intent intent = new Intent(getBaseContext(), TweetActivity.class);
                intent.putExtra(TWEET_ID, twId);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplication().startActivity(intent);
            }
        }


    }

    @Override
    public void onPeerConnected(Node node) {

    }

    @Override
    public void onPeerDisconnected(Node node) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        Wearable.NodeApi.removeListener(mGoogleApiClient, this);
    }


    private void sendTweetsToWearable(Long maxId) {
        Callback<List<Tweet>> twCallback = new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> listResult) {
                List<Tuit> tuits = new ArrayList<>();
                if (listResult.data.size() > 0) {
                    for (Tweet tweet : listResult.data) {
                        final PutDataMapRequest putRequest = PutDataMapRequest.create(TWEETS_DATA_ITEMS);
                        DataMap map = putRequest.getDataMap();
                        Tuit tuit = parseTuit(tweet);
                        map.putString(TWEET_TEXT, tuit.getText());
                        map.putString(TWEET_USERNAME, tuit.getUserName());
                        map.putLong(TWEET_TIMESTAMP, tuit.getTimestamp());
                        map.putLong(TWEET_ID, tuit.getId());
                        map.putByteArray(TWEET_IMAGE, tuit.getImage());
                        Wearable.DataApi.putDataItem(mGoogleApiClient, putRequest.asPutDataRequest());
                    }
                } else {
                    final PutDataMapRequest putRequest = PutDataMapRequest.create(TWEETS_DATA_ITEMS_EMPTY);
                    Wearable.DataApi.putDataItem(mGoogleApiClient, putRequest.asPutDataRequest());
                }
            }

            @Override
            public void failure(TwitterException e) {
            }
        };
        mTwClient.getStatusesService().homeTimeline(10, maxId, null, null, null, null, null, twCallback);
    }


    private void favouriteTweet(Long idTweet) {
        Callback<Tweet> twCallback = new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                LOGD(TAG, "favorited? " + result.data.favorited);
            }

            @Override
            public void failure(TwitterException e) {
                //FAILING FAVORITE
            }
        };

        mTwClient.getFavoriteService().create(idTweet, true, twCallback);
    }

    private void retweetTweet(Long idTweet) {
        Callback<Tweet> twCallback = new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                LOGD(TAG, "retweeted? " + result.data.favorited);
            }

            @Override
            public void failure(TwitterException e) {
                //FAILING FAVORITE
            }
        };
        mTwClient.getStatusesService().retweet(idTweet, true, twCallback);
    }

    private Tuit parseTuit(final Tweet tweet) {

        String imageUrlString = null;
        if (tweet.entities.media != null && tweet.entities.media.size() > 0) {
            imageUrlString = tweet.entities.media.get(0).mediaUrl;
        }
        URL imageUrl = null;
        try {
            imageUrl = imageUrlString != null ? new URL(imageUrlString) : null;
        } catch (MalformedURLException e) {
            //TODO handle exception
            e.printStackTrace();
        }
        final Tuit tuit = new Tuit();
        new BitmapLoadingTask() {

            @Override
            protected void onPostExecute(byte[] image) {
                tuit.setText(tweet.text);
                tuit.setUserName(tweet.user.name);
                tuit.setTimestamp(TwearUtils.parseTwitterDate(tweet.createdAt).getTime());
                tuit.setId(tweet.id);
                tuit.setImage(image);

            }
        }.execute(imageUrl);
        return tuit;
    }

    private static void LOGD(final String tag, String message) {
        Log.d(tag, message);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        LOGD(TAG, "onHandleIntent");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mTwSession = TwearSingleton.INSTANCE.getTwSession();
        mTwClient = new TwitterApiClient(mTwSession);
        mGoogleApiClient.connect();

    }
}