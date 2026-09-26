# --- ProGuard & R8 Optimization Rules for LearnGo ---

# Preserve Data Models
-keep class com.novamindlabs.learngo.data.model.** { *; }

# Preserve ViewBinding Classes
-keep class com.novamindlabs.learngo.databinding.** { *; }

# Preserve Firebase & Google Services
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.firebase.storage.** { *; }
-keep class com.google.android.gms.auth.api.** { *; }

# Preserve Hilt & Dagger
-keep class dagger.hilt.** { *; }

# Preserve Kotlin Coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }

# Preserve Play Billing Client
-keep class com.android.billingclient.api.** { *; }

# Preserve Line Numbers for Crash Reporting
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Optimization Settings
-optimizationpasses 5
-allowaccessmodification