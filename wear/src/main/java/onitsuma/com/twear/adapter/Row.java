package onitsuma.com.twear.adapter;

import android.app.Fragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by onitsuma on 02/04/15.
 */
public class Row {
    final List<Fragment> columns = new ArrayList<Fragment>();

    public Row(Fragment... fragments) {
        for (Fragment f : fragments) {
            add(f);
        }
    }

    public void add(Fragment f) {
        columns.add(f);
    }

    Fragment getColumn(int i) {
        return columns.get(i);
    }

    public int getColumnCount() {
        return columns.size();
    }
}
