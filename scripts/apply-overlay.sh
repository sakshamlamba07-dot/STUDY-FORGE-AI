#!/usr/bin/env bash
set -euo pipefail
APP=android/app/src/main
JAVA_DIR=$APP/java/com/studyforge/app

cp overlay/java/*.java "$JAVA_DIR/"
mkdir -p $APP/res/xml $APP/res/values
cp overlay/res/xml/focus_shield_service.xml $APP/res/xml/
cp overlay/res/values/focus_shield_strings.xml $APP/res/values/

python3 - <<'PY'
p = 'android/app/src/main/AndroidManifest.xml'
s = open(p).read()
snip = open('overlay/service-snippet.xml').read()
if 'ReelsBlockerService' not in s:
    s = s.replace('</application>', snip + '\n    </application>')
    open(p, 'w').write(s)
    print('Manifest patched')
else:
    print('Manifest already patched')
PY
