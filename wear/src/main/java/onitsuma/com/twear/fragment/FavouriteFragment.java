package onitsuma.com.twear.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.CircledImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import onitsuma.com.twear.R;
import onitsuma.com.twear.utils.TwearConstants;

public class FavouriteFragment extends Fragment implements TwearConstants {

    Long mTweetId;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        mTweetId = args.getLong(TWEET_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View ret = inflater.inflate(R.layout.fragment_favourite, container, false);

        CircledImageView loadMore = (CircledImageView) ret.findViewById(R.id.favorite_round);
        loadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(v.getContext(), ConfirmationActivity.class);
                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                        ConfirmationActivity.SUCCESS_ANIMATION);
                intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                        "Tweet Favourited");
                startActivity(intent);

            }
        });
        return ret;
    }


}
