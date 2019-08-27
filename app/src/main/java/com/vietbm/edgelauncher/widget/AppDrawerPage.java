package com.vietbm.edgelauncher.widget;

import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.vietbm.edgelauncher.R;
import com.vietbm.edgelauncher.manager.Setup;
import com.vietbm.edgelauncher.model.App;
import com.vietbm.edgelauncher.model.Item;
import com.vietbm.edgelauncher.util.DragAction;
import com.vietbm.edgelauncher.viewutil.ItemViewFactory;


import java.util.ArrayList;
import java.util.List;

public class AppDrawerPage extends ViewPager {
    private List<App> _apps;

    public List<ViewGroup> _pages = new ArrayList<>();

    private static int _columnCellCount;
    private static int _rowCellCount;

    private PagerIndicator _appDrawerIndicator;

    private int _pageCount = 0;

    private Animation animSlideUpIn;
    private Animation animSlideDownBottom;
    private Animation animSlideUpTop;
    private int exitAnim = 0;

    public AppDrawerPage(Context context, AttributeSet attr) {
        super(context, attr);
        init(context);
    }

    public AppDrawerPage(Context context) {
        super(context);
        init(context);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        if (_apps == null) {
            super.onConfigurationChanged(newConfig);
            return;
        }
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setLandscapeValue();
            calculatePage();
            setAdapter(new Adapter());
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setPortraitValue();
            calculatePage();
            setAdapter(new Adapter());
        }
        super.onConfigurationChanged(newConfig);
    }

    private void setPortraitValue() {
        _columnCellCount = Setup.appSettings().getDrawerColumnCount();
        _rowCellCount = Setup.appSettings().getDrawerRowCount();
    }

    private void setLandscapeValue() {
        _columnCellCount = Setup.appSettings().getDrawerRowCount();
        _rowCellCount = Setup.appSettings().getDrawerColumnCount();
    }

    private void calculatePage() {
        _pageCount = 0;
        int appsSize = _apps.size();
        while ((appsSize = appsSize - (_rowCellCount * _columnCellCount)) >= (_rowCellCount * _columnCellCount) || (appsSize > -(_rowCellCount * _columnCellCount))) {
            _pageCount++;
        }
    }

    private void init(Context c) {
        if (isInEditMode()) return;
        this.animSlideUpIn = AnimationUtils.loadAnimation(c, R.anim.slide_up);
        this.animSlideDownBottom = AnimationUtils.loadAnimation(c, R.anim.slide_down_bottom);
        this.animSlideUpTop = AnimationUtils.loadAnimation(c, R.anim.slide_up_top);

        setOverScrollMode(OVER_SCROLL_NEVER);
        boolean mPortrait = c.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (mPortrait) {
            setPortraitValue();
        } else {
            setLandscapeValue();
        }

        List<App> allApps = Setup.appLoader().getAllApps(c, false);
        if (allApps.size() != 0) {
            AppDrawerPage.this._apps = allApps;
            calculatePage();
            setAdapter(new Adapter());
            if (_appDrawerIndicator != null)
                _appDrawerIndicator.setViewPager(AppDrawerPage.this);
        }
        Setup.appLoader().addUpdateListener(apps -> {
            AppDrawerPage.this._apps = apps;
            calculatePage();
            setAdapter(new Adapter());
            if (_appDrawerIndicator != null)
                _appDrawerIndicator.setViewPager(AppDrawerPage.this);

            return false;
        });
    }

    public void withHome(PagerIndicator appDrawerIndicator) {
        _appDrawerIndicator = appDrawerIndicator;
        appDrawerIndicator.setMode(PagerIndicator.Mode.DOTS);
        if (getAdapter() != null)
            appDrawerIndicator.setViewPager(AppDrawerPage.this);
    }


    public void setExitAnim(int exitAnim) {
        this.exitAnim = exitAnim;
    }



    @Override
    public void setVisibility(int visibility) {
        if (visibility == VISIBLE) {
            if (this.animSlideUpIn != null) {
                startAnimation(this.animSlideUpIn);
            }
        } else if (visibility == GONE) {
            switch (exitAnim) {
                case 0:
                    if (this.animSlideDownBottom != null) {
                        startAnimation(this.animSlideDownBottom);
                    }
                    break;
                case 1:
                    if (this.animSlideUpTop != null) {
                        startAnimation(this.animSlideUpTop);
                    }
                    break;
            }

        }
        super.setVisibility(visibility);
    }

    public class Adapter extends PagerAdapter {

        private View getItemView(int page, int x, int y) {
            int pagePos = y * _columnCellCount + x;
            final int pos = _rowCellCount * _columnCellCount * page + pagePos;

            if (pos >= _apps.size())
                return null;

            final App app = _apps.get(pos);

            return ItemViewFactory.getItemView(getContext(), null, DragAction.Action.DRAWER, Item.newAppItem(app));
        }

        Adapter() {
            _pages.clear();
            for (int i = 0; i < getCount(); i++) {
                ViewGroup layout = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.view_app_drawer_page_inner, null);
                CellContainer cc = layout.findViewById(R.id.group);
                cc.setGridSize(_columnCellCount, _rowCellCount);

                for (int x = 0; x < _columnCellCount; x++) {
                    for (int y = 0; y < _rowCellCount; y++) {
                        View view = getItemView(i, x, y);
                        if (view != null) {
                            CellContainer.LayoutParams lp = new CellContainer.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, x, y, 1, 1);
                            view.setLayoutParams(lp);
                            cc.addViewToGrid(view);
                        }
                    }
                }
                _pages.add(layout);
            }
        }

        @Override
        public int getCount() {
            return _pageCount;
        }

        @Override
        public boolean isViewFromObject(View p1, Object p2) {
            return p1 == p2;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            int index = _pages.indexOf(object);
            if (index == -1)
                return POSITION_NONE;
            else
                return index;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int pos) {
            ViewGroup layout = _pages.get(pos);
            container.addView(layout);
            return layout;
        }
    }
}
