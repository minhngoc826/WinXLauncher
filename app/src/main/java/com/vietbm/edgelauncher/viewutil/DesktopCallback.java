package com.vietbm.edgelauncher.viewutil;

import android.view.View;

import com.vietbm.edgelauncher.interfaces.ItemHistory;
import com.vietbm.edgelauncher.model.Item;

public interface DesktopCallback extends ItemHistory {
    boolean addItemToPoint(Item item, int x, int y);

    boolean addItemToPage(Item item, int page);

    boolean addItemToCell(Item item, int x, int y);

    void removeItem(View view, boolean animate);
}
