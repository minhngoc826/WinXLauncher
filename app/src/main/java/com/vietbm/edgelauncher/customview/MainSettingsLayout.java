package com.vietbm.edgelauncher.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.vietbm.edgelauncher.R;
import com.vietbm.edgelauncher.activity.HomeActivity;

import net.gsantner.opoc.util.Callback;

import java.lang.reflect.Method;

import static com.vietbm.edgelauncher.widget.AppDrawerController.loadBitmapFromView;


public class MainSettingsLayout extends ConstraintLayout {

    private Animation animFadeIn;
    private Animation animFadeOut;


    public MainSettingsLayout(Context context) {
        super(context);
        init(context);
    }

    public MainSettingsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MainSettingsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        try {
            removeAllViews();
            if (getParent() != null) {
                ((ViewGroup) getParent()).removeView(this);
            }
        } catch (Throwable ignored) {

        }
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        LayoutInflater.from(context).inflate(R.layout.main_settings_view, this, true);


        this.animFadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in_0);
        this.animFadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out_0);
        this.animFadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        this.animFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Bitmap bm = loadBitmapFromView(HomeActivity.Companion.getLauncher().getDesktop(), getWidth(), getHeight());
//        HokoBlur.with(getContext())
//                .scheme(HokoBlur.SCHEME_NATIVE)
//                .mode(HokoBlur.MODE_STACK)
//                .radius(10)
//                .sampleFactor(2.0f)
//                .forceCopy(false)
//                .needUpscale(true)
//                .asyncBlur(bm, new AsyncBlurTask.Callback() {
//                    @Override
//                    public void onBlurSuccess(Bitmap bitmap) {
//                        setBackground(new BitmapDrawable(getResources(), bitmap));
//                    }
//
//                    @Override
//                    public void onBlurFailed(Throwable error) {
//
//                    }
//                });
        return super.onTouchEvent(event);

    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility == VISIBLE) {
            if (this.animFadeIn != null) {
                startAnimation(this.animFadeIn);
            }
        } else if (visibility == GONE) {
            if (this.animFadeOut != null) {
                startAnimation(this.animFadeOut);
            }
        }
        super.setVisibility(visibility);
    }

}
