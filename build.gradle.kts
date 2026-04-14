// Root build.gradle.kts
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.7.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.52")
        classpath("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.0.21")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
