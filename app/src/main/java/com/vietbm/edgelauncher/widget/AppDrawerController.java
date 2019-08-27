package com.vietbm.edgelauncher.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowInsets;

import com.vietbm.edgelauncher.R;
import com.vietbm.edgelauncher.customview.AppDrawerFrameLayout;
import com.vietbm.edgelauncher.util.Utils;


public class AppDrawerController extends AppDrawerFrameLayout implements AppDrawerFrameLayout.OnSwipeListener {
    public AppDrawerPage _drawerViewPage;
    public boolean _isOpen = false;
    private Context mContext;

    @Override
    public void OnSwipeParentViewEvent(int i) {
        switch (i) {
            case 3:
                _drawerViewPage.setExitAnim(0);
                close();
                break;
            case 4:
                _drawerViewPage.setExitAnim(1);
                close();
                _drawerViewPage.setExitAnim(0);
//                Bitmap bm = loadBitmapFromView(this, getWidth(), getHeight());
//                setBackground(new BitmapDrawable(getResources(), bm));
                break;
        }

    }


    public static class Mode {
        public static final int GRID = 1;
        public static final int PAGE = 2;
    }

    public AppDrawerController(Context context) {
        super(context);
        mContext = context;
    }

    public AppDrawerController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public AppDrawerController(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    public View getDrawer() {
        return _drawerViewPage;
    }

    public void open() {
        if (_isOpen) return;
        _isOpen = true;
        _drawerViewPage.setVisibility(VISIBLE);
        setVisibility(VISIBLE);
    }

    public void close() {
        if (!_isOpen) return;
        _isOpen = false;
        _drawerViewPage.setVisibility(GONE);
        setVisibility(GONE);
    }

    public void reset() {
        _drawerViewPage.setCurrentItem(0, false);
    }

    public void init() {
        if (isInEditMode()) return;
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        setVisibility(GONE);
        new LoadWallpaperBitMapAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        _drawerViewPage = (AppDrawerPage) layoutInflater.inflate(R.layout.view_app_drawer_page, this, false);
        addView(_drawerViewPage);
        PagerIndicator indicator = (PagerIndicator) layoutInflater.inflate(R.layout.view_drawer_indicator, this, false);
        addView(indicator);
        _drawerViewPage.withHome(indicator);
        setOnSwipeListener(this);

    }

    public static Bitmap loadBitmapFromView(View v, int width, int height) {
        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.draw(c);

        return b;
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        setPadding(0, insets.getSystemWindowInsetTop(), 0, insets.getSystemWindowInsetBottom());
        return insets;
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadWallpaperBitMapAsync extends AsyncTask<Void, Void, BitmapDrawable> {
        @Override
        protected BitmapDrawable doInBackground(Void... voids) {
            Bitmap bm = null;
            if (mContext != null) {
                bm = Utils.getWallpaperBitmap(mContext);
                if (bm != null) {
                    bm = Utils.fastblur(Bitmap.createScaledBitmap(bm, ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION, (bm.getHeight() * ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION) / bm.getWidth(), false), 10);
                }
            }
            return new BitmapDrawable(getResources(), bm);
        }

        @Override
        protected void onPostExecute(BitmapDrawable bitmap) {
            super.onPostExecute(bitmap);
            setBackground(bitmap);
        }
    }
}
