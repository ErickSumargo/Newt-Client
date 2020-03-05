package app.newt.id.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ViewUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import app.newt.id.R;
import app.newt.id.helper.Utils;

/**
 * Created by Erick Sumargo on 2/20/2018.
 */

public class RelativeLayoutCustom extends RelativeLayout {
    private int maxHeightDp;

    public RelativeLayoutCustom(Context context) {
        super(context);
    }

    public RelativeLayoutCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RelativeLayout, 0, 0);
        try {
            maxHeightDp = a.getInteger(R.styleable.RelativeLayout_maxHeight, 0);
        } finally {
            a.recycle();
        }
    }

    public RelativeLayoutCustom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxHeightPx = Utils.with(getContext()).dpToPx(maxHeightDp);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeightPx, MeasureSpec.AT_MOST);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}