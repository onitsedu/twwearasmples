package onitsuma.com.twear.singleton;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.TreeMap;

import onitsuma.com.twear.model.TweetRow;
import onitsuma.com.twear.utils.TweetComparator;


/**
 * Created by csuay on 10/04/15.
 */
public enum TwearWearableSingleton {
    INSTANCE;

//    private List<TweetRow> rows;

    private TreeMap<Long, TweetRow> rowsMap;

    private GoogleApiClient googleApiClient;

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
    }


    public TreeMap<Long, TweetRow> getRowsMap() {
        return rowsMap;
    }

    public void setRowsMap(TreeMap<Long, TweetRow> rowsMap) {
        this.rowsMap = rowsMap;
    }

    public void addRowsMap(Long id, TweetRow row) {
        if (this.rowsMap == null) {
            this.rowsMap = new TreeMap<>(new TweetComparator());
        }
        this.rowsMap.put(id, row);
    }
}
