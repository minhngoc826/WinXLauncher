package com.vietbm.edgelauncher.interfaces;

import android.view.View;

import com.vietbm.edgelauncher.model.Item;

public interface ItemHistory {
    void setLastItem(Item item, View view);

    void revertLastItem();

    void consumeLastItem();
}
