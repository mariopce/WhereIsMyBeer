#!/bin/bash
sed -i "s/TTKEY/$SECRET/g" app/src/main/AndroidManifest.xml
./gradlew insDeb
sed -i "s/$SECRET/TTKEY/g" app/src/main/AndroidManifest.xml
