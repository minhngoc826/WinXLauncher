package com.vietbm.edgelauncher.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.vietbm.edgelauncher.BuildConfig;
import com.vietbm.edgelauncher.R;
import com.vietbm.edgelauncher.activity.homeparts.HpAppDrawer;
import com.vietbm.edgelauncher.activity.homeparts.HpDesktopPickAction;
import com.vietbm.edgelauncher.activity.homeparts.HpDragOption;
import com.vietbm.edgelauncher.activity.homeparts.HpInitSetup;
import com.vietbm.edgelauncher.activity.homeparts.HpSearchBar;
import com.vietbm.edgelauncher.interfaces.AppDeleteListener;
import com.vietbm.edgelauncher.interfaces.AppUpdateListener;
import com.vietbm.edgelauncher.manager.Setup;
import com.vietbm.edgelauncher.model.App;
import com.vietbm.edgelauncher.model.Item;
import com.vietbm.edgelauncher.model.Item.Type;
import com.vietbm.edgelauncher.receivers.AppUpdateReceiver;
import com.vietbm.edgelauncher.receivers.ShortcutReceiver;
import com.vietbm.edgelauncher.util.AppManager;
import com.vietbm.edgelauncher.util.AppSettings;
import com.vietbm.edgelauncher.util.DatabaseHelper;
import com.vietbm.edgelauncher.util.Definitions.ItemPosition;
import com.vietbm.edgelauncher.util.LauncherAction;
import com.vietbm.edgelauncher.util.LauncherAction.Action;
import com.vietbm.edgelauncher.util.Tool;
import com.vietbm.edgelauncher.util.Utils;
import com.vietbm.edgelauncher.viewutil.DialogHelper;
import com.vietbm.edgelauncher.viewutil.MinibarAdapter;
import com.vietbm.edgelauncher.viewutil.WidgetHost;
import com.vietbm.edgelauncher.widget.AppDrawerController;
import com.vietbm.edgelauncher.widget.AppItemView;
import com.vietbm.edgelauncher.widget.CellContainer;
import com.vietbm.edgelauncher.widget.Desktop;
import com.vietbm.edgelauncher.widget.Desktop.OnDesktopEditListener;
import com.vietbm.edgelauncher.widget.DesktopOptionView;
import com.vietbm.edgelauncher.widget.DesktopOptionView.DesktopOptionViewListener;
import com.vietbm.edgelauncher.widget.Dock;
import com.vietbm.edgelauncher.widget.GroupPopupView;
import com.vietbm.edgelauncher.widget.ItemOptionView;
import com.vietbm.edgelauncher.widget.MinibarView;
import com.vietbm.edgelauncher.widget.PagerIndicator;
import com.vietbm.edgelauncher.widget.SearchBar;

import net.gsantner.opoc.util.ContextUtils;

import java.util.ArrayList;
import java.util.List;

public final class HomeActivity extends Activity implements OnDesktopEditListener, DesktopOptionViewListener {
    public static final Companion Companion = new Companion();
    public static final int REQUEST_CREATE_APPWIDGET = 0x6475;
    public static final int REQUEST_PERMISSION_STORAGE = 0x3648;
    public static final int REQUEST_PICK_APPWIDGET = 0x2678;
    public static WidgetHost _appWidgetHost;
    public static AppWidgetManager _appWidgetManager;
    public static boolean ignoreResume;
    public static float _itemTouchX;
    public static float _itemTouchY;

    // static launcher variables
    public static HomeActivity _launcher;
    public static DatabaseHelper _db;

    // receiver variables
    private static final IntentFilter _appUpdateIntentFilter = new IntentFilter();
    private static final IntentFilter _shortcutIntentFilter = new IntentFilter();
    private static final IntentFilter _timeChangedIntentFilter = new IntentFilter();
    private AppUpdateReceiver _appUpdateReceiver;
    private ShortcutReceiver _shortcutReceiver;
    private BroadcastReceiver _timeChangedReceiver;

    private int cx;
    private int cy;

    public static final class Companion {
        private Companion() {
        }

        public final HomeActivity getLauncher() {
            return _launcher;
        }

        public final void setLauncher(@Nullable HomeActivity v) {
            _launcher = v;
        }
    }

    static {
        _timeChangedIntentFilter.addAction("android.intent.action.TIME_TICK");
        _timeChangedIntentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
        _timeChangedIntentFilter.addAction("android.intent.action.TIME_SET");
        _appUpdateIntentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        _appUpdateIntentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        _appUpdateIntentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        _appUpdateIntentFilter.addDataScheme("package");
        _shortcutIntentFilter.addAction("com.android.launcher.action.INSTALL_SHORTCUT");
    }

