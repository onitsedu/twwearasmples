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
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridPagerAdapter;
import android.util.Log;

import java.util.Collections;

import onitsuma.com.twear.R;
import onitsuma.com.twear.model.TweetRow;
import onitsuma.com.twear.singleton.TwearWearableSingleton;

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

    private final Context mContext;
    private ColorDrawable mDefaultBg;

    private ColorDrawable mClearBg;
    private TwearWearableSingleton twSingleton;


    public SampleGridPagerAdapter(Context ctx, FragmentManager fm) {
        super(fm);
        mContext = ctx;
        mDefaultBg = new ColorDrawable(R.color.dark_grey);
        mClearBg = new ColorDrawable(android.R.color.transparent);
        twSingleton = TwearWearableSingleton.INSTANCE;
    }


    public void addRow(TweetRow row) {
        twSingleton.addRow(row);
        Collections.sort(twSingleton.getRows(), new TweetRow());
    }

    /*LruCache<Integer, Drawable> mRowBackgrounds = new LruCache<Integer, Drawable>(3) {
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
    };*/


    LruCache<Integer, Drawable> mRowBackgrounds = new LruCache<Integer, Drawable>(3) {
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

    LruCache<Point, Drawable> mPageBackgrounds = new LruCache<Point, Drawable>(3) {
        @Override
        protected Drawable create(final Point page) {
            // place bugdroid as the background at row 2, column 1
            if (page.y == 2 && page.x == 1) {
                int resid = R.drawable.bugdroid_large;
                new DrawableLoadingTask(mContext) {
                    @Override
                    protected void onPostExecute(Drawable result) {
                        TransitionDrawable background = new TransitionDrawable(new Drawable[]{
                                mClearBg,
                                result
                        });
                        mPageBackgrounds.put(page, background);
                        notifyPageBackgroundChanged(page.y, page.x);
                        background.startTransition(TRANSITION_DURATION_MILLIS);
                    }
                }.execute(resid);
            }
            return GridPagerAdapter.BACKGROUND_NONE;
        }
    };


    static final int[] BG_IMAGES = new int[]{
            R.drawable.debug_background_1,
            R.drawable.debug_background_2,
            R.drawable.debug_background_3,
            R.drawable.debug_background_4,
            R.drawable.debug_background_5
    };

    /**
     * A convenient container for a row of fragments.
     */


    @Override
    public Fragment getFragment(int row, int col) {
        Row adapterRow = twSingleton.getRows().get(row).getTweetRow();
        return adapterRow.getColumn(col);
    }

    @Override
    public Drawable getBackgroundForRow(final int row) {
        return mRowBackgrounds.get(row % 4);
    }

    @Override
    public Drawable getBackgroundForPage(final int row, final int column) {
        return mPageBackgrounds.get(new Point(column, row));
    }

    @Override
    public int getRowCount() {
        return twSingleton.getRows().size();
    }

    @Override
    public int getColumnCount(int rowNum) {
        return twSingleton.getRows().get(rowNum).getTweetRow().getColumnCount();
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
