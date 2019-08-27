package com.vietbm.edgelauncher.interfaces;

import com.vietbm.edgelauncher.model.App;

import java.util.List;

public interface AppUpdateListener {
    boolean onAppUpdated(List<App> apps);
}
