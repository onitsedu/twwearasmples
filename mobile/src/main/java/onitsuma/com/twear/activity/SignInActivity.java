package onitsuma.com.twear.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.tweetui.TweetUi;

import io.fabric.sdk.android.Fabric;
import onitsuma.com.twear.R;
import onitsuma.com.twear.singleton.TwearSingleton;


public class SignInActivity extends Activity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    public static final String TWITTER_KEY = "CrUzsu1kcZiL6NZstxRNRwHSv";
    public static final String TWITTER_SECRET = "04aMc1DwPNTlPFMMr4cKjWKuzYJHUzKuFkhGyOcCuJp3gklfnT";
    private TwitterLoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig), new Crashlytics(), new TweetUi());

        isTwConnected();

        setContentView(R.layout.activity_sign_in);
        loginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {

                TwearSingleton.INSTANCE.setTwSession(result.data);

                Intent intent = new Intent(getApplicationContext(), LoggedActivity.class);
                startActivity(intent);

            }

            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
            }
        });
    }

    @Override
    protected void onResume() {
        isTwConnected();
        super.onResume();
    }

    private void isTwConnected() {
        TwitterSession twSession = Twitter.getSessionManager().getActiveSession();
        if (twSession != null) {
            TwearSingleton.INSTANCE.setTwSession(twSession);
            Intent intent = new Intent(getApplicationContext(), LoggedActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
    }


}
