# 操作步骤

1. 因为 MacOS 的文件系统默认不区分大小写，为了方便在 MacOS 系统上反编译 apk 的代码，所以最好添加
   `-dontusemixedcaseclassnames`选项。
2. 集成 `logback-android` 为 jupnp 打开日志，以方便定位问题。
   在文件 `libs.version.toml`中添加依赖定义
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

   在文件 `app/build.gradle.kts` 中添加依赖
   ```kotlin
   implementation(libs.bundles.logback)
   ```

3. 为 `release` 编译准备签名信息

4. 以 `release` 模式运行，并根据日志信息，添加 proguard 规则，修复问题
