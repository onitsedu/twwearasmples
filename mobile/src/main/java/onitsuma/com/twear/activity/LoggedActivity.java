package onitsuma.com.twear.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;

import java.util.TreeMap;

import onitsuma.com.twear.R;
import onitsuma.com.twear.model.Tuit;
import onitsuma.com.twear.service.TwearListenerService;
import onitsuma.com.twear.singleton.TwearSingleton;

public class LoggedActivity extends ActionBarActivity {

    private TextView loggedName;

    private LinearLayout timelineLayout;
    private ScrollView scrollView;

    private TwitterSession twSession;

    private String TAG = "LoggedActivity";

    private SwipeRefreshLayout refreshLayout;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged);
        mContext = this;
        twSession = TwearSingleton.INSTANCE.getTwSession();
        loggedName = (TextView) findViewById(R.id.logged_in_name);
        loggedName.setText("  @" + twSession.getUserName());

        TwitterApiClient mTwClient = new TwitterApiClient(twSession);
        TreeMap<Long, Tuit> tuitsMap = new TreeMap<>(new Tuit());
        TwearSingleton.INSTANCE.setTuitsMap(tuitsMap);
        TwearSingleton.INSTANCE.setTwClient(mTwClient);


        /* Init service*/
        Intent intent = new Intent(this, TwearListenerService.class);
        startService(intent);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_logged, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.settings_logout) {
            TwearSingleton.INSTANCE.setTwSession(null);
            Twitter.getSessionManager().clearActiveSession();
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private static void LOGD(final String tag, String message) {
        Log.d(tag, message);
    }
}
