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

# Keep DTOs and Room Entities
-keep class com.ihor.thesystem.data.remote.dto.** { *; }
-keep class com.ihor.thesystem.data.local.room.entity.** { *; }
-keep class com.ihor.thesystem.domain.model.** { *; }

# Hilt/Dagger
-keep class dagger.hilt.android.internal.** { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Gemini AI (Google Generative AI)
-keep class com.google.ai.client.generativeai.** { *; }
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
