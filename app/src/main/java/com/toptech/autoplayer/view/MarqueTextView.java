package com.toptech.autoplayer.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * Created by zopuus on 2018/3/18.
 */

public final class MarqueTextView extends AppCompatTextView {
    public MarqueTextView(Context context) {
        super(context);
    }

    public MarqueTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
