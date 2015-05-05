package onitsuma.com.twear.task;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Collection;
import java.util.HashSet;

import onitsuma.com.twear.utils.TwearConstants;

/**
 * Created by csuay on 10/04/15.
 */
public class SendMessageAsyncTask extends AsyncTask<Void, Void, Void> implements TwearConstants {


    private static final String TAG = "SendMsgAsync";

    private GoogleApiClient mGoogleApiClient;

    public SendMessageAsyncTask(GoogleApiClient googleApiClient) {
        this.mGoogleApiClient = googleApiClient;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Collection<String> nodes = getNodes();
        for (String node : nodes) {
            sendNoLoggedMessage(node);
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


    private void sendNoLoggedMessage(String node) {
        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, node, TWEETS_NOT_LOGGED, null).setResultCallback(resultCallback());
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

}
