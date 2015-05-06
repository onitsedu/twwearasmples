package onitsuma.com.twear.task;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import onitsuma.com.twear.singleton.TwearWearableSingleton;
import onitsuma.com.twear.utils.TwearConstants;

/**
 * Created by csuay on 09/04/15.
 */
public class OpenOnPhoneActivityTask extends AsyncTask<Long, Void, Void> implements TwearConstants {

    private static final String TAG = "ReqTweets";
    private GoogleApiClient mGoogleApiClient;

    private Long mTweetId;

    public OpenOnPhoneActivityTask(Long tweetId) {
        this.mGoogleApiClient = TwearWearableSingleton.INSTANCE.getGoogleApiClient();
        this.mTweetId = tweetId;
    }

    @Override
    protected Void doInBackground(Long... params) {

        DataMap map = new DataMap();
        map.putLong(TWEET_ID, mTweetId);
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

        for (Node node : nodes.getNodes()) {
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, node.getId(), OPEN_ON_DEVICE_PATH, map.toByteArray()).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Log.e(TAG, "Failed to send message with status code: "
                                        + sendMessageResult.getStatus().getStatusCode());
                            }
                        }
                    }
            );
        }

        return null;
    }
}
