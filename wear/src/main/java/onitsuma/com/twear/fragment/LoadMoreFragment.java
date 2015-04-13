package onitsuma.com.twear.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import onitsuma.com.twear.R;
import onitsuma.com.twear.singleton.TwearWearableSingleton;
import onitsuma.com.twear.task.RequestTweetsActivityTask;

public class LoadMoreFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View ret = inflater.inflate(R.layout.fragment_load_more, container, false);
        Button loadMore = (Button) ret.findViewById(R.id.load_more_button);
        loadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long sinceId = null;
                if (TwearWearableSingleton.INSTANCE.getRows() != null && TwearWearableSingleton.INSTANCE.getRows().size() > 0) {
                    sinceId = TwearWearableSingleton.INSTANCE.getRows().get(TwearWearableSingleton.INSTANCE.getRows().size() - 2).getId();
                }
                new RequestTweetsActivityTask(10, null, sinceId);
            }
        });
        return ret;

    }


}
