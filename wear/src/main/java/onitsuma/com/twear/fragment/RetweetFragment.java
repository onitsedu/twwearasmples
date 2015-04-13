package onitsuma.com.twear.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.wearable.view.CircledImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import onitsuma.com.twear.R;
import onitsuma.com.twear.utils.TwearConstants;

public class RetweetFragment extends Fragment implements TwearConstants {
    Long mTweetId;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        mTweetId = args.getLong(TWEET_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View ret = inflater.inflate(R.layout.fragment_retweet, container, false);

        CircledImageView loadMore = (CircledImageView) ret.findViewById(R.id.retweet_round);
        loadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return ret;
    }


}