    public final DrawerLayout getDrawerLayout() {
        return findViewById(R.id.drawer_layout);
    }

    public final Desktop getDesktop() {
        return findViewById(R.id.desktop);
    }

    public final Dock getDock() {
        return findViewById(R.id.dock);
    }

    public final AppDrawerController getAppDrawerController() {
        return findViewById(R.id.appDrawerController);
    }

    public final GroupPopupView getGroupPopup() {
        return findViewById(R.id.groupPopup);
    }

    public final SearchBar getSearchBar() {
        return findViewById(R.id.searchBar);
    }

    public final View getBackground() {
        return findViewById(R.id.background_frame);
    }

    public final PagerIndicator getDesktopIndicator() {
        return findViewById(R.id.desktopIndicator);
    }

    public final DesktopOptionView getDesktopOptionView() {
        return findViewById(R.id.desktop_option);
    }

    public final ItemOptionView getItemOptionView() {
        return findViewById(R.id.item_option);
    }

    public final FrameLayout getMinibarFrame() {
        return findViewById(R.id.minibar_frame);
    }

    public final View getStatusView() {
        return findViewById(R.id.status_frame);
    }

    public final View getNavigationView() {
        return findViewById(R.id.navigation_frame);
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Companion.setLauncher(this);
        AppSettings appSettings = AppSettings.get();

        ContextUtils contextUtils = new ContextUtils(getApplicationContext());
        contextUtils.setAppLanguage(appSettings.getLanguage());
        super.onCreate(savedInstanceState);
        if (!Setup.wasInitialised()) {
            Setup.init(new HpInitSetup(this));
        }
        Companion.setLauncher(this);
        _db = Setup.dataManager();

        setContentView(getLayoutInflater().inflate(R.layout.activity_home, null));

        // transparent status and navigation
        if (VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            View decorView = window.getDecorView();
            decorView.setSystemUiVisibility(1536);
        }
        init();
    }

    private void init() {
        _appWidgetManager = AppWidgetManager.getInstance(this);
        _appWidgetHost = new WidgetHost(getApplicationContext(), R.id.app_widget_host);
        _appWidgetHost.startListening();

        // item drag and drop
        HpDragOption hpDragOption = new HpDragOption();
        View findViewById = findViewById(R.id.leftDragHandle);
        View findViewById2 = findViewById(R.id.rightDragHandle);
        hpDragOption.initDragNDrop(this, findViewById, findViewById2, getItemOptionView());

        registerBroadcastReceiver();
        initAppManager();
        initSettings();
        initViews();
    }

    protected void initAppManager() {
        if (Setup.appSettings().getAppFirstLaunch()) {
            Setup.appSettings().setAppFirstLaunch(false);
            Setup.appSettings().setAppShowIntro(false);
            Item appDrawerBtnItem = Item.newActionItem(8);
            appDrawerBtnItem._x = 2;
            _db.saveItem(appDrawerBtnItem, 0, ItemPosition.Dock);
        }
        Setup.appLoader().addUpdateListener(new AppUpdateListener() {
            @Override
            public boolean onAppUpdated(List<App> it) {
                getDesktop().initDesktop();
                getDock().initDock();
                return false;
            }
        });
        Setup.appLoader().addDeleteListener(new AppDeleteListener() {
            @Override
            public boolean onAppDeleted(List<App> apps) {
                getDesktop().initDesktop();
                getDock().initDock();
                return false;
            }
        });
        AppManager.getInstance(this).init();
    }

    protected void initViews() {
        new HpSearchBar(this, getSearchBar()).initSearchBar();
        getAppDrawerController().init();
        getDock().setHome(this);

        getDesktop().setDesktopEditListener(this);
        getDesktop().setPageIndicator(getDesktopIndicator());
        getDesktopIndicator().setMode(Setup.appSettings().getDesktopIndicatorMode());

        AppSettings appSettings = Setup.appSettings();
        getDesktopOptionView().setDesktopOptionViewListener(this);
        getDesktopOptionView().postDelayed(new Runnable() {
            @Override
            public void run() {
                getDesktopOptionView().updateLockIcon(appSettings.getDesktopLock());
            }
        }, 100);
        getDesktop().addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                getDesktopOptionView().updateHomeIcon(appSettings.getDesktopPageCurrent() == position);
            }

