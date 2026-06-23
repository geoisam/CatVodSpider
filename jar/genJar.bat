@echo off
chcp 65001 >nul

set OUTPUT_JAR=geoisam.jar

del "%~dp0\%OUTPUT_JAR%" 2>nul
:: rd /s /q "%~dp0\Smali_classes" 2>nul
rd /s /q "%~dp0\APK_decode" 2>nul

:: java -jar "%~dp0\tools\baksmali-2.5.2.jar" d "%~dp0\..\app\build\intermediates\dex\release\minifyReleaseWithR8\classes.dex" -o "%~dp0\Smali_classes"
java -jar "%~dp0\tools\apktool_2.12.1.jar" d "%~dp0\..\app\build\outputs\apk\release\app-release-unsigned.apk" --only-main-classes -f -o "%~dp0\APK_decode"

rd /s /q "%~dp0\spider.jar\smali\com\github\catvod\spider" 2>nul

if not exist "%~dp0\spider.jar\smali\com\github\catvod\" md "%~dp0\spider.jar\smali\com\github\catvod\"

:: move "%~dp0\Smali_classes\com\github\catvod\spider" "%~dp0\spider.jar\smali\com\github\catvod\"
move "%~dp0\APK_decode\smali\com\github\catvod\spider" "%~dp0\spider.jar\smali\com\github\catvod\"

java -jar "%~dp0\tools\apktool_2.12.1.jar" b "%~dp0\spider.jar" -c

move "%~dp0\spider.jar\dist\dex.jar" "%~dp0\%OUTPUT_JAR%"

certUtil -hashfile "%~dp0\%OUTPUT_JAR%" MD5 | find /i /v "md5" | find /i /v "certutil" > "%~dp0\%OUTPUT_JAR%.md5"

:: rd /s /q "%~dp0\Smali_classes" 2>nul
rd /s /q "%~dp0\APK_decode" 2>nul
rd /s /q "%~dp0\spider.jar\build" 2>nul
rd /s /q "%~dp0\spider.jar\smali" 2>nul
rd /s /q "%~dp0\spider.jar\dist" 2>nul