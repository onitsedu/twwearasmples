package onitsuma.com.twear.model;

import onitsuma.com.twear.adapter.Row;

/**
 * Created by csuay on 10/04/15.
 */
public class TweetRow {


    private Long id;

    private Row tweetRow;

    public TweetRow() {

    }

    public TweetRow(Long id, Row row) {
        this.tweetRow = row;
        this.id = id;
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
}
