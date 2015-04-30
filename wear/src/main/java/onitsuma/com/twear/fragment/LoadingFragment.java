package onitsuma.com.twear.fragment;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import onitsuma.com.twear.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoadingFragment extends Fragment {


    public LoadingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View ret = inflater.inflate(R.layout.fragment_loading, container, false);
        ImageView rotator = (ImageView) ret.findViewById(R.id.circle_image_loader);
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.circle);
        rotator.startAnimation(animation);
        return ret;
    }


}
