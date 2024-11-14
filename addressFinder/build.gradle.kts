// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
//    classpath ("com.google.gms:google-services:4.3.10")
    alias(libs.plugins.android.application) apply false
}
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath ("com.android.tools.build:gradle:7.0.4") // Make sure it's the right version
        classpath ("com.google.gms:google-services:4.3.15")  // Add this line for Firebase
    }
}
