# Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.wcapp.**$$serializer { *; }
-keepclassmembers class com.wcapp.** {
    *** Companion;
}
-keepclasseswithmembers class com.wcapp.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Coil
-dontwarn coil.**

# Koin
-keep class org.koin.** { *; }
