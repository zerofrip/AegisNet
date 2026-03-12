# AegisNet ProGuard Rules

# Keep JNI methods called from native code
-keepclassmembers class com.aegisnet.singbox.SingBoxController {
    native <methods>;
    public boolean protect(int);
}

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Retrofit & Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class retrofit2.** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# kotlinx-serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keep,includedescriptorclasses class com.aegisnet.**$$serializer { *; }

# Compose
-dontwarn androidx.compose.**

# Keep data classes used in JSON serialization
-keep class com.aegisnet.database.entity.** { *; }
-keep class com.aegisnet.singbox.model.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
