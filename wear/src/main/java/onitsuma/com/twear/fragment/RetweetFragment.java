package onitsuma.com.twear.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import onitsuma.com.twear.R;
import onitsuma.com.twear.task.RetweetActivityTask;
import onitsuma.com.twear.utils.TwearConstants;

public class RetweetFragment extends Fragment implements TwearConstants, DelayedConfirmationView.DelayedConfirmationListener {
    private Long mTweetId;
    private final static String TAG = "RetweetFragment";
    private DelayedConfirmationView mDelayedConfirmationView;
    private static final int NUM_SECONDS = 2;
    private boolean isAnimating = false;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        mTweetId = args.getLong(TWEET_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View ret = inflater.inflate(R.layout.fragment_retweet, container, false);

        mDelayedConfirmationView = (DelayedConfirmationView) ret.findViewById(R.id.rtw_delayed_confirmation);
        return ret;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Timer Clicked");
                if (isAnimating) {
                    isAnimating = false;
                    mDelayedConfirmationView.setImageResource(R.drawable.tw_rt_ed);
                    mDelayedConfirmationView.reset();
                    return;
                }
                isAnimating = true;
                mDelayedConfirmationView.setImageResource(R.drawable.ic_full_cancel);
                mDelayedConfirmationView.setTotalTimeMs(NUM_SECONDS * 1000);
                mDelayedConfirmationView.start();                    Intent intent = new Intent(getActivity(), ConfirmationActivity.class);
                    intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                            ConfirmationActivity.FAILURE_ANIMATION);
                    intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                            getString(R.string.cancel_text));
                    startActivity(intent);

            }
        });
        mDelayedConfirmationView.setListener(this);
    }

    public void onTimerSelected(View v) {
        v.setPressed(true);
        Log.d(TAG, "Timer Selected");
        // Prevent onTimerFinished from being heard.
        //  ((DelayedConfirmationView) v).setListener(null);
    }

    @Override
    public void onTimerFinished(View v) {
        Log.d(TAG, "Timer Finished - > favorite tweet with ID ->" + mTweetId);
        mDelayedConfirmationView.setImageResource(R.drawable.tw_rt_ed);
        mDelayedConfirmationView.reset();
        Intent intent = new Intent(getActivity(), ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                getString(R.string.retweeted_text));
        startActivity(intent);

        new RetweetActivityTask(mTweetId).execute();
    }

}
