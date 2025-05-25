# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontusemixedcaseclassnames

# region logback-android
-keepattributes *Annotation*
-dontwarn ch.qos.logback.core.net.*

# Issue #229
-keepclassmembers class ch.qos.logback.classic.pattern.* { <init>(); }

# The following rules should only be used if you plan to keep
# the logging calls in your released app.
-keepclassmembers class ch.qos.logback.** { *; }
-keepclassmembers class org.slf4j.impl.** { *; }

-keep class ** implements ch.qos.logback.core.Appender { *; }
#endregion

# region Upnp
-keepattributes *Annotation*

-keepclassmembers class ** extends org.jupnp.model.message.header.UpnpHeader {
   public <init>(...);
}

# When optimization is enabled, it is essential to preserve the annotations.
-keep @interface org.jupnp.binding.annotations.**

# Uncertain about the reason for the removal of annotations.
# These lines are used to preserve the names of classes, which in turn keeps the annotations intact.
# Since we create the UpnpService via AnnotationLocalServiceBinder.read(Class<?>), it is safe to allow shrinking.
-keepnames @org.jupnp.binding.annotations.UpnpService class **

-keepclassmembers @org.jupnp.binding.annotations.UpnpService class ** {
    public <init>(...);
    @org.jupnp.binding.annotations.UpnpStateVariable <fields>;
    @org.jupnp.binding.annotations.UpnpAction <methods>;
    public <methods>;
    public <fields>;
}

-dontwarn org.osgi.service.component.annotations.Component
-dontwarn org.osgi.service.metatype.annotations.Designate
# endregion
