// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id(BuildPlugins.androidApplication.module) version BuildPlugins.androidApplication.version apply false
    id(BuildPlugins.androidKotlin.module) version BuildPlugins.androidKotlin.version apply false
    id(BuildPlugins.hiltPlugin.module) version
            BuildPlugins.hiltPlugin.version apply false
    id(BuildPlugins.androidLibrary.module) version BuildPlugins.androidApplication.version apply false
    id(BuildPlugins.kotlinxSerialization.module) version
            BuildPlugins.kotlinxSerialization.version apply false
}

buildscript {
    dependencies {
        classpath(BuildPlugins.androidJunit5.mergedId)
        classpath(BuildPlugins.ciUtilsPlugin.mergedId)
        classpath(BuildPlugins.hilt.mergedId)
    }
}

allprojects {
    apply(plugin = BuildPlugins.ciUtilsPlugin.module)
}