package com.vietbm.edgelauncher.activity.homeparts;

import android.graphics.Point;

import com.vietbm.edgelauncher.activity.HomeActivity;
import com.vietbm.edgelauncher.interfaces.DialogListener;
import com.vietbm.edgelauncher.manager.Setup;
import com.vietbm.edgelauncher.model.Item;
import com.vietbm.edgelauncher.util.Definitions.ItemPosition;
import com.vietbm.edgelauncher.widget.Desktop;
import com.vietbm.edgelauncher.widget.Dock;

public class HpAppEditApplier implements DialogListener.OnEditDialogListener {
    private HomeActivity _homeActivity;
    private Item _item;

    public HpAppEditApplier(HomeActivity homeActivity) {
        _homeActivity = homeActivity;
    }

    public void onEditItem(final Item item) {
        _item = item;
        Setup.eventHandler().showEditDialog(_homeActivity, item, this);
    }

    @Override
    public void onRename(String name) {
        _item.setLabel(name);
        Setup.dataManager().saveItem(_item);
        Point point = new Point(_item._x, _item._y);

        if (_item._location.equals(ItemPosition.Desktop)) {
            Desktop desktop = _homeActivity.getDesktop();
            desktop.removeItem(desktop.getCurrentPage().coordinateToChildView(point), false);
            desktop.addItemToCell(_item, _item._x, _item._y);
        } else {
            Dock dock = _homeActivity.getDock();
            _homeActivity.getDock().removeItem(dock.coordinateToChildView(point), false);
            dock.addItemToCell(_item, _item._x, _item._y);
        }
    }
}