            public void onPageScrollStateChanged(int state) {
            }
        });
        new HpAppDrawer(this, findViewById(R.id.appDrawerIndicator)).initAppDrawer(getAppDrawerController());
        initMinibar();

        new LoadWallpaperBitMapAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadWallpaperBitMapAsync extends AsyncTask<Void, Void, BitmapDrawable> {
        @Override
        protected BitmapDrawable doInBackground(Void... voids) {
            Bitmap bm = null;
                bm = Utils.getWallpaperBitmap(HomeActivity.this);
                if (bm != null) {
                    //bm = Utils.fastblur(Bitmap.createScaledBitmap(bm, ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION, (bm.getHeight() * ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION) / bm.getWidth(), false), 10);
                }
            return new BitmapDrawable(getResources(), bm);
        }

        @Override
        protected void onPostExecute(BitmapDrawable bitmap) {
            super.onPostExecute(bitmap);
            getItemOptionView().setBackground(bitmap);
        }
    }


    public final void initMinibar() {
        final ArrayList<LauncherAction.ActionDisplayItem> items = AppSettings.get().getMinibarArrangement();
        MinibarView minibar = findViewById(R.id.minibar);
        minibar.setAdapter(new MinibarAdapter(this, items));
        minibar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                LauncherAction.RunAction(items.get(i), HomeActivity.this);
            }
        });
    }

    public final void initSettings() {
        updateHomeLayout();

        AppSettings appSettings = Setup.appSettings();
        if (appSettings.getDesktopFullscreen()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        // set background colors
        getDesktop().setBackgroundColor(appSettings.getDesktopBackgroundColor());
        getDock().setBackgroundColor(appSettings.getDockColor());

        // set frame colors
        getMinibarFrame().setBackgroundColor(appSettings.getMinibarBackgroundColor());
        getStatusView().setBackgroundColor(appSettings.getDesktopInsetColor());
        getNavigationView().setBackgroundColor(appSettings.getDesktopInsetColor());

        // lock the minibar
        getDrawerLayout().setDrawerLockMode(appSettings.getMinibarEnable() ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    private void registerBroadcastReceiver() {
        _appUpdateReceiver = new AppUpdateReceiver();
        _shortcutReceiver = new ShortcutReceiver();
        _timeChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                    updateSearchClock();
                }
            }
        };

        // register all receivers
        registerReceiver(_appUpdateReceiver, _appUpdateIntentFilter);
        registerReceiver(_shortcutReceiver, _shortcutIntentFilter);
        registerReceiver(_timeChangedReceiver, _timeChangedIntentFilter);
    }

    public void onRemovePage() {
        if (getDesktop().isCurrentPageEmpty()) {
            getDesktop().removeCurrentPage();
            return;
        }
        DialogHelper.alertDialog(this, getString(R.string.remove), "This page is not empty. Those items will also be removed.", new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                getDesktop().removeCurrentPage();
            }
        });
    }

    public final void onStartApp(@NonNull Context context, @NonNull App app, @Nullable View view) {
        if (BuildConfig.APPLICATION_ID.equals(app._packageName)) {
            LauncherAction.RunAction(Action.LauncherSettings, context);
        } else {
            try {
                Intent intent = Tool.getIntentFromApp(app);
                context.startActivity(intent, getActivityAnimationOpts(view));
                // close app drawer and other items in advance
                // annoying to wait for app drawer to close
                handleLauncherResume();
            } catch (Exception e) {
                e.printStackTrace();
                Tool.toast(context, R.string.toast_app_uninstalled);
            }
        }
    }

    public final void onUninstallItem(@NonNull Item item) {
        ignoreResume = true;
        Setup.eventHandler().showDeletePackageDialog(this, item);
    }

    public final void onRemoveItem(@NonNull Item item) {
        Desktop desktop = getDesktop();
        View coordinateToChildView;
        if (item._location.equals(ItemPosition.Desktop)) {
            coordinateToChildView = desktop.getCurrentPage().coordinateToChildView(new Point(item._x, item._y));
            desktop.removeItem(coordinateToChildView, true);
        } else {
            Dock dock = getDock();
            coordinateToChildView = dock.coordinateToChildView(new Point(item._x, item._y));
            dock.removeItem(coordinateToChildView, true);
        }
        _db.deleteItem(item, true);
    }

    public final void onInfoItem(@NonNull Item item) {
        if (item._type == Type.APP) {
            try {
                String str = "android.settings.APPLICATION_DETAILS_SETTINGS";
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("package:");
                Intent intent = item._intent;
                ComponentName component = intent.getComponent();
                stringBuilder.append(component.getPackageName());
                startActivity(new Intent(str, Uri.parse(stringBuilder.toString())));
            } catch (Exception e) {
                Tool.toast(this, R.string.toast_app_uninstalled);
            }
        }
    }

    private Bundle getActivityAnimationOpts(View view) {
        Bundle bundle = null;
        if (view == null) {
            return null;
        }
        ActivityOptions opts = null;
        if (VERSION.SDK_INT >= 23) {
            int left = 0;
            int top = 0;
            int width = view.getMeasuredWidth();
            int height = view.getMeasuredHeight();
            if (view instanceof AppItemView) {
                width = (int) ((AppItemView) view).getIconSize();
                left = (int) ((AppItemView) view).getDrawIconLeft();
                top = (int) ((AppItemView) view).getDrawIconTop();
            }
            opts = ActivityOptions.makeClipRevealAnimation(view, left, top, width, height);
        } else if (VERSION.SDK_INT < 21) {
            opts = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        }
        if (opts != null) {
            bundle = opts.toBundle();
        }
        return bundle;
    }

    public void onDesktopEdit() {
        Tool.visibleViews(100, getDesktopOptionView());
        updateDesktopIndicator(false);
        updateDock(false);
        updateSearchBar(false);
    }

    public void onFinishDesktopEdit() {
        Tool.invisibleViews(100, getDesktopOptionView());
        updateDesktopIndicator(true);
        updateDock(true);
        updateSearchBar(true);
    }

    public void onSetPageAsHome() {
        AppSettings appSettings = Setup.appSettings();
        appSettings.setDesktopPageCurrent(getDesktop().getCurrentItem());
    }

    public void onLaunchSettings() {
        Setup.eventHandler().showLauncherSettings(this);
    }

    public void onPickDesktopAction() {
        new HpDesktopPickAction(this).onPickDesktopAction();
    }

    public void onPickWidget() {
        pickWidget();
    }

    public final void dimBackground() {
        Tool.visibleViews(200, getBackground());
    }

    public final void unDimBackground() {
        Tool.invisibleViews(200, getBackground());
    }

    public final void clearRoomForPopUp() {
        Tool.invisibleViews(200, getDesktop());
        updateDesktopIndicator(false);
        updateDock(false);
    }

    public final void unClearRoomForPopUp() {
        Tool.visibleViews(200, getDesktop());
        updateDesktopIndicator(true);
        updateDock(true);
    }

    public final void updateDock(boolean show) {
        AppSettings appSettings = Setup.appSettings();
        if (appSettings.getDockEnable() && show) {
            Tool.visibleViews(100, getDock());
        } else {
            if (appSettings.getDockEnable()) {
                Tool.invisibleViews(100, getDock());
            } else {
                Tool.goneViews(100, getDock());
            }
        }
    }

    public final void updateSearchBar(boolean show) {
        AppSettings appSettings = Setup.appSettings();
        if (appSettings.getSearchBarEnable() && show) {
            Tool.visibleViews(100, getSearchBar());
        } else {
            if (appSettings.getSearchBarEnable()) {
                Tool.invisibleViews(100, getSearchBar());
            } else {
                Tool.goneViews(100, getSearchBar());
            }
        }
    }

    public final void updateDesktopIndicator(boolean show) {
        AppSettings appSettings = Setup.appSettings();
        if (appSettings.getDesktopShowIndicator() && show) {
            Tool.visibleViews(100, getDesktopIndicator());
        } else {
            Tool.goneViews(100, getDesktopIndicator());
        }
    }

    public final void updateSearchClock() {
        TextView textView = getSearchBar()._searchClock;

        if (textView.getText() != null) {
            try {
                getSearchBar().updateClock();
            } catch (Exception e) {
                getSearchBar()._searchClock.setText(R.string.bad_format);
            }
        }
    }

    public final void updateHomeLayout() {
        updateSearchBar(true);
        updateDock(true);
        updateDesktopIndicator(true);
    }

    private void pickWidget() {
        ignoreResume = true;
        int appWidgetId = _appWidgetHost.allocateAppWidgetId();
        Intent pickIntent = new Intent("android.appwidget.action.APPWIDGET_PICK");
        pickIntent.putExtra("appWidgetId", appWidgetId);
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
    }

    private void configureWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt("appWidgetId", -1);
        AppWidgetProviderInfo appWidgetInfo = _appWidgetManager.getAppWidgetInfo(appWidgetId);
        if (appWidgetInfo.configure != null) {
            Intent intent = new Intent("android.appwidget.action.APPWIDGET_CONFIGURE");
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra("appWidgetId", appWidgetId);
            startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
        } else {
            createWidget(data);
        }
    }

    private void createWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo = _appWidgetManager.getAppWidgetInfo(appWidgetId);
        Item item = Item.newWidgetItem(appWidgetId);
        Desktop desktop = getDesktop();
        List<CellContainer> pages = desktop.getPages();
        item._spanX = (appWidgetInfo.minWidth - 1) / pages.get(desktop.getCurrentItem()).getCellWidth() + 1;
        item._spanY = (appWidgetInfo.minHeight - 1) / pages.get(desktop.getCurrentItem()).getCellHeight() + 1;
        Point point = desktop.getCurrentPage().findFreeSpace(item._spanX, item._spanY);
        if (point != null) {
            item._x = point.x;
            item._y = point.y;

            // add item to database
            _db.saveItem(item, desktop.getCurrentItem(), ItemPosition.Desktop);
            desktop.addItemToPage(item, desktop.getCurrentItem());
        } else {
            Tool.toast(this, R.string.toast_not_enough_space);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK_APPWIDGET) {
                configureWidget(data);
            } else if (requestCode == REQUEST_CREATE_APPWIDGET) {
                createWidget(data);
            }
        } else if (resultCode == RESULT_CANCELED && data != null) {
            int appWidgetId = data.getIntExtra("appWidgetId", -1);
            if (appWidgetId != -1) {
                _appWidgetHost.deleteAppWidgetId(appWidgetId);
            }
        }
    }

    @Override
    public void onBackPressed() {
        handleLauncherResume();
    }

    @Override
    protected void onStart() {
        _appWidgetHost.startListening();
        _launcher = this;

        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _appWidgetHost.startListening();
        _launcher = this;

        // handle restart if something needs to be reset
        AppSettings appSettings = Setup.appSettings();
        if (appSettings.getAppRestartRequired()) {
            appSettings.setAppRestartRequired(false);
            recreate();
            return;
        }

        // handle launcher rotation
        if (appSettings.getDesktopOrientationMode() == 2) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (appSettings.getDesktopOrientationMode() == 1) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        handleLauncherResume();
    }

    @Override
    protected void onDestroy() {
        _appWidgetHost.stopListening();
        _launcher = null;

        unregisterReceiver(_appUpdateReceiver);
        unregisterReceiver(_shortcutReceiver);
        unregisterReceiver(_timeChangedReceiver);
        super.onDestroy();
    }

    private void handleLauncherResume() {
        if (ignoreResume) {
            // only triggers when a new activity is launched that should leave launcher state alone
            // uninstall package activity and pick widget activity
            ignoreResume = false;
        } else {
            getSearchBar().collapse();
            getGroupPopup().collapse();
            // close app option menu
            getItemOptionView().collapse();
            // close minibar
            getDrawerLayout().closeDrawers();
            if (getDesktop().getInEditMode()) {
                // exit desktop edit mode
                getDesktop().getCurrentPage().performClick();
            } else if (getAppDrawerController().getDrawer().getVisibility() == View.VISIBLE) {
                closeAppDrawer();
            } else if (getDesktop().getCurrentItem() != 0) {
                AppSettings appSettings = Setup.appSettings();
                getDesktop().setCurrentItem(appSettings.getDesktopPageCurrent());
            }
        }
    }



    public final void openAppDrawer(View view, int x, int y) {
        if (!(x > 0 && y > 0) && view != null) {
            int[] pos = new int[2];
            view.getLocationInWindow(pos);
            cx = pos[0];
            cy = pos[1];

            cx += view.getWidth() / 2f;
            cy += view.getHeight() / 2f;
            if (view instanceof AppItemView) {
                AppItemView appItemView = (AppItemView) view;
                if (appItemView != null && appItemView.getShowLabel()) {
                    cy -= Tool.dp2px(14) / 2f;
                }
            }
            cy -= getAppDrawerController().getPaddingTop();
        } else {
            cx = x;
            cy = y;
        }
        getAppDrawerController().open();
    }

    public void openAppDrawer(){
        getAppDrawerController().open();
    }

    public final void closeAppDrawer() {
        getAppDrawerController().close();
    }
}
