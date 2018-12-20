package com.click369.controlbp.util;

import android.app.Activity;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by 41856 on 2018/12/4.
 */

public class SoftKeyboardStateHelper implements ViewTreeObserver.OnGlobalLayoutListener {
    private View activityRootView;
    private int   lastSoftKeyboardHeightInPx;
    private boolean isSoftKeyboardOpened;
    private int height ;//控制软键盘打开时机

    public interface SoftKeyboardStateListener {
        void onSoftKeyboardOpened(int keyboardHeightInPx);
        void onSoftKeyboardClosed();
    }

    private final List<SoftKeyboardStateListener> listeners = new LinkedList<SoftKeyboardStateListener>();


    public SoftKeyboardStateHelper(Activity contextObj) {
        if (contextObj == null) {
            Log.i("dota", "contextObj is null");
            return;
        }
        activityRootView = findContentView(contextObj);
        if (activityRootView!=null){
            activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        }
//        this(activityRootView, false);
    }

    public SoftKeyboardStateHelper(Activity contextObj, boolean isSoftKeyboardOpened) {
        if (contextObj == null) {
            Log.i("dota", "contextObj is null");
            return;
        }
        activityRootView = findContentView(contextObj);
        if (activityRootView!=null){
            activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        }
//        this.activityRootView     = activityRootView;
//        this.isSoftKeyboardOpened = isSoftKeyboardOpened;
    }

    private View findContentView(Activity contextObj) {
        return contextObj.findViewById(android.R.id.content);
    }

    @Override
    public void onGlobalLayout() {
        final Rect r = new Rect();
        activityRootView.getWindowVisibleDisplayFrame(r);

        final int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);
        if (heightDiff == 1920){
            height = 200;
        }else {
            height = 100;
        }
        if (!isSoftKeyboardOpened && heightDiff > height ) { // if more than 100 pixels, its probably a keyboard...
            isSoftKeyboardOpened = true;
            notifyOnSoftKeyboardOpened(heightDiff);
        } else if (isSoftKeyboardOpened && heightDiff < height ) {
            isSoftKeyboardOpened = false;
            notifyOnSoftKeyboardClosed();
        }
    }

    public void setIsSoftKeyboardOpened(boolean isSoftKeyboardOpened) {
        this.isSoftKeyboardOpened = isSoftKeyboardOpened;
    }

    public boolean isSoftKeyboardOpened() {
        return isSoftKeyboardOpened;
    }

    /**
     * Default value is zero (0)
     * @return last saved keyboard height in px
     */
    public int getLastSoftKeyboardHeightInPx() {
        return lastSoftKeyboardHeightInPx;
    }

    public void addSoftKeyboardStateListener(SoftKeyboardStateListener listener) {
        listeners.add(listener);
    }

    public void removeSoftKeyboardStateListener(SoftKeyboardStateListener listener) {
        listeners.remove(listener);
    }

    private void notifyOnSoftKeyboardOpened(int keyboardHeightInPx) {
        this.lastSoftKeyboardHeightInPx = keyboardHeightInPx;

        for (SoftKeyboardStateListener listener : listeners) {
            if (listener != null) {
                listener.onSoftKeyboardOpened(keyboardHeightInPx);
            }
        }
    }

    private void notifyOnSoftKeyboardClosed() {
        for (SoftKeyboardStateListener listener : listeners) {
            if (listener != null) {
                listener.onSoftKeyboardClosed();
            }
        }
    }
}
