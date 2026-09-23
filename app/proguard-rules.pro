# Proguard rules for Save app

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Jsoup
-keep public class org.jsoup.** { public *; }

# Hilt
-keep class com.damianrdev.save.** { *; }
