package onitsuma.com.twear.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.twitter.sdk.android.Twitter;

import onitsuma.com.twear.R;
import onitsuma.com.twear.singleton.TwearSingleton;

/**
 * Created by onitsuma on 24/04/15.
 */
public abstract class BaseTwearActivity extends Activity {


    public abstract String getInterstitialUnitId();


    private String TAG = "BaseACtv";
    private InterstitialAd mInterstitialAd;

    protected AdRequest adRequest;
    AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        // adRequestBuilder.addTestDevice("D4B2114AC7CECA6FBA795D0C2AEFA520");
        adRequest = adRequestBuilder.build();
        // Start loading the ad in the background.
        mAdView = (AdView) findViewById(R.id.ad_view);
        mAdView.loadAd(adRequest);

        /*InterstitialAd*/
        mInterstitialAd = new InterstitialAd(this);

        mInterstitialAd.setAdUnitId(getInterstitialUnitId());

        // Create an ad request.


        // Set an AdListener.
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mInterstitialAd.show();
            }

            @Override
            public void onAdClosed() {
            }
        });

        // Start loading the ad now so that it is ready by the time the user is ready to go to
        // the next level.
        mInterstitialAd.loadAd(adRequestBuilder.build());


    }

    protected void logout() {
        TwearSingleton.INSTANCE.setTwSession(null);
        Twitter.getSessionManager().clearActiveSession();
        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
        startActivity(intent);
        finish();
    }
}
