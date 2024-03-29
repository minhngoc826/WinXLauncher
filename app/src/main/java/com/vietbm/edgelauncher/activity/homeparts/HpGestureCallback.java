package com.vietbm.edgelauncher.activity.homeparts;

import android.content.Intent;
import android.util.Log;

import com.vietbm.edgelauncher.activity.HomeActivity;
import com.vietbm.edgelauncher.manager.Setup;
import com.vietbm.edgelauncher.util.AppSettings;
import com.vietbm.edgelauncher.util.LauncherAction;
import com.vietbm.edgelauncher.util.Tool;
import com.vietbm.edgelauncher.viewutil.DesktopGestureListener;
import com.vietbm.edgelauncher.widget.Desktop;

public class HpGestureCallback implements DesktopGestureListener.DesktopGestureCallback {
    private AppSettings _appSettings;

    HpGestureCallback(AppSettings appSettings) {
        _appSettings = appSettings;
    }

    @Override
    public boolean onDrawerGesture(Desktop desktop, DesktopGestureListener.Type event) {
        Object gesture = null;
        switch (event) {
            case SwipeUp:
                gesture = _appSettings.getGestureSwipeUp();
                break;
            case SwipeDown:
                gesture = _appSettings.getGestureSwipeDown();
                break;
            case SwipeLeft:
            case SwipeRight:
                break;
            case Pinch:
                gesture = _appSettings.getGesturePinch();
                break;
            case Unpinch:
                gesture = _appSettings.getGestureUnpinch();
                break;
            case DoubleTap:
                gesture = _appSettings.getGestureDoubleTap();
                break;
            default:
                Log.e(getClass().toString(), "gesture error");
        }
        if (gesture != null) {
            if (_appSettings.getGestureFeedback()) {
                Tool.vibrate(desktop);
            }
            if (gesture instanceof Intent) {
                Intent intent = (Intent) gesture;
                Tool.startApp(HomeActivity._launcher, Setup.appLoader().findApp(intent), null);
            } else if (gesture instanceof LauncherAction.ActionDisplayItem) {
                LauncherAction.RunAction((LauncherAction.ActionDisplayItem) gesture, desktop.getContext());
            }
            return true;
        }
        return false;
    }
}
