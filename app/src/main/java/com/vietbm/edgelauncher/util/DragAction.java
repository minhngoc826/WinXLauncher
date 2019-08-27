package com.vietbm.edgelauncher.util;

public class DragAction {
    public Action action;

    public DragAction(Action action) {
        this.action = action;
    }

    public enum Action {
        DESKTOP, DRAWER, SEARCH, DOCK
    }
}
