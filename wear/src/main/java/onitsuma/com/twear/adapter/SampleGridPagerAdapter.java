/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package onitsuma.com.twear.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridPagerAdapter;
import android.util.Log;

import java.util.TreeMap;

import onitsuma.com.twear.R;
import onitsuma.com.twear.model.TweetRow;
import onitsuma.com.twear.singleton.TwearWearableSingleton;
import onitsuma.com.twear.utils.TweetComparator;

/**
 * Constructs fragments as requested by the GridViewPager. For each row a different background is
 * provided.
 * <p/>
 * Always avoid loading resources from the main thread. In this sample, the background images are
 * loaded from an background task and then updated using {@link #notifyRowBackgroundChanged(int)}
 * and {@link #notifyPageBackgroundChanged(int, int)}.
 */
public class SampleGridPagerAdapter extends FragmentGridPagerAdapter {
    private static final int TRANSITION_DURATION_MILLIS = 100;
    String TAG = "gridPageAdapter";
    private final Context mContext;
    private ColorDrawable mDefaultBg;

    private TwearWearableSingleton twSingleton;


    public SampleGridPagerAdapter(Context ctx, FragmentManager fm) {
        super(fm);
        mContext = ctx;
        mDefaultBg = new ColorDrawable(R.color.dark_grey);
        twSingleton = TwearWearableSingleton.INSTANCE;
        if (twSingleton.getRowsMap() == null) {
            twSingleton.setRowsMap(new TreeMap<Long, TweetRow>(new TweetComparator()));
        }
    }


    public void addRow(TweetRow row) {
        twSingleton.addRowsMap(row.getTimestamp(), row);

    }

    public void deleteRow(Long rowId) {
        twSingleton.getRowsMap().remove(rowId);

    }


    LruCache<Integer, Drawable> mRowBackgrounds = new LruCache<Integer, Drawable>(1) {
        @Override
        protected Drawable create(final Integer row) {
            int resid = BG_IMAGES[row % BG_IMAGES.length];
            new DrawableLoadingTask(mContext) {
                @Override
                protected void onPostExecute(Drawable result) {
                    TransitionDrawable background = new TransitionDrawable(new Drawable[]{
                            mDefaultBg,
                            result
                    });
                    mRowBackgrounds.put(row, background);
                    notifyRowBackgroundChanged(row);
                    background.startTransition(TRANSITION_DURATION_MILLIS);
                }
            }.execute(resid);
            return mDefaultBg;
        }
    };


    static final int[] BG_IMAGES = new int[]{
            R.drawable.twbackground,
    };

    /**
     * A convenient container for a row of fragments.
     */


    @Override
    public Fragment getFragment(int row, int col) {
        Row adapterRow = ((TweetRow) twSingleton.getRowsMap().values().toArray()[row]).getTweetRow();
        return adapterRow.getColumn(col);
    }

    @Override
    public Drawable getBackgroundForRow(final int row) {
        return mRowBackgrounds.get(0);
    }

    @Override
    public Drawable getBackgroundForPage(final int row, final int column) {
        return GridPagerAdapter.BACKGROUND_NONE;
    }

    @Override
    public int getRowCount() {
        return twSingleton.getRowsMap().size();
    }

    @Override
    public int getColumnCount(int rowNum) {
        if (rowNum > twSingleton.getRowsMap().values().toArray().length) {
            return 1;
        } else {
            return ((TweetRow) twSingleton.getRowsMap().values().toArray()[rowNum]).getTweetRow().getColumnCount();
        }
    }

    class DrawableLoadingTask extends AsyncTask<Integer, Void, Drawable> {
        private static final String TAG = "Loader";
        private Context context;

        DrawableLoadingTask(Context context) {
            this.context = context;
        }

        @Override
        protected Drawable doInBackground(Integer... params) {
            Log.d(TAG, "Loading asset 0x" + Integer.toHexString(params[0]));
            return context.getResources().getDrawable(params[0]);
        }
    }
}
