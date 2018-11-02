# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in E:\android_studio2.0\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-dontskipnonpubliclibraryclasses # 不忽略非公共的库类
-optimizationpasses 5             # 指定代码的压缩级别
-dontusemixedcaseclassnames      # 是否使用大小写混合
-dontpreverify                    # 混淆时是否做预校验
-verbose                          # 混淆时是否记录日志
-keepattributes *Annotation*     # 保持注解
-ignorewarning                   # 忽略警告
-dontoptimize                    # 优化不优化输入的类文件

-keepattributes SourceFile,LineNumberTable
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * implements de.robv.android.xposed.IXposedHookZygoteInit
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.preference.Preference
-keep public class * extends android.widget.TextView
-keepclassmembers class * extends android.app.Activity{
     public static boolean isModuleActive();
}