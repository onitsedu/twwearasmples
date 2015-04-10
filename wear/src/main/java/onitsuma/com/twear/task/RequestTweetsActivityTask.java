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

/**
 * Created by csuay on 09/04/15.
 */
public class RequestTweetsActivityTask extends AsyncTask<Long, Void, Void> {

    private static final String TAG = "ReqTweets";
    private GoogleApiClient mGoogleApiClient;
    private static final String RETRIEVE_TWEETS_PATH = "/twear-retrieve-tweets";

    public RequestTweetsActivityTask(GoogleApiClient googleApiClient) {
        this.mGoogleApiClient = googleApiClient;
    }

    @Override
    protected Void doInBackground(Long... params) {

        DataMap map = new DataMap();
        map.putInt("offset", params[0].intValue());
        map.putLong("maxId", params[1].longValue());

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
