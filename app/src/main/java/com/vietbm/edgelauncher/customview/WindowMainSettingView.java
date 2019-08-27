package com.vietbm.edgelauncher.customview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.vietbm.edgelauncher.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WindowMainSettingView extends ConstraintLayout implements View.OnClickListener {
    private Context mContext;

    @BindView(R.id.expand_button)
    AppCompatTextView expandButton;
    @BindView(R.id.search_app_view)
    SearchAppView searchAppView;

    @BindView(R.id.account_setting)
    AppCompatTextView accountButton;
    @BindView(R.id.document_setting)
    AppCompatTextView documentButton;
    @BindView(R.id.picture_setting)
    AppCompatTextView pictureButton;
    @BindView(R.id.launcher_setting)
    AppCompatTextView launcherSettingButton;
    @BindView(R.id.power_button)
    AppCompatTextView powerButton;

    public WindowMainSettingView(Context context) {
        super(context);
        initView(context);
    }

    public WindowMainSettingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public WindowMainSettingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void initView(Context context) {
        mContext = context;
        try {
            removeAllViews();
            if (getParent() != null) {
                ((ViewGroup) getParent()).removeView(this);
            }
        } catch (Throwable ignored) {

        }
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        LayoutInflater.from(mContext).inflate(R.layout.window_main_view, this, true);
        ButterKnife.bind(this);
        expandButton.setOnClickListener(this);
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        setPadding(0, 0, 0, 0);
        return insets;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.expand_button:
                updateExpandText();
                break;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void updateExpandText() {
        if (expandButton.getText().equals("") || expandButton.getText() == null) {
            searchAppView.animate().alpha(0).setDuration(300).setInterpolator(new DecelerateInterpolator()).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    searchAppView.setVisibility(GONE);
                    expandButton.setText(mContext.getString(R.string.main_expand_button));
                    accountButton.setText(mContext.getString(R.string.main_account_button));
                    documentButton.setText(mContext.getString(R.string.main_document_button));
                    pictureButton.setText(mContext.getString(R.string.main_background_button));
                    launcherSettingButton.setText(mContext.getString(R.string.main_settings_button));
                    powerButton.setText(mContext.getString(R.string.main_power_button));
                }
            });
        } else {
            searchAppView.animate().alpha(1).setDuration(300).setInterpolator(new DecelerateInterpolator()).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    searchAppView.setVisibility(VISIBLE);
                    expandButton.setText("");
                    accountButton.setText("");
                    documentButton.setText("");
                    pictureButton.setText("");
                    launcherSettingButton.setText("");
                    powerButton.setText("");
                }
            });
        }
    }
}
