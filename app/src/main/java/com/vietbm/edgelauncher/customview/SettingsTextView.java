package com.vietbm.edgelauncher.customview;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class SettingsTextView extends AppCompatTextView {

    public SettingsTextView(Context context) {
        super(context);
        initView(context);
    }

    public SettingsTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public SettingsTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void initView(Context context) {
        Typeface createFromAsset = Typeface.createFromAsset(context.getAssets(), "fonts/SFProTextMedium.otf");
        setTypeface(createFromAsset);
        setTextSize(13);
    }
}
