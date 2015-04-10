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

/**
 * Created by csuay on 10/04/15.
 */
public class SendMessageAsyncTask extends AsyncTask<Void, Void, Void> {

    private static final String SEND_TWEETS_PATH = "/send-tweets-twear";

    private static final String TAG = "SendMsgAsync";
    private List<Tuit> mTuits;
    private GoogleApiClient mGoogleApiClient;

    public SendMessageAsyncTask(GoogleApiClient googleApiClient, List<Tuit> tuits) {
        this.mTuits = tuits;
        this.mGoogleApiClient = googleApiClient;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Collection<String> nodes = getNodes();
        for (String node : nodes) {
            for (Tuit tuit : mTuits) {
                sendTuitsMessage(node, tuit);
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
                mGoogleApiClient, node, SEND_TWEETS_PATH, data.toByteArray()).setResultCallback(
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

    private DataMap parseTuit(Tuit tuit) {
        DataMap data = new DataMap();
        data.putString("text", tuit.getText());
        data.putString("user", tuit.getUserName());
        data.putLong("timestamp", tuit.getTimestamp());
        data.putLong("id", tuit.getId());
        data.putByteArray("image", tuit.getImage());
        return data;
    }
}
