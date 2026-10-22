// Drop into your Next.js app (e.g. lib/focusShield.js). No npm dependency needed:
// inside the Android APK, Capacitor injects window.Capacitor.Plugins.FocusShield.
// In a normal browser/PWA every function is a safe no-op.

const plugin = () =>
  typeof window !== 'undefined' ? window.Capacitor?.Plugins?.FocusShield : null;

export const focusShieldAvailable = () => !!plugin();

export async function startFocusShield(minutes = 180) {
  return plugin()?.setFocusActive({ active: true, until: Date.now() + minutes * 60000 });
}

export async function stopFocusShield() {
  return plugin()?.setFocusActive({ active: false });
}

export async function isShieldServiceOn() {
  const r = await plugin()?.isServiceEnabled();
  return r?.enabled ?? false;
}

export function openShieldSettings() {
  return plugin()?.openAccessibilitySettings();
}

// Wiring (in your focus timer):
//   on session start / resume -> startFocusShield(remainingMinutes)
//   on pause / stop / finish  -> stopFocusShield()
// Settings card: if focusShieldAvailable() && !(await isShieldServiceOn())
//   show an "Enable Focus Shield" button that calls openShieldSettings().
