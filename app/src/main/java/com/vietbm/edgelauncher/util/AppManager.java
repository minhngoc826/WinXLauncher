package com.vietbm.edgelauncher.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.UserHandle;
import androidx.annotation.NonNull;

import com.vietbm.edgelauncher.activity.HomeActivity;
import com.vietbm.edgelauncher.interfaces.AppDeleteListener;
import com.vietbm.edgelauncher.interfaces.AppUpdateListener;
import com.vietbm.edgelauncher.manager.Setup;
import com.vietbm.edgelauncher.model.App;
import com.vietbm.edgelauncher.model.Item;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class AppManager {
    private static AppManager appManager;

    public static AppManager getInstance(Context context) {
        return appManager == null ? (appManager = new AppManager(context)) : appManager;
    }

    private PackageManager _packageManager;
    private List<App> _apps = new ArrayList<>();
    private List<App> _nonFilteredApps = new ArrayList<>();
    public final List<AppUpdateListener> _updateListeners = new ArrayList<>();
    public final List<AppDeleteListener> _deleteListeners = new ArrayList<>();
    public boolean _recreateAfterGettingApps;
    private AsyncTask _task;
    private Context _context;

    public PackageManager getPackageManager() {
        return _packageManager;
    }

    public Context getContext() {
        return _context;
    }

    public AppManager(Context context) {
        _context = context;
        _packageManager = context.getPackageManager();
    }

    public App findApp(Intent intent) {
        if (intent == null || intent.getComponent() == null) return null;

        String packageName = intent.getComponent().getPackageName();
        String className = intent.getComponent().getClassName();
        for (App app : _apps) {
            if (app._className.equals(className) && app._packageName.equals(packageName)) {
                return app;
            }
        }
        return null;
    }

    public List<App> getApps() {
        return _apps;
    }

    public List<App> getNonFilteredApps() {
        return _nonFilteredApps;
    }

    public void init() {
        getAllApps();
    }

    public void getAllApps() {
        if (_task == null || _task.getStatus() == AsyncTask.Status.FINISHED)
            _task = new AsyncGetApps().execute();
        else if (_task.getStatus() == AsyncTask.Status.RUNNING) {
            _task.cancel(false);
            _task = new AsyncGetApps().execute();
        }
    }

    public List<App> getAllApps(Context context, boolean includeHidden) {
        return includeHidden ? getNonFilteredApps() : getApps();
    }

    public App findItemApp(Item item) {
        return findApp(item.getIntent());
    }

    public App createApp(Intent intent) {
        try {
            ResolveInfo info = _packageManager.resolveActivity(intent, 0);
            return new App(_packageManager, info);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void onAppUpdated(Context context, Intent intent) {
        getAllApps();
    }

    public void addUpdateListener(AppUpdateListener updateListener) {
        _updateListeners.add(updateListener);
    }

    public void addDeleteListener(AppDeleteListener deleteListener) {
        _deleteListeners.add(deleteListener);
    }

    public void notifyUpdateListeners(@NonNull List<App> apps) {
        Iterator<AppUpdateListener> iter = _updateListeners.iterator();
        while (iter.hasNext()) {
            if (iter.next().onAppUpdated(apps)) {
                iter.remove();
            }
        }
    }

    public void notifyRemoveListeners(@NonNull List<App> apps) {
        Iterator<AppDeleteListener> iter = _deleteListeners.iterator();
        while (iter.hasNext()) {
            if (iter.next().onAppDeleted(apps)) {
                iter.remove();
            }
        }
    }

    private class AsyncGetApps extends AsyncTask {
        private List<App> tempApps;

        @Override
        protected void onPreExecute() {
            tempApps = new ArrayList<>(_apps);
            super.onPreExecute();
        }

        @Override
        protected void onCancelled() {
            tempApps = null;
            super.onCancelled();
        }

        @Override
        protected Object doInBackground(Object[] p1) {
            _apps.clear();
            _nonFilteredApps.clear();

            // work profile support
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LauncherApps launcherApps = (LauncherApps) _context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
                List<UserHandle> profiles = launcherApps.getProfiles();
                for (UserHandle userHandle : profiles) {
                    // TODO lots of stuff required with the rest of the app to get this working
                    //List<LauncherActivityInfo> apps = launcherApps.getActivityList(null, userHandle);
                    //for (LauncherActivityInfo info : apps) {
                    //    _nonFilteredApps.add(new App(_packageManager, info.getApplicationInfo()));
                    //}
                }
            }

            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> activitiesInfo = _packageManager.queryIntentActivities(intent, 0);
            for (ResolveInfo info : activitiesInfo) {
                App app = new App(_packageManager, info);
                //if (!_nonFilteredApps.contains(app)) {
                app.setIcon(test(app.getIcon(), app.getPackageName()));
                _nonFilteredApps.add(app);
                //}
            }

            // sort the apps by label here
            Collections.sort(_nonFilteredApps, new Comparator<App>() {
                @Override
                public int compare(App one, App two) {
                    return Collator.getInstance().compare(one._label, two._label);
                }
            });

            List<String> hiddenList = AppSettings.get().getHiddenAppsList();
            if (hiddenList != null) {
                for (int i = 0; i < _nonFilteredApps.size(); i++) {
                    boolean shouldGetAway = false;
                    for (String hidItemRaw : hiddenList) {
                        if ((_nonFilteredApps.get(i)._packageName + "/" + _nonFilteredApps.get(i)._className).equals(hidItemRaw)) {
                            shouldGetAway = true;
                            break;
                        }
                    }
                    if (!shouldGetAway) {
                        _apps.add(_nonFilteredApps.get(i));
                    }
                }
            } else {
                for (ResolveInfo info : activitiesInfo)
                    _apps.add(new App(_packageManager, info));
            }

            AppSettings appSettings = AppSettings.get();
            if (!appSettings.getIconPack().isEmpty() && Tool.isPackageInstalled(appSettings.getIconPack(), _packageManager)) {
                IconPackHelper.applyIconPack(AppManager.this, Tool.dp2px(appSettings.getIconSize()), appSettings.getIconPack(), _apps);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            notifyUpdateListeners(_apps);

            List<App> removed = getRemovedApps(tempApps, _apps);
            if (removed.size() > 0) {
                notifyRemoveListeners(removed);
            }

            if (_recreateAfterGettingApps) {
                _recreateAfterGettingApps = false;
                if (_context instanceof HomeActivity)
                    ((HomeActivity) _context).recreate();
            }

            super.onPostExecute(result);
        }
    }

    private Drawable test(Drawable drawable, String x) {
        float _iconSize = Tool.dp2px(Setup.appSettings().getIconSize());
        Bitmap source = createIconBitmap(drawable, _context, (int) _iconSize, x);
        return new BitmapDrawable(_context.getResources(), makeBoundBitmap(source, (int) _iconSize));
    }


    public static boolean needToZoom(Bitmap source, String packetName) {
        int startWidth = source.getWidth() / 2;
        int count = 0;
        for (int y = 0; y < 20; y++) {
            int alpha = ((source.getPixel(startWidth, y) & 0xff000000) >> 24);
            if (alpha == 0) {
                count++;
            }
        }
        return !packetName.contains("com.zing.mp3") && !packetName.contains("com.google.android.apps.photos") && count <= 5;
    }

    private Bitmap makeBoundBitmap(Bitmap source, int _iconSize) {

        if (source == null) {
            throw new NullPointerException("Bitmap null");
        }

        RectF rectF = new RectF(
                0,
                0,
                _iconSize,
                _iconSize
        );

        Bitmap result = Bitmap.createBitmap(_iconSize, _iconSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP));
        maskPaint.setStyle(Paint.Style.FILL);
        maskPaint.setColor(Color.WHITE);
        canvas.drawRoundRect(
                rectF,
                50, // rx
                50, // ry
                maskPaint
        );

        Matrix matrix = new Matrix();
        Paint resultPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        resultPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(source, matrix, resultPaint);
        return result;
    }

    private static final Canvas sCanvas = new Canvas();
    private static final Rect sOldBounds = new Rect();

    /**
     * Returns a bitmap suitable for the all apps view.
     */
    public static Bitmap createIconBitmap(Drawable icon, Context context, int iconSize, String packetName) {
        synchronized (sCanvas) {

            Bitmap bm = drawableToBitmap(icon);
            int width = iconSize;
            int height = iconSize;
            if (needToZoom(bm, packetName)) {
                width = (int) (iconSize * 1.2);
                height = (int) (iconSize * 1.2);
            }

            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                // Ensure the bitmap has a density.
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }
            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();
            if (sourceWidth > 0 && sourceHeight > 0) {
                // Scale the icon proportionally to the icon dimensions
                final float ratio = (float) sourceWidth / sourceHeight;
                if (sourceWidth > sourceHeight) {
                    height = (int) (width / ratio);
                } else if (sourceHeight > sourceWidth) {
                    width = (int) (height * ratio);
                }
            }
            // no intrinsic size --> use default size

            final Bitmap bitmap = Bitmap.createBitmap(iconSize, iconSize,
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            final int left = (iconSize - width) / 2;
            final int top = (iconSize - height) / 2;

            sOldBounds.set(icon.getBounds());
            icon.setBounds(left, top, left + width, top + height);
            icon.draw(canvas);
            icon.setBounds(sOldBounds);
            canvas.setBitmap(null);

            return bitmap;
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }


    public static List<App> getRemovedApps(List<App> oldApps, List<App> newApps) {
        List<App> removed = new ArrayList<>();
        // if this is the first call then return an empty list
        if (oldApps.size() == 0) {
            return removed;
        }
        for (int i = 0; i < oldApps.size(); i++) {
            if (!newApps.contains(oldApps.get(i))) {
                removed.add(oldApps.get(i));
                break;
            }
        }
        return removed;
    }

}
