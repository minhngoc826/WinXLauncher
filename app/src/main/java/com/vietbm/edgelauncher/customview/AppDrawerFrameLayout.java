package com.vietbm.edgelauncher.customview;

import android.content.Context;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.vietbm.edgelauncher.R;

import net.gsantner.opoc.util.Callback;

import java.lang.reflect.Method;


public class AppDrawerFrameLayout extends FrameLayout {

    private Animation animFadeIn;
    private Animation animFadeOut;
    private Vibrator mVibrator;
    private Callback.a2<Boolean, Boolean> _appDrawerCallback;

    public interface OnSwipeListener {
        void OnSwipeParentViewEvent(int i);
    }

    private GestureDetector gestureDetector;
    private OnSwipeListener swipeListener;

    public void setOnSwipeListener(OnSwipeListener swipeListener) {
        this.swipeListener = swipeListener;
    }

    public AppDrawerFrameLayout(Context context) {
        super(context);
        init(context);
    }

    public AppDrawerFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AppDrawerFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setCallBack(Callback.a2<Boolean, Boolean> _appDrawerCallback) {
        this._appDrawerCallback = _appDrawerCallback;
    }

    private void init(Context context) {
        this.mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        this.animFadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in_0);
        this.animFadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out_0);
        this.animFadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                _appDrawerCallback.callback(true, true);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                clearAnimation();
                _appDrawerCallback.callback(true, false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        this.animFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                _appDrawerCallback.callback(false, true);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                clearAnimation();
                _appDrawerCallback.callback(false, false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        this.gestureDetector = new GestureDetector(context, new GestureListener());
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

    // Implement vibrate feedback soon
    private class VibrateRunnable implements Runnable {
        final AppDrawerFrameLayout appDrawerFrameLayout;

        VibrateRunnable(AppDrawerFrameLayout view) {
            this.appDrawerFrameLayout = view;
        }

        public final void run() {
            vibrate(mVibrator, 20);
        }
    }

    public void vibrate(@NonNull Vibrator vibrator, int time) {
        if (!semVibrateEnable()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(time);
            }
        }
    }

    private boolean semVibrateEnable() {
        try {
            Class classRef = Class.forName("android.os.Vibrator");
            Method method = classRef.getDeclaredMethod("semIsHapticSupported");
            Boolean isHapticSupported = (Boolean) method.invoke(mVibrator);
            if (isHapticSupported) {
                Class SemMagnitudeTypes = Class.forName("android.os.Vibrator$SemMagnitudeTypes");
                Object enumVal = SemMagnitudeTypes.getField("TYPE_TOUCH").get(null);
                method = Vibrator.class.getDeclaredMethod("semVibrate", Integer.TYPE, Integer.TYPE, AudioAttributes.class, SemMagnitudeTypes);
                method.invoke(mVibrator, 50025, -1, null, enumVal);
                return true;
            }
        } catch (Exception ignore) {
        }
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (gestureDetector != null) {
            this.gestureDetector.onTouchEvent(motionEvent);
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    public void onSwipeEvent(int direction) {
        if (swipeListener != null) {
            swipeListener.OnSwipeParentViewEvent(direction);
        }
    }


    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 200;
        private static final int SWIPE_VELOCITY_THRESHOLD = 200;

        private GestureListener() {
        }

        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float velocityX, float velocityY) {
            try {
                float y = motionEvent2.getY() - motionEvent.getY();
                float x = motionEvent2.getX() - motionEvent.getX();
                if (Math.abs(x) > Math.abs(y)) {
                    if (Math.abs(x) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (x > 0.0f) {
                            onSwipeEvent(1);
                            return false;
                        }
                        onSwipeEvent(2);
                        return false;
                    }
                } else if (Math.abs(y) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (y > 0.0f) {
                        //swipe down
                        onSwipeEvent(3);
                        return false;
                    }
                    //swipe up
                    onSwipeEvent(4);
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }
}
