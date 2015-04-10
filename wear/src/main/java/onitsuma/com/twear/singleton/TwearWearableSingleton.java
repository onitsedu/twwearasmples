package onitsuma.com.twear.singleton;

import java.util.ArrayList;
import java.util.List;

import onitsuma.com.twear.model.TweetRow;


/**
 * Created by csuay on 10/04/15.
 */
public enum TwearWearableSingleton {
    INSTANCE;

    List<TweetRow> rows;

    public List<TweetRow> getRows() {
        return rows;
    }

    public void setRows(List<TweetRow> rows) {
        this.rows = rows;
    }

    public void addRow(TweetRow row) {
        if (this.rows == null) {
            this.rows = new ArrayList<>();
        }
        this.rows.add(row);
    }
}
