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

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
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
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import onitsuma.com.twear.activity.SignInActivity;
import onitsuma.com.twear.activity.TweetActivity;
import onitsuma.com.twear.singleton.TwearSingleton;
import onitsuma.com.twear.task.BytearrayLoadingTask;
import onitsuma.com.twear.utils.TwearConstants;
import onitsuma.com.twear.utils.TwearUtils;

/**
 * Listens to Messages from the Wearable node.
 */
public class TwearListenerService extends Service implements DataApi.DataListener,
        MessageApi.MessageListener, NodeApi.NodeListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, TwearConstants {


    private static final String TAG = "LoggedActivity";


    private GoogleApiClient mGoogleApiClient;
    private TwitterSession mTwSession;
    private TwitterApiClient mTwClient;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOGD(TAG, "on Start command");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        TwitterAuthConfig authConfig = new TwitterAuthConfig(SignInActivity.TWITTER_KEY, SignInActivity.TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig), new Crashlytics());
        isTwConnected();
        mTwSession = TwearSingleton.INSTANCE.getTwSession();
        mTwClient = new TwitterApiClient(mTwSession);
        mGoogleApiClient.connect();
        return START_STICKY;
    }

    private void isTwConnected() {
        TwitterSession twSession = Twitter.getSessionManager().getActiveSession();
        if (twSession != null) {
            TwearSingleton.INSTANCE.setTwSession(twSession);
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        LOGD(TAG, "on Bind");
        return null;
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
            Long sinceId = map.getLong(MESSAGE_SINCE_ID) != 0 ? map.getLong(MESSAGE_SINCE_ID) : null;
            sendTweetsToWearable(maxId, sinceId);
        } else if (messageEvent.getPath().equals(FAVOURITE_TWEET_PATH)) {
            Long twId = map.getLong(TWEET_ID);
            favouriteTweet(twId);
        } else if (messageEvent.getPath().equals(RETWEET_PATH)) {
            Long twId = map.getLong(TWEET_ID);
            retweetTweet(twId);
        } else if (messageEvent.getPath().equals(OPEN_ON_DEVICE_PATH)) {
            Long twId = map.getLong(TWEET_ID);
            Intent intent = new Intent(getBaseContext(), TweetActivity.class);
            intent.putExtra(TWEET_ID, twId);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplication().startActivity(intent);
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


    private void sendTweetsToWearable(Long maxId, Long sinceId) {
        Callback<List<Tweet>> twCallback = new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> listResult) {
                if (listResult.data.size() > 0) {
                    for (Tweet tweet : listResult.data) {
                        syncTweet(tweet);
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
        mTwClient.getStatusesService().homeTimeline(10, sinceId, maxId, null, null, null, null, twCallback);
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

    private void syncTweet(final Tweet tweet) {

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
        new BytearrayLoadingTask() {

            @Override
            protected void onPostExecute(byte[] image) {
                final PutDataMapRequest putRequest = PutDataMapRequest.create(TWEETS_DATA_ITEMS);
                DataMap map = putRequest.getDataMap();
                map.putString(TWEET_TEXT, tweet.text);
                map.putString(TWEET_USERNAME, tweet.user.name);
                map.putLong(TWEET_TIMESTAMP, TwearUtils.parseTwitterDate(tweet.createdAt).getTime());
                map.putLong(TWEET_ID, tweet.id);
                map.putByteArray(TWEET_IMAGE, image);
                Wearable.DataApi.putDataItem(mGoogleApiClient, putRequest.asPutDataRequest());

            }
        }.execute(imageUrl);
    }

    private static void LOGD(final String tag, String message) {
        Log.d(tag, message);
    }


}