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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import onitsuma.com.twear.model.Tuit;
import onitsuma.com.twear.utils.TwearConstants;

/**
 * Created by csuay on 10/04/15.
 */
public class SendMessageAsyncTask extends AsyncTask<Void, Void, Void> implements TwearConstants {


    private static final String TAG = "SendMsgAsync";

    private List<Tuit> mTuits;
    private GoogleApiClient mGoogleApiClient;
    private String mAction;

    public SendMessageAsyncTask(GoogleApiClient googleApiClient, String action, List<Tuit> tuits) {
        this.mTuits = tuits;
        this.mGoogleApiClient = googleApiClient;
        this.mAction = action;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Collection<String> nodes = getNodes();
        for (String node : nodes) {
            if (mAction.equals(ACTION_SEND_TWEETS)) {
                Log.d(TAG,"Sending tweets");
                for (Tuit tuit : mTuits) {
                    sendTuitsMessage(node, tuit);
                }
            } else if (mAction.equals(ACTION_NO_TWEETS)) {
                Log.d(TAG,"Sending No tweets Message");
                sendNoTuitsMessage(node);
            }
        }
        return null;
    }

    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }
        return results;
    }

    private void sendTuitsMessage(String node, Tuit tuit) {
        DataMap data = parseTuit(tuit);
        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, node, SEND_TWEETS_PATH, data.toByteArray()).setResultCallback(resultCallback());
    }

    private void sendNoTuitsMessage(String node) {
        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, node, SEND_NO_TWEETS_PATH, null).setResultCallback(resultCallback());
    }

    private ResultCallback<MessageApi.SendMessageResult> resultCallback() {
        return new ResultCallback<MessageApi.SendMessageResult>() {
            @Override
            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                if (!sendMessageResult.getStatus().isSuccess()) {
                    Log.e(TAG, "Failed to send message with status code: "
                            + sendMessageResult.getStatus().getStatusCode());
                }
            }
        };
    }

    private DataMap parseTuit(Tuit tuit) {
        DataMap data = new DataMap();
        data.putString(TWEET_TEXT, tuit.getText());
        data.putString(TWEET_USERNAME, tuit.getUserName());
        data.putLong(TWEET_TIMESTAMP, tuit.getTimestamp());
        data.putLong(TWEET_ID, tuit.getId());
        data.putByteArray(TWEET_IMAGE, tuit.getImage());
        return data;
    }
}
