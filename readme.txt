./adb forward tcp:4444 localabstract:/adb-hub; ./adb connect localhost:4444

adb -s localhost:4444 uninstall com.example.testandroidwear
