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
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ===============================
# Firebase/Firestore Rules
# ===============================

# Keep all model classes used by Firebase Firestore
-keep class com.humblecoders.matricareog.model.** { *; }

# Keep all data classes with their constructors and fields
-keepclassmembers class com.humblecoders.matricareog.model.** {
    <init>();
    <init>(...);
    <fields>;
}

# Keep repository classes that might be used by Firebase
-keep class com.humblecoders.matricareog.repository.** { *; }

# Keep specific classes that Firebase needs to deserialize
-keep class com.humblecoders.matricareog.model.User { *; }
-keep class com.humblecoders.matricareog.model.MedicalHistory { *; }
-keep class com.humblecoders.matricareog.model.PersonalInformation { *; }
-keep class com.humblecoders.matricareog.model.PregnancyHistory { *; }
-keep class com.humblecoders.matricareog.model.AuthResult { *; }
-keep class com.humblecoders.matricareog.model.AuthResult$* { *; }
-keep class com.humblecoders.matricareog.model.HealthDataPoint { *; }
-keep class com.humblecoders.matricareog.model.ChartData { *; }
-keep class com.humblecoders.matricareog.model.HealthReport { *; }
-keep class com.humblecoders.matricareog.model.BloodPressure { *; }
-keep class com.humblecoders.matricareog.model.HealthMetric { *; }
-keep class com.humblecoders.matricareog.model.HealthStatus { *; }
-keep class com.humblecoders.matricareog.model.PregnancyInfo { *; }
-keep class com.humblecoders.matricareog.model.MatriCareState { *; }
-keep class com.humblecoders.matricareog.model.MatriCareState$* { *; }

# Keep repository data classes
-keep class com.humblecoders.matricareog.repository.MatriCareRepository$PredictionHistoryItem { *; }
-keep class com.humblecoders.matricareog.repository.MatriCareRepository$RiskHistoryItem { *; }

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep attributes required for serialization
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# ===============================
# Firebase specific rules
# ===============================

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.android.gms.internal.** { *; }

# Firebase Firestore
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.firebase.Timestamp { *; }
-keep class com.google.firebase.firestore.DocumentSnapshot { *; }
-keep class com.google.firebase.firestore.QuerySnapshot { *; }

# Firebase common
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# ===============================
# General Android/Kotlin rules
# ===============================

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep view constructors
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep activity methods
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

# ===============================
# Kotlin specific rules
# ===============================

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep data class generated methods
-keepclassmembers class * {
    *** component1();
    *** component2();
    *** component3();
    *** component4();
    *** component5();
    *** copy(...);
}

# ===============================
# TensorFlow Lite (for ML model)
# ===============================

-keep class org.tensorflow.lite.** { *; }
-keep class org.tensorflow.lite.support.** { *; }
-dontwarn org.tensorflow.lite.**