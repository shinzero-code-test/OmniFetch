# ProGuard rules for OmniFetch

# Keep yt-dlp classes
-keep class com.github.yausername.youtubedl_android.** { *; }

# Keep Room entities
-keep class com.exapps.omnifetch.data.local.entity.** { *; }

# Keep serializable classes
-keepattributes *Annotation*
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}

# General Android rules
-dontwarn javax.annotation.**
-keepattributes Signature
-keepattributes *Annotation*
