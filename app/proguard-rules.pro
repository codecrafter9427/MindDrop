# Keep line numbers so release crash reports stay readable, but hide the
# original source file names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---------------------------------------------------------------------------
# TensorFlow Lite
# ---------------------------------------------------------------------------
# TFLite reaches parts of its interpreter and delegates reflectively through
# JNI, so R8 cannot see those references and would strip them. GPU delegate
# classes are referenced optionally and absent from this build, hence dontwarn
# rather than a keep.
-keep class org.tensorflow.lite.** { *; }
-keepclassmembers class org.tensorflow.lite.** { *; }
-dontwarn org.tensorflow.lite.gpu.**
-dontwarn org.tensorflow.lite.**

# ---------------------------------------------------------------------------
# Hilt / Dagger
# ---------------------------------------------------------------------------
# Hilt and Dagger ship consumer rules covering their generated components;
# these cover the reflective entry points those rules assume.
-keep class dagger.hilt.** { *; }
-keepclasseswithmembernames class * {
    @dagger.* <fields>;
}
-keepclasseswithmembernames class * {
    @dagger.* <methods>;
}

# ---------------------------------------------------------------------------
# Room
# ---------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-dontwarn androidx.room.paging.**

# ---------------------------------------------------------------------------
# kotlinx.serialization (type-safe navigation routes)
# ---------------------------------------------------------------------------
# Navigation Compose serializes the @Serializable route classes, so their
# generated serializers must survive minification.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.dins.minddrop.navigation.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class com.dins.minddrop.navigation.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---------------------------------------------------------------------------
# Domain models
# ---------------------------------------------------------------------------
# Enum names are persisted to Room and DataStore as strings and read back with
# valueOf, so obfuscating them would break stored data across an upgrade.
-keepclassmembers enum com.dins.minddrop.domain.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
