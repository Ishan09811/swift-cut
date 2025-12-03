import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class RustTooling @Inject constructor(
    private val execOps: ExecOperations
) {
    fun cargoBuild() {
        execOps.exec {
            workingDir("src/main/backend")
            environment["CC_aarch64-linux-android"] =
                "${System.getenv("ANDROID_NDK")}/toolchains/llvm/prebuilt/linux-x86_64/bin/aarch64-linux-android21-clang"

            environment["CXX_aarch64-linux-android"] =
               "${System.getenv("ANDROID_NDK")}/toolchains/llvm/prebuilt/linux-x86_64/bin/aarch64-linux-android21-clang++"

            environment["AR_aarch64-linux-android"] =
               "${System.getenv("ANDROID_NDK")}/toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-ar"

            environment["LD_aarch64-linux-android"] =
               "${System.getenv("ANDROID_NDK")}/toolchains/llvm/prebuilt/linux-x86_64/bin/ld.lld"

            environment["CFLAGS_aarch64-linux-android"] =
               "--target=aarch64-linux-android21 --sysroot=${System.getenv("ANDROID_NDK")}/toolchains/llvm/prebuilt/linux-x86_64/sysroot -fPIC"

            environment["PKG_CONFIG_ALLOW_CROSS"] = "1"

            commandLine(
                "bash", "-c",
                """
                rustup target add aarch64-linux-android && \
                cargo install cargo-binstall && \
                cargo binstall cargo-ndk -y && \
                cargo ndk -t arm64-v8a -o ../jniLibs build --release
                """.trimIndent()
            )
        }
    }
}

val rustTooling = objects.newInstance(RustTooling::class)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("org.jetbrains.kotlin.plugin.serialization")
    id("kotlin-parcelize")
}

android {
    namespace = "io.github.swiftcut"
    compileSdk = 36
    ndkVersion = "29.0.14206865"

    defaultConfig {
        applicationId = "io.github.swiftcut"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += listOf("arm64-v8a")
        }

        buildConfigField("String", "Version", "\"v${versionName}\"")
    }

    signingConfigs {
        create("custom-key") {
            val keystoreAlias = System.getenv("KEYSTORE_ALIAS") ?: ""
            val keystorePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            val keystorePath = System.getenv("KEYSTORE_PATH") ?: ""

            if (keystorePath.isNotEmpty() && file(keystorePath).exists() && file(keystorePath).length() > 0) {
                keyAlias = keystoreAlias
                keyPassword = keystorePassword
                storeFile = file(keystorePath)
                storePassword = keystorePassword
            } else {
                val debugKeystoreFile = file("${System.getProperty("user.home")}/debug.keystore")

                println("⚠️ Custom keystore not found or empty! creating debug keystore.")

                if (!debugKeystoreFile.exists()) {
                    Runtime.getRuntime().exec(
                        arrayOf(
                            "keytool", "-genkeypair",
                            "-v", "-keystore", debugKeystoreFile.absolutePath,
                            "-storepass", "android",
                            "-keypass", "android",
                            "-alias", "androiddebugkey",
                            "-keyalg", "RSA",
                            "-keysize", "2048",
                            "-validity", "10000",
                            "-dname", "CN=Android Debug,O=Android,C=US"
                        )
                    ).waitFor()
                }

                keyAlias = "androiddebugkey"
                keyPassword = "android"
                storeFile = debugKeystoreFile
                storePassword = "android"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("custom-key") ?: signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
}

tasks.register("cargoBuild") {
    doLast {
        rustTooling.cargoBuild()
    }
}

tasks.configureEach {
    if (name == "javaPreCompileDebug" || name == "javaPreCompileRelease") {
        dependsOn("cargoBuild")
    }
}

base.archivesName = "swiftCut"

dependencies {
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.ui.tooling.preview.android)
    val composeBom = platform("androidx.compose:compose-bom:2025.02.00")
    implementation(composeBom)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil.compose)
    implementation(libs.squareup.okhttp3)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.common)
}
