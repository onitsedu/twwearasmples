package onitsuma.com.twear;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.view.View;
import android.view.WindowInsets;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import onitsuma.com.twear.adapter.Row;
import onitsuma.com.twear.adapter.SampleGridPagerAdapter;

public class TimeLineActivity extends Activity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_line);
        final Resources res = getResources();
        final GridViewPager pager = (GridViewPager) findViewById(R.id.pager);
        pager.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                // Adjust page margins:
                //   A little extra horizontal spacing between pages looks a bit
                //   less crowded on a round display.
                final boolean round = insets.isRound();
                int rowMargin = res.getDimensionPixelOffset(R.dimen.page_row_margin);
                int colMargin = res.getDimensionPixelOffset(round ?
                        R.dimen.page_column_margin_round : R.dimen.page_column_margin);
                pager.setPageMargins(rowMargin, colMargin);

                // GridViewPager relies on insets to properly handle
                // layout for round displays. They must be explicitly
                // applied since this listener has taken them over.
                pager.onApplyWindowInsets(insets);
                return insets;
            }
        });

        List<Row> mRows = new ArrayList<Row>();
        fakeRows(mRows);

        pager.setAdapter(new SampleGridPagerAdapter(this, getFragmentManager(), mRows));
        DotsPageIndicator dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
        dotsPageIndicator.setPager(pager);
    }

    private void fakeRows(List<Row> mRows) {
        mRows.add(new Row(cardFragment(R.string.welcome_title, R.string.welcome_text)));
        mRows.add(new Row(cardFragment(R.string.about_title, R.string.about_text)));
        mRows.add(new Row(
                cardFragment(R.string.cards_title, R.string.cards_text),
                cardFragment(R.string.expansion_title, R.string.expansion_text)));
        mRows.add(new Row(
                cardFragment(R.string.backgrounds_title, R.string.backgrounds_text),
                cardFragment(R.string.columns_title, R.string.columns_text)));
        mRows.add(new Row(cardFragment(R.string.dismiss_title, R.string.dismiss_text)));
    }

    private Fragment cardFragment(int titleRes, int textRes) {
        Resources res = this.getResources();
        CardFragment fragment =
                CardFragment.create(res.getText(titleRes), res.getText(textRes));
        // Add some extra bottom margin to leave room for the page indicator
        fragment.setCardMarginBottom(
                res.getDimensionPixelSize(R.dimen.card_margin_bottom));
        return fragment;
    }

}
