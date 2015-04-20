package onitsuma.com.twear.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterSession;

import onitsuma.com.twear.R;
import onitsuma.com.twear.service.TwearListenerService;
import onitsuma.com.twear.singleton.TwearSingleton;

public class LoggedActivity extends ActionBarActivity {

    private TextView loggedName;
    private ImageButton twButton;

    private LinearLayout timelineLayout;

    private TwitterSession twSession;

    private String TAG = "LoggedActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged);
        twSession = TwearSingleton.INSTANCE.getTwSession();
        loggedName = (TextView) findViewById(R.id.logged_in_name);
        loggedName.setText("  " + twSession.getUserName());
        twButton = (ImageButton) findViewById(R.id.tw_button);
        twButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTweet(twSession.getUserName());
            }
        });
        


        /* Init service*/
        Intent intent = new Intent(this, TwearListenerService.class);
        startService(intent);

    }


    private void openTweet(String userName) {
        Intent intent = null;
        try {
            // get the Twitter app if possible
            this.getPackageManager().getPackageInfo("com.twitter.android", 0);
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/" + userName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } catch (Exception e) {
            // no Twitter app, revert to browser
        }
        this.startActivity(intent);
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
        if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, message);
        }
    }
}
