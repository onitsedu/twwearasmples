package onitsuma.com.twear.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.TreeMap;

import onitsuma.com.twear.R;
import onitsuma.com.twear.model.Tuit;
import onitsuma.com.twear.service.TwearListenerService;
import onitsuma.com.twear.singleton.TwearSingleton;
import onitsuma.com.twear.task.BitmapLoadingTask;
import onitsuma.com.twear.twitter.CustomTwitterApiClient;

public class LoggedActivity extends BaseActionBarActivity {

    private TextView loggedName;

    private LinearLayout timelineLayout;
    private ScrollView scrollView;

    private TwitterSession twSession;

    private String TAG = "LoggedActivity";

    private SwipeRefreshLayout refreshLayout;

    private ImageView backGroundView;
    private ImageView userImageView;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged);
        mContext = this;
        twSession = TwearSingleton.INSTANCE.getTwSession();
        loggedName = (TextView) findViewById(R.id.logged_in_name);
        loggedName.setText("  @" + twSession.getUserName());

        CustomTwitterApiClient mTwClient = new CustomTwitterApiClient(twSession);
        TreeMap<Long, Tuit> tuitsMap = new TreeMap<>(new Tuit());
        TwearSingleton.INSTANCE.setTuitsMap(tuitsMap);
        TwearSingleton.INSTANCE.setTwClient(mTwClient);
        backGroundView = (ImageView) findViewById(R.id.backGroundView);
        userImageView = (ImageView) findViewById(R.id.userImage);


        mTwClient.getUserProfileService().show(twSession.getUserId(), new Callback<User>() {
            @Override
            public void success(Result<User> userResult) {
                URL backgroundUrl = null;
                try {
                    backgroundUrl = new URL(userResult.data.profileBannerUrl);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                new BitmapLoadingTask() {

                    @Override
                    protected void onPostExecute(final Bitmap image) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                backGroundView.setImageBitmap(image);
                            }
                        });


                    }

                }.execute(backgroundUrl);

                URL userUrl = null;
                try {
                    userUrl = new URL(userResult.data.profileImageUrl);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                new BitmapLoadingTask() {

                    @Override
                    protected void onPostExecute(final Bitmap image) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                userImageView.setImageBitmap(image);
                            }
                        });


                    }

                }.execute(userUrl);

            }

            @Override
            public void failure(TwitterException e) {

            }
        });


        /* Init service*/

        Intent intent = new Intent(this, TwearListenerService.class);
        startService(intent);

    }


    protected ServiceConnection mServerConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
        }
    };


    private static void LOGD(final String tag, String message) {
        Log.d(tag, message);
    }
}
