package onitsuma.com.twear.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by csuay on 10/04/15.
 */
public class BitmapLoadingTask extends AsyncTask<URL, Void, Bitmap> {

    @Override
    protected Bitmap doInBackground(URL... params) {
        if (params[0] == null) {
            return null;
        }
        Bitmap bm;
        try {
            bm = BitmapFactory.decodeStream((InputStream) params[0].getContent());
        } catch (IOException e) {
            Log.e("ERROR", "URL ERROR");
            return null;
        }
        return bm;
    }
}