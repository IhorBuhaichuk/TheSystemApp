# Keep all domain models and enums to prevent R8 from breaking Room TypeConverters and Serialization
-keep class com.ihor.thesystem.domain.model.** { *; }
-keepclassmembers enum com.ihor.thesystem.domain.model.** { *; }

# Kotlin Serialization
-keepattributes *Annotation*, EnclosingMethod, InnerClasses, Signature
-keep,allowobfuscation,allowshrinking class kotlinx.serialization.json.** { *; }
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class ** {
    *** Companion;
    *** $serializer;
}

# Room specific rules
-keep class androidx.room.BoundDb { *; }
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.TypeConverter

# Hilt
-keep class dagger.hilt.android.internal.** { *; }

# Gemini AI
-keep class com.google.ai.client.generativeai.** { *; }
