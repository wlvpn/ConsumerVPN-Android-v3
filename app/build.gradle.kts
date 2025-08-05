import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    id(BuildPlugins.androidApplication.module)
    id(BuildPlugins.androidKotlin.module)
    id(BuildPlugins.ksp.module)
    id(BuildPlugins.hiltPlugin.module)
    id(BuildPlugins.detekt.module) version BuildPlugins.detekt.version
    id(BuildPlugins.protoBuff.module) version BuildPlugins.protoBuff.version
    id(BuildPlugins.composeCompiler.module)
}

apply(plugin = BuildPlugins.androidJunit5ApplyName)

apply(from = "../config/quality/detekt/detekt-config.gradle")
apply(from = "../config/quality/lint/lint-config.gradle")

android {
    namespace = AndroidSdk.namespace
    compileSdk = AndroidSdk.compileSdk

    defaultConfig {
        applicationId = "com.wlvpn.consumervpn"  // Update this with your own application id
        minSdk = AndroidSdk.minSdk
        targetSdk = AndroidSdk.targetSdk
        versionCode = getVersionCode()
        versionName = getVersionName()
        buildConfigField("String", "CLIENT", "\"Android-${versionName}b${versionCode}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField ("String", "FLAGS_HOSTNAME", "\"https://static.wlvpn.com/flags/\"")

    }

    // These keys are intended for tests environments only, the release keys should not live in the gradle file
    signingConfigs {

        getByName("debug") {
            storeFile = rootProject.file("config/keystores/debug-consumer.jks")
            keyAlias = "consumervpn-v2"
            keyPassword = "5MLdn3mB7pCFJ7"
            storePassword = "5MLdn3mB7pCFJ7"
        }

        create("release") {
            storeFile = rootProject.file("config/keystores/staging-consumer.jks")
            keyAlias = "consumervpn-v2"
            keyPassword = "hy4f8dhETrnjME"
            storePassword = "hy4f8dhETrnjME"
        }
    }

    setFlavorDimensions(listOf("platform"))

    productFlavors {
        create("mobile") {
            dimension = "platform"
            buildConfigField(type = "boolean", name = "IS_TV_BUILD", value = "false")
        }

        create("tv") {
            dimension = "platform"
            buildConfigField(type = "boolean", name = "IS_TV_BUILD", value = "true")
        }
    }

    buildTypes {
        debug  {
            isDebuggable = true
            applicationIdSuffix = ".debug"

            signingConfig = signingConfigs.getByName("debug")

        }

        create("qa") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("release")
        }

        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Dependencies.compose.version
    }
    packaging {
        jniLibs {
            // OpenVPN does not work without this when is run through an aab bundle
            useLegacyPackaging = true
        }
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(Dependencies.kotlinCore.mergedId)
    implementation(Dependencies.lifecycle.mergedId)

    //Datastore
    implementation(Dependencies.dataStoreProto.mergedId)
    implementation(Dependencies.dataStoreCore.mergedId)
    implementation(Dependencies.dataStorePreferences.mergedId)

    // Compose
    implementation(platform(Dependencies.composeBom.mergedId))
    implementation(Dependencies.composeActivity.mergedId)
    implementation(Dependencies.composeUi.mergedId)
    implementation(Dependencies.composeUiGraphics.mergedId)
    implementation(Dependencies.composeUiPreview.mergedId)
    implementation(Dependencies.composeMaterial.mergedId)
    implementation(Dependencies.composeMaterialIcons.mergedId)
    implementation(Dependencies.composeNavigation.mergedId)
    implementation(Dependencies.composeRuntimeLivedata.mergedId)
    implementation(Dependencies.lifecycleRuntimeCompose.mergedId)
    implementation(Dependencies.googleFont.mergedId)
    implementation(Dependencies.accompanistPermissions.mergedId)

    // Coil
    implementation(Dependencies.coil.mergedId)

    //Dagger Hilt
    implementation(Dependencies.daggerHilt.mergedId)
    ksp(Dependencies.daggerHiltCompiler.mergedId)

    implementation(Dependencies.hiltNavigationCompose.mergedId)

    // Protobuff
    implementation(Dependencies.protoBuff.mergedId)

    // Logging
    implementation(Dependencies.logback.mergedId)
    implementation(Dependencies.sl4j.mergedId)
    implementation(Dependencies.timber.mergedId)

    // VPN SDK
    implementation(Dependencies.vpnSdk.mergedId)

    // Detekt plugins
    detektPlugins(Dependencies.detektFormatting.mergedId)
    detektPlugins(Dependencies.detektGitlabReport.mergedId)

    //Leak Canary
    debugImplementation (Dependencies.leakCanary.mergedId)

    // Testing
    testImplementation(Dependencies.junit.mergedId)
    testImplementation(Dependencies.mockk.mergedId)
    testImplementation(Dependencies.junitParams.mergedId)
    testRuntimeOnly(Dependencies.junitEngine.mergedId)

    // Splash
    implementation(Dependencies.splashScreen.mergedId)

    // TV
    "tvImplementation"(Dependencies.composeTv.mergedId)

    coreLibraryDesugaring(Dependencies.desugaringJavaApi.mergedId)

}

protobuf {
    protoc {
        artifact =  Dependencies.protoBuffArtifact.mergedId
    }

    generateProtoTasks {
        all().forEach { task ->
            task.plugins{
                create("java") {
                    option("lite")
                }
            }
        }
    }
}

fun getVersionCode(): Int = Integer.valueOf(System.getenv("CI_PIPELINE_IID") ?: "1")

fun getVersionName(): String {
    val versionMajor = properties["version.major"]
    val versionMinor = properties["version.minor"]
    val versionPatch = properties["version.patch"]
    val pipeline = Integer.valueOf(System.getenv("CI_PIPELINE_IID") ?: "1")

    return "${versionMajor}.${versionMinor}.${versionPatch}.${pipeline}"
}