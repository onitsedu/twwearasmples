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
public class RequestTweetsActivityTask extends AsyncTask<Long, Void, Void> implements TwearConstants {

    private static final String TAG = "ReqTweets";
    private GoogleApiClient mGoogleApiClient;

    private Integer mOffset;
    private Long mMaxId;
    private Long mSinceId;

    public RequestTweetsActivityTask(Integer offset, Long maxId, Long sinceId) {
        this.mGoogleApiClient = TwearWearableSingleton.INSTANCE.getGoogleApiClient();
        this.mOffset = offset;
        this.mMaxId = maxId;
        this.mSinceId = sinceId;
    }

    @Override
    protected Void doInBackground(Long... params) {
        Log.d(TAG, "Requesting messages maxId" + mMaxId + " SinceId " + mSinceId);
        DataMap map = new DataMap();
        map.putInt(MESSAGE_OFFSET, mOffset);
        if (mMaxId != null) {
            map.putLong(MESSAGE_MAX_ID, mMaxId);
        }
        if (mSinceId != null) {
            map.putLong(MESSAGE_SINCE_ID, mSinceId);
        }

        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

        for (Node node : nodes.getNodes()) {
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, node.getId(), RETRIEVE_TWEETS_PATH, map.toByteArray()).setResultCallback(
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
