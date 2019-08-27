package com.vietbm.edgelauncher.activity.homeparts;

import android.graphics.Point;

import com.vietbm.edgelauncher.R;
import com.vietbm.edgelauncher.activity.HomeActivity;
import com.vietbm.edgelauncher.interfaces.DialogListener;
import com.vietbm.edgelauncher.manager.Setup;
import com.vietbm.edgelauncher.model.Item;
import com.vietbm.edgelauncher.util.Tool;

public class HpDesktopPickAction implements DialogListener.OnActionDialogListener {
    private HomeActivity _homeActivity;

    public HpDesktopPickAction(HomeActivity homeActivity) {
        _homeActivity = homeActivity;
    }

    public void onPickDesktopAction() {
        Setup.eventHandler().showPickAction(_homeActivity, this);
    }

    @Override
    public void onAdd(int type) {
        Point pos = _homeActivity.getDesktop().getCurrentPage().findFreeSpace();
        if (pos != null) {
            _homeActivity.getDesktop().addItemToCell(Item.newActionItem(type), pos.x, pos.y);
        } else {
            Tool.toast(_homeActivity, R.string.toast_not_enough_space);
        }
    }
}
