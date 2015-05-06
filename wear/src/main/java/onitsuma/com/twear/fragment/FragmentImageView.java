package onitsuma.com.twear.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import onitsuma.com.twear.R;

public class FragmentImageView extends Fragment {

    private final static String TAG = "FragmentImgVw";
    ImageView imageView;
    byte[] mImage;

    public FragmentImageView() {
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        mImage = args.getByteArray("image");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "creatingView");
        View view = inflater.inflate(R.layout.fragment_fragment_image_view, null);
        imageView = (ImageView) view.findViewById(R.id.fragment_imageview);

        new BitmapLoadingTask() {

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
            }
        }.execute(mImage);
        return view;
    }

    class BitmapLoadingTask extends AsyncTask<byte[], Void, Bitmap> {
        private static final String TAG = "Loader";

        @Override
        protected Bitmap doInBackground(byte[]... params) {
            Bitmap bm = BitmapFactory.decodeByteArray(params[0], 0, params[0].length);
            return bm;

        }
    }


}
