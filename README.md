This is an educational project demonstrating how to use jupnp on Android.

# Logback
To enable logs for jupnp sdk, [logback-android](https://github.com/tony19/logback-android) will be help.

In file: `libs.version.toml`
```toml
[versions]
slf4j = "2.0.17"
logbackAndroid = "3.0.0"

[libraries]
slf4j = { group = "org.slf4j", name = "slf4j-api", version.ref = "slf4j" }
logback-android = { group = "com.github.tony19", name = "logback-android", version.ref = "logbackAndroid" }

[bundles]
logback = ["slf4j", "logback-android"]
```

In file: `app/build.gradle.kts`
```kotlin
implementation(libs.bundles.logback)
```

Adding `logback.xml` to `app/src/main/assets/logback.xml`, 
please note change the LOG_DIR to any directory your app has permission to write.
```xml
<configuration xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns="https://tony19.github.io/logback-android/xml"
    xsi:schemaLocation="https://tony19.github.io/logback-android/xml https://cdn.jsdelivr.net/gh/tony19/logback-android/logback.xsd">

    <property name="LOG_DIR"
        value="/sdcard/Android/data/org.jupnp.example.binarylight/files/logs" />

    <!-- Create a logcat appender -->
    <appender name="logcat" class="ch.qos.logback.classic.android.LogcatAppender">
        <encoder>
            <pattern>%msg</pattern>
        </encoder>
    </appender>

    <root level="ALL">
        <appender-ref ref="logcat" />
    </root>
</configuration>
```

And don't miss the proguard rules for logback if you try to enable `Minify`

```
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
```

# ProGuard

When optimizing your application with ProGuard, include the following rules to make jupnp working
properly:

```
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
```