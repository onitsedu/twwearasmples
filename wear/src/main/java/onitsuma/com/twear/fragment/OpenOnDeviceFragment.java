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
import onitsuma.com.twear.task.OpenOnPhoneActivityTask;
import onitsuma.com.twear.utils.TwearConstants;

/**
 * A simple {@link Fragment} subclass.
 */
public class OpenOnDeviceFragment extends Fragment implements TwearConstants, DelayedConfirmationView.DelayedConfirmationListener {

    private final static String TAG = "OpenOnDeviceFragment";
    private DelayedConfirmationView mDelayedConfirmationView;
    private static final int NUM_SECONDS = 2;
    private boolean isAnimating = false;
    private Long mTweetId;

    public OpenOnDeviceFragment() {
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        mTweetId = args.getLong(TWEET_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View ret = inflater.inflate(R.layout.fragment_open_on_device, container, false);
        mDelayedConfirmationView = (DelayedConfirmationView) ret.findViewById(R.id.open_delayed_confirmation);
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
                    mDelayedConfirmationView.setImageResource(R.drawable.common_full_open_on_phone);
                    mDelayedConfirmationView.reset();
                    Intent intent = new Intent(getActivity(), ConfirmationActivity.class);
                    intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                            ConfirmationActivity.FAILURE_ANIMATION);
                    intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                            getString(R.string.cancel_text));
                    startActivity(intent);
                    return;
                }
                isAnimating = true;
                mDelayedConfirmationView.setImageResource(R.drawable.ic_full_cancel);
                mDelayedConfirmationView.setTotalTimeMs(NUM_SECONDS * 1000);
                mDelayedConfirmationView.start();

            }
        });
        mDelayedConfirmationView.setListener(this);
    }

    @Override
    public void onTimerFinished(View v) {
        Log.d(TAG, "Timer Finished - > open tweet with ID ->" + mTweetId);
        mDelayedConfirmationView.setImageResource(R.drawable.common_full_open_on_phone);
        mDelayedConfirmationView.reset();
        Intent intent = new Intent(getActivity(), ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.OPEN_ON_PHONE_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                getString(R.string.open_on_phone_text));
        startActivity(intent);
        new OpenOnPhoneActivityTask(mTweetId).execute();

    }

    @Override
    public void onTimerSelected(View v) {
        v.setPressed(true);
        Log.d(TAG, "Timer Selected");
    }
}
