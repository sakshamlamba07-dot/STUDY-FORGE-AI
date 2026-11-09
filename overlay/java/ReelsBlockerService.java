package com.studyforge.app;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

/**
 * While a StudyForge focus session is running, Instagram and YouTube cannot be used:
 * the moment either one comes to the foreground, the user is sent back to StudyForge.
 * (Class name kept so the manifest and plugin need no changes.)
 */
public class ReelsBlockerService extends AccessibilityService {
    private static final String TAG = "FocusShield";

    private static final String[] BLOCKED = {
        "com.instagram.android",
        "com.instagram.lite",
        "com.google.android.youtube"
    };

    private final Handler handler = new Handler(Looper.getMainLooper());
    private long lastAction = 0;
    private long lastToast = 0;

    private boolean focusActive() {
        long until = getSharedPreferences(FocusShieldPlugin.PREFS, Context.MODE_PRIVATE)
            .getLong(FocusShieldPlugin.KEY_UNTIL, 0);
        return until > System.currentTimeMillis();
    }

    private boolean isBlocked(CharSequence pkg) {
        if (pkg == null) return false;
        for (String b : BLOCKED) {
            if (b.contentEquals(pkg)) return true;
        }
        return false;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        if (!isBlocked(event.getPackageName()) || !focusActive()) return;

        long now = SystemClock.elapsedRealtime();
        if (now - lastAction < 700) return;
        lastAction = now;
        Log.d(TAG, "Blocking " + event.getPackageName());

        performGlobalAction(GLOBAL_ACTION_HOME);
        handler.postDelayed(this::openStudyForge, 150);

        if (now - lastToast > 4000) {
            lastToast = now;
            Toast.makeText(this, "Focus mode: Instagram and YouTube are blocked", Toast.LENGTH_SHORT).show();
        }
    }

    private void openStudyForge() {
        try {
            Intent i = getPackageManager().getLaunchIntentForPackage(getPackageName());
            if (i != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not reopen StudyForge", e);
        }
    }

    @Override
    public void onInterrupt() {}
}
