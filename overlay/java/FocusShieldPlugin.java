package com.studyforge.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "FocusShield")
public class FocusShieldPlugin extends Plugin {
    static final String PREFS = "focus_shield";
    static final String KEY_UNTIL = "until";

    private SharedPreferences prefs() {
        return getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** { active: boolean, until?: epoch ms }  Default safety cap: 3 hours. */
    @PluginMethod
    public void setFocusActive(PluginCall call) {
        boolean active = Boolean.TRUE.equals(call.getBoolean("active", false));
        long until = 0;
        if (active) {
            Double u = call.getDouble("until");
            until = (u != null) ? u.longValue() : System.currentTimeMillis() + 3L * 60 * 60 * 1000;
        }
        prefs().edit().putLong(KEY_UNTIL, until).apply();
        call.resolve();
    }

    @PluginMethod
    public void isServiceEnabled(PluginCall call) {
        String enabled = Settings.Secure.getString(
            getContext().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        ComponentName cn = new ComponentName(getContext(), ReelsBlockerService.class);
        boolean on = enabled != null
            && (enabled.contains(cn.flattenToString()) || enabled.contains(cn.flattenToShortString()));
        JSObject ret = new JSObject();
        ret.put("enabled", on);
        call.resolve(ret);
    }

    @PluginMethod
    public void openAccessibilitySettings(PluginCall call) {
        Intent i = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(i);
        call.resolve();
    }
}
