package onitsuma.com.twear.model;

import onitsuma.com.twear.adapter.Row;

/**
 * Created by csuay on 10/04/15.
 */
public class TweetRow {


    private Long id;

    private Long timestamp;

    private Row tweetRow;

    public TweetRow() {

    }

    public TweetRow(Long timestamp, Long id, Row row) {
        this.tweetRow = row;
        this.id = id;
        this.timestamp = timestamp;
    }

    public Row getTweetRow() {
        return tweetRow;
    }


    public void setTweetRow(Row tweetRow) {
        this.tweetRow = tweetRow;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
