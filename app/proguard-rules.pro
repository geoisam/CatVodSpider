# 字典配置
-obfuscationdictionary build/obfuscation-dictionary.txt
-classobfuscationdictionary build/class-dictionary.txt
-packageobfuscationdictionary build/package-dictionary.txt


# 基础优化
-flattenpackagehierarchy com.github.catvod.spider.merge


# Dontwarn
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.xmlpull.v1.**
-dontwarn com.google.re2j.**
-dontwarn android.content.res.**
-dontwarn org.ietf.jgss.**
-dontwarn javax.**
-dontwarn sun.misc.**
-dontwarn org.codehaus.mojo.animal_sniffer.*


# AndroidX
-keep class androidx.core.** { *; }


# Spider
-keep class com.github.catvod.crawler.* { *; }
-keep class com.github.catvod.spider.* { public <methods>; }


# Gson
-keepattributes Signature
-keepattributes *Annotation*

-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken


# OkHttp
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-adaptresourcefilenames okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz


# QuickJS
-keep class com.whl.quickjs.** { *; }


# Logger
-keep class com.orhanobut.logger.** { *; }


# 其他
-keepattributes SourceFile,LineNumberTable
-keep class * extends android.app.Activity
-keep class * extends android.app.Application
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider
-keep class * extends android.webkit.WebViewClient
-keep class * extends android.webkit.WebChromeClient