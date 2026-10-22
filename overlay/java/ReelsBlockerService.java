package com.studyforge.app;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
import java.util.List;

/**
 * While StudyForge focus mode is active, bounces the user out of Instagram Reels.
 * Reel detection relies on Instagram's internal view IDs (best-effort, may change
 * with Instagram updates; see README for how to refresh them).
 */
public class ReelsBlockerService extends AccessibilityService {
    private static final String TAG = "FocusShield";
    private static final String IG = "com.instagram.android";

    // Present only while a reels viewer is on screen.
    private static final String[] REEL_VIEW_IDS = {
        "com.instagram.android:id/clips_viewer_view_pager",
        "com.instagram.android:id/clips_swipe_refresh_container",
        "com.instagram.android:id/root_clips_layout"
    };
    private static final String REELS_TAB_ID = "com.instagram.android:id/clips_tab";

    private long lastAction = 0;
    private long lastToast = 0;

    private boolean focusActive() {
        long until = getSharedPreferences(FocusShieldPlugin.PREFS, Context.MODE_PRIVATE)
            .getLong(FocusShieldPlugin.KEY_UNTIL, 0);
        return until > System.currentTimeMillis();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        CharSequence pkg = event.getPackageName();
        if (pkg == null || !IG.contentEquals(pkg)) return;
        if (!focusActive()) return;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;
        try {
            String hit = reelsMatch(root);
            if (hit != null) block(hit);
        } finally {
            root.recycle();
        }
    }

    private String reelsMatch(AccessibilityNodeInfo root) {
        for (String id : REEL_VIEW_IDS) {
            List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(id);
            if (nodes != null && !nodes.isEmpty()) return id;
        }
        List<AccessibilityNodeInfo> tabs = root.findAccessibilityNodeInfosByViewId(REELS_TAB_ID);
        if (tabs != null) {
            for (AccessibilityNodeInfo t : tabs) {
                if (t.isSelected()) return REELS_TAB_ID + " (selected)";
            }
        }
        return null;
    }

    private void block(String matchedId) {
        long now = SystemClock.elapsedRealtime();
        if (now - lastAction < 600) return; // debounce
        // First hit: Back. If still in Reels shortly after, go Home.
        int action = (now - lastAction < 3000) ? GLOBAL_ACTION_HOME : GLOBAL_ACTION_BACK;
        lastAction = now;
        Log.d(TAG, "Blocking reels via " + matchedId + " action=" + action);
        performGlobalAction(action);
        if (now - lastToast > 4000) {
            lastToast = now;
            Toast.makeText(this, "Focus mode: Reels are blocked", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onInterrupt() {}
}
