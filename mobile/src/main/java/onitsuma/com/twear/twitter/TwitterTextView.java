package onitsuma.com.twear.twitter;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import onitsuma.com.twear.utils.TypeFaceProvider;

/**
 * Created by csuay on 29/04/15.
 */
public class TwitterTextView extends TextView {

    public TwitterTextView(final Context context) {
        this(context, null, 0);
    }

    public TwitterTextView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TwitterTextView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        Typeface mTypeface = TypeFaceProvider.getTypeFace(context, "arista");
        setTypeface(mTypeface);
    }

}
