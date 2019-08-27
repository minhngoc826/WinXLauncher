package com.vietbm.edgelauncher.interfaces;

import com.vietbm.edgelauncher.model.App;

import java.util.List;

public interface AppDeleteListener {
    boolean onAppDeleted(List<App> apps);
}
