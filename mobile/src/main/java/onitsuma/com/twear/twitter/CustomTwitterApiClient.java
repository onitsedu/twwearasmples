package onitsuma.com.twear.twitter;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;

import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by onitsuma on 28/04/15.
 */
public class CustomTwitterApiClient extends TwitterApiClient {


    public CustomTwitterApiClient(TwitterSession session) {
        super(session);
    }

    /**
     * Provide CustomService with defined endpoints
     */
    public UserProfileService getUserProfileService() {
        return getService(UserProfileService.class);
    }

    public interface UserProfileService {
        @GET("/1.1/users/show.json")
        void show(@Query("user_id") long id, Callback<User> cb);
    }
}
