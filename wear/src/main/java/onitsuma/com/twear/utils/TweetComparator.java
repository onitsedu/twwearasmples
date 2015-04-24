package onitsuma.com.twear.utils;

import java.util.Comparator;

/**
 * Created by csuay on 24/04/15.
 */
public class TweetComparator implements Comparator<Long> {
    @Override
    public int compare(Long lhs, Long rhs) {
        return (int) (rhs - lhs);
    }
}
