// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.4" 
    id("org.jetbrains.kotlin.android") version "1.9.0"
    id ('org.jetbrains.kotlin.plugin.serialization') version '1.9.21'
}
dependencies {
  // Lifecycle & ViewModel
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0"
    implementation "androidx.lifecycle:lifecycle-livedata-ktx:2.7.0"
    implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.7.0"
    
    // Coroutines
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
    
    // Bolt Database
    implementation "io.github.jan-tennert.Bolt Database:postgrest-kt:2.0.0"
    implementation "io.github.jan-tennert.Bolt Database:realtime-kt:2.0.0"
    implementation "io.ktor:ktor-client-android:2.3.7"
    
    // Kotlinx Serialization
    implementation "org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2"
    
    // ViewPager2 for carousel
    implementation "androidx.viewpager2:viewpager2:1.0.0"
    
    // CardView for flashcards
    implementation "androidx.cardview:cardview:1.0.0"
    
    // RecyclerView
    implementation "androidx.recyclerview:recyclerview:1.3.2"

    
    
}
