package onitsuma.com.twear.model;

import java.util.Comparator;

import onitsuma.com.twear.adapter.Row;

/**
 * Created by csuay on 10/04/15.
 */
public class TweetRow implements Comparator<TweetRow> {

    private Long timestamp;

    private Long id;

    private Row tweetRow;

    public TweetRow() {

    }

    public TweetRow(Long timestamp, Long id, Row row) {
        this.timestamp = timestamp;
        this.tweetRow = row;
        this.id = id;
    }

    @Override
    public int compare(TweetRow card1, TweetRow card2) {
        return (int) (card1.timestamp - card2.timestamp);
    }

    public Row getTweetRow() {
        return tweetRow;
    }

    public void setTweetRow(Row tweetRow) {
        this.tweetRow = tweetRow;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
