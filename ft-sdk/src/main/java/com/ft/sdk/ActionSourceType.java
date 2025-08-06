package com.ft.sdk;

/**
 * Click source type
 */
public enum ActionSourceType {
    CLICK_VIEW,             // Normal View click
    CLICK_LIST_ITEM,        // ListView、GridView item
    CLICK_EXPAND_LIST_ITEM, // ExpandListView item
    CLICK_EXPAND_GROUP_ITEM, // ExpandListView group
    CLICK_RADIO_BUTTON, // ExpandListView
    CLICK_TAB,              // Tab item
    CLICK_DIALOG_BUTTON,    // DialogInterface button
    CLICK_MENU_ITEM,        // MenuItem
    CLICK_BACK,
    CLICK_MENU,
    CLICK_PAD_CENTER,
    LAUNCH_HOT,             // launch hot
    LAUNCH_COLD,             // launch cold
}